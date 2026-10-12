package fr.luuuccasss.consanguinite.mixin;

import fr.luuuccasss.consanguinite.Tare;
import fr.luuuccasss.consanguinite.VillagerGenetics;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.entity.Entity;
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

    @Inject(method = "setAngles", at = @At("TAIL"))
    private void consanguinite$tiredHead(Entity entity, float limbAngle, float limbDistance, float animationProgress,
                                         float headYaw, float headPitch, CallbackInfo ci) {
        // Le modèle est partagé entre tous les villageois : on repositionne toujours la tête.
        if (entity instanceof VillagerGenetics genetics && genetics.consanguinite$getTare() == Tare.GRAVE) {
            head.pitch = 1.5f;
            head.pivotZ = -2f;
            head.pivotY = -0.5f;
        } else {
            head.pivotZ = 0f;
            head.pivotY = 0f;
        }
    }
}
