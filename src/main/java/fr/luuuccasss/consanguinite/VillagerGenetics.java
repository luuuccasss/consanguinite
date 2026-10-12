package fr.luuuccasss.consanguinite;

/** Interface ajoutée à VillagerEntity par mixin. */
public interface VillagerGenetics {
    Pedigree consanguinite$getPedigree();

    void consanguinite$setPedigree(Pedigree pedigree);

    Tare consanguinite$getTare();

    boolean consanguinite$isSterile();

    void consanguinite$setSterile(boolean sterile);

    long consanguinite$getLifeTicks();
}
