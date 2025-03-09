package com.fredplugins.kroovy.events;

import lombok.Data;
import net.runelite.api.coords.LocalPoint;

@Data
public class DestinationChanged
{

	private final LocalPoint from;

	private final LocalPoint to;

}