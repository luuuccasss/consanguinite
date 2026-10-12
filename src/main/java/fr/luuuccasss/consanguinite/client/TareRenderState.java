package fr.luuuccasss.consanguinite.client;

import fr.luuuccasss.consanguinite.Tare;

/** Niveau de tare ajouté à VillagerEntityRenderState par mixin. */
public interface TareRenderState {
    Tare consanguinite$getTare();

    void consanguinite$setTare(Tare tare);
}
