package com.fredplugins.giantsfoundry.enums

import com.fredplugins.giantsfoundry.FredsGiantsFoundryClientIDs
import net.runelite.api.Client

import java.util

enum SStage(val name: String, val heat: SHeat, val progressPerAction: Int, val heatChange: Int){
	case TripHammer extends SStage("Hammer", SHeat.High, 20, -25)
	case GrindStone extends SStage("Grind", SHeat.Med, 10, 15)
	case PolishingWheel extends SStage("Polish", SHeat.Low, 10, -17)

	def isHeating: Boolean = heatChange > 0
	def isCooling: Boolean = heatChange < 0
}

object SStage {
	def fromWidget(client: Client, stages: util.List[SStage]): Unit = {
		if (stages.isEmpty)
		{
			val progressParent = client.getWidget(FredsGiantsFoundryClientIDs.WIDGET_PROGRESS_PARENT)
			if (progressParent != null && progressParent.getChildren != null) {
				val tempStageList = progressParent.getChildren.toList.flatMap(child => {
					fromSprite(child.getSpriteId)
				})
				tempStageList.foreach(s => stages.add(s))
			}
		}
	}
	def fromSprite(s: Int): Option[SStage] = {
		Option(s match {
			case FredsGiantsFoundryClientIDs.SPRITE_ID_TRIP_HAMMER => TripHammer
			case FredsGiantsFoundryClientIDs.SPRITE_ID_GRINDSTONE => GrindStone
			case FredsGiantsFoundryClientIDs.SPRITE_ID_POLISHING_WHEEL => PolishingWheel
			case _ => null
		})
	}
}
