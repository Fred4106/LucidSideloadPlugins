package com.fredplugins.titheFarm

import net.runelite.api.{Client, GameObject}
import net.runelite.api.coords.{LocalPoint, WorldPoint}

import java.time.{Duration, Instant}

object STitheFarmPlant {
	def fromObject(go: GameObject)(using client: Client): Option[STitheFarmPlant] = {
		SPlantType.getState(go.getId).map {
			case (plantType, state) => STitheFarmPlant(plantType, state, go, Instant.now(), client.getTickCount)
		}
	}
}
case class STitheFarmPlant(tpe: SPlantType, state: SPlantState, go: GameObject, planted: Instant, plantedTick: Int) {
	def millisSincePlanted: Int = {
		Option(Duration.between(planted, Instant.now).toMillis).filter(_.isValidInt).map(_.toInt).get
	}
	def debugStr(using client: Client): String = {
//		client.getTopLevelWorldView.getScene

		val x = getWorldLocation.getX - client.getTopLevelWorldView.getBaseX
		val y = getWorldLocation.getY - client.getTopLevelWorldView.getBaseY

		s"${state} ${go.getId} ($x,$y) ${client.getTickCount - plantedTick}"
	}
	def ticksSincePlanted(client: Client): Int = client.getTickCount - plantedTick
	def getId: Int = go.getId
	def getAge: Int = {
		state.age
	}

	def getRelative: Double = {
		scala.math.min(scala.math.max(millisSincePlanted.toDouble / 60000, 0.0d), 1.0d)
	}
	def getWorldLocation: WorldPoint = go.getWorldLocation
}