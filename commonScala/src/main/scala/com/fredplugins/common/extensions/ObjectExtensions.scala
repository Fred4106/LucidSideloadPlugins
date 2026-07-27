package com.fredplugins.common.extensions

import com.formdev.flatlaf.util.Animator
import com.fredplugins.common.utils.TWorldPoint
import com.fredplugins.common.utils.WorldPointUtils
import net.runelite.api.{Animation, Client, DecorativeObject, DynamicObject, GameObject, GraphicsObject, GroundObject, ItemLayer, ObjectComposition, TileObject, WallObject}
import net.runelite.api.coords.WorldPoint

import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.StreamConverters.*
import scala.jdk.OptionConverters.*
import scala.util.{Random, Try}
import scala.util.chaining.*

object ObjectExtensions {
	final class TileObjectWrapper(to: TileObject) {
//		e.getHash
		def bits: Long = to.getHash()
		def id: Int = (bits >> 17 & 0xffffffff).toInt
		def wall: Int = (bits >> 16 & 1).toInt
		def tpe: Int = (bits >> 14 & 3).toInt
		def sceneLoc: (Int, Int) = {
			val sceneY: Int = (bits >> 7 & 127).toInt
			val sceneX: Int = (bits >> 0 & 127).toInt
			(sceneX, sceneY)
		}
	}
	extension(e: GameObject)(using client: Client) {
		def animationOpt: Option[Animation] = {
			Option(e.getRenderable).collect {
				case d: DynamicObject => Option(d.getAnimation)
			}.flatten
		}
		def animationFrameAndCycleOpt: Option[(Int, Int)] = {
			Option(e.getRenderable).collect {
				case d: DynamicObject => Option(d.getAnimFrame -> d.getAnimCycle)
			}.flatten
		}
	}
	extension (e: GraphicsObject)(using client: Client) {
		def worldLocation: WorldPoint = {
			WorldPoint.fromLocalInstance(client, e.getLocation)
		}
		def templateLocation: WorldPoint = {
			TWorldPoint.get(e.worldLocation)//WorldPointUtils.toTemplate(WorldPoint.fromLocalInstance(client, e.getLocation))
		}
		def animationId: Int = Option(e.getAnimation).fold(-1)(_.getId)
	}
	extension (e: TileObject)(using client: Client) {
		def wrapped: TileObjectWrapper = TileObjectWrapper(e)

		def templateLocation: WorldPoint = {
			TWorldPoint.get(e.getWorldLocation) // WorldPointUtils.toTemplate(e.getWorldLocation)
		}

		def composition: ObjectComposition = client.getObjectDefinition(e.getId)
		def impostorComposition: Option[ObjectComposition] = {
			Option.when(isImpostor){composition.getImpostor}
		}
		def morphId: Int = {
			impostorComposition.map(_.getId).getOrElse(-1)
		}
		def isImpostor: Boolean = {
			composition.getImpostorIds != null
		}

		def niceString: String = {
			val morphString = Option(morphId).filter(_ != -1).map(i=>s", morph=${i}").getOrElse("")
			val nicePrefix = Option(e).collect{
				case _: GameObject => "GameObject"
				case _: WallObject => "WallObject"
			}.getOrElse("TileObject")
//			e match {
//				case gameObject: GameObject => {
//					s"GameObject(id=${gameObject.getId}${morphString}, sLoc=${gameObject.getLocalLocation.pipe(ll => s"(${ll.getSceneX}, ${ll.getSceneY})")}, tLoc=${gameObject.templateLocation})"
//				}
//				case wallObject: WallObject =>
//				case other => s"TileObject(id=${other.getId}${morphString}, sLoc=${other.getLocalLocation.pipe(ll => s"(${ll.getSceneX}, ${ll.getSceneY})")}, tLoc=${other.templateLocation})"
//			}
			s"${nicePrefix}(id=${e.getId}${morphString}, sLoc=${e.getLocalLocation.pipe(ll => s"(${ll.getSceneX}, ${ll.getSceneY})")}, tLoc=${templateLocation})"
		}
	}
}
