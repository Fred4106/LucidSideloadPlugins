package com.fredplugins.devkit

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Client, GameObject, TileObject}
import net.runelite.api.coords.WorldPoint
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.{LineComponent, TitleComponent}
import net.runelite.client.ui.overlay.{OverlayLayer, OverlayPanel, OverlayPosition}
import org.slf4j.Logger

import java.awt.{Color, Dimension, Graphics2D}
import java.time.Instant
import scala.util.Try
@Singleton
class RegionsOverlayPanel @Inject()(plugin: DevKitPlugin) extends OverlayPanel(plugin) with ShimUtils.Logging("DEBUG") {
	setLayer(OverlayLayer.ABOVE_SCENE)
	setPosition(OverlayPosition.BOTTOM_LEFT)

	override def render(graphics: Graphics2D): Dimension = {
//		plugin.client.getTopLevelWorldView.getScene.getTiles
		val title = TitleComponent.builder.text("Regions").build()
		val loc = LineComponent.builder.left("Location").right(plugin.client.getLocalPlayer.getWorldLocation.packed + "").build()

		val regions: Seq[LayoutableRenderableEntity] = plugin.regions.toList.filter {
			case (str, region) => region.worldPoints.exists(_.isInScene(plugin.client))
		}.flatMap[LayoutableRenderableEntity] {
			case (str, region) => {
				val title = TitleComponent.builder.text(str).build()
				val xx =region.worldPoints.groupBy(_.getY).map((y, tiles) => y -> tiles.map(_.getX).sorted).toList.sortBy(_._1)
				val coords = xx.map {
					case (y, xs) => LineComponent.builder.left(s"${y}").right(xs.mkString(", ")).build()
				}
				coords.prepended(title)
			}
		}

		Seq(title, loc).appended(regions).flatMap {
			case x: LayoutableRenderableEntity => Seq(x)
			case x: Seq[?] => {
				x.flatMap{
					case y: LayoutableRenderableEntity => Some(y)
					case _ => None
				}
			}
		}.foreach(e => panelComponent.getChildren.add(e))
//				x.flatMap {
//					case b1: LayoutableRenderableEntity => Seq(b1)
//					case b2 => Seq.empty
//				}
//		}

		super.render(graphics)
	}
}
