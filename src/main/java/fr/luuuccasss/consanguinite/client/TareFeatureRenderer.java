package fr.luuuccasss.consanguinite.client;

import fr.luuuccasss.consanguinite.Consanguinite;
import fr.luuuccasss.consanguinite.Tare;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/** Teint maladif, cernes, vêtements sales et rapiécés, bandages pour les tares graves. */
public class TareFeatureRenderer extends FeatureRenderer<VillagerEntityRenderState, VillagerResemblingModel> {
    private static final Identifier[] TEXTURES = {
        null,
        Identifier.of(Consanguinite.MOD_ID, "textures/entity/tare_legere.png"),
        Identifier.of(Consanguinite.MOD_ID, "textures/entity/tare_moderee.png"),
        Identifier.of(Consanguinite.MOD_ID, "textures/entity/tare_grave.png"),
    };

    public TareFeatureRenderer(FeatureRendererContext<VillagerEntityRenderState, VillagerResemblingModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, VillagerEntityRenderState state,
                       float limbAngle, float limbDistance) {
        if (state.invisible) return;
        Tare tare = ((TareRenderState) state).consanguinite$getTare();
        if (tare == Tare.AUCUNE) return;
        // Couche translucide : teinte maladive par-dessus le skin, quel que soit le biome ou le métier.
        queue.getBatchingQueue(1).submitModel(getContextModel(), state, matrices,
            RenderLayers.entityTranslucent(TEXTURES[tare.ordinal()]), light, LivingEntityRenderer.getOverlay(state, 0f), -1,
            null, state.outlineColor, null);
    }
}
