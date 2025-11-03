package com.fredplugins.teleportMaps.ui;

import com.fredplugins.teleportMaps.definition.TreeDefinition;
import lombok.Getter;
import net.runelite.api.widgets.Widget;

@Getter
public class Tree extends AdventureLogEntry
{
	private String displayedName;

	public Tree(TreeDefinition definition, Widget widget, String shortcut, String displayedName)
	{
		super(definition, widget, shortcut);
		this.displayedName = displayedName;
	}
}
