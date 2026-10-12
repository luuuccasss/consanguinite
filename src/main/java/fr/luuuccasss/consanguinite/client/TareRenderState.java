package fr.luuuccasss.consanguinite.client;

import fr.luuuccasss.consanguinite.Tare;

/** Ajouté à VillagerEntityRenderState par mixin : niveau de tare du villageois rendu. */
public interface TareRenderState {
    Tare consanguinite$getTare();

    void consanguinite$setTare(Tare tare);
}
