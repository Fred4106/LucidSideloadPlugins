package com.fredplugins.kroovy.events;

import com.fredplugins.kroovy.ActionEnum;
import lombok.Data;

@Data
public abstract class GameActionEvent
{

	private final ActionEnum action;

	private final int productId;

	private final int actionCount;

	private final int startTick;

	private final int endTick;

}