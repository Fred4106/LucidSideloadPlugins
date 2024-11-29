package com.fredplugins.teleportMaps.ui;

import com.fredplugins.teleportMaps.definition.AdventureLogEntryDefinition;
import lombok.Getter;
import net.runelite.api.widgets.Widget;

@Getter
public class ClueCompass extends AdventureLogEntry
{
	private String displayedName;

	public ClueCompass(AdventureLogEntryDefinition definition, Widget widget, String shortcut, String displayedName)
	{
		super(definition, widget, shortcut);
		this.displayedName = displayedName;
	}
}
