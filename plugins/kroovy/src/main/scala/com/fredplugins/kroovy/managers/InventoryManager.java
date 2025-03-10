package com.fredplugins.kroovy.managers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fredplugins.kroovy.CoalBag;
import com.fredplugins.kroovy.events.ItemSelectionChanged;
import com.fredplugins.kroovy.events.ItemSelectionChanged$;
import com.fredplugins.kroovy.events.SlottedItem;
import com.fredplugins.kroovy.events.SlottedItem$;
import com.fredplugins.kroovy.events.SlottedItem.Empty$;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import java.util.Arrays;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

@Slf4j
@Singleton
public class InventoryManager
{
	static {
		((Logger) log).setLevel(Level.DEBUG);
	}

	@Inject private Client client;

	@Inject private EventBus eventBus;

	private SlottedItem previousSelectedItem = SlottedItem$.MODULE$.apply(null, -1);

	@Subscribe
	public void onGameTick(GameTick evt)  {
		if(previousSelectedItem != Empty$.MODULE$ && !client.isWidgetSelected()) {
			this.eventBus.post(new ItemSelectionChanged(previousSelectedItem,  Empty$.MODULE$));
			previousSelectedItem = Empty$.MODULE$;
		}
		//client.getSelectedWidget()
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked evt)
	{
		if (evt.getMenuAction() != MenuAction.WIDGET_TARGET) {
			return;
		}
		ItemContainer inventory = this.client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null) {
			return;
		}
		int idx = evt.getParam0();
		SlottedItem item = SlottedItem.apply(inventory, idx);
		if(item != previousSelectedItem) {
			this.eventBus.post(new ItemSelectionChanged(previousSelectedItem, item));
		}
		this.previousSelectedItem =  item;
	}

	public Stream<Item> getItems()
	{
		ItemContainer inventory = this.client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null) {
			return Stream.empty();
		}
		return Stream.of(inventory.getItems());
	}

	public int getFreeSpaces()
	{
		ItemContainer container = this.client.getItemContainer(InventoryID.INVENTORY);
		if (container == null) {
			return 0;
		}
		int free = 28;
		for (Item item : container.getItems()) {
			if (item.getId() >= 0) {
				free--;
			}
		}
		return free;
	}

	public int getItemCount(IntPredicate idPredicate)
	{
		return this.getItems().filter(it -> idPredicate.test(it.getId())).mapToInt(Item::getQuantity).sum();
	}

	public int getItemCountById(int... ids)
	{
		if (ids.length == 0) {
			throw new IllegalArgumentException("Must specify at least one item ID");
		}
		int[] copy = ids.clone();
		Arrays.sort(copy);
		return this.getItemCount(id -> Arrays.binarySearch(copy, id) >= 0);
	}

	public int getActionsUntilFull(int nCreatePerAction, int nDestroyPerAction)
	{
		int freeSlots = getFreeSpaces();
		int diffPerAction = (nCreatePerAction - nDestroyPerAction);
		if (diffPerAction <= 0) {
			return Integer.MAX_VALUE;
		}
		return (freeSlots / diffPerAction) + (freeSlots % nCreatePerAction == 0 ? 0 : 1);
	}

}