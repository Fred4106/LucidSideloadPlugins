package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItemWithStorageEmptyable;
import com.fred4106.improvedCharges.store.ids.ItemId;
import net.runelite.api.Skill;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;
import com.fred4106.improvedCharges.item.ChargedItemWithStorage;
import com.fred4106.improvedCharges.item.storage.StorableItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.WidgetId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fred4106.improvedCharges.store.ids.ItemContainerId.INVENTORY;

import java.util.List;
import java.util.Map;

public class U_PlankSack extends ChargedItemWithStorageEmptyable {
	public U_PlankSack(final Provider provider) {
		super(Constants.PLANK_SACK, ItemId.PLANK_SACK, provider);
		storage.setMaximumTotalQuantity(28).emptyIsNegative().storableItems(
			new StorableItem(ItemId.PLANK).checkName("Regular plank"),
			new StorableItem(ItemId.OAK_PLANK).checkName("Oak plank"),
			new StorableItem(ItemId.TEAK_PLANK).checkName("Teak plank"),
			new StorableItem(ItemId.MAHOGANY_PLANK).checkName("Mahogany plank"),
			new StorableItem(ItemId.CAMPHOR_PLANK).checkName("Camphor plank"),
			new StorableItem(ItemId.IRONWOOD_PLANK).checkName("Ironwood plank"),
			new StorableItem(ItemId.ROSEWOOD_PLANK).checkName("Rosewood plank")
		);

		this.items = new TriggerItem[]{
			new TriggerItem(ItemId.PLANK_SACK),
		};

		this.triggers.addAll(List.of(
			new OnChatMessage("Your sack is currently empty.").onItemClick().emptyStorage(),

			new OnVarbitsMapChanged(
				Map.of(
					VarbitID.PLANK_SACK_PLAIN, ItemId.PLANK,
					VarbitID.PLANK_SACK_OAK, ItemId.OAK_PLANK,
					VarbitID.PLANK_SACK_TEAK, ItemId.TEAK_PLANK,
					VarbitID.PLANK_SACK_MAHOGANY, ItemId.MAHOGANY_PLANK,
					VarbitID.PLANK_SACK_CAMPHOR, ItemId.CAMPHOR_PLANK,
					VarbitID.PLANK_SACK_IRONWOOD, ItemId.IRONWOOD_PLANK,
					VarbitID.PLANK_SACK_ROSEWOOD, ItemId.ROSEWOOD_PLANK
				)
			)
		));
	}
}