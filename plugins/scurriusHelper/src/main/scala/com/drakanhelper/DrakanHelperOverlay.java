package com.drakanhelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;

import ch.qos.logback.classic.Level;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrakanHelperOverlay extends Overlay {

	private final static Logger log;
	static {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(DrakanHelperOverlay.class)).setLevel(Level.DEBUG);
		log = LoggerFactory.getLogger(DrakanHelperOverlay.class);
	}

	private final Client client;
	private final DrakanHelperPlugin plugin;
	private final DrakanHelperConfig config;

	@Inject
	DrakanHelperOverlay(Client client, DrakanHelperPlugin plugin, DrakanHelperConfig config) {
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		final NPC boss = plugin.getBoss();
		if (boss == null) {
			return null;
		}
		graphics.setFont(plugin.getFont());
		// Live blood marks — only trustworthy for the big radial AoE (for the spear lunge they
		// render offset from the real strike, so the lunge is driven off the animation instead).
		final Set<Point> marks = new HashSet<>();
		final List<LocalPoint> markLp = new ArrayList<>();
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		for (final GraphicsObject go : client.getGraphicsObjects()) {
			if (go.getId() != DrakanHelperPlugin.DANGER_MARK_GFX) {
				continue;
			}
			final LocalPoint lp = go.getLocation();
			if (lp == null) {
				continue;
			}
			markLp.add(lp);
			final int sx = lp.getSceneX();
			final int sy = lp.getSceneY();
			marks.add(new Point(sx, sy));
			minX = Math.min(minX, sx);
			maxX = Math.max(maxX, sx);
			minY = Math.min(minY, sy);
			maxY = Math.max(maxY, sy);
		}

		final boolean bloom = marks.size() >= config.aoeThreshold();

		if (bloom && config.highlightAoe()) {
			for (final LocalPoint lp : markLp) {
				fillTile(graphics, lp, config.dangerColor());
			}
			for (int sx = minX; sx <= maxX; sx++) {
				for (int sy = minY; sy <= maxY; sy++) {
					if (!marks.contains(new Point(sx, sy))) {
						fillTile(graphics, sceneTile(sx, sy), config.safeColor());
					}
				}
			}
		}
		if (config.safeClickTiles()) {
			renderSafeSequence(graphics, boss);
		}

		if (config.specialSafeSpots() && plugin.specialType() != 0) {
			renderSpecialSafe(graphics, boss);
		}

		if (config.specialSafeSpots() && plugin.chargeTicks() > 0) {
			renderChargeSafe(graphics);
		}

		if (config.lungeForecast()) {
			renderForecast(graphics, boss);
		}

		drawBanner(graphics);
		return null;
	}

	/**
	 * Exact dodge tiles for the remaining strikes of the forecast combo, numbered in click order.
	 * The tiles come from the plugin's world-locked plan: they are computed once at wind-up and
	 * STAY PUT while the player follows the path (the plan only re-anchors on a real deviation),
	 * so each box is a stable click target rather than an offset that chases the player.
	 */
	private void renderSafeSequence(Graphics2D g, NPC boss) {
		final List<LocalPoint> tiles = plugin.plannedTiles();
		final String chain = plugin.plannedChain();
		final int consumed = plugin.strikesConsumed();
		if (tiles.isEmpty() || consumed >= tiles.size() || chain.length() < tiles.size()) {
			return;
		}

//		final Font font = g.getFont().deriveFont(Font.BOLD, 16f);
//		g.setFont(font);
		final FontMetrics fm = g.getFontMetrics();
		final boolean hot = plugin.clickNow();

		for (int i = consumed; i < tiles.size(); i++) {
			final LocalPoint tile = tiles.get(i);

			// Numbers are PERMANENT for the combo (assigned at build, never renumbered) — the
			// renumbering cascade after each strike read as "tiles jumping around". Consumed
			// tiles simply vanish; the pulsing HOT outline — not the numbers — marks the current
			// click. The next tile stays cold until its click window opens: clicking early walks
			// the player onto it to stand and be auto-hit; the roll only procs on an on-beat click.
			final Color base = config.safeColor();
			final boolean thisHot = i == consumed && hot;
			final int alpha = thisHot ? Math.min(255, base.getAlpha() + 80) : base.getAlpha();
			fillTile(g, tile, new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));

			final net.runelite.api.Point tp = Perspective.localToCanvas(client, tile, client.getPlane());
			if (tp == null) {
				continue;
			}
			if (thisHot) {
				final Polygon poly = Perspective.getCanvasTilePoly(client, tile);
				if (poly != null) {
					final boolean on = (client.getGameCycle() / 20) % 2 == 0;
					g.setColor(on ? Color.YELLOW : Color.WHITE);
					g.setStroke(new BasicStroke(4));
					g.drawPolygon(poly);
				}
			}
			final String label = chain.charAt(i) == 'B' ? (i + 1) + "*" : Integer.toString(i + 1);
			final int lx = tp.getX() - fm.stringWidth(label) / 2;
			g.setColor(Color.BLACK);
			g.drawString(label, lx + 1, tp.getY() + 1);
			g.setColor(thisHot ? Color.YELLOW : Color.WHITE);
			g.drawString(label, lx, tp.getY());
		}
	}

	/**
	 * Dodge arrows for the predicted combo chain, drawn above Drakan. Each arrow is the SCREEN
	 * side to be on for that strike (opposite the struck tile); '✱' marks the radial AoE (hug
	 * Drakan or leave the ring). Struck-through past strikes are grayed, the next is yellow.
	 */
	private void renderForecast(Graphics2D g, NPC boss) {
		final String chain = plugin.forecastChain();
		if (chain.isEmpty()) {
			return;
		}

		// Which screen side is Drakan's left? Compare canvas x of his tile vs one tile to his left.
		final LocalPoint c = boss.getLocalLocation();
		if (c == null) {
			return;
		}
		final int[] f = cardinal(boss.getOrientation());
		final int[] l = {-f[1], f[0]};
		final net.runelite.api.Point p0 = Perspective.localToCanvas(client, c, client.getPlane());
		final net.runelite.api.Point pL = Perspective.localToCanvas(
			client,
			new LocalPoint(
				c.getX() + l[0] * Perspective.LOCAL_TILE_SIZE,
				c.getY() + l[1] * Perspective.LOCAL_TILE_SIZE
			), client.getPlane()
		);
		if (p0 == null || pL == null) {
			return;
		}
		final boolean hisLeftIsScreenLeft = pL.getX() < p0.getX();

		final Font font = g.getFont();
//		final Font font = g.getFont().deriveFont(Font.BOLD, 28f);
//		g.setFont(font);

		final String[] symbs = new String[] {"<", ">", "*"};
		final String symbolList = "\u25C1\u25B7\u25EF";
		for(int jjj = 0; jjj < symbs.length; jjj++) {
			int codePoint = symbolList.codePointAt(jjj);
			char charz = symbolList.charAt(jjj);
			String reversedCodePoint = Character.toString(codePoint);
			if (font.canDisplay(codePoint)) {
				symbs[jjj] = ""+charz;
			}
		}

		final StringBuilder disp = new StringBuilder();
		for (int i = 0; i < chain.length(); i++) {
			final char s = chain.charAt(i);
			if (s == 'B') {
				disp.append(symbs[2]);
			} else {
				// dodge side = opposite of the struck tile, converted to screen space
				final boolean dodgeHisLeft = s == 'R';
				disp.append(dodgeHisLeft == hisLeftIsScreenLeft ? symbs[0] : symbs[1]);
			}
			if (i < chain.length() - 1) {
				disp.append(' ');
			}
		}

		final String text = disp.toString();
		final net.runelite.api.Point loc = boss.getCanvasTextLocation(g, text, 320);
		if (loc == null) {
			return;
		}
		final FontMetrics fm = g.getFontMetrics();
		final int consumed = plugin.strikesConsumed();
		int x = loc.getX();
		final int y = loc.getY();
		int idx = 0; // strike index (display has separator spaces)
		for (int i = 0; i < text.length(); i++) {
			final String ch = String.valueOf(text.charAt(i));
			if (!ch.equals(" ")) {
				final Color col = idx < consumed ? new Color(110, 110, 110)
					: idx == consumed ? Color.YELLOW : Color.WHITE;
				g.setColor(Color.BLACK);
				g.drawString(ch, x + 2, y + 2);
				g.setColor(col);
				g.drawString(ch, x, y);
				idx++;
			}
			x += fm.stringWidth(ch);
		}
	}

	/**
	 * Safe tiles for the two wind-up specials, using the orientation locked at wind-up:
	 * front/back wave → his flanks (2 tiles per side, one tile of clearance from his body);
	 * semicircle → directly behind him (2 ranks x both lanes). A tick countdown to impact is
	 * drawn on the safe tile nearest the player.
	 */
	private void renderSpecialSafe(Graphics2D g, NPC boss) {
		final LocalPoint c = boss.getLocalLocation();
		if (c == null) {
			return;
		}
		final int[] f = cardinal(plugin.specialOrientation());
		final int[] l = {-f[1], f[0]};
		final int t = Perspective.LOCAL_TILE_SIZE;
		final int h = t / 2;

		final List<LocalPoint> tiles = new ArrayList<>();
		if (plugin.specialType() == 1) {
			// front/back wave: beside his two body ranks, 2 tiles out from each flank
			for (int side = -1; side <= 1; side += 2) {
				for (int rank = -1; rank <= 1; rank += 2) {
					tiles.add(new LocalPoint(
						c.getX() + l[0] * side * (h + 2 * t) + f[0] * rank * h,
						c.getY() + l[1] * side * (h + 2 * t) + f[1] * rank * h,
						c.getWorldView()
					));
				}
			}
		} else {
			// semicircle: directly behind him, both lanes, two ranks deep
			for (int lane = -1; lane <= 1; lane += 2) {
				for (int k = 1; k <= 2; k++) {
					tiles.add(new LocalPoint(
						c.getX() - f[0] * (h + k * t) + l[0] * lane * h,
						c.getY() - f[1] * (h + k * t) + l[1] * lane * h
					));
				}
			}
		}

		final Color base = config.safeColor();
		final Color bright = new Color(
			base.getRed(), base.getGreen(), base.getBlue(),
			Math.min(255, base.getAlpha() + 60)
		);
		for (final LocalPoint tile : tiles) {
			fillTile(g, tile, bright);
		}

		// countdown on the safe tile nearest the player
		final Player local = client.getLocalPlayer();
		final int impact = plugin.specialImpactTicks();
		if (local == null || local.getLocalLocation() == null || impact <= 0) {
			return;
		}
		final LocalPoint p = local.getLocalLocation();
		LocalPoint nearest = tiles.get(0);
		long best = Long.MAX_VALUE;
		for (final LocalPoint tile : tiles) {
			final long dx = tile.getX() - p.getX();
			final long dy = tile.getY() - p.getY();
			final long d2 = dx * dx + dy * dy;
			if (d2 < best) {
				best = d2;
				nearest = tile;
			}
		}
		final net.runelite.api.Point tp = Perspective.localToCanvas(client, nearest, client.getPlane());
		if (tp != null) {
//			g.setFont(g.getFont().deriveFont(Font.BOLD, 22f));
			final String label = Integer.toString(impact);
			final FontMetrics fm = g.getFontMetrics();
			final int lx = tp.getX() - fm.stringWidth(label) / 2;
			g.setColor(Color.BLACK);
			g.drawString(label, lx + 1, tp.getY() + 1);
			g.setColor(Color.YELLOW);
			g.drawString(label, lx, tp.getY());
		}
	}

	/**
	 * P3 reappear-charge: Drakan dashes along the line to the player; safe = a perpendicular
	 * sidestep ("above or below his vision"), running at or away from him stays on the line.
	 * Paints 2 sidestep tiles on each side of where the player stood at the reappear.
	 */
	private void renderChargeSafe(Graphics2D g) {
		final LocalPoint a = plugin.chargeAnchor();
		final int[] perp = plugin.chargePerp();
		if (a == null || perp == null) {
			return;
		}
		final int t = Perspective.LOCAL_TILE_SIZE;
		final Color base = config.p3safeColor();
		final Color bright = new Color(
			base.getRed(), base.getGreen(), base.getBlue(),
			Math.min(255, base.getAlpha() + 60)
		);
		final List<LocalPoint> tiles = new ArrayList<>();
		for (int side = -1; side <= 1; side += 2) {
			for (int d = 2; d <= 3; d++) {
				tiles.add(new LocalPoint(a.getX() + perp[0] * side * d * t, a.getY() + perp[1] * side * d * t, a.getWorldView()));
			}
		}
		for (final LocalPoint tile : tiles) {
			fillTile(g, tile, bright);
		}

		// countdown on the tile nearest the player
		final Player local = client.getLocalPlayer();
		if (local == null || local.getLocalLocation() == null) {
			return;
		}
		final LocalPoint p = local.getLocalLocation();
		LocalPoint nearest = tiles.get(0);
		long best = Long.MAX_VALUE;
		for (final LocalPoint tile : tiles) {
			final long dx = tile.getX() - p.getX();
			final long dy = tile.getY() - p.getY();
			final long d2 = dx * dx + dy * dy;
			if (d2 < best) {
				best = d2;
				nearest = tile;
			}
		}
		final net.runelite.api.Point tp = Perspective.localToCanvas(client, nearest, client.getPlane());
		if (tp != null) {
//			g.setFont(g.getFont().deriveFont(Font.BOLD, 22f));
			final String label = Integer.toString(plugin.chargeTicks());
			final FontMetrics fm = g.getFontMetrics();
			final int lx = tp.getX() - fm.stringWidth(label) / 2;
			g.setColor(Color.BLACK);
			g.drawString(label, lx + 1, tp.getY() + 1);
			g.setColor(Color.YELLOW);
			g.drawString(label, lx, tp.getY());
		}
	}

	/**
	 * Snap a 0..2047 orientation to a cardinal unit vector (x east, y north).
	 */
	private static int[] cardinal(int orientation) {
		final int s = Math.floorMod(Math.round(orientation / 512f), 4);
		switch (s) {
			case 0:
				return new int[]{0, -1}; // South
			case 1:
				return new int[]{-1, 0}; // West
			case 2:
				return new int[]{0, 1};  // North
			default:
				return new int[]{1, 0};  // East
		}
	}

	private static LocalPoint sceneTile(int sceneX, int sceneY) {
		return new LocalPoint(
			sceneX * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_TILE_SIZE / 2,
			sceneY * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_TILE_SIZE / 2
		);
	}

	private void fillTile(Graphics2D g, LocalPoint lp, Color c) {
		final Polygon poly = Perspective.getCanvasTilePoly(client, lp);
		if (poly == null) {
			return;
		}
		g.setColor(c);
		g.fillPolygon(poly);
		g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.min(255, c.getAlpha() + 130)));
		g.setStroke(new BasicStroke(2));
		g.drawPolygon(poly);
	}

	private void drawBanner(Graphics2D g) {
		String text = null;
		Color col = Color.WHITE;
		boolean flashing = false;
		if (config.prayerFlash() && plugin.prayMagic()) {
			text = "PRAY MAGIC";
			col = new Color(120, 180, 255);
			flashing = true;
		} else if (config.aoeWarning() && plugin.bloomActive()) {
			text = "MOVE!  hug Drakan or get out";
			col = new Color(255, 140, 0);
		} else if (config.aoeWarning() && plugin.lungeActive()) {
			text = "SPEAR COMBO — stay on green, back away";
			col = Color.YELLOW;
		} else if (config.aoeWarning() && plugin.comboIncoming() > 0) {
			text = "COMBO INCOMING — " + plugin.comboIncoming();
			col = new Color(255, 200, 0);
		}

		if (text != null && !(flashing && (client.getGameCycle() / 25) % 2 != 0)) {
			drawCentered(g, text, col);
		}

		if (config.showPhase()) {
			final int hp = plugin.hpPct();
			drawCorner(g, plugin.phase() + "   " + (hp >= 0 ? hp + "%" : "?"));
		}
	}

	private void drawCentered(Graphics2D g, String text, Color col) {
//		g.setFont(g.getFont().deriveFont(Font.BOLD, 26f));
//		g.setFont(plugin.getFont());
		final FontMetrics fm = g.getFontMetrics();
		final int w = fm.stringWidth(text);
		final int cx = client.getViewportXOffset() + client.getViewportWidth() / 2;
		final int y = client.getViewportYOffset() + 90;
		final int x = cx - w / 2;
		g.setColor(new Color(0, 0, 0, 150));
		g.fillRoundRect(x - 14, y - fm.getAscent() - 6, w + 28, fm.getHeight() + 12, 12, 12);
		g.setColor(Color.BLACK);
		g.drawString(text, x + 2, y + 2);
		g.setColor(col);
		g.drawString(text, x, y);
	}

	private void drawCorner(Graphics2D g, String text) {
//		g.setFont(g.getFont().deriveFont(Font.BOLD, 16f));
		final int x = client.getViewportXOffset() + 10;
		final int y = client.getViewportYOffset() + client.getViewportHeight() - 14;
		g.setColor(Color.BLACK);
		g.drawString(text, x + 1, y + 1);
		g.setColor(Color.WHITE);
		g.drawString(text, x, y);
	}
}
