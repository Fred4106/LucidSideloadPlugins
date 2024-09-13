
package com.fredplugins.mta;

import java.awt.Graphics2D;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.input.KeyListener;

public abstract class MTARoom
{
	@Getter(AccessLevel.PROTECTED)
	protected final MTAConfig config;

	@Inject
	protected MTARoom(MTAConfig config)
	{
		this.config = config;
	}

	public abstract boolean inside();

	public void under(Graphics2D graphics2D)
	{
	}

	public KeyListener[] keyListeners()
	{
		return new KeyListener[] {};
	}

	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
	}
}
