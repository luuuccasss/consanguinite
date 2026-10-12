package fr.luuuccasss.consanguinite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.Uuids;

/** Sauvegarde de l'arbre généalogique (codec récursif). */
public final class PedigreeCodec {
    public static final Codec<Pedigree> CODEC = Codec.recursive("Pedigree", self ->
        RecordCodecBuilder.create(instance -> instance.group(
            Uuids.INT_STREAM_CODEC.fieldOf("id").forGetter(Pedigree::id),
            Codec.FLOAT.fieldOf("f").forGetter(Pedigree::inbreeding),
            self.optionalFieldOf("a").forGetter(p -> Optional.ofNullable(p.parentA())),
            self.optionalFieldOf("b").forGetter(p -> Optional.ofNullable(p.parentB()))
        ).apply(instance, (id, f, a, b) -> new Pedigree(id, f, a.orElse(null), b.orElse(null)))));

    private PedigreeCodec() {
    }
}
