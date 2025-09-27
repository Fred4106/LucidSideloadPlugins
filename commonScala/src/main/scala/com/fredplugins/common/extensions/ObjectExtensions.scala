package com.fredplugins.common.extensions

import com.formdev.flatlaf.util.Animator
import com.fredplugins.common.utils.TWorldPoint
import com.fredplugins.common.utils.WorldPointUtils
import net.runelite.api.Animation
import net.runelite.api.DynamicObject
import net.runelite.api.GameObject
import net.runelite.api.GraphicsObject
import net.runelite.api.coords.WorldPoint
import net.runelite.api.{Client, ObjectComposition, TileObject}

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
		def templateLocation: WorldPoint = {
			TWorldPoint.get(e.getWorldLocation)// WorldPointUtils.toTemplate(e.getWorldLocation)
		}
	}
	extension (e: GraphicsObject)(using client: Client) {
		def templateLocation: WorldPoint = {
			TWorldPoint.get(WorldPoint.fromLocalInstance(client, e.getLocation))//WorldPointUtils.toTemplate(WorldPoint.fromLocalInstance(client, e.getLocation))
		}
	}
	extension (e: TileObject)(using client: Client) {
		def wrapped: TileObjectWrapper = TileObjectWrapper(e)
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
	}
}
