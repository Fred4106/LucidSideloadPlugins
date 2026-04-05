package com.fredplugins.mixology;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PotionOrder that = (PotionOrder) o;
        return idx == that.idx && fulfilled == that.fulfilled && potionType == that.potionType && potionModifier == that.potionModifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idx, potionType, potionModifier, fulfilled);
    }

    @Override
    public String toString() {
        return "PotionOrder(" +
                "idx=" + idx +
                ", potionType=" + potionType +
                ", potionModifier=" + potionModifier +
                ", fulfilled=" + fulfilled +
                ')';
    }
}
