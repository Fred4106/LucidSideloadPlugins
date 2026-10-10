package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.ItemID;

public class EchoAhrimsHood extends _BarrowsItem {
    public EchoAhrimsHood(Provider provider) {
        super("Echo Ahrim's hood", ItemID.BARROWS_AHRIM_HEAD_ORNAMENT, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BARROWS_AHRIM_HEAD_ORNAMENT).fixedCharges(1000),
            new TriggerItem(ItemID.BARROWS_AHRIM_HEAD_ORNAMENT_100),
            new TriggerItem(ItemID.BARROWS_AHRIM_HEAD_ORNAMENT_75),
            new TriggerItem(ItemID.BARROWS_AHRIM_HEAD_ORNAMENT_50),
            new TriggerItem(ItemID.BARROWS_AHRIM_HEAD_ORNAMENT_25),
            new TriggerItem(ItemID.BARROWS_AHRIM_HEAD_ORNAMENT_BROKEN).fixedCharges(0),
        };
    }
}