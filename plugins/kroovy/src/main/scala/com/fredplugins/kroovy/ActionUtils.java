package com.fredplugins.kroovy;

import com.fredplugins.kroovy.api.InventoryManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ActionUtils
{

	@Inject private InventoryManager inventoryManager;

	public int getActionsUntilFull(int nCreatePerAction, int nDestroyPerAction)
	{
		int freeSlots = this.inventoryManager.getFreeSpaces();
		int diffPerAction = (nCreatePerAction - nDestroyPerAction);
		if (diffPerAction <= 0) {
			return Integer.MAX_VALUE;
		}
		return (freeSlots / diffPerAction) + (freeSlots % nCreatePerAction == 0 ? 0 : 1);
	}

}