package com.fredplugins.kroovy.detectors;

import com.fredplugins.kroovy.managers.ActionManager;
import com.fredplugins.kroovy.managers.InventoryManager;
import com.fredplugins.kroovy.data.Herblore;
import com.fredplugins.kroovy.ActionEnum;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.runelite.api.*;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

import static com.fredplugins.kroovy.ActionEnum.HERB_CLEAN;

@Singleton
public class ItemClickDetector extends ActionDetector
{

	@Inject private Client client;

	@Inject private InventoryManager inventoryManager;

	@Inject private ActionManager actionManager;

	@Override
	public void setup() {}

	@Override
	public void shutDown() 	{}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked evt)
	{
		if (evt.getMenuAction() != MenuAction.CC_OP || !evt.getMenuOption().toLowerCase().equals("clean")) {
			return;
		}
		int itemID = evt.getItemId();
		ItemContainer inventory = this.client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null || !inventory.contains(itemID)) {
			return;
		}
		if(!ArrayUtils.contains(Herblore.GRIMY_HERBS, itemID)) {
			return;
		}
		int amount = this.inventoryManager.getItemCountById(itemID);
		this.actionManager.setAction(HERB_CLEAN, amount, itemID);
	}

}
