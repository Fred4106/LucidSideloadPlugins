package com.fredplugins.devkit

import com.fredplugins.common.OldOverlayUtil
import com.fredplugins.common.overlays
import com.fredplugins.common.utils.ShimUtils
import com.google.inject.Inject
import com.google.inject.Singleton
import net.runelite.api.coords.WorldPoint
import net.runelite.api.Client
import net.runelite.api.GameObject
import net.runelite.api.Perspective
import net.runelite.api.TileObject
import net.runelite.api.coords.LocalPoint
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import net.runelite.client.ui.overlay.OverlayLayer
import net.runelite.client.ui.overlay.OverlayPanel
import net.runelite.client.ui.overlay.OverlayPosition
import net.runelite.client.ui.overlay.OverlayUtil
import org.locationtech.jts.awt.ShapeWriter
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.CoordinateArrays
import org.locationtech.jts.geom.Coordinates
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory
import org.slf4j.Logger
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import net.runelite.api.Point

import java.awt
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.geom.PathIterator
import java.time.Instant
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.Try
import scala.util.chaining.*
import scala.util.Random
import scala.util.Try

@Singleton
class RegionsOverlay @Inject()(plugin: DevKitPlugin) extends Overlay(plugin) with ShimUtils.Logging("DEBUG") {
	setPosition(OverlayPosition.DYNAMIC)
	setLayer(OverlayLayer.ABOVE_SCENE)

	override def render(graphics: Graphics2D): Dimension = {
		plugin.regions
			.map{
				case (str, region) => str -> region.worldPoints.filter(_.isInScene(plugin.client))
			}
			.filter {
				case (str, tiles) => tiles.nonEmpty
			}
			.foreach{
				(str, tiles) => {
					val tilePolys: Seq[awt.Polygon] = tiles.map(t => Perspective.getCanvasTilePoly(plugin.client, LocalPoint.fromWorld(plugin.client, t)))
					val geomPolys: Seq[Geometry]     = tilePolys.map(p => {
						overlays.toJtsGeometry(p)
					})
					val geometry = geomPolys.reduce((a, b) => a.union(b))
					val center: Point = overlays.toPoint(geometry.getCentroid)

					val textLoc = {
						val bounds = graphics.getFontMetrics.getStringBounds(str, graphics)
						new Point((center.getX - (bounds.getWidth / 2)).toInt, (center.getY + (bounds.getHeight / 2)).toInt)
					}

					OverlayUtil.renderPolygon(graphics, overlays.toPolygon(geometry), Color.CYAN, Color.CYAN.withAlpha(48))
					OverlayUtil.renderPolygon(graphics, overlays.toPolygon(overlays.toJtsPoint(center)), Color.YELLOW.withAlpha(200))
					OverlayUtil.renderPolygon(graphics, overlays.toPolygon(overlays.toJtsPoint(textLoc)), Color.BLUE.withAlpha(200))
//					OverlayUtil.renderPolygon(graphics, overlays.toPolygon(), Color.CYAN, Color.CYAN.withAlpha(48))
					OverlayUtil.renderTextLocation(graphics, textLoc, str, Color.WHITE)
				}
			}

		null.asInstanceOf[Dimension]
	}
}
