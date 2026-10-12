package fr.luuuccasss.consanguinite;

/** Niveaux de tares génétiques selon le coefficient de consanguinité. */
public enum Tare {
    AUCUNE("aucune", 0f, 0, 0, 0, 0, 0, 0, 0),
    LEGERE("légère", 0.0625f, 0.15, 0, 0, 0.15, 0, 0, 0),
    MODEREE("modérée", 0.125f, 0.30, 0.15, 0.10, 0.35, 0.30, 0, 12),
    GRAVE("grave", 0.25f, 0.45, 0.30, 0.20, 0.60, 0.70, 0.50, 6);

    public final String label;
    public final float threshold;
    public final double healthMalus;
    public final double speedMalus;
    public final double scaleMalus;
    public final double priceMalus;
    public final double sterileChance;
    public final double joblessChance;
    /** Espérance de vie en jours Minecraft, 0 = normale. */
    public final int lifespanDays;

    Tare(String label, float threshold, double healthMalus, double speedMalus, double scaleMalus,
         double priceMalus, double sterileChance, double joblessChance, int lifespanDays) {
        this.label = label;
        this.threshold = threshold;
        this.healthMalus = healthMalus;
        this.speedMalus = speedMalus;
        this.scaleMalus = scaleMalus;
        this.priceMalus = priceMalus;
        this.sterileChance = sterileChance;
        this.joblessChance = joblessChance;
        this.lifespanDays = lifespanDays;
    }

    public static Tare of(float inbreeding) {
        Tare result = AUCUNE;
        for (Tare t : values()) if (inbreeding >= t.threshold) result = t;
        return result;
    }

    public static Tare byId(int id) {
        return values()[Math.max(0, Math.min(id, values().length - 1))];
    }
}
