package com.fredplugins.mixology;

public enum PotionModifier {
    // Clicking the quick-time event on the Agitator gives 14 experience, this event can happen 1-2 times
    HOMOGENOUS(AlchemyObject.AGITATOR, 21),
    // Each click on the Retort gives 2 experience for a max of 10 clicks
    CONCENTRATED(AlchemyObject.RETORT, 20),
    // Clicking the quick-time event on the Alembic gives 14 experience
    CRYSTALISED(AlchemyObject.ALEMBIC, 14);

    private static final PotionModifier[] TYPES = PotionModifier.values();

    private final AlchemyObject alchemyObject;
    private final int quickActionExperience;

    PotionModifier(AlchemyObject alchemyObject, int quickActionExperience) {
        this.alchemyObject = alchemyObject;
        this.quickActionExperience = quickActionExperience;
    }

    public static PotionModifier from(int potionModifierId) {
        if (potionModifierId < 0 || potionModifierId >= TYPES.length) {
            return null;
        }
        return TYPES[potionModifierId];
    }

    public AlchemyObject alchemyObject() {
        return alchemyObject;
    }

    public int quickActionExperience() {
        return quickActionExperience;
    }
}
