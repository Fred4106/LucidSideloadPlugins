package com.fredplugins.kroovy.detectors;

import com.fredplugins.kroovy.ActionEnum;
import com.fredplugins.kroovy.FredsActionProgressConfig;
import com.fredplugins.kroovy.data.Magic;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.runelite.api.*;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class StringJewellerySpellDetector extends ActionDetector
{
	@Inject private FredsActionProgressConfig config;

	@Inject private Client client;

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked evt)
	{
		if (!this.config.magicStringJewellery()) {
			return;
		}
		if (evt.getMenuAction() != MenuAction.CC_OP) {
			return;
		}
		ItemContainer inventory = this.client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null) {
			return;
		}
		if (!evt.getMenuTarget().equals("<col=00ff00>String Jewellery</col>")) {
			return;
		}

		for (Magic.StringJewellerySpell stringJewellerySpell : Magic.StringJewellerySpell.values()) {
			Magic.Spell spell = stringJewellerySpell.getSpell();
			Widget widget = this.client.getWidget(spell.getWidgetId());
			if (widget != null && widget.getBorderType() == 0) {
				int itemId = stringJewellerySpell.getJewelleryItemId();
				if (inventory.count(itemId) <= 0) {
					continue;
				}

				int amount = Math.min(inventory.count(itemId), spell.getAvailableCasts(this.client));
				this.actionManager.setAction(ActionEnum.MAGIC_STRING_JEWELLERY, amount, itemId);
				break;
			}
		}
	}

	@Override
	public void setup() {

	}

	@Override
	public void shutDown() {

	}
}
