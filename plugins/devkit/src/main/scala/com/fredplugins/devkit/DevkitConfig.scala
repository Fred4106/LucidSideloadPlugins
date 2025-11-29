package com.fredplugins.devkit

import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem
import net.runelite.client.config.ConfigSection

import scala.compiletime.constValue
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object Sections {
	inline val WorldRegion = constValue["World Region"]
//	inline val WorldRegion = constValue["World Region"]
}
@ConfigGroup(value = GROUP)
trait DevkitConfig extends Config {
	@ConfigSection(
		name = "World Region Settings",
		description = "Change world region setttings",
		position = 1
	)
	def WorldRegionSection: String = Sections.WorldRegion

	@ConfigItem(
		name = "Regions",
		description = "String containing the world regions to debug.",
		position = 0,
		keyName = "regions",
		section = Sections.WorldRegion
	)
	def regions: String = ""
	@ConfigItem(
		name = "Cache Id",
		description = "Id of the cache for openrs2.org/api.",
		position = 1,
		keyName = "cacheId",
		section = Sections.WorldRegion
	)
	def cacheId: Int = 2341

	@ConfigItem(
		name = "Test Region",
		description = "Region id to test on",
		position = 2,
		keyName = "testRegion",
		section = Sections.WorldRegion
	)
	def testRegion: Int = 16196

	@ConfigItem(
		name = "Result",
		description = "test region result",
		position = 3,
		keyName = "testRegionResult",
		section = Sections.WorldRegion
	)
	def testRegionResult: String = ""
}