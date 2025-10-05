package com.fredplugins.pyramidplundercounter

import net.runelite.api.ClientThreadInvoke
import net.runelite.api.Client
import net.runelite.api.gameval.VarbitID

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

sealed trait RoomEnum {
	def number: Int;
	def baseRate: Int = {
		List(0, 4200,2800,1600,950,800,750,650,650).apply(number)
	}
	def percentageOds: Double = if(baseRate == 0) 0.0d else (1.0d/baseRate.toDouble)
}
object RoomEnum {
	case object Lobby extends RoomEnum {
		def number: Int = 0
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