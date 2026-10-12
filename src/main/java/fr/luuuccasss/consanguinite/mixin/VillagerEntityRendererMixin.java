package fr.luuuccasss.consanguinite.mixin;

import fr.luuuccasss.consanguinite.VillagerGenetics;
import fr.luuuccasss.consanguinite.client.TareRenderState;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntityRenderer.class)
public abstract class VillagerEntityRendererMixin {
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/passive/VillagerEntity;Lnet/minecraft/client/render/entity/state/VillagerEntityRenderState;F)V",
        at = @At("TAIL"))
    private void consanguinite$copyTare(VillagerEntity villager, VillagerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        ((TareRenderState) state).consanguinite$setTare(((VillagerGenetics) villager).consanguinite$getTare());
    }
}
