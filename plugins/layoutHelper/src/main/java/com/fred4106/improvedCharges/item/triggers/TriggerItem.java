package com.fred4106.improvedCharges.item.triggers;

import java.util.Optional;

public class TriggerItem {
    public final int itemId;

    public Optional<Boolean> quantityCharges = Optional.empty();
    public Optional<Boolean> hideOverlay = Optional.empty();
    public Optional<Boolean> needsToBeEquipped = Optional.empty();
    public Optional<Integer> maxCharges = Optional.empty();
    public Optional<Integer> fixedCharges = Optional.empty();

    public TriggerItem(final int itemId) {
        this.itemId = itemId;
    }

    public TriggerItem fixedCharges(final int charges) {
        this.fixedCharges = Optional.of(charges);
        return this;
    }

    public TriggerItem quantityCharges() {
        this.quantityCharges = Optional.of(true);
        return this;
    }

    public TriggerItem hideOverlay() {
        this.hideOverlay = Optional.of(true);
        return this;
    }

    public TriggerItem needsToBeEquipped() {
        this.needsToBeEquipped = Optional.of(true);
        return this;
    }

    public TriggerItem maxCharges(final int charges) {
        this.maxCharges = Optional.of(charges);
        return this;
    }
}
