package com.fredplugins.teleportMaps.ui;

import com.fredplugins.teleportMaps.definition.MushtreeDefinition;
import lombok.Getter;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.Keybind;

@Getter
public class Mushtree
{
	private Widget widget;
	private MushtreeDefinition definition;
	private Keybind hotkey;

	public Mushtree(MushtreeDefinition definition, Widget widget, Keybind hotkey)
	{
		this.definition = definition;
		this.widget = widget;
		this.hotkey = hotkey;
	}
}