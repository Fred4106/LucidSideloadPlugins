package com.fredplugins.kroovy.detectors;

import com.fredplugins.kroovy.ActionEnum;
import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.annotations.Varbit;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

public class TemporossRewardPoolDetector extends ActionDetector
{

	@Varbit private static final int VAR_TEMPOROSS_PERMITS = 11936;

	@Inject private Client client;

	private int previousPermits = -1;

	@Subscribe
	public void onGameTick(GameTick evt)
	{
		int permits = this.client.getVarbitValue(VAR_TEMPOROSS_PERMITS);
		if (permits < this.previousPermits) {
			if (this.actionManager.getCurrentAction() != ActionEnum.TEMPOROSS_REWARD_POOL) {
				this.actionManager.setAction(ActionEnum.TEMPOROSS_REWARD_POOL, permits + 1, -1);
			}
		}
		this.previousPermits = permits;
	}

	@Override
	public void setup() {

	}

	@Override
	public void shutDown() {

	}
}
