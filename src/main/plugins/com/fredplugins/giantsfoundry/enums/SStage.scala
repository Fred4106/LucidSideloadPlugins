package com.fredplugins.giantsfoundry.enums

enum SStage(name: String, heat: SHeat, progressPerAction: Int, heatChange: Int){
	case TripHammer extends SStage("Hammer", SHeat.High, 20, -25)
	case GrindStone extends SStage("Grind", SHeat.Med, 10, 15)
	case PolishingWheel extends SStage("Polish", SHeat.Low, 10, -17)

	def isHeating: Boolean = heatChange > 0
	def isCooling: Boolean = heatChange < 0
}
