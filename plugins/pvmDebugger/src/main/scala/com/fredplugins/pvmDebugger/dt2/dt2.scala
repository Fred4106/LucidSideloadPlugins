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

package object dt2 {
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

	val vardorvisArea:WorldRegion = new WorldArea(1124, 3413, 11, 11, 0)
	def inBossArea(wp: WorldPoint): Boolean = {
		Option(wp).map(TWorldPoint.get(_)).map(vardorvisArea.contains(_)).getOrElse(false)
	}

	def inRegion(rid: Int): Boolean = {
		rid == 4405
	}

	sealed trait VardorvisProjectile(val pid: Int) extends enumeratum.EnumEntry {}
	object VardorvisProjectiles extends enumeratum.Enum[VardorvisProjectile] with ShimUtils.Logging() {
		case object Range extends VardorvisProjectile(ProjectileID.VARDORVIS_RANGED_PROJ_ID)
		case object Mage extends VardorvisProjectile(ProjectileID.VARDORVIS_MAGIC_PROJ_ID)

		def unapply(v: Projectile): Option[(VardorvisProjectile, Projectile)] = {
			Option(v).map(_.getId).flatMap(pid => values.find(_.pid == pid))
				.map(tp => (tp, v))
		}

		override def values: IndexedSeq[VardorvisProjectile] = findValues
	}
}
