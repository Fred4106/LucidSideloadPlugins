package com.fred4106.improvedCharges.item.triggers;

import java.util.Optional;

public class OnResetDaily extends TriggerBase {
    public Optional<Integer> resetSpecificItem = Optional.empty();

    public OnResetDaily specificItem(final int itemId) {
        this.resetSpecificItem = Optional.of(itemId);
        return this;
    }
}
