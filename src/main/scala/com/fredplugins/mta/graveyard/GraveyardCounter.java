
package com.fredplugins.mta.graveyard;

import java.awt.Color;
import java.awt.image.BufferedImage;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.Counter;

public class GraveyardCounter extends Counter
{
	GraveyardCounter(BufferedImage image, Plugin plugin)
	{
		super(image, plugin, 0);
	}

	@Override
	public Color getTextColor()
	{
		int count = getCount();
		if (count >= GraveyardRoom.MIN_SCORE)
		{
			return Color.GREEN;
		}
		else if (count == 0)
		{
			return Color.RED;
		}
		else
		{
			return Color.ORANGE;
		}
	}
}
