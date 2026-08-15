package com.fred4106.improvedCharges.item.triggers;

public class OnItemUsed extends TriggerBase {
    public int usedItemId;
    public int targetItemId;
    public boolean isBothWays = false;

    public OnItemUsed(int usedItemId, int targetItemId) {
        this.usedItemId = usedItemId;
        this.targetItemId = targetItemId;
    }

    public OnItemUsed isBothWays() {
        this.isBothWays = true;
        return this;
    }
}
