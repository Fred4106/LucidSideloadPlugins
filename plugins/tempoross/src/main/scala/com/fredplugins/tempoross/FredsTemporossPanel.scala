package com.fredplugins.tempoross

import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Singleton}
import net.runelite.api.Client
import net.runelite.api.coords.WorldPoint
import net.runelite.client.ui.overlay.components.{LineComponent, TitleComponent}
import net.runelite.client.ui.overlay.{OverlayLayer, OverlayPanel, OverlayPosition}
import org.slf4j.Logger

import java.awt.{Color, Dimension, Graphics2D}
import java.time.Instant
@Singleton
class FredsTemporossPanel @Inject()(val client: Client, plugin: FredsTemporossPlugin) extends OverlayPanel(plugin) {
	setLayer(OverlayLayer.ABOVE_SCENE)
	setPosition(OverlayPosition.BOTTOM_LEFT)
	setResizable(false)
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	override def render(graphics: Graphics2D): Dimension = {
		val region = WorldPoint.fromLocalInstance(plugin.client, plugin.client.getLocalPlayer.getLocalLocation).getRegionID
		if (region == Constants.TEMPOROSS_REGION) {
			import FredsTemporossLogic.{phase, cookedFish, crystalFish, uncookedFish,waveIncomingStartTime}
			panelComponent.getChildren.add(TitleComponent.builder.text("Tempoross").color(Color.CYAN).build)

			panelComponent.getChildren.add(
				LineComponent.builder
					.left("Phase")
					.right(s"${phase}")
					.build
			)

			panelComponent.getChildren.add(
				LineComponent.builder
					.left("Raw")
					.right(s"${uncookedFish}")
					.build
			)

			panelComponent.getChildren.add(
				LineComponent.builder
					.left("Cooked")
					.right(s"${cookedFish}")
					.build
			)

			panelComponent.getChildren.add(
				LineComponent.builder
					.left("Total")
					.right(s"${uncookedFish + cookedFish + crystalFish}")
					.build
			)

			val ticksTillWave = Option(waveIncomingStartTime).map(wt => ((wt.toEpochMilli + 7800 - Instant.now.toEpochMilli) / 600).toInt).getOrElse(-1)
			panelComponent.getChildren.add(
				LineComponent.builder
					.left("Wave")
					.right(if (ticksTillWave != -1) s"${ticksTillWave}t" else "")
					.rightColor(
						ticksTillWave match {
							case -1 => Color.WHITE
							case t if (0 until 4 contains t) => Color.RED
							case t if (4 until 8 contains t) => Color.ORANGE
							case t => Color.YELLOW
						}
					)
					.build
			)

			log.debug("Rendering panel!")
			return super.render(graphics)
		} else {
			return null
		}
	}
}
