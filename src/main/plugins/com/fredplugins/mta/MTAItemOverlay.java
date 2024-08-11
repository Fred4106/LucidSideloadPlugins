
package com.fredplugins.mta;

import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

class MTAItemOverlay extends WidgetItemOverlay
{
	private final FredsMTAPlugin plugin;

	@Inject
	public MTAItemOverlay(FredsMTAPlugin plugin)
	{
		this.plugin = plugin;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		for (MTARoom room : plugin.getRooms())
		{
			if (room.inside())
			{
				room.renderItemOverlay(graphics, itemId, widgetItem);
			}
		}
	}
}
