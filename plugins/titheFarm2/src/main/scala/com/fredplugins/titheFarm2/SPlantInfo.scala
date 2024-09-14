package com.fredplugins.titheFarm2

import com.fredplugins.titheFarm2.SPlantInfo.GrownPlantInfo

sealed trait SPlantType2(val base: Int) {}
case object Empty extends SPlantType2(27383) {}
case object Golovanova extends SPlantType2(27393) {}
case object Bologano extends SPlantType2(27404) {}
case object Logavano extends SPlantType2(27415) {}

sealed trait SPlantInfo(val tpe: SPlantType2, val age: Int) {
	def needsWater: Boolean = false
	def offset: Int
	def id: Int = tpe.base - offset // = SPlantInfo.reverseLookup(this).get
}


object SPlantInfo {
	sealed trait NonEmptyPlantInfo {
		this: SPlantInfo =>
	}
	case object EmptyPlantInfo extends SPlantInfo(Empty, 0) {
		override def offset: Int = 0
	}
	case class DryPlantInfo(t: SPlantType2, a: Int) extends SPlantInfo(t, a) with NonEmptyPlantInfo {
		override def offset: Int = (9 - (0 + (age * 3)))
		override def needsWater: Boolean = true
	}
	case class WateredPlantInfo(t: SPlantType2, a: Int) extends SPlantInfo(t, a) with NonEmptyPlantInfo {
		override def offset: Int = (9 - (1 + (age * 3)))
	}
	case class DeadPlantInfo(t: SPlantType2, a: Int) extends SPlantInfo(t, a) with NonEmptyPlantInfo {
		override def offset: Int = (9 - (2 + (age * 3)))
	}
	case class GrownPlantInfo(t: SPlantType2) extends SPlantInfo(t, 3) with NonEmptyPlantInfo {
		override def offset: Int = 0
	}

	val values: List[SPlantInfo] = {
		List(Golovanova, Bologano, Logavano).flatMap(t => {
			List(0, 1, 2).flatMap(a => List(DryPlantInfo(t, a), WateredPlantInfo(t, a), DeadPlantInfo(t, a))).appended(GrownPlantInfo(t))
		}).prepended(EmptyPlantInfo)
	}
	def lookup(objId: Int): Option[SPlantInfo] = {
		Option(objId).collect{
			case 27383 =>	EmptyPlantInfo
			case 27384 =>	DryPlantInfo(Golovanova, 0)
			case 27385 =>	WateredPlantInfo(Golovanova, 0)
			case 27386 =>	DeadPlantInfo(Golovanova, 0)
			case 27387 =>	DryPlantInfo(Golovanova, 1)
			case 27388 =>	WateredPlantInfo(Golovanova, 1)
			case 27389 =>	DeadPlantInfo(Golovanova, 1)
			case 27390 =>	DryPlantInfo(Golovanova, 2)
			case 27391 =>	WateredPlantInfo(Golovanova, 2)
			case 27392 =>	DeadPlantInfo(Golovanova, 2)
			case 27393 =>	GrownPlantInfo(Golovanova)
			case 27395 =>	DryPlantInfo(Bologano, 0)
			case 27396 =>	WateredPlantInfo(Bologano, 0)
			case 27397 =>	DeadPlantInfo(Bologano, 0)
			case 27398 =>	DryPlantInfo(Bologano, 1)
			case 27399 =>	WateredPlantInfo(Bologano, 1)
			case 27400 =>	DeadPlantInfo(Bologano, 1)
			case 27401 =>	DryPlantInfo(Bologano, 2)
			case 27402 =>	WateredPlantInfo(Bologano, 2)
			case 27403 =>	DeadPlantInfo(Bologano, 2)
			case 27404 =>	GrownPlantInfo(Bologano)
			case 27406 =>	DryPlantInfo(Logavano, 0)
			case 27407 =>	WateredPlantInfo(Logavano, 0)
			case 27408 =>	DeadPlantInfo(Logavano, 0)
			case 27409 =>	DryPlantInfo(Logavano, 1)
			case 27410 =>	WateredPlantInfo(Logavano, 1)
			case 27411 =>	DeadPlantInfo(Logavano, 1)
			case 27412 =>	DryPlantInfo(Logavano, 2)
			case 27413 =>	WateredPlantInfo(Logavano, 2)
			case 27414 =>	DeadPlantInfo(Logavano, 2)
			case 27415 =>	GrownPlantInfo(Logavano)
		}
	}


	def main(args: Array[String]): Unit = {
//		val table = SPlantType.values.flatMap(tpe => {
//			tpe.states.flatMap(state => {
//				val id = tpe.getIdForState(state)
//				lookup(id).map(ii => (id, tpe, state, ii))
//			})
//		})
		values.foreach(v => {
				val lookupVersion = lookup(v.id)
				println(s"case ${v} => ${v.id} => lookup(${v.id}) = ${lookupVersion}")
		})
		println(s"${values.find(_.id == 27410)}")

//		(0 until 5).flatMap(x => (0 until 9).map(y => (x, y))).toList.filter((x, y) => y < 8 || x == 2).map(c => {
//			(c, TitheFarmLookup.getCoords(c._1, c._2))
//		}).foreach {
//			case (c, sceneC) => println(s"TitheFarmLookup.getCoords(${c._1}, ${c._2}) = ${sceneC}")
//		}
	}
}
