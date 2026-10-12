package fr.luuuccasss.consanguinite.mixin;

import fr.luuuccasss.consanguinite.VillagerGenetics;
import fr.luuuccasss.consanguinite.client.TareRenderData;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Copie le niveau de tare dans l'état de rendu du villageois. */
@Mixin(VillagerEntityRenderer.class)
public abstract class VillagerEntityRendererMixin {
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/passive/VillagerEntity;Lnet/minecraft/client/render/entity/state/VillagerEntityRenderState;F)V",
        at = @At("TAIL"))
    private void consanguinite$updateRenderState(VillagerEntity villager, VillagerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        state.setData(TareRenderData.TARE, ((VillagerGenetics) villager).consanguinite$getTare());
    }
}
