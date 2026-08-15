package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class GuthansChainskirt extends _BarrowsItem {
    public GuthansChainskirt(Provider provider) {
        super("Guthan's skirt", ItemID.BARROWS_GUTHAN_LEGS, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BARROWS_GUTHAN_LEGS).fixedCharges(1000),
            new TriggerItem(ItemID.BARROWS_GUTHAN_LEGS_100),
            new TriggerItem(ItemID.BARROWS_GUTHAN_LEGS_75),
            new TriggerItem(ItemID.BARROWS_GUTHAN_LEGS_50),
            new TriggerItem(ItemID.BARROWS_GUTHAN_LEGS_25),
            new TriggerItem(ItemID.BARROWS_GUTHAN_LEGS_BROKEN).fixedCharges(0),
        };
    }
}