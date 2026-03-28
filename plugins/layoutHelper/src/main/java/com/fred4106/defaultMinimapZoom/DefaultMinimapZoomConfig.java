package com.fred4106.defaultMinimapZoom;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("DefaultMinimapZoom")
public interface DefaultMinimapZoomConfig extends Config
{
	@ConfigItem(
			name = "Default zoom level",
			keyName = "zoomLevel",
			description = "Zoom has to be enabled in the Minimap plugin and the Minimap plugin has to be enabled as well!<br>" +
					"This is the number of pixels per tile on the minimap. A higher number is more zoomed in; a lower number is more zoomed out.",
			position = 0
	)
	default MinimapZoomLevel zoomLevel()
	{
		return MinimapZoomLevel.Zoom400;
	}

	@ConfigItem(
			keyName = "zoomWhenStartingClient",
			name = "Set zoom when starting client",
			description = "Set the default zoom when starting the client",
			position = 1
	)
	default boolean zoomWhenStartingClient() {
		return true;
	}

	@ConfigItem(
			keyName = "zoomWhenLogin",
			name = "Set zoom at every login",
			description = "Set the default zoom every time you log in",
			position = 2
	)
	default boolean zoomWhenLogin() {
		return false;
	}

	@ConfigItem(
			keyName = "zoomWhenHopping",
			name = "Set zoom when hopping worlds",
			description = "Set the default zoom every time you hop worlds",
			position = 3
	)
	default boolean zoomWhenHopping() {
		return false;
	}

	@ConfigItem(
			keyName = "zoomWhenRightClick",
			name = "Set zoom when right-clicking minimap",
			description = "Set the default zoom when you right-click the minimap",
			position = 4
	)
	default boolean zoomWhenRightClick() {
		return true;
	}
}