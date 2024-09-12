package com.fredplugins.giantsfoundry

import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.events.GameTick
import net.runelite.client.eventbus.Subscribe
import org.slf4j.LoggerFactory

object FoundryAutomation {
	private val log = LoggerFactory.getLogger(classOf[FoundryAutomation])
}
class FoundryAutomation(val plugin: FredsGiantsFoundryPlugin, val state: FredsGiantsFoundryState, val ethanApiPlugin: EthanApiPlugin) {
	@Subscribe
	def onGameTick(tick: GameTick): Unit = {
//		FoundryAutomation.log.info("state: {}, heat: {}", state.getCurrentStage, state.heatingCoolingState)
	}
}
