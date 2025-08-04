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

/**
 * Enumeration representing the different rooms of the pyramid, where each room
 * is associated with a minimum skill level required to access it and a corresponding base rate.
 *
 * @constructor
 * Defines the room level and associated base rate.
 * @param level
 * The minimum level required to access the room.
 * @param baseRate
 * The base rate associated with the room. Ie, a base rate of 450 implys a drop rate of (1/450)
 */
//sealed trait TimedRoom(val levelRequirement: Int, val baseRate: Int) {
//	self: RoomEnum => {}
//
////	def roomNumber: Int = (level - 11) / 10
////	def percentageOds: Double = (1.0d / baseRate.toDouble)
//}

sealed trait RoomEnum {
	def number: Int;
	def baseRate: Int = {
		List(0, 4200,2800,1600,950,800,750,650,650).apply(number)
	}
	def percentageOds: Double = if(baseRate == 0) 0.0d else (1.0d/baseRate.toDouble)
//	def percentageOds: Double = (1.0d / baseRate.toDouble)
}
object RoomEnum {
////	def roomNumber: Int = {
////		this match {
////			case l if l == RoomEnum.Lobby => 0
////			case timed: TimedRoom => (timed.levelRequirement - 11) / 10
////		}
////	}
//	def percentageOds: Double= {
//		this match {
//			case l if l ==  RoomEnum.Lobby => 0.0d
//			case timed: TimedRoom => (1.0d/timed.baseRate.toDouble)
//		}
//	}
	case object Lobby extends RoomEnum {
	def number: Int = 0
}
	case class Room(number: Int) extends RoomEnum{

	}//(1) with TimedRoom(21, 4200)
//	case Room2 extends RoomEnum(2) with TimedRoom(31, 2800)
//	case Room3 extends RoomEnum(3) with TimedRoom(41, 1600)
//	case Room4 extends RoomEnum(4) with TimedRoom(51, 950)
//	case Room5 extends RoomEnum(5) with TimedRoom(61, 800)
//	case Room6 extends RoomEnum(6) with TimedRoom(71, 750)
//	case Room7 extends RoomEnum(7) with TimedRoom(81, 650)
//	case Room8 extends RoomEnum(8) with TimedRoom(91, 650)
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
//		if(isInPyramidPlunder) {}
//		if(getLpRegionId == PYRAMID_PLUNDER_REGION) {
//			if(getTimerCount > 0)  {
//				Option(getFloorAndReq).flatMap {
//					case (floor, minLevel) => RoomEnum.values.toList.flatMap(r => Option.when(r.isInstanceOf[TimedRoom])(r.asInstanceOf[RoomEnum & TimedRoom])).find(r => .roomNumber == floor && r.levelRequirement == minLevel)
//				}
//			} else {
//				Some(RoomEnum.Lobby)
//			}
//		} else {
//			Option.empty
//		}
	}
}