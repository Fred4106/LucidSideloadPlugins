package com.fred4106.improvedCharges.item.triggers;

import java.util.*;

public class OnResetDaily extends TriggerBase {
    public Optional<Integer> resetSpecificItem = Optional.empty();

    public OnResetDaily specificItem(int itemId) {
        this.resetSpecificItem = Optional.of(itemId);
        return this;
    }
}
