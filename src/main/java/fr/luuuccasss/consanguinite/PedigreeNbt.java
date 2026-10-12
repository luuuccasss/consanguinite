package fr.luuuccasss.consanguinite;

import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Sauvegarde de l'arbre généalogique dans les données de l'entité. */
public final class PedigreeNbt {
    private PedigreeNbt() {
    }

    public static void write(ValueOutput out, Pedigree pedigree) {
        out.store("id", UUIDUtil.CODEC, pedigree.id());
        out.putFloat("f", pedigree.inbreeding());
        if (pedigree.parentA() != null) write(out.child("a"), pedigree.parentA());
        if (pedigree.parentB() != null) write(out.child("b"), pedigree.parentB());
    }

    public static Pedigree read(ValueInput in) {
        return in.read("id", UUIDUtil.CODEC)
            .map(id -> new Pedigree(id, in.getFloatOr("f", 0f),
                in.child("a").map(PedigreeNbt::read).orElse(null),
                in.child("b").map(PedigreeNbt::read).orElse(null)))
            .orElse(null);
    }
}
