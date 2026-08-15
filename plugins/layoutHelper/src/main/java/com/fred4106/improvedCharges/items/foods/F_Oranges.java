package com.fred4106.improvedCharges.items.foods;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class F_Oranges extends _Basket {
    public F_Oranges(Provider provider) {
        super("oranges", new TriggerItem[]{
            new TriggerItem(ItemID.BASKET_ORANGE_1).fixedCharges(1),
            new TriggerItem(ItemID.BASKET_ORANGE_2).fixedCharges(2),
            new TriggerItem(ItemID.BASKET_ORANGE_3).fixedCharges(3),
            new TriggerItem(ItemID.BASKET_ORANGE_4).fixedCharges(4),
            new TriggerItem(ItemID.BASKET_ORANGE_5).fixedCharges(5),
        }, provider);
    }   
}
