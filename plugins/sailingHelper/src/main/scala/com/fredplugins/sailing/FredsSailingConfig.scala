package com.fredplugins.sailing


import com.fredplugins.sailing.FredsSailingConfig.Barracuda
import com.fredplugins.sailing.FredsSailingConfig.Overlay
import net.runelite.client.config.Alpha
import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem
import net.runelite.client.config.ConfigSection
import net.runelite.client.config.Range

import java.awt.Color
import scala.compiletime.constValue
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object FredsSailingConfig {
	inline val GROUP            : "FredsSailing" = constValue["FredsSailing"]
	inline val Barracuda = constValue["Barracuda"]
	inline val Overlay = constValue["Overlay"]
}

@ConfigGroup(value = FredsSailingConfig.GROUP)
trait FredsSailingConfig extends Config {
	//region Barracuda Trials
	@ConfigSection(
		name = "Barracuda Trials",
		description = "Settings for Barracuda Trials",
		position = 60,
		closedByDefault = false
	)
	def barracuda_section: String = Barracuda

	@ConfigItem(
		keyName = "barracudaHighlightLostCrates",
		name = "Highlight Crates",
		description = "Highlight lost crates that need to be collected during Barracuda Trials.",
		section = Barracuda,
		position = 1
	)
	def barracudaHighlightLostCrates(): Boolean = true

	@ConfigItem(
		keyName = "barracudaHighlightLostCratesColour",
		name = "Crate Colour",
		description = "The colour to highlight lost crates.",
		section = Barracuda,
		position = 2
	)
	@Alpha
	def barracudaHighlightLostCratesColour(): Color = Color.ORANGE
	//endregion

	//region Overlay
	@ConfigSection(
		name = "Overlay",
		description = "Overlay settings",
		position = 100
	)
	def overlay_section: String = Overlay

	@Range(min = 6, max = 32)
	@ConfigItem(
		keyName = "fontSize",
		name = "Font Size",
		description = "sets font size for overlay",
		position = 0,
		section = Overlay
	)
	def getFontSize(): Int = 14

	@ConfigItem(
		keyName = "fontBold",
		name = "Bold Font",
		description = "sets bold font for overlay",
		position = 1,
		section = Overlay
	)
	def getFontBold(): Boolean = true
	//endregion
}