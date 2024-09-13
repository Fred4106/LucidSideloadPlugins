package com.fredplugins.titheFarm2

import com.fredplugins.titheFarm2.SPlantInfo.GrownPlantInfo

sealed trait SPlantType2(val base: Int) {}
case object Golovanova extends SPlantType2(27393) {}
case object Bologano extends SPlantType2(27404) {}
case object Logavano extends SPlantType2(27415) {}

sealed trait SPlantInfo(val tpe: Option[SPlantType2], val age: Int) {
//	def tpe: Option[SPlantType2]
//	def getType: Option[SPlantType2] = Option.apply(tpe)
	def needsWater: Boolean = false
	def id: Int// = SPlantInfo.reverseLookup(this).get
}


object SPlantInfo {
	sealed transparent trait NotEmptyInfo {
		this: SPlantInfo =>
		def offset: Int
		override def id: Int = {
			this.tpe.map(_.base - offset).get
		}
	}
	case object EmptyPlantInfo extends SPlantInfo(None, 0) {
		override def id: Int = 27383
	}
	case class DryPlantInfo(t: SPlantType2, a: Int) extends SPlantInfo(Some(t), a) with NotEmptyInfo{
		override def offset: Int = (9 - (0 + (age * 3)))
		override def needsWater: Boolean = true
	}
	case class WateredPlantInfo(t: SPlantType2, a: Int) extends SPlantInfo(Some(t), a) with NotEmptyInfo {
		override def offset: Int = (9 - (1 + (age * 3)))
	}
	case class DeadPlantInfo(t: SPlantType2, a: Int) extends SPlantInfo(Some(t), a) with NotEmptyInfo{
		override def offset: Int = (9 - (2 + (age * 3)))
	}
	case class GrownPlantInfo(t: SPlantType2) extends SPlantInfo(Some(t),3) with NotEmptyInfo{
		override def offset: Int = 0
	}

	def validIds: List[Int] = List(27383,
27384,
27385,
27386,
27387,
27388,
27389,
27390,
27391,
27392,
27393,
27395,
27396,
27397,
27398,
27399,
27400,
27401,
27402,
27403,
27404,
27406,
27407,
27408,
27409,
27410,
27411,
27412,
27413,
27414,
27415
)
	def lookup(objId: Int): Option[SPlantInfo] = {
		Option(objId).collect{
			case 27383 /*Empty[Grown]*/ => EmptyPlantInfo
			case 27384 /*Golovanova[Unwatered(0)]*/ => DryPlantInfo(Golovanova, 0)
			case 27385 /*Golovanova[Watered(0)]*/ => WateredPlantInfo(Golovanova, 0)
			case 27386 /*Golovanova[Dead(0)]*/ => DeadPlantInfo(Golovanova, 0)
			case 27387 /*Golovanova[Unwatered(1)]*/ => DryPlantInfo(Golovanova, 1)
			case 27388 /*Golovanova[Watered(1)]*/ => WateredPlantInfo(Golovanova, 1)
			case 27389 /*Golovanova[Dead(1)]*/ => DeadPlantInfo(Golovanova, 1)
			case 27390 /*Golovanova[Unwatered(2)]*/ => DryPlantInfo(Golovanova, 2)
			case 27391 /*Golovanova[Watered(2)]*/ => WateredPlantInfo(Golovanova, 2)
			case 27392 /*Golovanova[Dead(2)]*/ => DeadPlantInfo(Golovanova, 2)
			case 27393 /*Golovanova[Grown]*/ => GrownPlantInfo(Golovanova)
			case 27395 /*Bologano[Unwatered(0)]*/ => DryPlantInfo(Bologano, 0)
			case 27396 /*Bologano[Watered(0)]*/ => WateredPlantInfo(Bologano, 0)
			case 27397 /*Bologano[Dead(0)]*/ => DeadPlantInfo(Bologano, 0)
			case 27398 /*Bologano[Unwatered(1)]*/ => DryPlantInfo(Bologano, 1)
			case 27399 /*Bologano[Watered(1)]*/ => WateredPlantInfo(Bologano, 1)
			case 27400 /*Bologano[Dead(1)]*/ => DeadPlantInfo(Bologano, 1)
			case 27401 /*Bologano[Unwatered(2)]*/ => DryPlantInfo(Bologano, 2)
			case 27402 /*Bologano[Watered(2)]*/ => WateredPlantInfo(Bologano, 2)
			case 27403 /*Bologano[Dead(2)]*/ => DeadPlantInfo(Bologano, 2)
			case 27404 /*Bologano[Grown]*/ => GrownPlantInfo(Bologano)
			case 27406 /*Logavano[Unwatered(0)]*/ => DryPlantInfo(Logavano, 0)
			case 27407 /*Logavano[Watered(0)]*/ => WateredPlantInfo(Logavano, 0)
			case 27408 /*Logavano[Dead(0)]*/ => DeadPlantInfo(Logavano, 0)
			case 27409 /*Logavano[Unwatered(1)]*/ => DryPlantInfo(Logavano, 1)
			case 27410 /*Logavano[Watered(1)]*/ => WateredPlantInfo(Logavano, 1)
			case 27411 /*Logavano[Dead(1)]*/ => DeadPlantInfo(Logavano, 1)
			case 27412 /*Logavano[Unwatered(2)]*/ => DryPlantInfo(Logavano, 2)
			case 27413 /*Logavano[Watered(2)]*/ => WateredPlantInfo(Logavano, 2)
			case 27414 /*Logavano[Dead(2)]*/ => DeadPlantInfo(Logavano, 2)
			case 27415 /*Logavano[Grown]*/ => GrownPlantInfo(Logavano)
		}
	}
//	def reverseLookup(info: SPlantInfo): Option[Int] = {
//		validIds.find(i => lookup(i).contains(info))
//	}

	def main(args: Array[String]): Unit = {
//		val table = SPlantType.values.flatMap(tpe => {
//			tpe.states.flatMap(state => {
//				val id = tpe.getIdForState(state)
//				lookup(id).map(ii => (id, tpe, state, ii))
//			})
//		})
		validIds.foreach(id => {
				val lookupId = lookup(id)
				println(s"case ${id} => ${lookupId} => ${lookupId.map(_.id).getOrElse(-1)}")
		})
	}
}
