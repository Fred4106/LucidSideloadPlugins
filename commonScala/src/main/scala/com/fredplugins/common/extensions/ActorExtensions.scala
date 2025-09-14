package com.fredplugins.common.extensions

import com.fredplugins.common.utils.WorldPointUtils
import net.runelite.api.Actor
import net.runelite.api.Client
import net.runelite.api.NPC
import net.runelite.api.NPCComposition
import net.runelite.api.coords.WorldPoint
import net.runelite.client.RuneLite
import net.runelite.client.game.NPCManager

import scala.util.chaining.scalaUtilChainingOps

object ActorExtensions {
//	given Conversion[Actor, `
	private val npcManager = RuneLite.getInjector.getInstance(classOf[NPCManager])

	extension (e: Actor)(using client: Client) {
		def templateLocation: WorldPoint = {
			WorldPointUtils.toTemplate(e.getWorldLocation)
		}
	}

	extension (n: NPC)(using client: Client) {
		def health: Int = {
			val ratio           = n.getHealthRatio
			val scale           = n.getHealthScale
//			val targetHpPercent = ratio.toDouble / scale.toDouble
			(npcManager.getHealth(n.getId).toDouble * (ratio.toDouble / scale.toDouble)).toInt
		}
		def safeTransformedComposition: NPCComposition = {
			Option(n.getTransformedComposition).getOrElse(n.getComposition)
		}
	}
}
