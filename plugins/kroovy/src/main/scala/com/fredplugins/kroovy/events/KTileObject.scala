package com.fredplugins.kroovy.events

import com.fredplugins.common.utils.{SceneUtils, WorldPointUtils}
import com.fredplugins.kroovy.api.Orientation
import com.fredplugins.kroovy.events.KTileObject.KTileItem.KOwnershipEntry
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.coords.WorldPoint
import net.runelite.api.coords.{Angle, LocalPoint}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import net.runelite.api.{Constants, DynamicObject, HeadIcon, SceneTileModel, Actor as RlActor, DecorativeObject as RlDecorativeObject, GameObject as RlGameObject, GroundObject as RlGroundObject, ItemLayer as RlItemLayer, NPC as RlNpc, Node as RlNode, Player as RlPlayer, Tile as RlTile, TileItem as RlTileItem, TileObject as RlTileObject, WallObject as RlWallObject}

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.reflect.{TypeTest, Typeable}
//import com.fredplugins.kroovy.api.Orientation
import com.fredplugins.kroovy.events.KTileObject.KTileItem.KOwnership
import scala.reflect.Selectable.reflectiveSelectable

sealed trait KTileObject {
	type Wrapped <: RlTileObject & Matchable
	def wrapped: Wrapped
	def id: Int = wrapped.getId

//	def scenePos: (Int, Int) = {
//		val extendedOffset = ((Constants.EXTENDED_SCENE_SIZE - Constants.SCENE_SIZE) / 2)
//		val worldLoc = wrapped.getWorldLocation
//		Option(LocalPoint.fromWorld(wrapped.getWorldView, worldLoc.getX, worldLoc.getY)).map(ll =>
//			(ll.getSceneX() + extendedOffset, ll.getSceneY() + extendedOffset)
//		).getOrElse((-1, -1))
//	}

	def position: WorldPoint = {
//				val extendedOffset = ((Constants.EXTENDED_SCENE_SIZE - Constants.SCENE_SIZE) / 2)
		wrapped.getWorldLocation
	}
	def size: (Int, Int) = (1, 1)

//	def tile: RlTile = {
//		val tiles = wrapped.getWorldView.getScene.getExtendedTiles
//		scenePos match {
//			case (x, y) => tiles(wrapped.getWorldView.getPlane)(x)(y)
//		}
//	}
}

object KTileObject {
	case class KGameObject(wrapped: RlGameObject) extends KTileObject {
		override type Wrapped = RlGameObject & Matchable
		def animation: Option[Int] = {
			Option(wrapped.getRenderable).collect {
				case dynamicObject: DynamicObject => Option(dynamicObject.getAnimation).map(_.getId)
			}.flatten/*.map(anim => anim.getId)*/
		}
		override def size: (Int, Int) = (wrapped.sizeX(), wrapped.sizeY())
		override def position: WorldPoint = {
			//			val extendedOffset: Int        = ((Constants.EXTENDED_SCENE_SIZE - Constants.SCENE_SIZE) / 2)
			val minPos: (Int, Int) = getMinScenePosition
			val lp                 = LocalPoint.fromScene(minPos._1, minPos._2, wrapped.getWorldView)
			val wp                 = WorldPoint.fromLocal(wrapped.getWorldView, lp.getX, lp.getY, wrapped.getPlane)
			//((minPos._1 + extendedOffset), (minPos._2 + extendedOffset))
			wp
			//			Option(LocalPoint.fromScen(wrapped.getWorldView, minPos._1, minPos._2)).map(ll =>{
			//				(ll.getSceneX + extendedOffset, ll.getSceneY + extendedOffset)
			//			}).getOrElse((-1, -1))
		}
		def orientation: Angle = new Angle(wrapped.getOrientation)
		def getMinScenePosition: (Int, Int) = wrapped.getSceneMinLocation.pipe(minLoc => minLoc.getX -> minLoc.getY)
		def getMaxScenePosition: (Int, Int) = wrapped.getSceneMaxLocation.pipe(maxLoc => maxLoc.getX -> maxLoc.getY)

		///*, */
		override def toString: String = s"GameObject(id=${id}, size=${size}, loc=${position}, orientation=${orientation}${
			animation.map {
				a => s", animation=${a}"
			}.getOrElse("")
		})"
	}
	case class KGroundObject(wrapped: RlGroundObject) extends KTileObject {
		override type Wrapped = RlGroundObject & Matchable
		override def toString: String = s"GroundObject(id=${id}, loc=${position}, size=${size})"
	}
	case class KWallObject(wrapped: RlWallObject) extends KTileObject {
		override type Wrapped = RlWallObject & Matchable
		def orientationA: Orientation = Orientation.fromEncoded(wrapped.getOrientationA)
		def orientationB: Orientation = Orientation.fromEncoded(wrapped.getOrientationB)
		override def toString: String = s"WallObject(id=${id}, loc=${position}, size=${size}, orientation=${(orientationA -> orientationB)})"
	}
	case class KDecorativeObject(wrapped: RlDecorativeObject) extends KTileObject {
		override type Wrapped = RlDecorativeObject & Matchable
		def offset: (Int, Int) = (wrapped.getXOffset, wrapped.getYOffset)
		override def toString: String = s"DecorativeObject(id=${id}, loc=${position}, size=${size}, offset=${offset})"
	}

	object KTileItem {
		sealed trait KOwnershipEntry extends enumeratum.EnumEntry {}

		object KOwnership extends enumeratum.Enum[KOwnershipEntry] {
			case object None extends KOwnershipEntry()
			case object Self extends KOwnershipEntry()
			case object Other extends KOwnershipEntry()
			case object Group extends KOwnershipEntry()
			val values: IndexedSeq[KOwnershipEntry] = findValues
			def get(in: Int): KOwnershipEntry = values.apply(in)
		}
	}
	case class KTileItem(id: Int, qty: Int, ticksTillVisible: Int, ticksTillDespawn: Int, ownership: KOwnershipEntry) {}

	case class KItemLayer(wrapped: RlItemLayer) extends KTileObject {
		override type Wrapped = RlItemLayer & Matchable
		def items: Seq[KTileItem] = {
			val acumulator = scala.collection.mutable.ListBuffer.empty[KTileItem]
			var current: RlNode = wrapped.getTop
			while (current.isInstanceOf[RlTileItem]) {
				def c: RlTileItem = current.asInstanceOf[RlTileItem]
				acumulator.addOne(KTileItem(c.getId, c.getQuantity, c.getVisibleTime, c.getDespawnTime, KOwnership.get(c.getOwnership)))

				current = current.getNext
			}
			acumulator.toList
		}

		override def toString: String = s"ItemLayer(id=${id}, loc=${position}, size=${size}, items=${items.mkString("(\n\t", ",\n\t", "\n)")})"
	}

	inline transparent def apply[X <: RlTileObject](inline rlTileObject: X & Matchable): KItemLayer | KGameObject | KWallObject | KGroundObject | KDecorativeObject = {
		inline rlTileObject match {
			case gameObject: RlGameObject => KGameObject(gameObject).asInstanceOf[KGameObject]
			case itemLayer: RlItemLayer => KItemLayer(itemLayer).asInstanceOf[KItemLayer]
			case wallObject: RlWallObject => KWallObject(wallObject).asInstanceOf[KWallObject]
			case groundObject: RlGroundObject => KGroundObject(groundObject).asInstanceOf[ KGroundObject]
			case decorativeObject: RlDecorativeObject => KDecorativeObject(decorativeObject).asInstanceOf[KDecorativeObject]
		}
	}
}