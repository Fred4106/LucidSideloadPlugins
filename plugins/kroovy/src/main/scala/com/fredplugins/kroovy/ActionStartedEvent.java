package com.fredplugins.kroovy;

public class ActionStartedEvent extends GameActionEvent
{

	public ActionStartedEvent(ActionEnum action, int productId, int actionCount, int startTick, int endTick)
	{
		super(action, productId, actionCount, startTick, endTick);
	}

}