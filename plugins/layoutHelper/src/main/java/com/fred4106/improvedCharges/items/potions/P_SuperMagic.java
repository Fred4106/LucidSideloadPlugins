package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.*;

public class P_SuperMagic extends _Potion {
    public P_SuperMagic(Provider provider) {
        super("super_magic", new TriggerItem[]{
            new TriggerItem(ItemID.NZONE1DOSE2MAGICPOTION).fixedCharges(1),
            new TriggerItem(ItemID.NZONE2DOSE2MAGICPOTION).fixedCharges(2),
            new TriggerItem(ItemID.NZONE3DOSE2MAGICPOTION).fixedCharges(3),
            new TriggerItem(ItemID.NZONE4DOSE2MAGICPOTION).fixedCharges(4),
        }, provider);
    }
}
