package com.fredplugins.titheFarm2

import com.fredplugins.common.ShimUtils
import com.fredplugins.titheFarm2.TitheFarmLookup.{PlantData, log}
import net.runelite.api.{Client, GameObject}
import org.slf4j.Logger

import scala.collection.mutable
import scala.util.chaining.scalaUtilChainingOps

object TitheFarmLookup {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	case class PlantData(cachedInfo: SPlantInfo, go: GameObject, composted: Boolean = false, countdown: Int = 0) {}
}
class TitheFarmLookup {
	private val dataMap: mutable.HashMap[TitheFarmPatchLoc, PlantData] = scala.collection.mutable.HashMap.empty[TitheFarmPatchLoc, PlantData]
	def getData: List[(TitheFarmPatchLoc, PlantData)] = {
		dataMap.toList
	}
	def clear(): Unit = dataMap.clear()
	def getPlantData(patch: TitheFarmPatchLoc): Option[PlantData] = dataMap.get(patch)
	def putPlantInfo(patch: TitheFarmPatchLoc, go: GameObject): Unit = {
		val data = SPlantInfo.lookup(go.getId).getOrElse(SPlantInfo.EmptyPlantInfo)
		log.debug("calling putPlantInfo({}, {})", patch, data)
		val removedItemOpt = dataMap.remove(patch)
		val addedItemOpt = {
			val inheritCompost = removedItemOpt.exists(_.composted)
			Option(data).collect {
				case dpi@SPlantInfo.DryPlantInfo(t, a) => PlantData(dpi, go, inheritCompost, 100)
				case wpi@SPlantInfo.WateredPlantInfo(t, a) => PlantData(wpi, go, inheritCompost, removedItemOpt.map(_.countdown).getOrElse(100))
				case deadPi@SPlantInfo.DeadPlantInfo(t, a) => PlantData(deadPi, go, inheritCompost, 100)
				case gpi@SPlantInfo.GrownPlantInfo(t) => PlantData(gpi, go, inheritCompost, 0)
				case emty@SPlantInfo.EmptyPlantInfo => PlantData(emty, go, false, 0)
//				case uuu => PlantData(uuu, inheritCompost, 0)
				//					case info: SPlantInfo.NotEmptyInfo if dataMap.get(patch).map(_.cachedInfo.age).contains(info.age) => {
				//						PlantData(info, dataMap.get(patch).exists(_._2), dataMap.get(patch).map(_._3).getOrElse(100))
				//					}
				//					case info: SPlantInfo.NotEmptyInfo => {
				//						PlantData(info, dataMap.get(patch).exists(_._2), dataMap(patch)._3)
				//						//						STitheFarmPlant(info, gameObject, Instant.now, client.getTickCount)
				//					}
			}.tap(_.foreach(toAdd => {
				dataMap.put(patch, toAdd)
			}))
		}

		(Option((removedItemOpt, addedItemOpt)) collect  {
			case (Some(removed), Some(added)) => s"Replaced ${removed} with ${added}\n"
			case (Some(removed), None) => s"Removed ${removed}\n"
			case (None, Some(added)) => s"Added ${added}\n"
		}).foreach {
			log.debug("{}", _)
		}
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
