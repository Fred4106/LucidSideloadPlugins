package com.fredplugins.teleportMaps.definition;

import lombok.Getter;

@Getter
public class AdventureLogEntryDefinition
{
	private String name;
	private int x;
	private int y;
	private HotKeyDefinition hotkey = new HotKeyDefinition(-1, -1);
}
