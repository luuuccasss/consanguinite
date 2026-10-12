package fr.luuuccasss.consanguinite;

import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

/**
 * Arbre généalogique d'un villageois, tronqué à {@link #MAX_DEPTH} générations.
 * Le calcul du coefficient de consanguinité suit la méthode récursive de la parenté (Wright / Malécot).
 */
public record Pedigree(UUID id, float inbreeding, @Nullable Pedigree parentA, @Nullable Pedigree parentB) {
    public static final int MAX_DEPTH = 6;

    public static Pedigree founder(UUID id) {
        return new Pedigree(id, 0f, null, null);
    }

    public static Pedigree child(UUID id, Pedigree a, Pedigree b) {
        float f = (float) kinship(a, b);
        return new Pedigree(id, f, a.truncate(MAX_DEPTH - 1), b.truncate(MAX_DEPTH - 1));
    }

    public boolean hasParents() {
        return parentA != null || parentB != null;
    }

    /** Coefficient de parenté entre deux individus = consanguinité de leur enfant. */
    public static double kinship(@Nullable Pedigree x, @Nullable Pedigree y) {
        if (x == null || y == null) return 0;
        if (x.id.equals(y.id)) return 0.5 * (1 + x.inbreeding);
        // On remonte toujours par l'individu qui n'est pas ancêtre de l'autre.
        if (x.hasAncestor(y.id)) return 0.5 * (kinship(x.parentA, y) + kinship(x.parentB, y));
        if (y.hasParents()) return 0.5 * (kinship(x, y.parentA) + kinship(x, y.parentB));
        if (x.hasParents()) return 0.5 * (kinship(x.parentA, y) + kinship(x.parentB, y));
        return 0;
    }

    public boolean hasAncestor(UUID target) {
        return (parentA != null && (parentA.id.equals(target) || parentA.hasAncestor(target)))
            || (parentB != null && (parentB.id.equals(target) || parentB.hasAncestor(target)));
    }

    public int ancestorCount() {
        int n = 0;
        if (parentA != null) n += 1 + parentA.ancestorCount();
        if (parentB != null) n += 1 + parentB.ancestorCount();
        return n;
    }

    public Pedigree truncate(int depth) {
        if (depth <= 0) return new Pedigree(id, inbreeding, null, null);
        return new Pedigree(id, inbreeding,
            parentA == null ? null : parentA.truncate(depth - 1),
            parentB == null ? null : parentB.truncate(depth - 1));
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("id", id);
        nbt.putFloat("f", inbreeding);
        if (parentA != null) nbt.put("a", parentA.toNbt());
        if (parentB != null) nbt.put("b", parentB.toNbt());
        return nbt;
    }

    public static Pedigree fromNbt(NbtCompound nbt) {
        return new Pedigree(nbt.getUuid("id"), nbt.getFloat("f"),
            nbt.contains("a") ? fromNbt(nbt.getCompound("a")) : null,
            nbt.contains("b") ? fromNbt(nbt.getCompound("b")) : null);
    }
}
