package fr.luuuccasss.consanguinite.mixin;

import fr.luuuccasss.consanguinite.Consanguinite;
import fr.luuuccasss.consanguinite.Pedigree;
import fr.luuuccasss.consanguinite.PedigreeCodec;
import fr.luuuccasss.consanguinite.Tare;
import fr.luuuccasss.consanguinite.VillagerGenetics;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin implements VillagerGenetics {
    @Unique
    private static final TrackedData<Integer> CONSANGUINITE_TARE =
        DataTracker.registerData(VillagerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Unique private Pedigree consanguinite$pedigree;
    @Unique private boolean consanguinite$sterile;
    @Unique private long consanguinite$lifeTicks;

    @Unique
    private VillagerEntity consanguinite$self() {
        return (VillagerEntity) (Object) this;
    }

    @Override
    public Pedigree consanguinite$getPedigree() {
        if (consanguinite$pedigree == null) consanguinite$pedigree = Pedigree.founder(consanguinite$self().getUuid());
        return consanguinite$pedigree;
    }

    @Override
    public void consanguinite$setPedigree(Pedigree pedigree) {
        consanguinite$pedigree = pedigree;
        consanguinite$self().getDataTracker().set(CONSANGUINITE_TARE, Tare.of(pedigree.inbreeding()).ordinal());
    }

    @Override
    public Tare consanguinite$getTare() {
        return Tare.byId(consanguinite$self().getDataTracker().get(CONSANGUINITE_TARE));
    }

    @Override
    public boolean consanguinite$isSterile() {
        return consanguinite$sterile;
    }

    @Override
    public void consanguinite$setSterile(boolean sterile) {
        consanguinite$sterile = sterile;
    }

    @Override
    public long consanguinite$getLifeTicks() {
        return consanguinite$lifeTicks;
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void consanguinite$initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(CONSANGUINITE_TARE, 0);
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void consanguinite$write(WriteView view, CallbackInfo ci) {
        WriteView data = view.get("Consanguinite");
        data.put("Pedigree", PedigreeCodec.CODEC, consanguinite$getPedigree());
        data.putBoolean("Sterile", consanguinite$sterile);
        data.putLong("LifeTicks", consanguinite$lifeTicks);
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void consanguinite$read(ReadView view, CallbackInfo ci) {
        view.getOptionalReadView("Consanguinite").ifPresent(data -> {
            data.read("Pedigree", PedigreeCodec.CODEC).ifPresent(this::consanguinite$setPedigree);
            consanguinite$sterile = data.getBoolean("Sterile", false);
            consanguinite$lifeTicks = data.getLong("LifeTicks", 0L);
        });
    }

    @Inject(method = "createChild(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/PassiveEntity;)Lnet/minecraft/entity/passive/VillagerEntity;",
        at = @At("RETURN"))
    private void consanguinite$createChild(ServerWorld world, PassiveEntity partner, CallbackInfoReturnable<VillagerEntity> cir) {
        VillagerEntity child = cir.getReturnValue();
        if (child != null && partner instanceof VillagerEntity other) {
            Consanguinite.onBirth(world, consanguinite$self(), other, child);
        }
    }

    @Inject(method = "isReadyToBreed", at = @At("HEAD"), cancellable = true)
    private void consanguinite$canBreed(CallbackInfoReturnable<Boolean> cir) {
        if (consanguinite$sterile) cir.setReturnValue(false);
    }

    @Inject(method = "prepareOffersFor", at = @At("TAIL"))
    private void consanguinite$worseTrades(PlayerEntity player, CallbackInfo ci) {
        double malus = consanguinite$getTare().priceMalus;
        if (malus <= 0) return;
        for (TradeOffer offer : consanguinite$self().getOffers()) {
            int base = offer.getOriginalFirstBuyItem().getCount();
            offer.increaseSpecialPrice(Math.max(1, (int) Math.ceil(base * malus)));
        }
    }

    /** Quinte de toux de temps en temps (≈ toutes les 20 s en modéré, 10 s en grave). */
    @Unique
    private static void consanguinite$cough(ServerWorld world, VillagerEntity self, Tare tare) {
        int chance = switch (tare) {
            case MODEREE -> 400;
            case GRAVE -> 200;
            default -> 0;
        };
        if (chance == 0 || self.isSleeping() || world.random.nextInt(chance) != 0) return;
        var look = self.getRotationVec(1f);
        world.spawnParticles(ParticleTypes.POOF, self.getX() + look.x * 0.4, self.getEyeY() - 0.2, self.getZ() + look.z * 0.4,
            4, 0.05, 0.05, 0.05, 0.02);
        world.playSound(null, self.getBlockPos(), SoundEvents.ENTITY_PANDA_SNEEZE, SoundCategory.NEUTRAL, 0.6f,
            0.6f + world.random.nextFloat() * 0.15f);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void consanguinite$tick(CallbackInfo ci) {
        VillagerEntity self = consanguinite$self();
        if (!(self.getWorld() instanceof ServerWorld world) || !self.isAlive()) return;
        Tare tare = consanguinite$getTare();
        consanguinite$cough(world, self, tare);
        if (tare.lifespanDays <= 0) return;
        consanguinite$lifeTicks++;
        if (consanguinite$lifeTicks >= tare.lifespanDays * Consanguinite.TICKS_PER_DAY
            && world.getGameRules().getBoolean(Consanguinite.MORT_PRECOCE)) {
            world.spawnParticles(ParticleTypes.SMOKE, self.getX(), self.getBodyY(0.5), self.getZ(), 20, 0.3, 0.5, 0.3, 0.02);
            self.kill(world);
        }
    }
}
