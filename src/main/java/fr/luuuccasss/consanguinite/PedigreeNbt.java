package fr.luuuccasss.consanguinite;

import net.minecraft.nbt.NbtCompound;

/** Sauvegarde de l'arbre généalogique en NBT. */
public final class PedigreeNbt {
    private PedigreeNbt() {
    }

    public static NbtCompound write(Pedigree pedigree) {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("id", pedigree.id());
        nbt.putFloat("f", pedigree.inbreeding());
        if (pedigree.parentA() != null) nbt.put("a", write(pedigree.parentA()));
        if (pedigree.parentB() != null) nbt.put("b", write(pedigree.parentB()));
        return nbt;
    }

    public static Pedigree read(NbtCompound nbt) {
        return new Pedigree(nbt.getUuid("id"), nbt.getFloat("f"),
            nbt.contains("a") ? read(nbt.getCompound("a")) : null,
            nbt.contains("b") ? read(nbt.getCompound("b")) : null);
    }
}
