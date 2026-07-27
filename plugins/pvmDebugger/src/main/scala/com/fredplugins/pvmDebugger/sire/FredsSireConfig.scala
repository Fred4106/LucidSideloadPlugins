package com.fredplugins.pvmDebugger.sire

import net.runelite.client.config.{Config, ConfigGroup, ConfigItem, ConfigSection}
import net.runelite.client.util.ColorUtil

import java.awt.Color
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.{Random, Try}

@ConfigGroup(value = ConfigDef.Group, secondaryConfig = true)
trait FredsSireConfig extends Config {
	@ConfigSection(name = "Color Settings", description = "Set various overlay colors", position = 20)
	def colors: String = ConfigDef.Colors

	@ConfigItem(name = "Enabled", description = "Is Abyssal Sire helper enabled?", position = 0, keyName = "enabled")
	def enabled(): Boolean = false

//	region colors
	@ConfigItem(
		name = "Fill Transparancy",
		description = "Fill color alpha for all highlights",
		keyName = "fillAlpha",
		section = ConfigDef.Colors,
		position = 0
	)
	def fillAlpha = 64

	@ConfigItem(
		name = "Sleeping Color",
		description = "Color to highlight abyssal sire when sleeping.",
		keyName = "sireSleepingColor",
		section = ConfigDef.Colors,
		position = 10
	)
	def sireSleepingColor: Color = new Color(83,83,83)

	@ConfigItem(
		name = "Awake Color",
		description = "Color to highlight abyssal sire when awake.",
		keyName = "sireAwakeColor",
		section = ConfigDef.Colors,
		position = 11
	)
	def sireAwakeColor: Color = new Color(255,135,0)

	@ConfigItem(
		name = "Stunned Color",
		description = "Color to highlight abyssal sire when stunned.",
		keyName = "sireStunnedColor",
		section = ConfigDef.Colors,
		position = 12
	)
	def sireStunnedColor: Color = new Color(0,255,255)

	@ConfigItem(
		name = "Puppet Color",
		description = "Color to highlight abyssal sire when puppeted.",
		keyName = "sirePuppetColor",
		section = ConfigDef.Colors,
		position = 13
	)
	def sirePuppetColor: Color = new Color(255,0,250)

	@ConfigItem(
		name = "Wandering Color",
		description = "Color to highlight abyssal sire when wandering.",
		keyName = "sireWanderingColor",
		section = ConfigDef.Colors,
		position = 14
	)
	def sireWanderingColor: Color = new Color(0,38,255)

	@ConfigItem(
		name = "Panicking Color",
		description = "Color to highlight abyssal sire when panicking.",
		keyName = "sirePanickingColor",
		section = ConfigDef.Colors,
		position = 15
	)
	def sirePanickingColor: Color = new Color(0,255,0)

	@ConfigItem(
		name = "Apocalypse Color",
		description = "Color to highlight abyssal sire when in apocalypse mode.",
		keyName = "sireApocalypseColor",
		section = ConfigDef.Colors,
		position = 16
	)
	def sireApocalypseColor: Color = new Color(255,0,0)
//	endregion
}
