package com.fredplugins.scurriushelper

import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Client, GameObject, TileObject}
import net.runelite.api.coords.WorldPoint
import net.runelite.client.ui.overlay.components.{LineComponent, TitleComponent}
import net.runelite.client.ui.overlay.{OverlayLayer, OverlayPanel, OverlayPosition}
import org.slf4j.Logger

import java.awt.{Color, Dimension, Graphics2D}
import java.time.Instant
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps
@Singleton
class FredsScurriusPanel @Inject()(val client: Client, plugin: FredsScurriusHelper) extends OverlayPanel(plugin) {
	setLayer(OverlayLayer.ABOVE_SCENE)
	setPosition(OverlayPosition.BOTTOM_LEFT)
		val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	override def render(graphics: Graphics2D): Dimension = {
		import plugin.{inBossRoom, getBoss, getJustDodged, getLastDodgeTick, getLastRatTick, getLastActivateTick}
//		if(!inBossRoom) return null
		panelComponent.getChildren.add(TitleComponent.builder.text("Scurrius").color(Color.CYAN).build)
//		if(inBossRoom) {
		panelComponent.getChildren.add(
			LineComponent.builder
				.left("Npc")
				.right(s"${getBoss}")
				.build
		)
		panelComponent.getChildren.add(
			LineComponent.builder
				.left("justDodged")
				.right(s"${getJustDodged}")
				.build
		)
		panelComponent.getChildren.add(
			LineComponent.builder
				.left("lastDodgeTick")
				.right(s"${getLastDodgeTick}")
				.build
		)
		panelComponent.getChildren.add(
			LineComponent.builder
				.left("lastRatTick")
				.right(s"${getLastRatTick}")
				.build
		)
		panelComponent.getChildren.add(
			LineComponent.builder
				.left("lastActivateTick")
				.right(s"${getLastActivateTick}")
				.build
		)
		panelComponent.getChildren.add(TitleComponent.builder.text("attacks").build)

		plugin.getAttacks.map(p => {
			LineComponent.builder.left(s"${p.getId} -> ${p.getTarget}").right(s"(${p.getEndCycle} - ${p.getStartCycle}) = ${p.getRemainingCycles}")
		}).foreach(lc => panelComponent.getChildren.add(lc.build()))

		panelComponent.getChildren.add(TitleComponent.builder.text("ceiling").build)
		plugin.getFallingCeilingToTicks.toList.map(p => {
//			val wp = WorldPoint.fromLocalInstance(client, p._1.getLocation)
			LineComponent.builder.left(s"${p._1.getLocation.toString} -> ${p._1.getId}").right(s"(${p._2})")
		}).foreach(lc => panelComponent.getChildren.add(lc.build()))

		//		}
		if(plugin.inBossRoom) {
			panelComponent.setBackgroundColor(new Color(70-20, 61+40, 50-20, 156))
		} else {
			panelComponent.setBackgroundColor(new Color(70+40, 61-20, 50-20, 156))
		}
		super.render(graphics)
	}
}
