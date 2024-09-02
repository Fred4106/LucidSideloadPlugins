package com.fredplugins.common;

import com.fredplugins.gauntlet.Vertex;
import com.fredplugins.gauntlet.overlay.InterfaceTab;
import com.fredplugins.gauntlet.overlay.PrayerExtended;
import com.google.common.base.Strings;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Model;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.Scene;
import net.runelite.api.VarClientInt;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.model.Jarvis;
import net.runelite.api.widgets.Widget;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.runelite.api.Constants.EXTENDED_SCENE_SIZE;
import static net.runelite.api.Constants.TILE_FLAG_BRIDGE;
import static net.runelite.client.ui.overlay.OverlayUtil.renderPolygon;

public class OldOverlayUtil {
	private static int getHeight(@Nonnull Scene scene, int localX, int localY, int plane)
	{
		int sceneX = (localX >> Perspective.LOCAL_COORD_BITS) + ((Constants.EXTENDED_SCENE_SIZE - Constants.SCENE_SIZE) / 2);
		int sceneY = (localY >> Perspective.LOCAL_COORD_BITS) + ((Constants.EXTENDED_SCENE_SIZE - Constants.SCENE_SIZE) / 2);
		if (sceneX >= 0 && sceneY >= 0 && sceneX < Constants.EXTENDED_SCENE_SIZE && sceneY < Constants.EXTENDED_SCENE_SIZE)
		{
			int[][][] tileHeights = scene.getTileHeights();

			int x = localX & (Perspective.LOCAL_TILE_SIZE - 1);
			int y = localY & (Perspective.LOCAL_TILE_SIZE - 1);
			int var8 = x * tileHeights[plane][sceneX + 1][sceneY] + (Perspective.LOCAL_TILE_SIZE - x) * tileHeights[plane][sceneX][sceneY] >> Perspective.LOCAL_COORD_BITS;
			int var9 = tileHeights[plane][sceneX][sceneY + 1] * (Perspective.LOCAL_TILE_SIZE - x) + x * tileHeights[plane][sceneX + 1][sceneY + 1] >> Perspective.LOCAL_COORD_BITS;
			return (Perspective.LOCAL_TILE_SIZE - y) * var8 + y * var9 >> Perspective.LOCAL_COORD_BITS;
		}

		return 0;
	}

	public static Polygon getCanvasTileAreaPoly(
			@Nonnull Client client,
			@Nonnull LocalPoint localLocation,
			int sizeX,
			int sizeY,
			double scale,
			int zOffset) {
		final int msx = localLocation.getSceneX() + ((Constants.EXTENDED_SCENE_SIZE - Constants.SCENE_SIZE) / 2);
		final int msy = localLocation.getSceneY() + ((Constants.EXTENDED_SCENE_SIZE - Constants.SCENE_SIZE) / 2);
		final var wv = client.getWorldView(localLocation.getWorldView());

		if(msx < 0 || msy < 0 || msx >= EXTENDED_SCENE_SIZE || msy >= EXTENDED_SCENE_SIZE || wv == null) {
			// out of scene
			return null;
		}

		int plane = wv.getPlane();

		var scene = wv.getScene();
		final byte[][][] tileSettings = scene.getExtendedTileSettings();

		int tilePlane = plane;
		if(plane < Constants.MAX_Z - 1 && (tileSettings[1][msx][msy] & TILE_FLAG_BRIDGE) == TILE_FLAG_BRIDGE) {
			tilePlane = plane + 1;
		}

		int sizeYO = (int)(scale * (sizeY * ((double)Perspective.LOCAL_TILE_SIZE / 2)));
		int sizeXO =  (int)(scale * (sizeX * ((double)Perspective.LOCAL_TILE_SIZE / 2)));

		final int swX = localLocation.getX() - sizeXO;
		final int swY = localLocation.getY() - sizeYO;

		final int neX = localLocation.getX() + sizeXO;
		final int neY = localLocation.getY() + sizeYO;

		final int seX = swX;
		final int seY = neY;

		final int nwX = neX;
		final int nwY = swY;

		final int swHeight = getHeight(scene, swX, swY, tilePlane) - zOffset;
		final int nwHeight = getHeight(scene, nwX, nwY, tilePlane) - zOffset;
		final int neHeight = getHeight(scene, neX, neY, tilePlane) - zOffset;
		final int seHeight = getHeight(scene, seX, seY, tilePlane) - zOffset;

		Point p1 = Perspective.localToCanvas(client, swX, swY, swHeight);
		Point p2 = Perspective.localToCanvas(client, nwX, nwY, nwHeight);
		Point p3 = Perspective.localToCanvas(client, neX, neY, neHeight);
		Point p4 = Perspective.localToCanvas(client, seX, seY, seHeight);

		if(p1 == null || p2 == null || p3 == null || p4 == null) {
			return null;
		}

		Polygon poly = new Polygon();
		poly.addPoint(p1.getX(), p1.getY());
		poly.addPoint(p2.getX(), p2.getY());
		poly.addPoint(p3.getX(), p3.getY());
		poly.addPoint(p4.getX(), p4.getY());

		return poly;
	}

	public static Rectangle renderPrayerOverlay(Graphics2D graphics, Client client, Prayer prayer, Color color) {
		Widget widget = client.getWidget(PrayerExtended.getPrayerWidgetId(prayer));

		if(widget == null || client.getVarbitValue(VarClientInt.INVENTORY_TAB) != InterfaceTab.PRAYER.getId()) {
			return null;
		}

		Rectangle bounds = widget.getBounds();
		renderPolygon(graphics, rectangleToPolygon(bounds), color);
		return bounds;
	}

