package com.fredplugins

import com.fredplugins.common.utils.ShimUtils
import net.runelite.api.{Client, GameObject, WallObject}
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*

import java.awt.Color
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

package object pyramidplundercounter {
	sealed trait UrnState(val ids: Int*) extends enumeratum.EnumEntry {}
	object UrnStates extends enumeratum.Enum[UrnState] with ShimUtils.Logging() {
		case object Closed extends UrnState(21261, 21262, 21263)
		case object Snake extends UrnState(21269, 21270, 21273)
		case object Charmed extends UrnState(21276, 21277, 21278)
		case object Opened extends UrnState(21265, 21266, 21267)

		override def values: IndexedSeq[UrnState] = findValues
		def getState(go: GameObject)(using client: Client): Option[UrnState] = {
			Option(go)
				.flatMap(go => values.find(_.ids.contains(go.morphId)))
		}
	}

	sealed trait TombDoorState(val morp: Int) extends enumeratum.EnumEntry {}
	object TombDoorStates extends enumeratum.Enum[TombDoorState] with ShimUtils.Logging() {
		case object Locked extends TombDoorState(20948)
		case object Opened extends TombDoorState(20949)

		override def values: IndexedSeq[TombDoorState] = findValues

		def getState(go: WallObject)(using client: Client): Option[TombDoorState] = {
			Option(go)
				.flatMap(go => values.find(_.morp == go.morphId))
		}
	}

	sealed trait SarcophagusState(val morp: Int) extends enumeratum.EnumEntry {}

	object SarcophagusStates extends enumeratum.Enum[SarcophagusState] with ShimUtils.Logging() {
		case object Closed extends SarcophagusState(21255)
		case object Opened extends SarcophagusState(21256)
		case object Opening extends SarcophagusState(21257)
		override def values: IndexedSeq[SarcophagusState] = findValues

		def getState(go: GameObject)(using client: Client): Option[SarcophagusState] = {
			Option(go)
				.flatMap(go => values.find(_.morp == go.morphId))
		}
	}

	sealed trait GrandChestState(val morp: Int) extends enumeratum.EnumEntry {}

	object GrandChestStates extends enumeratum.Enum[GrandChestState] with ShimUtils.Logging() {
		case object Closed extends GrandChestState(20946)
		case object Opened extends GrandChestState(20947)

		override def values: IndexedSeq[GrandChestState] = findValues

		def getState(go: GameObject)(using client: Client): Option[GrandChestState] = {
			Option(go)
				.flatMap(go => values.find(_.morp == go.morphId))
		}
	}

//	val NTK_SARCOPHAGUS_CLOSED = 21255
//	val NTK_SARCOPHAGUS_OPEN = 21256
//	val NTK_SARCOPHAGUS_ANIM = 21257
}
