package fr.luuuccasss.consanguinite.mixin;

import fr.luuuccasss.consanguinite.VillagerGenetics;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 1.20.1 n'a pas d'attribut de taille : les villageois tarés sont rapetissés au rendu. */
@Mixin(VillagerEntityRenderer.class)
public abstract class VillagerEntityRendererMixin {
    @Inject(method = "scale(Lnet/minecraft/entity/passive/VillagerEntity;Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At("TAIL"))
    private void consanguinite$scale(VillagerEntity villager, MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        float malus = (float) ((VillagerGenetics) villager).consanguinite$getTare().scaleMalus;
        if (malus > 0) matrices.scale(1f - malus, 1f - malus, 1f - malus);
    }
}
