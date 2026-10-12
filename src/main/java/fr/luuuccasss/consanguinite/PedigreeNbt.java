package fr.luuuccasss.consanguinite;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;

/** Sauvegarde de l'arbre généalogique (WriteView / ReadView depuis 1.21.6). */
public final class PedigreeNbt {
    private PedigreeNbt() {
    }

    public static void write(WriteView view, Pedigree pedigree) {
        view.put("id", Uuids.INT_STREAM_CODEC, pedigree.id());
        view.putFloat("f", pedigree.inbreeding());
        if (pedigree.parentA() != null) write(view.get("a"), pedigree.parentA());
        if (pedigree.parentB() != null) write(view.get("b"), pedigree.parentB());
    }

    public static Pedigree read(ReadView view) {
        return view.read("id", Uuids.INT_STREAM_CODEC)
            .map(id -> new Pedigree(id, view.getFloat("f", 0f),
                view.getOptionalReadView("a").map(PedigreeNbt::read).orElse(null),
                view.getOptionalReadView("b").map(PedigreeNbt::read).orElse(null)))
            .orElse(null);
    }
}
