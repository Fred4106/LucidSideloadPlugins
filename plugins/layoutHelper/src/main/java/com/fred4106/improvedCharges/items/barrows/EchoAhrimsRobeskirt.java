package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.ItemID;

public class EchoAhrimsRobeskirt extends _BarrowsItem {
    public EchoAhrimsRobeskirt(Provider provider) {
        super("Echo Ahrim's skirt", ItemID.BARROWS_AHRIM_LEGS_ORNAMENT, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BARROWS_AHRIM_LEGS_ORNAMENT).fixedCharges(1000),
            new TriggerItem(ItemID.BARROWS_AHRIM_LEGS_ORNAMENT_100),
            new TriggerItem(ItemID.BARROWS_AHRIM_LEGS_ORNAMENT_75),
            new TriggerItem(ItemID.BARROWS_AHRIM_LEGS_ORNAMENT_50),
            new TriggerItem(ItemID.BARROWS_AHRIM_LEGS_ORNAMENT_25),
            new TriggerItem(ItemID.BARROWS_AHRIM_LEGS_ORNAMENT_BROKEN).fixedCharges(0),
        };
    }
}