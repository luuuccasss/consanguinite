package fr.luuuccasss.consanguinite.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.renderer.entity.VillagerRenderer;

public class ConsanguiniteClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof VillagerRenderer villagerRenderer) {
                helper.register(new TareLayer(villagerRenderer));
            }
        });
    }
}
