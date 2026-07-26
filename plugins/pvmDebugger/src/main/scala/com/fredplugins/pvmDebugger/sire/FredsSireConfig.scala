package com.fredplugins.pvmDebugger.sire

import net.runelite.client.config.{Config, ConfigGroup, ConfigItem, ConfigSection}

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
		section = ConfigDef.Colors
	)
	def fillAlpha = 64

	@ConfigItem(
		name = "Sleeping Color",
		description = "Color to highlight abyssal sire when sleeping.",
		keyName = "sireSleepingColor",
		section = ConfigDef.Colors
	)
	def sireSleepingColor: Color = Color.CYAN

	@ConfigItem(
		name = "Awake Color",
		description = "Color to highlight abyssal sire when awake.",
		keyName = "sireAwakeColor",
		section = ConfigDef.Colors
	)
	def sireAwakeColor: Color = Color.CYAN

	@ConfigItem(
		name = "Stunned Color",
		description = "Color to highlight abyssal sire when stunned.",
		keyName = "sireStunnedColor",
		section = ConfigDef.Colors
	)
	def sireStunnedColor: Color = Color.CYAN

	@ConfigItem(
		name = "Puppet Color",
		description = "Color to highlight abyssal sire when puppeted.",
		keyName = "sirePuppetColor",
		section = ConfigDef.Colors
	)
	def sirePuppetColor: Color = Color.CYAN

	@ConfigItem(
		name = "Wandering Color",
		description = "Color to highlight abyssal sire when wandering.",
		keyName = "sireWanderingColor",
		section = ConfigDef.Colors
	)
	def sireWanderingColor: Color = Color.CYAN

	@ConfigItem(
		name = "Panicking Color",
		description = "Color to highlight abyssal sire when panicking.",
		keyName = "sirePanickingColor",
		section = ConfigDef.Colors
	)
	def sirePanickingColor: Color = Color.CYAN

	@ConfigItem(
		name = "Apocalypse Color",
		description = "Color to highlight abyssal sire when in apocalypse mode.",
		keyName = "sireApocalypseColor",
		section = ConfigDef.Colors
	)
	def sireApocalypseColor: Color = Color.CYAN
//	endregion
}
