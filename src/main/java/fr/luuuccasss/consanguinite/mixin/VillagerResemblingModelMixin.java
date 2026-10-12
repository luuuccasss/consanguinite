package fr.luuuccasss.consanguinite.mixin;

import fr.luuuccasss.consanguinite.Tare;
import fr.luuuccasss.consanguinite.client.TareRenderState;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tares graves : trop épuisé pour relever la tête, le regard reste bloqué vers le sol. */
@Mixin(VillagerResemblingModel.class)
public abstract class VillagerResemblingModelMixin {
    @Shadow @Final private ModelPart head;

    // Le modèle remet ses pièces à zéro à chaque frame (resetTransforms), pas besoin de restaurer.
    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/VillagerEntityRenderState;)V", at = @At("TAIL"))
    private void consanguinite$tiredHead(VillagerEntityRenderState state, CallbackInfo ci) {
        if (((TareRenderState) state).consanguinite$getTare() == Tare.GRAVE) {
            head.pitch = 1.5f;
            head.pivotZ = -2f;
            head.pivotY = -0.5f;
        }
    }
}
