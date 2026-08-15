package com.fred4106.improvedCharges.items.foods;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class _Basket extends ChargedItem {
    public _Basket(
        String configKey,
        TriggerItem[] items,
        Provider provider
    ) {
        super(FredsItemChargesConfig.baskets + "_" + configKey, items[0].itemId, provider);
        this.items = items;
    }

    @Override
    public String getConfigKey() {
        return FredsItemChargesConfig.baskets;
    }
}
