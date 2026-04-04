package com.fredplugins.mixology;

public class PotionOrder {

    private final int idx;
    private final PotionType potionType;
    private final PotionModifier potionModifier;

    private boolean fulfilled;

    public PotionOrder(int idx, PotionType potionType, PotionModifier potionModifier) {
        this.idx = idx;
        this.potionType = potionType;
        this.potionModifier = potionModifier;
    }

    public int idx() {
        return idx;
    }

    public PotionType potionType() {
        return potionType;
    }

    public PotionModifier potionModifier() {
        return potionModifier;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

    public boolean fulfilled() {
        return fulfilled;
    }

    @Override
    public String toString() {
        return "PotionOrder{" +
                "idx=" + idx +
                ", potionType=" + potionType +
                ", potionModifier=" + potionModifier +
                '}';
    }
}
