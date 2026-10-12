package fr.luuuccasss.consanguinite;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.GameRules;

public class Consanguinite implements ModInitializer {
    public static final String MOD_ID = "consanguinite";
    public static final long TICKS_PER_DAY = 24000L;

    public static GameRules.Key<GameRules.BooleanRule> MORT_PRECOCE;

    @Override
    public void onInitialize() {
        MORT_PRECOCE = GameRuleRegistry.register("consanguiniteMortPrecoce", GameRules.Category.MOBS,
            GameRuleFactory.createBooleanRule(true));

        // Accroupi + clic droit avec un livre : fiche généalogique du villageois.
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (!(entity instanceof VillagerEntity villager) || !player.isSneaking()
                || !player.getStackInHand(hand).isOf(Items.BOOK)) return ActionResult.PASS;
            if (!world.isClient) player.sendMessage(describe(villager), false);
            return ActionResult.SUCCESS;
        });
    }

    /** Appelé à la naissance d'un bébé villageois. */
    public static void onBirth(ServerWorld world, VillagerEntity parentA, VillagerEntity parentB, VillagerEntity child) {
        Pedigree pedigree = Pedigree.child(child.getUuid(),
            ((VillagerGenetics) parentA).consanguinite$getPedigree(),
            ((VillagerGenetics) parentB).consanguinite$getPedigree());
        VillagerGenetics genetics = (VillagerGenetics) child;
        genetics.consanguinite$setPedigree(pedigree);

        Tare tare = Tare.of(pedigree.inbreeding());
        if (tare == Tare.AUCUNE) return;

        addMalus(child, EntityAttributes.MAX_HEALTH, "sante", tare.healthMalus);
        addMalus(child, EntityAttributes.MOVEMENT_SPEED, "vitesse", tare.speedMalus);
        addMalus(child, EntityAttributes.SCALE, "taille", tare.scaleMalus);
        child.setHealth(child.getMaxHealth());

        if (world.random.nextDouble() < tare.sterileChance) genetics.consanguinite$setSterile(true);
        if (world.random.nextDouble() < tare.joblessChance) {
            child.setVillagerData(child.getVillagerData().withProfession(world.getRegistryManager(), VillagerProfession.NITWIT));
        }
        if (tare.ordinal() >= Tare.MODEREE.ordinal()) {
            world.spawnParticles(ParticleTypes.SMOKE, parentA.getX(), parentA.getBodyY(0.6), parentA.getZ(),
                12, 0.4, 0.4, 0.4, 0.01);
        }
    }

    private static void addMalus(VillagerEntity villager, RegistryEntry<EntityAttribute> attribute, String name, double malus) {
        if (malus <= 0) return;
        EntityAttributeInstance instance = villager.getAttributeInstance(attribute);
        if (instance == null) return;
        Identifier id = Identifier.of(MOD_ID, name);
        instance.removeModifier(id);
        instance.addPersistentModifier(new EntityAttributeModifier(id, -malus,
            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static Text describe(VillagerEntity villager) {
        VillagerGenetics genetics = (VillagerGenetics) villager;
        Pedigree pedigree = genetics.consanguinite$getPedigree();
        Tare tare = genetics.consanguinite$getTare();
        Formatting color = switch (tare) {
            case AUCUNE -> Formatting.GREEN;
            case LEGERE -> Formatting.YELLOW;
            case MODEREE -> Formatting.GOLD;
            case GRAVE -> Formatting.RED;
        };
        var text = Text.literal("Généalogie de ").formatted(Formatting.GRAY)
            .append(villager.getDisplayName().copy().formatted(Formatting.WHITE))
            .append(Text.literal("\n  Consanguinité : ").formatted(Formatting.GRAY))
            .append(Text.literal(String.format("%.1f %%", pedigree.inbreeding() * 100)).formatted(color))
            .append(Text.literal("  ·  tares : ").formatted(Formatting.GRAY))
            .append(Text.literal(tare.label).formatted(color))
            .append(Text.literal("\n  Ancêtres connus : " + pedigree.ancestorCount()).formatted(Formatting.GRAY));
        if (tare != Tare.AUCUNE) {
            StringBuilder effects = new StringBuilder("\n  Santé −" + pct(tare.healthMalus));
            if (tare.speedMalus > 0) effects.append(", vitesse −").append(pct(tare.speedMalus));
            if (tare.scaleMalus > 0) effects.append(", taille −").append(pct(tare.scaleMalus));
            effects.append(", prix +").append(pct(tare.priceMalus));
            text.append(Text.literal(effects.toString()).formatted(Formatting.GRAY));
        }
        if (genetics.consanguinite$isSterile()) text.append(Text.literal("\n  Stérile").formatted(Formatting.RED));
        if (tare.lifespanDays > 0 && villager.getWorld() instanceof ServerWorld serverWorld && serverWorld.getGameRules().getBoolean(MORT_PRECOCE)) {
            long left = tare.lifespanDays * TICKS_PER_DAY - genetics.consanguinite$getLifeTicks();
            text.append(Text.literal(String.format("\n  Espérance de vie restante : %.1f jours", Math.max(0, left) / (double) TICKS_PER_DAY))
                .formatted(Formatting.GRAY));
        }
        return text;
    }

    private static String pct(double v) {
        return Math.round(v * 100) + " %";
    }
}
