package com.fredplugins.titheFarm

object STitheFarmPlantType {

	def main(args: Array[String]): Unit = {
		val zzz: Seq[(Int, (SPlantType, SPlantState))] = (27383 until 27416).flatMap(i => SPlantType.getState(i).map(j => i -> j))
//		val xxx: List[(String, List[Int])]                                                                                                                               = .values().toList.flatMap(x => x.getObjectIds().toList.map(y => x.getName -> y)).groupMap(_._1)(_._2).map((x, y) => x -> y.sortWith((jjj, kkk) => jjj < kkk)).toList
		zzz.foreach(println)
	}
}


