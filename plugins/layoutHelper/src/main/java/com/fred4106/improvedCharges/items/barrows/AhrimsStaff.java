package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class AhrimsStaff extends _BarrowsItem {
    public AhrimsStaff(Provider provider) {
        super("Ahrim's weapon", ItemID.BARROWS_AHRIM_WEAPON, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BARROWS_AHRIM_WEAPON).fixedCharges(1000),
            new TriggerItem(ItemID.BARROWS_AHRIM_WEAPON_100),
            new TriggerItem(ItemID.BARROWS_AHRIM_WEAPON_75),
            new TriggerItem(ItemID.BARROWS_AHRIM_WEAPON_50),
            new TriggerItem(ItemID.BARROWS_AHRIM_WEAPON_25),
            new TriggerItem(ItemID.BARROWS_AHRIM_WEAPON_BROKEN).fixedCharges(0),
        };
    }
}