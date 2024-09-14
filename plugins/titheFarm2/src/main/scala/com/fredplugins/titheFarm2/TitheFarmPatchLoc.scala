package com.fredplugins.titheFarm2

import enumeratum.values.{IntEnum, IntEnumEntry, LongEnum, LongEnumEntry, StringEnumEntry, ValueEnumEntry}
import enumeratum.{Enum, EnumEntry}
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.{Client, GameObject, Tile}

import scala.util.chaining.*

sealed trait TitheFarmPatchLoc(val x: Int, val y: Int) extends EnumEntry {
	//int lo; // Integer to fill lower bits
	//int hi; // Integer to fill upper bits
	//long val = (((long) hi) << 32) | (lo & 0xffffffffL);

//	def value: Long =  (x.toLong << 32) | (y & 0xffffffffL)

//	def worldLocation(using client: Client): Option[WorldPoint] = {
//		val wv         = client.getTopLevelWorldView
//		val worldPoint = WorldPoint.fromScene(wv, x, y, wv.getPlane)
//		//				.getWorldLocation.getRegionID == 7222
//		Option.when(worldPoint.getRegionID == 7222) {
//			worldPoint
//		}
//	}
//	def getTile(using client: Client): Option[Tile] = {
////		val wv = client.getTopLevelWorldView
//		//			val worldPoint = WorldPoint.fromScene(wv, x, y, wv.getPlane)
//		////				.getWorldLocation.getRegionID == 7222
//		//			Option.when(worldPoint.getRegionID == 7222) {
//		//				wv.getScene.getTiles(wv.getPlane)(x)(y)
//		//			}
//		Option(client.getTopLevelWorldView.getScene.getTiles()(client.getLocalPlayer.getWorldView.getPlane)(x)(y).pipe(t => {
//			if(t.getWorldLocation.getRegionID == 7222) t
//			else null
//		}))
////		worldLocation.flatMap(wl =>
////			Option(client.getTopLevelWorldView.getScene.getTiles).map(_.apply(wl.getPlane)(x)(y))
////		)
//	}
//	def getGameObject(using client: Client): List[GameObject] = {
//		getTile.flatMap(t => Option(t.getGameObjects).map(_.toList)).getOrElse(List.empty[GameObject]).filter(_ != null).filter(_.getId != -1).toList
//		//.flatMap(go => SPlantInfo.lookup(go.getId).map(info => go -> info)).map(_._1)
//		//		val tile: Tile = client.getTopLevelWorldView.getScene.getTiles()(client.getTopLevelWorldView.getPlane)(patch.x)(patch.y)
//		//		WorldPoint.fromScene(client.getTopLevelWorldView, patch.x, patch.y, client.getTopLevelWorldView.getPlane)
//
//		//(go => SPlantInfo.lookup(go.getId)).headOption)
//	}
}
object TitheFarmPatchLoc extends Enum[TitheFarmPatchLoc] {
	val values: IndexedSeq[TitheFarmPatchLoc] = findValues

