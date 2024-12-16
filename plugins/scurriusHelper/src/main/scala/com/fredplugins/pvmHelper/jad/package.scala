package com.fredplugins.pvmHelper

import com.fredplugins.common.Locatable
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.{NPC, Prayer}
import net.runelite.client.plugins.attackstyles.AttackStylesPlugin

import scala.compiletime.uninitialized
import scala.util.{Random, Try}
import scala.util.chaining.*

package object jad {
//	enum AttackStyle {
//		case MELEE
//		case RANGE
//		case MAGIC
//	}

//	trait Locatable {
//		def worldPoint: WorldPoint
//		def localPoint: LocalPoint
//		def distanceTo(other: Locatable): Int = {
//			worldPoint.distanceTo(other.worldPoint)
//		}
//	}

	enum TzMobType(ids: Int *)  {
		case TzKih extends TzMobType(2189, 2190, 3116, 3117)
		case TzKek extends TzMobType(2191, 2192, 3118, 3119, 3120)
		case TokXil extends TzMobType(2193, 3121, 2194, 3122)
		case YtMejKot extends TzMobType(3123, 3124)
		case KetZek extends TzMobType(3125, 3126)
		case TzTokJad extends TzMobType(3127)
		case YtHurKot extends TzMobType(3128, 7701, 7705)

		def npcIsType(npc: NPC): Boolean = {
			npc != null && ids.contains(npc.getId)
		}
	}
	abstract class TzMob(val tpe: TzMobType) {
		def wrapped: NPC
		def getId: Int = wrapped.getId
		def getName: String = wrapped.getName
	}

	object TzMob {
		def apply(npc: NPC): Option[TzMob] = {
			TzMobType.values.find(_.npcIsType(npc)).map(tpe => {
				new TzMob(tpe) {
					override val wrapped: NPC = npc
				}
			})
		}
	}

	given Conversion[TzMob, Locatable] = (a: TzMob) => a.wrapped

}
