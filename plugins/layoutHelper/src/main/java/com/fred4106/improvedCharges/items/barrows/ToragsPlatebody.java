package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class ToragsPlatebody extends _BarrowsItem {
    public ToragsPlatebody(Provider provider) {
        super("Torag's body", ItemID.BARROWS_TORAG_BODY, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BARROWS_TORAG_BODY).fixedCharges(1000),
            new TriggerItem(ItemID.BARROWS_TORAG_BODY_100),
            new TriggerItem(ItemID.BARROWS_TORAG_BODY_75),
            new TriggerItem(ItemID.BARROWS_TORAG_BODY_50),
            new TriggerItem(ItemID.BARROWS_TORAG_BODY_25),
            new TriggerItem(ItemID.BARROWS_TORAG_BODY_BROKEN).fixedCharges(0)
        };
    }
}