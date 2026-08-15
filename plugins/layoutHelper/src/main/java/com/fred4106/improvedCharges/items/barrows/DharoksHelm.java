package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class DharoksHelm extends _BarrowsItem {
    public DharoksHelm(Provider provider) {
        super("Dharok's helmet", ItemID.BARROWS_DHAROK_HEAD, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BARROWS_DHAROK_HEAD).fixedCharges(1000),
            new TriggerItem(ItemID.BARROWS_DHAROK_HEAD_100),
            new TriggerItem(ItemID.BARROWS_DHAROK_HEAD_75),
            new TriggerItem(ItemID.BARROWS_DHAROK_HEAD_50),
            new TriggerItem(ItemID.BARROWS_DHAROK_HEAD_25),
            new TriggerItem(ItemID.BARROWS_DHAROK_HEAD_BROKEN).fixedCharges(0),
        };
    }
}