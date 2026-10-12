package fr.luuuccasss.consanguinite.mixin;

import fr.luuuccasss.consanguinite.Consanguinite;
import fr.luuuccasss.consanguinite.Pedigree;
import fr.luuuccasss.consanguinite.PedigreeNbt;
import fr.luuuccasss.consanguinite.Tare;
import fr.luuuccasss.consanguinite.VillagerGenetics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public abstract class VillagerMixin implements VillagerGenetics {
    @Unique
    private static final EntityDataAccessor<Integer> CONSANGUINITE_TARE =
        SynchedEntityData.defineId(Villager.class, EntityDataSerializers.INT);

    @Unique private Pedigree consanguinite$pedigree;
    @Unique private boolean consanguinite$sterile;
    @Unique private long consanguinite$lifeTicks;

    @Unique
    private Villager consanguinite$self() {
        return (Villager) (Object) this;
    }

    @Override
    public Pedigree consanguinite$getPedigree() {
        if (consanguinite$pedigree == null) consanguinite$pedigree = Pedigree.founder(consanguinite$self().getUUID());
        return consanguinite$pedigree;
    }

    @Override
    public void consanguinite$setPedigree(Pedigree pedigree) {
        consanguinite$pedigree = pedigree;
        consanguinite$self().getEntityData().set(CONSANGUINITE_TARE, Tare.of(pedigree.inbreeding()).ordinal());
    }

    @Override
    public Tare consanguinite$getTare() {
        return Tare.byId(consanguinite$self().getEntityData().get(CONSANGUINITE_TARE));
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

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void consanguinite$defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(CONSANGUINITE_TARE, 0);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void consanguinite$write(ValueOutput output, CallbackInfo ci) {
        ValueOutput data = output.child("Consanguinite");
        PedigreeNbt.write(data.child("Pedigree"), consanguinite$getPedigree());
        data.putBoolean("Sterile", consanguinite$sterile);
        data.putLong("LifeTicks", consanguinite$lifeTicks);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void consanguinite$read(ValueInput input, CallbackInfo ci) {
        input.child("Consanguinite").ifPresent(data -> {
            data.child("Pedigree").map(PedigreeNbt::read).ifPresent(this::consanguinite$setPedigree);
            consanguinite$sterile = data.getBooleanOr("Sterile", false);
            consanguinite$lifeTicks = data.getLongOr("LifeTicks", 0L);
        });
    }

    @Inject(method = "getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/npc/villager/Villager;",
        at = @At("RETURN"))
    private void consanguinite$createChild(ServerLevel level, AgeableMob partner, CallbackInfoReturnable<Villager> cir) {
        Villager child = cir.getReturnValue();
        if (child != null && partner instanceof Villager other) {
            Consanguinite.onBirth(level, consanguinite$self(), other, child);
        }
    }

    @Inject(method = "canBreed", at = @At("HEAD"), cancellable = true)
    private void consanguinite$canBreed(CallbackInfoReturnable<Boolean> cir) {
        if (consanguinite$sterile) cir.setReturnValue(false);
    }

    @Inject(method = "updateSpecialPrices", at = @At("TAIL"))
    private void consanguinite$worseTrades(Player player, CallbackInfo ci) {
        double malus = consanguinite$getTare().priceMalus;
        if (malus <= 0) return;
        for (MerchantOffer offer : consanguinite$self().getOffers()) {
            int base = offer.getBaseCostA().getCount();
            offer.addToSpecialPriceDiff(Math.max(1, (int) Math.ceil(base * malus)));
        }
    }

    /** Quinte de toux de temps en temps (≈ toutes les 20 s en modéré, 10 s en grave). */
    @Unique
    private static void consanguinite$cough(ServerLevel level, Villager self, Tare tare) {
        int chance = switch (tare) {
            case MODEREE -> 400;
            case GRAVE -> 200;
            default -> 0;
        };
        if (chance == 0 || self.isSleeping() || level.getRandom().nextInt(chance) != 0) return;
        Vec3 look = self.getLookAngle();
        level.sendParticles(ParticleTypes.POOF, self.getX() + look.x * 0.4, self.getEyeY() - 0.2, self.getZ() + look.z * 0.4,
            4, 0.05, 0.05, 0.05, 0.02);
        level.playSound(null, self.blockPosition(), SoundEvents.PANDA_SNEEZE, SoundSource.NEUTRAL, 0.6f,
            0.6f + level.getRandom().nextFloat() * 0.15f);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void consanguinite$tick(CallbackInfo ci) {
        Villager self = consanguinite$self();
        if (!(self.level() instanceof ServerLevel level) || !self.isAlive()) return;
        Tare tare = consanguinite$getTare();
        consanguinite$cough(level, self, tare);
        if (tare.lifespanDays <= 0) return;
        consanguinite$lifeTicks++;
        if (consanguinite$lifeTicks >= tare.lifespanDays * Consanguinite.TICKS_PER_DAY && Consanguinite.mortPrecoce(level)) {
            level.sendParticles(ParticleTypes.SMOKE, self.getX(), self.getY(0.5), self.getZ(), 20, 0.3, 0.5, 0.3, 0.02);
            self.kill(level);
        }
    }
}
