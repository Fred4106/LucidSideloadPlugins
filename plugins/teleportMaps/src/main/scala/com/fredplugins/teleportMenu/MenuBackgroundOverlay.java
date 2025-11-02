package com.fredplugins.teleportMenu;

import com.google.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.WeakHashMap;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.Rasterizer;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class MenuBackgroundOverlay extends Overlay
{
	public static final int IF_MENU = 187;

	private final Client client;
	private final ClientThread clientThread;
	private final FredsBetterTeleportMenuConfig config;

	private boolean drawBackground;
	private WeakHashMap<Widget, Integer> defaultHeight = new WeakHashMap<>();
	private boolean hasPatched = false;

	@Inject
	public MenuBackgroundOverlay(Client client, ClientThread clientThread, FredsBetterTeleportMenuConfig config)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.config = config;

		drawAfterLayer(IF_MENU, 0);
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(-1.f);
		setLayer(OverlayLayer.MANUAL);
	}

	public void onInterfaceLoaded()
	{
		this.hasPatched = false;
	}

	public void resize()
	{
		drawBackground = resize0();
	}

	private boolean resize0()
	{
		Widget bgContainer = client.getWidget(IF_MENU, 0);
		if (bgContainer == null || !config.expandScrollMenu())
		{
			return false;
		}

		Widget parent = bgContainer.getParent(); // 164:16
		Widget model = bgContainer.getChild(0);
		Widget title = bgContainer.getChild(1);
		Widget scrollbar = client.getWidget(IF_MENU, 2);
		Widget contents = client.getWidget(IF_MENU, 3);
		if (parent == null || model == null || scrollbar == null || contents == null || title == null || title.isHidden()) // isHidden for spirit-tree-maps compat
		{
			return false;
		}

		int contentHeight = defaultHeight(contents);
		int boundHeight = defaultHeight(bgContainer);

		int enlarge = Math.max(0, contents.getScrollHeight() - contentHeight);
		enlarge = Math.min(enlarge, parent.getParent().getHeight() - boundHeight);

		if (enlarge == 0)
		{
			return hasPatched;
		}

		hasPatched = true;

		int scrollY = contents.getScrollY();

		patchHeight(bgContainer, enlarge);
		patchHeight(parent, enlarge); // this is safe to change because toplevel_resize & toplevel_subchange will fix it for us
		patchHeight(scrollbar, enlarge);
		patchHeight(contents, enlarge);
		scrollbar.revalidateScroll();

		model.setHidden(true);

		boolean needScroll = contents.getHeight() < contents.getScrollHeight();
		scrollbar.setHidden(!needScroll);
		if (needScroll)
		{
			clientThread.invokeLater(() -> client.runScript(ScriptID.UPDATE_SCROLLBAR, scrollbar.getId(), contents.getId(), scrollY));
		}

		return true;
	}

	private int defaultHeight(Widget w)
	{
		return defaultHeight.computeIfAbsent(w, Widget::getOriginalHeight);
	}

	private void patchHeight(Widget w, int delta)
	{
		w.setOriginalHeight(defaultHeight(w) + delta);
		w.revalidate();
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (!drawBackground)
		{
			return null;
		}

		Widget container = client.getWidget(IF_MENU, 0);
		Widget content = client.getWidget(IF_MENU, 3);
		if (container == null || content == null || container.getChild(1) == null)
		{
			drawBackground = false;
			return null;
		}

		Widget text = container.getChild(1);

		ModelData md = client.loadModelData(26397);
		if (md == null)
		{
			return null;
		}

		Model m = md.cloneVertices().light();

		float[] vy = m.getVerticesZ();
		float min = Integer.MAX_VALUE;
		float max = Integer.MIN_VALUE;
		for (float y : vy)
		{
			min = Math.min(min, y);
			max = Math.max(max, y);
		}

		float totalHeight = max - min;
		float hiEdge = max - (312 * totalHeight / 334);
		float loEdge = max - (90 + totalHeight / 334);
		float adj = (hiEdge + loEdge) / 2;

		for (int i = 0; i < vy.length; i++)
		{
			float y = vy[i];
			float y2 = Math.min(Math.max(y, hiEdge), loEdge) - adj;
			y = (y - y2) + (y2 * content.getHeight()) / 232;
			vy[i] = y;
		}

		m.calculateBoundsCylinder();

		Rectangle bounds = container.getBounds();

		Rasterizer r = client.getRasterizer();
		r.setDrawRegion(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
		r.resetRasterClipping();
		m.drawFrustum(0,
			0, 0, 512,
			-1, 1020 - 68, -68);

		Rectangle textBounds = text.getBounds();
		text.getFont().drawWidgetText(text.getText(),
			textBounds.x, textBounds.y, textBounds.width, textBounds.height,
			text.getTextColor(), text.getTextShadowed() ? 0 : -1, 255,
			text.getXTextAlignment(), text.getYTextAlignment(), text.getLineHeight());

		return null;
	}
}
