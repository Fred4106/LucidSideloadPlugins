package com.fredplugins.kroovy.api.events;
import lombok.Data;
import net.runelite.api.coords.LocalPoint;

@Data
public class LocalPositionChanged
{

	private final LocalPoint from;

	private final LocalPoint to;

}
