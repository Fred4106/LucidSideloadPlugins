package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class KarilsCoif extends _BarrowsItem {
    public KarilsCoif(Provider provider) {
        super("Karil's coif", ItemID.BARROWS_KARIL_HEAD, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BARROWS_KARIL_HEAD).fixedCharges(1000),
            new TriggerItem(ItemID.BARROWS_KARIL_HEAD_100),
            new TriggerItem(ItemID.BARROWS_KARIL_HEAD_75),
            new TriggerItem(ItemID.BARROWS_KARIL_HEAD_50),
            new TriggerItem(ItemID.BARROWS_KARIL_HEAD_25),
            new TriggerItem(ItemID.BARROWS_KARIL_HEAD_BROKEN).fixedCharges(0)
        };
    }
}