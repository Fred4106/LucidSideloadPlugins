package com.fredplugins.pvmDebugger

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger.titans.FredsTitanConfig.MagePrayer
import com.fredplugins.pvmDebugger.titans.FredsTitanConfig.MeleePrayer
import com.fredplugins.pvmDebugger.titans.FredsTitanConfig.RangePrayer
import net.runelite.api.Prayer

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

package object titans extends ShimUtils .Logging() {
	val TitansRegion: Int = 11669
	def parseList(string: String): List[Int] = {
		assert(!string.contains('-'))
		val rawList = string.linesIterator.toList.flatMap(line => {
			line.split(Array(',', ';'))
		}).map(_.strip).filter(_.nonEmpty)
		val idList = rawList.map(s => s.toIntOption.getOrElse(-1))
		rawList.zip(idList).filter(_._2 == -1).foreach(x =>
			log.debug(s"cant parse \"{}\" to valid int", x._1)
		)
		idList.filter(_ > 0)
	}


	sealed trait SetupType extends enumeratum.EnumEntry {}
	object SetupTypes extends enumeratum.Enum[SetupType] with ShimUtils.Logging() {
		case object Magic extends SetupType
		case object Melee extends SetupType
		case object Range extends SetupType
		override def values: IndexedSeq[SetupType] = findValues
	}
	case class EquipmentSet(weaponIds: List[Int], gearIds: List[Int], prayer: Option[Prayer]) {

	}

	val magePrayers : List[Prayer] = MagePrayer.values().toList.flatMap(_.getPrayer.pipe(Option(_)))
	val rangePrayers: List[Prayer] =RangePrayer.values().toList.flatMap(_.getPrayer.pipe(Option(_)))
	val meleePrayers    : List[Prayer] = MeleePrayer.values().toList.flatMap(_.getPrayer.pipe(Option(_)))
	val offensivePrayers: Array[Prayer] = magePrayers.appendedAll(rangePrayers).appendedAll(meleePrayers).toArray[Prayer]

}
