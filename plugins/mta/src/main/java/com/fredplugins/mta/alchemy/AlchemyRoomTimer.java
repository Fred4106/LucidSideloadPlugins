
package com.fredplugins.mta.alchemy;

import java.awt.image.BufferedImage;
import java.time.temporal.ChronoUnit;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.Timer;
import net.runelite.client.util.ImageUtil;

public class AlchemyRoomTimer extends Timer
{
	private static final int RESET_PERIOD = 60;
	private static BufferedImage image;

	public AlchemyRoomTimer(Plugin plugin)
	{
		super(RESET_PERIOD, ChronoUnit.SECONDS, getResetImage(), plugin);
		this.setTooltip("Time until items swap");
	}

	private static BufferedImage getResetImage()
	{
		if (image != null)
		{
			return image;
		}

		image = ImageUtil.loadImageResource(AlchemyRoomTimer.class, "/util/reset.png");

		return image;
	}
}
