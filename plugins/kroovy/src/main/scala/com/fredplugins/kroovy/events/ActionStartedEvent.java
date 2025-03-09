package com.fredplugins.kroovy.events;

import com.fredplugins.kroovy.ActionEnum;

public class ActionStartedEvent extends GameActionEvent
{

	public ActionStartedEvent(ActionEnum action, int productId, int actionCount, int startTick, int endTick)
	{
		super(action, productId, actionCount, startTick, endTick);
	}

}