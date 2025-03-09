package com.fredplugins.kroovy.api.events;

import lombok.Data;

/**
 * An event fired when an action is interrupted
 */
@Data
public class Interrupt
{

	private final Object source;

	private boolean consumed = false;

}
