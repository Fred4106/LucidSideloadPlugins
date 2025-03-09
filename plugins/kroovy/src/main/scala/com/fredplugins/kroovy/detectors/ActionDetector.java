package com.fredplugins.kroovy.detectors;

import com.fredplugins.kroovy.ActionEnum;
import com.fredplugins.kroovy.FredsActionProgressPlugin;
import com.fredplugins.kroovy.managers.ActionManager;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public abstract class ActionDetector
{
//	protected final HashMap<Integer, ActionEnum> itemActions = new HashMap<Integer, ActionEnum>();

	@Inject protected ItemManager itemManager;

	@Inject protected ActionManager actionManager;
//
//	protected void registerAction(ActionEnum action, int... itemIds)
//	{
//		for (int id : itemIds) {
//			this.itemActions.put(id, action);
//			log.debug("Registered action {} for item: {}", action, id);
//		}
//	}
//
//	protected void setActionByItemId(int itemId, int amount)
//	{
//		log.debug("looking for action by item id: {}", itemId);
//		ActionEnum action = this.itemActions.get(itemId);
//		if (action == null) {
//			this.unhandled(itemId);
//		} else {
//			this.actionManager.setAction(action, amount, itemId);
//			log.debug("set action {} {} {}", action, amount, itemId);
//		}
//	}

//	protected void unhandled(int itemId)
//	{
//		log.error("Unhandled product: {}", itemId);
//	}

	public abstract void setup();

	public abstract void shutDown();

}