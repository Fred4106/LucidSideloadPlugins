package com.fredplugins.titheFarm

sealed trait SPlantState(val offset: Int) {
	this: Product =>
	def age: Int
}
object SPlantState {
	case class Unwatered(age: Int) extends SPlantState(9 - (0 + (age * 3))) {}
	case class Watered(age: Int) extends SPlantState(9 - (1 + (age * 3))) {}
	case class Dead(age: Int) extends SPlantState(9 - (2 + (age * 3))) {}
	case object Grown extends SPlantState(0) {def age: Int = 3}
	val values: List[SPlantState] = List(Unwatered(0), Watered(0), Dead(0), Unwatered(1), Watered(1), Dead(1), Unwatered(2), Watered(2), Dead(2), Grown)
}