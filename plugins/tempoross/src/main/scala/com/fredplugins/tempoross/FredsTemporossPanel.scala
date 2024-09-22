package com.fredplugins.tempoross

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.tempoross.Constants.{VARB_REWARD_POOL_NUMBER, WAVE_IMPACT_TICKS}
import com.fredplugins.tempoross.FredsTemporossLogic.{findAmmoCrate, findTetherSpot, inMinigame, inRewardArea, ticksTillWave, waveIncomingStartTick}
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Client, GameObject, TileObject}
import net.runelite.api.coords.WorldPoint
import net.runelite.client.ui.overlay.components.{LineComponent, TitleComponent}
import net.runelite.client.ui.overlay.{OverlayLayer, OverlayPanel, OverlayPosition}
import org.slf4j.Logger

import java.awt.{Color, Dimension, Graphics2D}
import java.time.Instant
import scala.util.Try
@Singleton
class FredsTemporossPanel @Inject()(val client: Client, plugin: FredsTemporossPlugin) extends OverlayPanel(plugin) {
	setLayer(OverlayLayer.ABOVE_SCENE)
	setPosition(OverlayPosition.BOTTOM_LEFT)
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	override def render(graphics: Graphics2D): Dimension = {
		import FredsTemporossLogic.{phase, cookedFish, crystalFish, uncookedFish, waveIncomingStartTime}
		if(!inRewardArea && !inMinigame) return null
		panelComponent.getChildren.add(TitleComponent.builder.text("Tempoross").color(Color.CYAN).build)
		if (inMinigame) {
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

			panelComponent.getChildren.add(
				LineComponent.builder
					.left("ammo crate")
					.right(FredsTemporossLogic.expandNpcToStr(findAmmoCrate))//s"${findAmmoCrate.map(to => client.getObjectDefinition(to.getId)).map(c => Option.when(c.getImpostorIds != null)(c.getImpostor).getOrElse(c)).map(comp => (comp.getName, comp.getId, comp.getActions.mkString))}")
					.build
			)

			panelComponent.getChildren.add(
				LineComponent.builder
					.left("tether")
					.right(FredsTemporossLogic.expandTileObjToStr(findTetherSpot))//s"${findTetherSpot.map(to => client.getObjectDefinition(to.getId)).map(c => Option.when(c.getImpostorIds != null)(c.getImpostor).getOrElse(c)).map(comp => (comp.getName, comp.getId, comp.getActions.mkString))}")
					.build
			)

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
		}
		if(inRewardArea) {
			panelComponent.getChildren.add(
				LineComponent.builder
					.left("Permits")
					.right(s"${client.getVarbitValue(VARB_REWARD_POOL_NUMBER)}")
					.build
			)
		}
		super.render(graphics)
	}
}
