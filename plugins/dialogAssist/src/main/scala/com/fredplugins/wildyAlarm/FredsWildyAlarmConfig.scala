package com.fredplugins.wildyAlarm

import com.fredplugins.common.utils.ShimUtils
import com.google.inject.Singleton
import ethanApiPlugin.EthanApiPlugin
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.config.{Alpha, Config, ConfigGroup, ConfigItem, ConfigSection, Range, Units}

import java.awt.Color
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.{constValue, uninitialized}


@ConfigGroup(value = "FredsWildyAlarm")
trait FredsWildyAlarmConfig extends Config {

	@Range(max = 30, min = 0)
	@ConfigItem(
		keyName = "alarmRadius",
		name = "Alarm radius",
		description = "Distance for another player to trigger the alarm. WARNING: Players within range that are not rendered will not trigger the alarm.",
		position = 1
	)
	def alarmRadius: Int = 15

	@ConfigItem(
		keyName = "flashColor", 
		name = "Flash color", 
		description = "Sets the color of the alarm flashes", 
		position = 8
	)
	def flashColor: Color = new Color(255, 255, 0, 70)

	@ConfigItem(
		keyName = "flashControl",
		name = "Flash speed", 
		description = "Control the cadence at which the screen will flash with the chosen color",
		position = 9
	)
	def flashControl: FlashSpeed = FlashSpeed.NORMAL

	@ConfigItem(
		keyName = "flashLayer",
		name = "Flash layer", 
		description = "Advanced: control the layer that the flash renders on",
		position = 10
	)
	def flashLayer: FlashLayer = FlashLayer.ABOVE_SCENE

	@Units(Units.SECONDS)
	@ConfigItem(
		keyName = "timeoutToIgnore",
		name = "Timeout",
		description = "Ignores players after they've been present for the specified time.",
		position = 2
	)
	def timeoutToIgnore: Int = 10

	@Alpha
	@ConfigItem(
		name = "Player Highlight Color",
		description = "Color to highlight potential attackers",
		keyName = "playerColor",
		position = 10
	)
	def playerColor: Color = new Color(255, 0, 0, 180)
}
