package com.fredplugins.pvmDebugger

import com.fredplugins.common.ProjectileID
import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.api.WorldRegion.*
import com.fredplugins.common.utils.{ShimUtils, TWorldPoint}
import net.runelite.api.*
import net.runelite.api.coords.{LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.events.*
import net.runelite.api.gameval.{AnimationID, InterfaceID, ItemID, NpcID}

import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

package object cox {
//	val safeTiles = List(
//		new WorldPoint(2359, 9448, 0),
//		new WorldPoint(2367, 9453, 0),
//		new WorldPoint(2365, 9454, 0),
//		new WorldPoint(2362, 9454, 0),
//		new WorldPoint(2360, 9453, 0),
//		new WorldPoint(2359, 9451, 0),
//		new WorldPoint(2360, 9446, 0),
//		new WorldPoint(2362, 9445, 0),
//		new WorldPoint(2365, 9445, 0),
//		new WorldPoint(2367, 9446, 0),
//		new WorldPoint(2368, 9451, 0),
//		new WorldPoint(2368, 9448, 0)
//	)
//
//	val dangerousTiles = {
//		val minTile = new WorldPoint(safeTiles.map(_.getX).min, safeTiles.map(_.getY).min, 0)
//		val maxTile = new WorldPoint(safeTiles.map(_.getX).max, safeTiles.map(_.getY).max, 0)
//		val size = (maxTile.getX - minTile.getX, maxTile.getY - minTile.getY)
//		val padding = 2
//
//		new WorldArea(minTile.dx(-padding).dy(-padding), size._1 + (2*padding), size._2+(2*padding)).toWorldPointList.asScala.toList
//			.filterNot(safeTiles.contains(_))
//	}

	val CoxArea:WorldRegion = new WorldArea(1124, 3413, 11, 11, 0)
	def inBossArea(wp: WorldPoint): Boolean = {
		Option(wp).map(TWorldPoint.get(_)).map(CoxArea.contains(_)).getOrElse(false)
	}

	def inRegion(rid: Int): Boolean = {
		rid == 4405
	}

	sealed trait OlmProjectile(val pid: Int) extends enumeratum.EnumEntry {}
	object OlmProjectiles extends enumeratum.Enum[OlmProjectile] with ShimUtils.Logging() {
		case object Range extends OlmProjectile(ProjectileID.OLM_RANGE_ATTACK)
		case object Mage extends OlmProjectile(ProjectileID.OLM_MAGE_ATTACK)

		case object FallingCrystal extends OlmProjectile(ProjectileID.OLM_FALLING_CRYSTAL)
		case object FallingCrystalTrail extends OlmProjectile(ProjectileID.OLM_FALLING_CRYSTAL_TRAIL)
		case object Burning extends OlmProjectile(ProjectileID.OLM_BURNING)
		case object AcidTrail extends OlmProjectile(ProjectileID.OLM_ACID_TRAIL)
		case object FireLine extends OlmProjectile(ProjectileID.OLM_FIRE_LINE)
		case object MageAttack extends OlmProjectile(ProjectileID.OLM_MAGE_ATTACK)
		case object RangeAttack extends OlmProjectile(ProjectileID.OLM_RANGE_ATTACK)

		def unapply(v: Projectile): Option[(OlmProjectile, Projectile)] = {
			Option(v).map(_.getId).flatMap(pid => values.find(_.pid == pid))
				.map(tp => (tp, v))
		}

		override def values: IndexedSeq[OlmProjectile] = findValues
	}
}
