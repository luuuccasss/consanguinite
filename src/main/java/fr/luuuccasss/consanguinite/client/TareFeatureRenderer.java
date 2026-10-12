package fr.luuuccasss.consanguinite.client;

import fr.luuuccasss.consanguinite.Consanguinite;
import fr.luuuccasss.consanguinite.Tare;
import fr.luuuccasss.consanguinite.VillagerGenetics;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Identifier;

/** Teint maladif, cernes, vêtements sales et rapiécés, bandages pour les tares graves. */
public class TareFeatureRenderer extends FeatureRenderer<VillagerEntity, VillagerResemblingModel<VillagerEntity>> {
    private static final Identifier[] TEXTURES = {
        null,
        new Identifier(Consanguinite.MOD_ID, "textures/entity/tare_legere.png"),
        new Identifier(Consanguinite.MOD_ID, "textures/entity/tare_moderee.png"),
        new Identifier(Consanguinite.MOD_ID, "textures/entity/tare_grave.png"),
    };

    public TareFeatureRenderer(FeatureRendererContext<VillagerEntity, VillagerResemblingModel<VillagerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, VillagerEntity entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (entity.isInvisible()) return;
        Tare tare = ((VillagerGenetics) entity).consanguinite$getTare();
        if (tare == Tare.AUCUNE) return;
        // Couche translucide : teinte maladive par-dessus le skin, quel que soit le biome ou le métier.
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURES[tare.ordinal()]));
        getContextModel().render(matrices, consumer, light, LivingEntityRenderer.getOverlay(entity, 0f), 1f, 1f, 1f, 1f);
    }
}
