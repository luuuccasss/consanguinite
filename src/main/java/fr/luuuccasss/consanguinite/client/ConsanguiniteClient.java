package fr.luuuccasss.consanguinite.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.VillagerEntityRenderer;

public class ConsanguiniteClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof VillagerEntityRenderer villagerRenderer) {
                helper.register(new TareFeatureRenderer(villagerRenderer));
            }
        });
    }
}
