package com.fredplugins.pvmDebugger.moons

import net.runelite.api.Client

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

enum MoonRoomEnum(val regionId: Int) {
	case Antechamber extends MoonRoomEnum(5782)
	case EclipseRoom extends MoonRoomEnum(6038)
	case BloodRoom extends MoonRoomEnum(5526)
	case BlueRoom extends MoonRoomEnum(5783)
	case RewardsCavern extends MoonRoomEnum(6037)
	case AncientPrison extends MoonRoomEnum(5525)
	case SteamboundCavern extends MoonRoomEnum(6039)
	case EarthboundCavern extends MoonRoomEnum(5527)
	case Entrance extends MoonRoomEnum(5781)
}
object MoonRoomEnum {
	def test(client: Client): Option[MoonRoomEnum] = {
		Option(client.getLocalPlayer)
			.flatMap(p => Option(p.getWorldLocation))
			.map(_.getRegionID)
			.flatMap(rid => values.find(_.regionId == rid))
	}
}
