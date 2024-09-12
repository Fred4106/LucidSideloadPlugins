package com.fredplugins.giantsfoundry.enums

enum SCommissionType {
	case Narrow extends SCommissionType // 1
	case Light extends SCommissionType // 2
	case Flat extends SCommissionType // 3
	case Broad extends SCommissionType // 4
	case Heavy extends SCommissionType // 5
	case Spiked extends SCommissionType // 6
}
object SCommissionType {
	def forVarbit(varbitValue: Int): Option[SCommissionType] = {
		Option.when(varbitValue > 0 && varbitValue <= values.length) {
			values(varbitValue - 1)
		}
	}
}