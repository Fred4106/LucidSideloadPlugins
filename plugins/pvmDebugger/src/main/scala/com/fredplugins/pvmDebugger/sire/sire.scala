package com.fredplugins.pvmDebugger

import com.fredplugins.common.ProjectileID
import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.api.WorldRegion.*
import com.fredplugins.common.utils.{ShimUtils, TWorldPoint}
import net.runelite.api.NPC
import net.runelite.api.gameval.*
import net.runelite.api.coords.{WorldArea, WorldPoint}

import scala.compiletime.{constValue, uninitialized}
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import NpcID.{ABYSSALSIRE_SIRE_APOCALYPSE, ABYSSALSIRE_SIRE_PANICKING, ABYSSALSIRE_SIRE_PUPPET, ABYSSALSIRE_SIRE_STASIS_AWAKE, ABYSSALSIRE_SIRE_STASIS_SLEEPING, ABYSSALSIRE_SIRE_STASIS_STUNNED, ABYSSALSIRE_SIRE_WANDERING}
import NpcID.{ABYSSALSIRE_SCION, ABYSSALSIRE_SCION_DYING, ABYSSALSIRE_SPAWN, ABYSSALSIRE_SPAWN_DYING}

package object sire {
	object ConfigDef {
		inline val Group: "FredsSireHelper" = constValue["FredsSireHelper"]
		inline val Colors: "Colors" = constValue["Colors"]
	}
	val area1 = new WorldArea(3093, 4745, 36, 54, 0)
	val area2 = new WorldArea(3088, 4809, 36, 54, 0)
	val area3 = new WorldArea(2953, 4745, 36, 54, 0)
	val area4 = new WorldArea(2962, 4809, 36, 54, 0)
	inline def sireRegions: Set[Int] = Set(
		12363,12362, 11851, 11850
	)

	sealed trait SireMode(val npcId: Int) extends enumeratum.EnumEntry {}
	object SireMode extends enumeratum.Enum[SireMode] {
		case object Sleeping extends SireMode(ABYSSALSIRE_SIRE_STASIS_SLEEPING)
		case object Awake extends SireMode(ABYSSALSIRE_SIRE_STASIS_AWAKE)
		case object Stunned extends SireMode(ABYSSALSIRE_SIRE_STASIS_STUNNED)
		case object Puppet extends SireMode(ABYSSALSIRE_SIRE_PUPPET)
		case object Wandering extends SireMode(ABYSSALSIRE_SIRE_WANDERING)
		case object Panicking extends SireMode(ABYSSALSIRE_SIRE_PANICKING)
		case object Apocalypse extends SireMode(ABYSSALSIRE_SIRE_APOCALYPSE)
		def unapply(v: NPC): Option[(SireMode, NPC)] = {
			Option(v).map(_.getId).flatMap(id => values.find(_.npcId == id))
				.map(tp => (tp, v))
		}

		override def values: IndexedSeq[SireMode] = findValues
	}

	sealed trait NpcType(val ids: Int *) extends enumeratum.EnumEntry {
		def unapply(npc: NPC): Boolean = {
			ids.contains(
				Option(npc).fold(-1)(_.getId)
			)
		}
	}

	object NpcType extends enumeratum.Enum[NpcType] {
		case object Sire extends NpcType(ABYSSALSIRE_SIRE_STASIS_SLEEPING, ABYSSALSIRE_SIRE_STASIS_AWAKE, ABYSSALSIRE_SIRE_STASIS_STUNNED, ABYSSALSIRE_SIRE_PUPPET, ABYSSALSIRE_SIRE_WANDERING, ABYSSALSIRE_SIRE_PANICKING, ABYSSALSIRE_SIRE_APOCALYPSE)
		case object Spawn extends NpcType(ABYSSALSIRE_SPAWN, ABYSSALSIRE_SPAWN_DYING)
		case object Scion extends NpcType(ABYSSALSIRE_SCION, ABYSSALSIRE_SCION_DYING)

		def unapply(v: NPC): Option[(NpcType, NPC)] = {
			Option(v).zip(values.find(_.unapply(v))).map(_.swap)
		}

		override def values: IndexedSeq[NpcType] = findValues
	}

}
