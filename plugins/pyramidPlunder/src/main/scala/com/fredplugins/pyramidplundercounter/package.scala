package com.fredplugins

import com.fredplugins.common.utils.ShimUtils
import net.runelite.api.GameObject
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import net.runelite.api.Client

import java.awt.Color
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

package object pyramidplundercounter {
	sealed trait UrnState(val color: Color, val ids: Int *) extends enumeratum.EnumEntry {}

	object UrnStates extends enumeratum.Enum[UrnState] with ShimUtils.Logging() {
		case object Closed extends UrnState(Color.yellow, 21261,21262,21263)
		case object Snake extends UrnState(Color.green, 21269,21270,21273)
		case object Charmed extends UrnState(Color.orange, 21276,21277,21278)
		case object Opened extends UrnState(Color.gray, 21265,21266,21267)

		override def values: IndexedSeq[UrnState] = findValues

		def urnIds: Seq[Int] = values.flatMap(_.ids)
		def getUrnState(go: GameObject)(using client: Client): Option[UrnState] = {
			Option(go)
				.flatMap(go => values.find(_.ids.contains(go.morphId)))
//					case 21261|21262|21263 => Closed
//					case 21269|21270|21273 => Snake
//					case 21276|21277|21278 => Charmed
//					case 21265|21266|21267 => Opened
//				}
		}
	}
}
