package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class GuthansPlatebody extends _BarrowsItem {
    public GuthansPlatebody(Provider provider) {
        super("Guthan's body", ItemID.BARROWS_GUTHAN_BODY, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BARROWS_GUTHAN_BODY).fixedCharges(1000),
            new TriggerItem(ItemID.BARROWS_GUTHAN_BODY_100),
            new TriggerItem(ItemID.BARROWS_GUTHAN_BODY_75),
            new TriggerItem(ItemID.BARROWS_GUTHAN_BODY_50),
            new TriggerItem(ItemID.BARROWS_GUTHAN_BODY_25),
            new TriggerItem(ItemID.BARROWS_GUTHAN_BODY_BROKEN).fixedCharges(0),
        };
    }
}