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
sealed trait TimedRoom(val levelRequirement: Int, val baseRate: Int) {
	self: RoomEnum => {}

//	def roomNumber: Int = (level - 11) / 10
//	def percentageOds: Double = (1.0d / baseRate.toDouble)
}

enum RoomEnum() {
	def roomNumber: Int = {
		this match {
			case l if l == RoomEnum.Lobby => 0
			case timed: TimedRoom => (timed.levelRequirement - 11) / 10
		}
	}
	def percentageOds: Double= {
		this match {
			case l if l ==  RoomEnum.Lobby => 0.0d
			case timed: TimedRoom => (1.0d/timed.baseRate.toDouble)
		}
	}

	case Lobby extends RoomEnum()
	case Room1 extends RoomEnum() with TimedRoom(21, 4200)
	case Room2 extends RoomEnum() with TimedRoom(31, 2800)
	case Room3 extends RoomEnum() with TimedRoom(41, 1600)
	case Room4 extends RoomEnum() with TimedRoom(51, 950)
	case Room5 extends RoomEnum() with TimedRoom(61, 800)
	case Room6 extends RoomEnum() with TimedRoom(71, 750)
	case Room7 extends RoomEnum() with TimedRoom(81, 650)
	case Room8 extends RoomEnum() with TimedRoom(91, 650)
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

	def getCurrentFloor(using client: Client): Option[RoomEnum] = {
		if(getLpRegionId == PYRAMID_PLUNDER_REGION) {
			if(getTimerCount > 0)  {
				Option(getFloorAndReq).flatMap {
					case (floor, minLevel) => RoomEnum.values.toList.flatMap(r => Option.when(r.isInstanceOf[TimedRoom])(r.asInstanceOf[RoomEnum & TimedRoom])).find(r => r.roomNumber == floor && r.levelRequirement == minLevel)
				}
			} else {
				Some(RoomEnum.Lobby)
			}
		} else {
			Option.empty
		}
	}
}