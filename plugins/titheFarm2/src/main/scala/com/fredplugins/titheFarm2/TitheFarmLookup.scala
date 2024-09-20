package com.fredplugins.titheFarm2

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.titheFarm2.TitheFarmLookup.{PlantData, log}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.{Client, GameObject, NPC}
import org.slf4j.Logger

import scala.collection.mutable
import scala.util.chaining.scalaUtilChainingOps

object TitheFarmLookup {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	case class PlantData(cachedInfo: SPlantInfo, go: GameObject, composted: Boolean = false, countdown: Int = 0) {}
}
class TitheFarmLookup {
	private val dataMap: mutable.Map[WorldPoint, PlantData] = mutable.HashMap.empty
//	private val dataMap: mutable.HashMap[TitheFarmPatchLoc, PlantData] = scala.collection.mutable.HashMap.empty[TitheFarmPatchLoc, PlantData]
	def getData: List[(WorldPoint, PlantData)] = {
		dataMap.toList
	}
	def clear(): Unit = dataMap.clear()
	def getPlantData(patch: WorldPoint): Option[PlantData] = dataMap.get(patch)
	def putPlantInfo(key: WorldPoint, go: GameObject): Unit = {
		for {
			info <- SPlantInfo.lookup(go.getId)
			(inheritCompost, inheritCountdown) = dataMap.get(key).map(uu => uu.composted -> uu.countdown).getOrElse((false, 100))
			dataToAdd = Option(info).collect {
				case dpi@SPlantInfo.DryPlantInfo(t, a) => PlantData(dpi, go, inheritCompost || inheritCountdown > 0, 100)
				case wpi@SPlantInfo.WateredPlantInfo(t, a) => PlantData(wpi, go, inheritCompost, inheritCountdown)
				case deadPi@SPlantInfo.DeadPlantInfo(t, a) => PlantData(deadPi, go, inheritCompost, 100)
				case gpi@SPlantInfo.GrownPlantInfo(t) => PlantData(gpi, go, inheritCompost)
				case emty@SPlantInfo.EmptyPlantInfo => PlantData(emty, go)
			}
			removedData = dataToAdd match {
				case Some(value) => dataMap.put(key, value)
				case None => dataMap.remove(key)
			}
			strMessage <- Option(removedData -> dataToAdd).collect {
				case (Some(removed), Some(added)) => s"Replaced ${removed} with ${added}\n"
				case (Some(removed), None) => s"Removed ${removed}\n"
				case (None, Some(added)) => s"Added ${added}\n"
			}
			//		} yield removedData -> dataToAdd)
		} log.debug("transform: {}", strMessage)


//		{
//		}).foreach {
//			log.debug("{}", _)
//		}
	}

	def tick(): Unit = {
		dataMap.mapValuesInPlace {
			case (key, value) if (value.countdown > 0) => {
				val modifiedValue = value.copy(countdown = value.countdown - (if (value.composted) 2 else 1))
//				log.debug(s"ticked {} from {} to {}", key, value, modifiedValue)
				modifiedValue
			}
			case (key, value) => {
				value
			}
		}
	}
}
