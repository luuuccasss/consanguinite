package fr.luuuccasss.consanguinite.mixin;

import fr.luuuccasss.consanguinite.Tare;
import fr.luuuccasss.consanguinite.client.TareRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tares graves : trop épuisé pour relever la tête, le regard reste bloqué vers le sol. */
@Mixin(VillagerModel.class)
public abstract class VillagerModelMixin {
    @Shadow @Final private ModelPart head;

    // setupAnim commence par remettre la pose à zéro : les décalages ne s'accumulent pas d'une image à l'autre.
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/VillagerRenderState;)V", at = @At("TAIL"))
    private void consanguinite$tiredHead(VillagerRenderState state, CallbackInfo ci) {
        if (((TareRenderState) state).consanguinite$getTare() == Tare.GRAVE) {
            head.xRot = 1.5f;
            head.z += -2f;
            head.y += -0.5f;
        }
    }
}
