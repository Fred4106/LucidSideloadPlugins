package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class KarilsCrossbow extends _BarrowsItem {
    public KarilsCrossbow(Provider provider) {
        super("Karil's weapon", ItemID.BARROWS_KARIL_WEAPON, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BARROWS_KARIL_WEAPON).fixedCharges(1000),
            new TriggerItem(ItemID.BARROWS_KARIL_WEAPON_100),
            new TriggerItem(ItemID.BARROWS_KARIL_WEAPON_75),
            new TriggerItem(ItemID.BARROWS_KARIL_WEAPON_50),
            new TriggerItem(ItemID.BARROWS_KARIL_WEAPON_25),
            new TriggerItem(ItemID.BARROWS_KARIL_WEAPON_BROKEN).fixedCharges(0)
        };
    }
}