package fr.luuuccasss.consanguinite.client;

import fr.luuuccasss.consanguinite.Tare;

/** Niveau de tare transporté dans le render state du villageois (1.21.2+). */
public interface TareRenderState {
    Tare consanguinite$getTare();

    void consanguinite$setTare(Tare tare);
}
