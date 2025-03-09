package com.fredplugins.kroovy.detectors;

import com.fredplugins.kroovy.ActionEnum;
import com.fredplugins.kroovy.events.LocalAnimationChanged;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@Slf4j
public class SandpitDetector extends ActionDetector {
	@Inject private Client client;

	@Subscribe
	public void onLocalAnimationChanged(LocalAnimationChanged evt)
	{
		Player me = evt.player();
		if (me.getAnimation() != AnimationID.SAND_COLLECTION) {
			return;
		}
		if (this.actionManager.getCurrentAction() == ActionEnum.COLLECT_SAND) {
			return;
		}
		ItemContainer inventory = this.client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null) {
			return;
		}
		int buckets = inventory.count(ItemID.BUCKET);
		this.actionManager.setAction(ActionEnum.COLLECT_SAND, buckets, ItemID.BUCKET_OF_SAND);
	}

	@Override
	public void setup() {

	}

	@Override
	public void shutDown() {

	}
}
