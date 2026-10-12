package fr.luuuccasss.consanguinite.mixin;

import fr.luuuccasss.consanguinite.Tare;
import fr.luuuccasss.consanguinite.client.TareRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(VillagerRenderState.class)
public abstract class VillagerRenderStateMixin implements TareRenderState {
    @Unique private Tare consanguinite$tare = Tare.AUCUNE;

    @Override
    public Tare consanguinite$getTare() {
        return consanguinite$tare;
    }

    @Override
    public void consanguinite$setTare(Tare tare) {
        consanguinite$tare = tare;
    }
}
