package com.fredplugins.pyramidplundercounter

import net.runelite.api.ClientThreadInvoke
import net.runelite.api.Client
import net.runelite.api.coords.WorldArea
import net.runelite.api.gameval.VarbitID

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

sealed trait RoomEnum {
	val number: Int;
	lazy val baseRate: Int = {
		List(0, 4200,2800,1600,950,800,750,650,650).apply(number)
	}
	lazy val areas: Seq[WorldArea] = {
		List(
			List(
				new WorldArea(1922, 4446, 22, 21, 2),
				new WorldArea(1958, 4418, 21, 19, 2),
				new WorldArea(1958, 4447, 19, 20, 3),
				new WorldArea(1923, 4418, 23, 19, 3)
			),
			List(new WorldArea(1920, 4462, 15, 18, 0)),
			List(new WorldArea(1946, 4463, 15, 16, 0)),
			List(new WorldArea(1968, 4452, 13, 22, 0)),
			List(new WorldArea(1921, 4448, 22, 12, 0)),
			List(new WorldArea(1949, 4441, 18, 16, 0)),
			List(new WorldArea(1920, 4423, 13, 18, 0)),
			List(new WorldArea(1939, 4418, 17, 17, 0)),
			List(new WorldArea(1964, 4419, 17, 20, 0))
		)(number)
	}
	def percentageOds: Double = if(baseRate == 0) 0.0d else (1.0d/baseRate.toDouble)
}
object RoomEnum {
	case object Lobby extends RoomEnum {
		override val number: Int = 0
	}
	case class Room(number: Int) extends RoomEnum{}
}

object PyramidPlunderHelper {
	private val PYRAMID_PLUNDER_REGION = 7749
	val GRAND_GOLD_CHEST_TARGET = "<col=ffff>Grand Gold Chest"
	val SARCOPHAGUS_TARGET      = "<col=ffff>Sarcophagus"
	val SPEAR_TRAP              = "<col=ffff>Speartrap"

	def getTimerCount(using client: Client): Int = {
		client.getVarbitValue(VarbitID.NTK_PLAYER_TIMER_COUNT)
	}

	def getFloorAndReq(using client: Client): (Int, Int) = {
		client.getVarbitValue(VarbitID.NTK_ROOM_NUMBER) -> client.getVarbitValue(VarbitID.NTK_THIEVING_REQUIRED)
	}

	def getLpRegionId(using client: Client): Int = {
		Try {
			client.getLocalPlayer.getWorldLocation
		}.toOption.map(x => x.getRegionID) .getOrElse(-1)
	}

	def isInPyramidPlunder(using client: Client): Boolean = {
		getLpRegionId == PYRAMID_PLUNDER_REGION && getTimerCount > 0
	}

	def isInPyramidPlunderOrLobby(using client: Client): Boolean = {
		getLpRegionId == PYRAMID_PLUNDER_REGION
	}

	def getCurrentFloor(using client: Client): Option[RoomEnum] = {
		Option.when(isInPyramidPlunderOrLobby) {
			(if(getTimerCount > 0) {
				RoomEnum.Room.apply((client.getVarbitValue(VarbitID.NTK_THIEVING_REQUIRED)-11)/10)
			} else {
				RoomEnum.Lobby
			})
		}
	}
}