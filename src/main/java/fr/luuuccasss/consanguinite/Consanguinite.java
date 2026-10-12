package fr.luuuccasss.consanguinite;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public class Consanguinite implements ModInitializer {
    public static final String MOD_ID = "consanguinite";
    public static final long TICKS_PER_DAY = 24000L;

    public static GameRule<Boolean> MORT_PRECOCE;

    @Override
    public void onInitialize() {
        MORT_PRECOCE = GameRuleBuilder.forBoolean(true).category(GameRuleCategory.MOBS)
            .buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "mort_precoce"));

        // Accroupi + clic droit avec un livre : fiche généalogique du villageois.
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (!(entity instanceof Villager villager) || !player.isShiftKeyDown()
                || !player.getItemInHand(hand).is(Items.BOOK)) return InteractionResult.PASS;
            if (level instanceof ServerLevel serverLevel) player.sendSystemMessage(describe(serverLevel, villager));
            return InteractionResult.SUCCESS;
        });
    }

    public static boolean mortPrecoce(ServerLevel level) {
        return level.getGameRules().get(MORT_PRECOCE);
    }

    /** Appelé à la naissance d'un bébé villageois. */
    public static void onBirth(ServerLevel level, Villager parentA, Villager parentB, Villager child) {
        Pedigree pedigree = Pedigree.child(child.getUUID(),
            ((VillagerGenetics) parentA).consanguinite$getPedigree(),
            ((VillagerGenetics) parentB).consanguinite$getPedigree());
        VillagerGenetics genetics = (VillagerGenetics) child;
        genetics.consanguinite$setPedigree(pedigree);

        Tare tare = Tare.of(pedigree.inbreeding());
        if (tare == Tare.AUCUNE) return;

        addMalus(child, Attributes.MAX_HEALTH, "sante", tare.healthMalus);
        addMalus(child, Attributes.MOVEMENT_SPEED, "vitesse", tare.speedMalus);
        addMalus(child, Attributes.SCALE, "taille", tare.scaleMalus);
        child.setHealth(child.getMaxHealth());

        if (level.getRandom().nextDouble() < tare.sterileChance) genetics.consanguinite$setSterile(true);
        if (level.getRandom().nextDouble() < tare.joblessChance) {
            child.setVillagerData(child.getVillagerData().withProfession(level.registryAccess(), VillagerProfession.NITWIT));
        }
        if (tare.ordinal() >= Tare.MODEREE.ordinal()) {
            level.sendParticles(ParticleTypes.SMOKE, parentA.getX(), parentA.getY(0.6), parentA.getZ(),
                12, 0.4, 0.4, 0.4, 0.01);
        }
    }

    private static void addMalus(Villager villager, Holder<Attribute> attribute, String name, double malus) {
        if (malus <= 0) return;
        AttributeInstance instance = villager.getAttribute(attribute);
        if (instance == null) return;
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        instance.removeModifier(id);
        instance.addPermanentModifier(new AttributeModifier(id, -malus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static Component describe(ServerLevel level, Villager villager) {
        VillagerGenetics genetics = (VillagerGenetics) villager;
        Pedigree pedigree = genetics.consanguinite$getPedigree();
        Tare tare = genetics.consanguinite$getTare();
        ChatFormatting color = switch (tare) {
            case AUCUNE -> ChatFormatting.GREEN;
            case LEGERE -> ChatFormatting.YELLOW;
            case MODEREE -> ChatFormatting.GOLD;
            case GRAVE -> ChatFormatting.RED;
        };
        MutableComponent text = Component.literal("Généalogie de ").withStyle(ChatFormatting.GRAY)
            .append(villager.getDisplayName().copy().withStyle(ChatFormatting.WHITE))
            .append(Component.literal("\n  Consanguinité : ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(String.format("%.1f %%", pedigree.inbreeding() * 100)).withStyle(color))
            .append(Component.literal("  ·  tares : ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(tare.label).withStyle(color))
            .append(Component.literal("\n  Ancêtres connus : " + pedigree.ancestorCount()).withStyle(ChatFormatting.GRAY));
        if (tare != Tare.AUCUNE) {
            StringBuilder effects = new StringBuilder("\n  Santé −" + pct(tare.healthMalus));
            if (tare.speedMalus > 0) effects.append(", vitesse −").append(pct(tare.speedMalus));
            if (tare.scaleMalus > 0) effects.append(", taille −").append(pct(tare.scaleMalus));
            effects.append(", prix +").append(pct(tare.priceMalus));
            text.append(Component.literal(effects.toString()).withStyle(ChatFormatting.GRAY));
        }
        if (genetics.consanguinite$isSterile()) text.append(Component.literal("\n  Stérile").withStyle(ChatFormatting.RED));
        if (tare.lifespanDays > 0 && mortPrecoce(level)) {
            long left = tare.lifespanDays * TICKS_PER_DAY - genetics.consanguinite$getLifeTicks();
            text.append(Component.literal(String.format("\n  Espérance de vie restante : %.1f jours", Math.max(0, left) / (double) TICKS_PER_DAY))
                .withStyle(ChatFormatting.GRAY));
        }
        return text;
    }

    private static String pct(double v) {
        return Math.round(v * 100) + " %";
    }
}
