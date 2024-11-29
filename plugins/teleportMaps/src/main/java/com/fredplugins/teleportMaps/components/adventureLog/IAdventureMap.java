package com.fredplugins.teleportMaps.components.adventureLog;

import com.fredplugins.teleportMaps.components.IMap;
import com.fredplugins.teleportMaps.ui.AdventureLogEntry;
import com.fredplugins.teleportMaps.ui.ClueCompass;
import com.fredplugins.teleportMaps.ui.UIHotkey;
import net.runelite.api.widgets.Widget;

public interface IAdventureMap extends IMap
{
	boolean matchesTitle(String title);
	void buildInterface(Widget adventureLogContainer);
}