	private static Polygon rectangleToPolygon(Rectangle rect) {
		int[] xpoints = {rect.x, rect.x + rect.width, rect.x + rect.width, rect.x};
		int[] ypoints = {rect.y, rect.y, rect.y + rect.height, rect.y + rect.height};

		return new Polygon(xpoints, ypoints, 4);
	}

	public static void renderTextLocation(Graphics2D graphics, String txtString, int fontSize, int fontStyle, Color fontColor, Point canvasPoint, boolean shadows, int yOffset) {
		graphics.setFont(new Font("Arial", fontStyle, fontSize));
		if(canvasPoint != null) {
			final net.runelite.api.Point canvasCenterPoint = new net.runelite.api.Point(
					(int) canvasPoint.getX(),
					(int) (canvasPoint.getY() + yOffset));
			final net.runelite.api.Point canvasCenterPoint_shadow = new net.runelite.api.Point(
					(int) (canvasPoint.getX() + 1),
					(int) (canvasPoint.getY() + 1 + yOffset));
			if(shadows) {
				renderTextLocation(graphics, canvasCenterPoint_shadow, txtString, Color.BLACK);
			}
			renderTextLocation(graphics, canvasCenterPoint, txtString, fontColor);
		}
	}

	public static void renderTextLocation(Graphics2D graphics, net.runelite.api.Point txtLoc, String text, Color color) {
		if(Strings.isNullOrEmpty(text)) {
			return;
		}

		int x = (int) txtLoc.getX();
		int y = (int) txtLoc.getY();

		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x + 1, y + 1);

		graphics.setColor(color);
		graphics.drawString(text, x, y);
	}

	public static void drawOutlineAndFill(final Graphics2D graphics2D, final Color outlineColor, final Color fillColor, final float strokeWidth, final Shape shape) {
		final Color originalColor = graphics2D.getColor();
		final Stroke originalStroke = graphics2D.getStroke();

		graphics2D.setStroke(new BasicStroke(strokeWidth));
		graphics2D.setColor(outlineColor);
		graphics2D.draw(shape);

		graphics2D.setColor(fillColor);
		graphics2D.fill(shape);

		graphics2D.setColor(originalColor);
		graphics2D.setStroke(originalStroke);
	}

	public static Polygon getProjectilePolygon(final Client client, final Projectile projectile) {
		if(projectile == null || projectile.getModel() == null) {
			return null;
		}

		final Model model = projectile.getModel();

		final LocalPoint localPoint = new LocalPoint((int) projectile.getX(), (int) projectile.getY(), client.getTopLevelWorldView());

		final int tileHeight = Perspective.getTileHeight(client, localPoint, client.getTopLevelWorldView().getPlane());

		double angle = Math.atan(projectile.getVelocityY() / projectile.getVelocityX());
		angle = Math.toDegrees(angle) + (projectile.getVelocityX() < 0 ? 180 : 0);
		angle = angle < 0 ? angle + 360 : angle;
		angle = 360 - angle - 90;

		double ori = angle * (512d / 90d);
		ori = ori < 0 ? ori + 2048 : ori;

		final int orientation = (int) Math.round(ori);

		final java.util.List<Vertex> vertices = getVertices(model);

		vertices.replaceAll(vertex -> vertex.rotate(orientation));

		final java.util.List<Point> list = new ArrayList<>();

		for(final Vertex vertex : vertices) {
			final Point point = Perspective.localToCanvas(client, localPoint.getX() - vertex.getX(),
					localPoint.getY() - vertex.getZ(), tileHeight + vertex.getY() + (int) projectile.getZ());

			if(point == null) {
				continue;
			}

			list.add(point);
		}

		final List<Point> convexHull = Jarvis.convexHull(list);

		if(convexHull == null) {
			return null;
		}

		final Polygon polygon = new Polygon();

		for(final Point point : convexHull) {
			polygon.addPoint(point.getX(), point.getY());
		}

		return polygon;
	}

//    public static Polygon getNpcPolygon(final Client client, final NPC npc)
//    {
//        if (npc == null || npc.getConvexHull() == null)
//        {
//            return null;
//        }
////        npc.getModel().getModel().cli
//        Perspective.getClickbox(client, npc.getModel(), npc.getOrientation(), npc.getLocalLocation().getX())
//
//        for (final Vertex vertex : vertices)
//        {
//            final Point point = Perspective.localToCanvas(client, localPoint.getX() - vertex.getX(),
//                    localPoint.getY() - vertex.getZ(), tileHeight + vertex.getY() + (int) projectile.getZ());
//
//            if (point == null)
//            {
//                continue;
//            }
//
//            list.add(point);
//        }
//
//        final List<Point> convexHull = Jarvis.convexHull(list);
//
//        if (convexHull == null)
//        {
//            return null;
//        }
//
//        final Polygon polygon = new Polygon();
//
//        for (final Point point : convexHull)
//        {
//            polygon.addPoint(point.getX(), point.getY());
//        }
//
//        return polygon;
//    }

	public static List<Vertex> getVertices(final Model model) {
		final float[] verticesX = model.getVerticesX();
		final float[] verticesY = model.getVerticesY();
		final float[] verticesZ = model.getVerticesZ();
		final int modelVerticesCount = model.getVerticesCount();
		final List<Vertex> vertexList = new ArrayList<>(modelVerticesCount);

		for(int i = 0; i < modelVerticesCount; i++) {
			final Vertex vertex = new Vertex((int) verticesX[i], (int) verticesY[i], (int) verticesZ[i]);
			vertexList.add(vertex);
		}
		return vertexList;
	}
}
