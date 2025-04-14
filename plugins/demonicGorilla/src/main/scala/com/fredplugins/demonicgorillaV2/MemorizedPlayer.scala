package com.fredplugins.demonicgorillaV2

import net.runelite.api.Hitsplat
import net.runelite.api.Player
import net.runelite.api.coords.WorldArea

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class MemorizedPlayer(val player:Player) {
	private var lastWorldArea: Option[WorldArea] = Option.empty
	private var recentHitsplats = List.empty[Hitsplat]
	def hit(splat: Hitsplat): Unit = {
		recentHitsplats = recentHitsplats :+ splat
	}

	def update(): Unit = {
		lastWorldArea = Option(player).map(_.getWorldArea)
		recentHitsplats = List.empty[Hitsplat]
	}

	def getRecentHitsplats: List[Hitsplat] = recentHitsplats
	def getLastWorldArea: WorldArea | Null = lastWorldArea.orNull
}
