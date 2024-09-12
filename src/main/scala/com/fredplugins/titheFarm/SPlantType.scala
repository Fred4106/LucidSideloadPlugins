package com.fredplugins.titheFarm

sealed trait SPlantType(val base: Int, val states: SPlantState *) {
	this: Product =>
	def getIdForState(state: SPlantState): Int = {
		states.find(_ == state).map(o => {
			val res = base - o.offset
//			println(s"${productPrefix}(${base}).getIdForState(${o}) = ${base} - ${o.offset} = ${res}")
			res
		}).getOrElse(-1)
	}
	def getStateForId(i: Int): Option[SPlantState] = {
		states.find(_.offset == base - i)
	}
	def name: String = productPrefix
}
object SPlantType {
	sealed transparent trait NotEmpty {
		this: SPlantType =>
	}
	type NotEmptyType = Golovanova.type | Bologano.type | Logavano.type
	case object Empty extends SPlantType(27383, SPlantState.Grown) {}
	case object Golovanova extends SPlantType(27393, SPlantState.Unwatered(0), SPlantState.Watered(0), SPlantState.Dead(0), SPlantState.Unwatered(1), SPlantState.Watered(1), SPlantState.Dead(1), SPlantState.Unwatered(2), SPlantState.Watered(2), SPlantState.Dead(2), SPlantState.Grown) with NotEmpty {}
	case object Bologano extends SPlantType(27404, SPlantState.Unwatered(0), SPlantState.Watered(0), SPlantState.Dead(0), SPlantState.Unwatered(1), SPlantState.Watered(1), SPlantState.Dead(1), SPlantState.Unwatered(2), SPlantState.Watered(2), SPlantState.Dead(2), SPlantState.Grown) with NotEmpty {}
	case object Logavano extends SPlantType(27415, SPlantState.Unwatered(0), SPlantState.Watered(0), SPlantState.Dead(0), SPlantState.Unwatered(1), SPlantState.Watered(1), SPlantState.Dead(1), SPlantState.Unwatered(2), SPlantState.Watered(2), SPlantState.Dead(2), SPlantState.Grown) with NotEmpty {}
	val values: List[SPlantType] = List(Empty, Golovanova, Bologano, Logavano)

	def getState(objectId: Int): Option[(SPlantType, SPlantState)] = {
		values/*.find(_.getStateForId(objectId).isDefined).*/.flatMap(x => {
			x.getStateForId(objectId).map(u => x -> u)// (u => (x, u))
//			x.exists(y => x.getIdForState(y) == objectId)
		}).headOption//.flatMap(t => t.getStateForId(objectId).map(u => (objectId, t, u)))
	}
}