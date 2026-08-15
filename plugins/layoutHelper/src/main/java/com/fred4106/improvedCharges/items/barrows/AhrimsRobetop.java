package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class AhrimsRobetop extends _BarrowsItem {
    public AhrimsRobetop(Provider provider) {
        super("Ahrim's body", ItemID.BARROWS_AHRIM_BODY, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BARROWS_AHRIM_BODY).fixedCharges(1000),
            new TriggerItem(ItemID.BARROWS_AHRIM_BODY_100),
            new TriggerItem(ItemID.BARROWS_AHRIM_BODY_75),
            new TriggerItem(ItemID.BARROWS_AHRIM_BODY_50),
            new TriggerItem(ItemID.BARROWS_AHRIM_BODY_25),
            new TriggerItem(ItemID.BARROWS_AHRIM_BODY_BROKEN).fixedCharges(0),
        };
    }
}