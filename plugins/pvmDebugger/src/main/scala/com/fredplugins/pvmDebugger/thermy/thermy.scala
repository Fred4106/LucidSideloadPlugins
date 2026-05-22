package com.fredplugins.pvmDebugger

import com.fredplugins.common.ProjectileID
import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.api.WorldRegion.*
import com.fredplugins.common.utils.{ShimUtils, TWorldPoint}
import net.runelite.api.coords.{LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.events.*
import net.runelite.api.gameval.{AnimationID, InterfaceID, ItemID, NpcID}
import net.runelite.api.*

import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

package object thermy {
	val safeTiles = List(
		new WorldPoint(2359, 9448, 0),
		new WorldPoint(2367, 9453, 0),
		new WorldPoint(2365, 9454, 0),
		new WorldPoint(2362, 9454, 0),
		new WorldPoint(2360, 9453, 0),
		new WorldPoint(2359, 9451, 0),
		new WorldPoint(2360, 9446, 0),
		new WorldPoint(2362, 9445, 0),
		new WorldPoint(2365, 9445, 0),
		new WorldPoint(2367, 9446, 0),
		new WorldPoint(2368, 9451, 0),
		new WorldPoint(2368, 9448, 0)
	)

	val dangerousTiles = {
		val minTile = new WorldPoint(safeTiles.map(_.getX).min, safeTiles.map(_.getY).min, 0)
		val maxTile = new WorldPoint(safeTiles.map(_.getX).max, safeTiles.map(_.getY).max, 0)
		val size = (maxTile.getX - minTile.getX, maxTile.getY - minTile.getY)
		val padding = 2

		new WorldArea(minTile.dx(-padding).dy(-padding), size._1 + (2*padding), size._2+(2*padding)).toWorldPointList.asScala.toList
			.filterNot(safeTiles.contains(_))
	}

	val thermyArea:WorldRegion = new WorldArea(2351, 9438, 27, 24, 0)
	def inBossRoom(wp: WorldPoint): Boolean = {
		Option(wp).map(TWorldPoint.get(_)).map(thermyArea.contains(_)).getOrElse(false)
	}

	def inRegion(rid: Int): Boolean = {
		Seq(9363, 9619).contains(rid)
	}

	def isDangerousTile(wp: WorldPoint): Boolean = {
		Option(wp).map(TWorldPoint.get(_)).map(twp => {
			thermyArea.contains(twp) && dangerousTiles.contains(twp)
		}).getOrElse(false)
		//			.exists(twp => safeTiles.contains(twp))
	}

	sealed trait ThermyProjectile(val pid: Int) extends enumeratum.EnumEntry {}
	object ThermyProjectiles extends enumeratum.Enum[ThermyProjectile] with ShimUtils.Logging() {
		case object Spec extends ThermyProjectile(ProjectileID.THERMY_SPEC_PROJ)
		case object Range extends ThermyProjectile(ProjectileID.THERMY_RANGE_PROJ)
		case object Mage extends ThermyProjectile(ProjectileID.THERMY_MAGE_PROJ)

		def unapply(v: Projectile): Option[(ThermyProjectile, Projectile)] = {
			Option(v).map(_.getId).flatMap(pid => values.find(_.pid == pid))
				.map(tp => (tp, v))
		}

		override def values: IndexedSeq[ThermyProjectile] = findValues
	}
}
