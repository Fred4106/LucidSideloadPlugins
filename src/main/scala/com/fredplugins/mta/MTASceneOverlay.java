
package com.fredplugins.mta;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class MTASceneOverlay extends Overlay
{
	private final FredsMTAPlugin plugin;

	@Inject
	public MTASceneOverlay(FredsMTAPlugin plugin)
	{
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (MTARoom room : plugin.getRooms())
		{
			if (room.inside())
			{
				graphics.setFont(FontManager.getRunescapeFont());
				room.under(graphics);
			}
		}

		return null;
	}
}
