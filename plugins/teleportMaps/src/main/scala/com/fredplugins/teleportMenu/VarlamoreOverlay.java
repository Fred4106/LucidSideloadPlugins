package com.fredplugins.teleportMenu;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.Perspective;
import net.runelite.api.Rasterizer;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class VarlamoreOverlay extends Overlay
{
	private static final int IF_VARLAMORE_TELEMENU = 874;
	private static final int CC_VT_BG_LAYER = 2;
	private static final int CC_VT_BG_MODEL = 3;

	private final Client client;
	private final FredsBetterTeleportMenuConfig config;

	@Inject
	public VarlamoreOverlay(Client client, FredsBetterTeleportMenuConfig config)
	{
		this.client = client;
		this.config = config;

		drawAfterLayer(IF_VARLAMORE_TELEMENU, CC_VT_BG_LAYER);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.MANUAL);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		Widget bgWidget = client.getWidget(IF_VARLAMORE_TELEMENU, CC_VT_BG_MODEL);
		if (bgWidget == null || bgWidget.getType() != WidgetType.MODEL || !config.enhanceQuetzalContrast())
		{
			return null;
		}

		ModelData md = client.loadModelData(bgWidget.getModelId());
		if (md == null)
		{
			return null;
		}

		Model m = md.cloneTransparencies(true).light();

		byte[] trans = m.getFaceTransparencies();
		for (int i = 0; i < trans.length; i++)
		{
			int v = 256 - trans[i];
			v /= 2;
			trans[i] = (byte) (256 - v);
		}

		m.calculateBoundsCylinder();

		Rasterizer r = client.getRasterizer();

		Rectangle bounds = bgWidget.getParent().getBounds();

		r.setDrawRegion(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
		r.resetRasterClipping();

		int var32 = bgWidget.getModelZoom() * Perspective.SINE[bgWidget.getRotationX()] >> 16;
		int var24 = bgWidget.getModelZoom() * Perspective.COSINE[bgWidget.getRotationX()] >> 16;
		m.drawOrtho(0,
			bgWidget.getRotationZ(), bgWidget.getRotationY(), bgWidget.getRotationX(),
			0, var32, var24, bgWidget.getModelZoom());

		return null;
	}
}
