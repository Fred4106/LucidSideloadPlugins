package com.fredplugins.kroovy.detectors;

import com.fredplugins.kroovy.ActionEnum;
import com.fredplugins.kroovy.CoalBag;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@Slf4j
public class SmithingDetector extends ActionDetector implements KeyListener
{

	@Inject private Client client;

	@Inject private ClientThread clientThread;
	
	@Inject private KeyManager keyManager;

	private static final Pattern X_BARS_PATTERN = Pattern.compile("^(?<x>\\d*) (bars?)$");

	private static final int VAR_AVAILABLE_MATERIALS = 2224;

	private static final int VAR_SMITHING_INTERFACE = 989;

	private static final int VAR_SELECTED_SMITHING_INDEX = 13895;

	private static final int SCRIPT_SMITHING_INIT = 431;

	private static final int ENUM_SMITHING_WIDGET_INDEX = 1101;

	private static final int ENUM_SMITHING_ITEM_BAR = 845;

	private int smithingItemid;

	private int numberOfBarsForSelectedItem;

	private boolean waitingForSmithingSelection = false;

	private HashMap<Integer, Integer> indexToItemId = new HashMap<Integer,Integer>();

	@Override
	public void keyTyped(KeyEvent event)
	{

	}
	
	@Override
	public void keyReleased(KeyEvent event)
	{

	}

	@Override
	public void keyPressed(KeyEvent event)
	{
		if (waitingForSmithingSelection && event.getKeyCode() == KeyEvent.VK_SPACE)
		{
			clientThread.invokeLater(() -> {
				int availableBars = this.client.getVarpValue(VAR_AVAILABLE_MATERIALS);
				if(numberOfBarsForSelectedItem == 0)
				{
					return;
				}

				if(isWearingSmithOutfit()){
					this.actionManager.setAction(ActionEnum.SMITHING_WITH_SMITH_OUTFIT, (availableBars / numberOfBarsForSelectedItem), smithingItemid);
				}
				else {
					this.actionManager.setAction(ActionEnum.SMITHING, (availableBars / numberOfBarsForSelectedItem), smithingItemid);
				}
			});
		}
	}

	@Override
	public void setup()
	{

		keyManager.registerKeyListener(this);
		CoalBag.setUnknownAmount();
	}

	@Override
	public void shutDown()
	{
		keyManager.unregisterKeyListener(this);
	}
	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		// running this under onClientTick as it is possible to close the widget on the same tick that it opens.
		// because the coal bag sometimes displays the emptied amount message as a widget, we need to check for that here.
		Widget coalBagWidget = client.getWidget(12648450);
		if (coalBagWidget != null)
		{
			CoalBag.updateAmount(coalBagWidget.getText());
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() == ChatMessageType.GAMEMESSAGE)
		{
			CoalBag.updateAmount(event.getMessage());
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged evt)
	{
		if (evt.getVarbitId() == VAR_SELECTED_SMITHING_INDEX) {			
			int index = evt.getValue();
			if(indexToItemId.containsKey(index)){
				smithingItemid = indexToItemId.get(index);
				numberOfBarsForSelectedItem = client.getEnum(ENUM_SMITHING_ITEM_BAR).getIntValue(smithingItemid);
			}			
		}
	}

	@Subscribe
	public void onVarClientIntChanged(VarClientIntChanged varClientIntChanged)
	{
		if (varClientIntChanged.getIndex() == VAR_SMITHING_INTERFACE)
		{
			waitingForSmithingSelection = !waitingForSmithingSelection;
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired evt)
	{
		if (evt.getScriptId() == SCRIPT_SMITHING_INIT){
			final int[] intStack = client.getIntStack();
			final int intStackSize = client.getIntStackSize();
			final int widgetId = intStack[intStackSize - 4];			
			final int itemId = intStack[intStackSize - 3];

			int index = client.getEnum(ENUM_SMITHING_WIDGET_INDEX).getIntValue(widgetId);
			indexToItemId.put(index, itemId);

			int var_index = this.client.getVarbitValue(VAR_SELECTED_SMITHING_INDEX);

			try {
				smithingItemid = indexToItemId.get(var_index);
				numberOfBarsForSelectedItem = client.getEnum(ENUM_SMITHING_ITEM_BAR).getIntValue(smithingItemid);
			}catch (NullPointerException e){
				log.debug("Item not found in indexToItemId map");
			}

		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked evt)
	{
		int param1 = evt.getParam1();
		if (param1 == -1) {
			return;
		}
		Widget widget = this.client.getWidget(param1);
		if (widget == null) {
			return;
		}
		if (widget.getParentId() == ComponentID.SMITHING_INVENTORY_ITEM_CONTAINER) {
			Widget widget1 = widget.getChild(1);
			Widget widget2 = widget.getChild(2);
			String text = widget1.getText();
			String text2 = widget2.getText();
			Matcher matcher = X_BARS_PATTERN.matcher(text);
			Matcher matcher2 = X_BARS_PATTERN.matcher(text2);
			if (matcher.matches() || matcher2.matches()) {
				String x = matcher.matches() ? matcher.group("x") : matcher2.group("x");
				int barsPerItem = Integer.parseInt(x);
				int availableBars = this.client.getVarpValue(VAR_AVAILABLE_MATERIALS);
				int productId = matcher.matches() ? widget2.getItemId() : widget1.getItemId() ;
				if(isWearingSmithOutfit()){
					this.actionManager.setAction(ActionEnum.SMITHING_WITH_SMITH_OUTFIT, (availableBars / barsPerItem), productId);
				}
				else {
					this.actionManager.setAction(ActionEnum.SMITHING, (availableBars / barsPerItem), productId);
				}
			}
		}
	}

	private boolean isWearingSmithOutfit(){
		ItemContainer gear = this.client.getItemContainer(InventoryID.EQUIPMENT);
		if (gear == null) {
			return false;
		}
        return gear.contains(ItemID.SMITHS_TUNIC) &&
                gear.contains(ItemID.SMITHS_TROUSERS) &&
                gear.contains(ItemID.SMITHS_BOOTS) &&
                (gear.contains(ItemID.SMITHS_GLOVES) || gear.contains(ItemID.SMITHS_GLOVES_I));
    }

}
