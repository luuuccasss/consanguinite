package fr.luuuccasss.consanguinite.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.luuuccasss.consanguinite.Consanguinite;
import fr.luuuccasss.consanguinite.Tare;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Teint maladif, cernes, vêtements sales et rapiécés, bandages pour les tares graves. */
public class TareLayer extends RenderLayer<VillagerRenderState, VillagerModel> {
    private static final Identifier[] TEXTURES = {
        null,
        Identifier.fromNamespaceAndPath(Consanguinite.MOD_ID, "textures/entity/tare_legere.png"),
        Identifier.fromNamespaceAndPath(Consanguinite.MOD_ID, "textures/entity/tare_moderee.png"),
        Identifier.fromNamespaceAndPath(Consanguinite.MOD_ID, "textures/entity/tare_grave.png"),
    };

    public TareLayer(RenderLayerParent<VillagerRenderState, VillagerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, VillagerRenderState state,
                       float yRot, float xRot) {
        // Les bébés ont leur propre modèle et leur propre texture : la couche ne s'applique qu'aux adultes.
        if (state.isInvisible || state.isBaby) return;
        Tare tare = ((TareRenderState) state).consanguinite$getTare();
        if (tare == Tare.AUCUNE) return;
        // Couche translucide : teinte maladive par-dessus le skin, quel que soit le biome ou le métier.
        collector.order(2).submitModel(getParentModel(), state, poseStack,
            RenderTypes.entityTranslucent(TEXTURES[tare.ordinal()]), light,
            LivingEntityRenderer.getOverlayCoords(state, 0f), -1, null, state.outlineColor);
    }
}
