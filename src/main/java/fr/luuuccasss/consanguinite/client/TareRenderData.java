package fr.luuuccasss.consanguinite.client;

import fr.luuuccasss.consanguinite.Tare;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

/** Niveau de tare transmis du villageois à son état de rendu. */
public final class TareRenderData {
    public static final RenderStateDataKey<Tare> TARE = RenderStateDataKey.create(() -> "consanguinite:tare");

    private TareRenderData() {
    }
}
