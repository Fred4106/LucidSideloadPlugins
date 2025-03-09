package com.fredplugins.kroovy;

import lombok.Getter;

@Getter
public class ActionStoppedEvent extends GameActionEvent
{

	private final boolean interrupted;

	public ActionStoppedEvent(
			ActionEnum action, int productId, int actionCount, int startTick, int endTick, boolean interrupted)
	{
		super(action, productId, actionCount, startTick, endTick);
		this.interrupted = interrupted;
	}

}