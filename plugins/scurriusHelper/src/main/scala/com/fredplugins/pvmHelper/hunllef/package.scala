package com.fredplugins.pvmHelper

import com.fredplugins.common.Locatable.{LocatableType, distanceTo, findSceneCord,findWorldCord}
import net.runelite.api.coords.{LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.{Client, NPC, NpcID, NullNpcID, Projectile, Skill}

import scala.compiletime.uninitialized

import scala.util.chaining.*
import java.awt.image.BufferedImage
import scala.util.Try

package object hunllef {
	sealed trait HunllefCycle {}

	case class Range(couldBeInverted: Boolean = false) extends HunllefCycle {}
	case object Mage extends HunllefCycle {}

	val HunllefIds: List[Int] = List(NpcID.CRYSTALLINE_HUNLLEF, NpcID.CRYSTALLINE_HUNLLEF_9022,
				NpcID.CRYSTALLINE_HUNLLEF_9023, NpcID.CRYSTALLINE_HUNLLEF_9024,
				NpcID.CORRUPTED_HUNLLEF, NpcID.CORRUPTED_HUNLLEF_9036,
				NpcID.CORRUPTED_HUNLLEF_9037, NpcID.CORRUPTED_HUNLLEF_9038)
	def isHunllef(n: NPC): Boolean = {
		HunllefIds.contains(n.getId)
	}

	abstract class Tornado(val wrapped: NPC)(using client: Client) {
		val spawnTick: Int = client.getTickCount
		val spawnLoc: (Int, Int) = getSceneCord
		def age: Int = client.getTickCount - spawnTick
		def getSceneCord: (Int, Int) = wrapped.findSceneCord.get
	}

	object Tornado {
		class ChaseTornado(w: NPC)(using client: Client) extends Tornado(w) {
			val diesOnTick: Int = spawnTick + 21
			def timeToLive: Int = Math.max(diesOnTick - client.getTickCount, 0)
		}

		class RoamingTornado(w: NPC)(using client: Client) extends Tornado(w) {}

		def isTornado(wrapped: NPC): Boolean = {
			Set(NullNpcID.NULL_9025, NullNpcID.NULL_9039, NullNpcID.NULL_14142).contains(wrapped.getId)
		}

		def apply(npc: NPC)(using client: Client): Option[Tornado] = {
			Option(npc.getId).collect {
				case NullNpcID.NULL_14142 => ChaseTornado(npc)
				case j if Seq(NullNpcID.NULL_9025, NullNpcID.NULL_9039).contains(j) => RoamingTornado(npc)
			}
		}
	}
}
