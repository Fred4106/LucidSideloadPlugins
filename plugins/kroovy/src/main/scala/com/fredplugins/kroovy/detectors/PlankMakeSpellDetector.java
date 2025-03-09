package com.fredplugins.kroovy.detectors;

import com.fredplugins.kroovy.ActionEnum;
import com.fredplugins.kroovy.FredsActionProgressConfig;
//import com.fredplugins.kroovy.data.Magic;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@Slf4j
public class PlankMakeSpellDetector extends ActionDetector
{

	@Inject private FredsActionProgressConfig config;

	@Inject private Client client;

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked evt)
	{
		if (!this.config.magicPlankMake()) {
			return;
		}
		if (evt.getMenuAction() != MenuAction.WIDGET_TARGET_ON_WIDGET) {
			return;
		}
		ItemContainer inventory = this.client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null) {
			return;
		}
		if (!evt.getMenuTarget().contains("<col=00ff00>Plank Make</col><col=ffffff> ->")) {
			return;
		}
//		for (Magic.PlankMakeSpell plankMakeSpell : Magic.PlankMakeSpell.values()) {
//			Magic.Spell spell = plankMakeSpell.getSpell();
//			Widget widget = this.client.getWidget(spell.getWidgetId());
//			if (widget == null || widget.getBorderType() != 2) {
//				continue;
//			}
//			int itemId = evt.getItemId();
//			if (plankMakeSpell.getPlank() != itemId || !inventory.contains(ItemID.COINS_995)) {
//				continue;
//			}
//
//			int coinsQuantity = 0;
//			for (Item item : inventory.getItems()) {
//				if(item.getId() == ItemID.COINS_995){
//					coinsQuantity = item.getQuantity();
//				}
//			}
//
//			int amount = Math.min(inventory.count(itemId), Math.min(spell.getAvailableCasts(this.client), coinsQuantity / plankMakeSpell.getCost()));
//			this.actionManager.setAction(ActionEnum.MAGIC_PLANK_MAKE, amount, itemId);
//			break;
//		}
	}

	@Override
	public void setup() {

	}

	@Override
	public void shutDown() {

	}
}