	case object Patch00 extends TitheFarmPatchLoc(59, 65)
	case object Patch01 extends TitheFarmPatchLoc(59, 62)
	case object Patch02 extends TitheFarmPatchLoc(59, 59)
	case object Patch03 extends TitheFarmPatchLoc(59, 56)
	case object Patch04 extends TitheFarmPatchLoc(59, 50)
	case object Patch05 extends TitheFarmPatchLoc(59, 47)
	case object Patch06 extends TitheFarmPatchLoc(59, 44)
	case object Patch07 extends TitheFarmPatchLoc(59, 41)
	case object Patch10 extends TitheFarmPatchLoc(64, 65)
	case object Patch11 extends TitheFarmPatchLoc(64, 62)
	case object Patch12 extends TitheFarmPatchLoc(64, 59)
	case object Patch13 extends TitheFarmPatchLoc(64, 56)
	case object Patch14 extends TitheFarmPatchLoc(64, 50)
	case object Patch15 extends TitheFarmPatchLoc(64, 47)
	case object Patch16 extends TitheFarmPatchLoc(64, 44)
	case object Patch17 extends TitheFarmPatchLoc(64, 41)
	case object Patch20 extends TitheFarmPatchLoc(69, 65)
	case object Patch21 extends TitheFarmPatchLoc(69, 62)
	case object Patch22 extends TitheFarmPatchLoc(69, 59)
	case object Patch23 extends TitheFarmPatchLoc(69, 56)
	case object Patch24 extends TitheFarmPatchLoc(69, 50)
	case object Patch25 extends TitheFarmPatchLoc(69, 47)
	case object Patch26 extends TitheFarmPatchLoc(69, 44)
	case object Patch27 extends TitheFarmPatchLoc(69, 41)
	case object Patch28 extends TitheFarmPatchLoc(69, 35)
	case object Patch30 extends TitheFarmPatchLoc(74, 65)
	case object Patch31 extends TitheFarmPatchLoc(74, 62)
	case object Patch32 extends TitheFarmPatchLoc(74, 59)
	case object Patch33 extends TitheFarmPatchLoc(74, 56)
	case object Patch34 extends TitheFarmPatchLoc(74, 50)
	case object Patch35 extends TitheFarmPatchLoc(74, 47)
	case object Patch36 extends TitheFarmPatchLoc(74, 44)
	case object Patch37 extends TitheFarmPatchLoc(74, 41)
	case object Patch40 extends TitheFarmPatchLoc(79, 65)
	case object Patch41 extends TitheFarmPatchLoc(79, 62)
	case object Patch42 extends TitheFarmPatchLoc(79, 59)
	case object Patch43 extends TitheFarmPatchLoc(79, 56)
	case object Patch44 extends TitheFarmPatchLoc(79, 50)
	case object Patch45 extends TitheFarmPatchLoc(79, 47)
	case object Patch46 extends TitheFarmPatchLoc(79, 44)
	case object Patch47 extends TitheFarmPatchLoc(79, 41)

	def lookup(x: Int, y: Int): Option[TitheFarmPatchLoc] = {
		values.find(v => v.x == x && v.y == y).tap(_.foreach(found => {
//			println(s"looking for (${x}, ${y}) and found ${found})")
		}))
	}
	def lookup(c: (Int, Int)): Option[TitheFarmPatchLoc] = lookup(c._1, c._2) //values.find(v => v.x == x &&v.y == y)
	def lookup(lp: LocalPoint): Option[TitheFarmPatchLoc] = Option(lp).flatMap(l => lookup(l.getSceneX, l.getSceneY)) //values.find(v => v.x == x &&v.y == y)
	def lookup(wp: WorldPoint)(using client: Client): Option[TitheFarmPatchLoc] = Option(wp).flatMap(w => Option(LocalPoint.fromWorld(client.getTopLevelWorldView, w))).flatMap(lookup) //values.find(v => v.x == x &&v.y == y)
}



//	def isInRegion(using client: Client): Boolean = {
//		client.getLocalPlayer.getWorldLocation.getRegionID == 7222
//	}

//	def getGameObject(patch: TitheFarmPatchLoc)(using client: Client): Option[GameObject] = {
////		val tile: Tile = client.getTopLevelWorldView.getScene.getTiles()(client.getTopLevelWorldView.getPlane)(patch.x)(patch.y)
////		WorldPoint.fromScene(client.getTopLevelWorldView, patch.x, patch.y, client.getTopLevelWorldView.getPlane)
//
//		patch.getTile.flatMap(t => Option(t.getGameObjects).map(_.toList)).flatMap(gol => gol.flatMap(go => SPlantInfo.lookup(go.getId).map(info => go -> info))).headOption.map(_._1)
//
//		//(go => SPlantInfo.lookup(go.getId)).headOption)
//	}

//	def getCoords(x: Int, y: Int): (Int, Int) = {
//		assert(x >= 0 && x < 5)
//		assert(y >= 0 && y < 9)
//		if(y == 8) assert(x == 2)
//
//		val xC = (x * 5) + 59
//		val yC = 65-((y + (y / 4))*3)
//		xC -> yC
//	}

