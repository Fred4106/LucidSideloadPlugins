package com.fredplugins.common

import net.runelite.api.{Actor, Client, GameObject, NPC, Player, Projectile, Scene, TileObject, WorldView}
import net.runelite.api.coords.{LocalPoint, WorldArea, WorldPoint}
import Locatable.given
import scala.util.chaining.*

type LocatableType = NPC | Player | TileObject | Projectile
class Locatable(val wrapped: LocatableType) {
	def localPoint: LocalPoint = {
		wrapped match {
			case actor: Actor => actor.getLocalLocation
			case to: TileObject => to.getLocalLocation
			case proj: Projectile => LocalPoint(proj.getX.intValue, proj.getY.intValue, proj.getZ.intValue)
		}
	}
	def worldPoint(using client: Client): WorldPoint = {
		WorldPoint.fromLocalInstance(client, localPoint)
//		wrapped match {
//			case actor: Actor => actor.getWorldLocation
//			case to: TileObject => to.getWorldLocation
//			case _: Projectile =>
//		}
	}
	def worldArea(using client: Client): WorldArea = {
		wrapped match {
			case actor: Actor => actor.getWorldArea
			case to: GameObject => {
				WorldPoint.fromScene(client.getTopLevelWorldView, to.getSceneMinLocation.getX, to.getSceneMinLocation.getY, to.getPlane)
					.pipe(mwp => WorldArea(mwp, to.sizeX(), to.sizeY()))
			}
			case to: TileObject => {
								WorldArea(worldPoint, 1, 1)
			}
			case proj: Projectile => WorldArea(worldPoint, 1, 1)
		}
	}

	def distanceTo(other: Locatable)(using client: Client): Int = {
		worldArea.distanceTo(other.worldArea)
	}
}

object Locatable {
	given Conversion[LocatableType, Locatable] = (x: LocatableType) => Locatable(x)

//	(lpt: LocatableType)(using client: Client): Locatable = {
//		new Locatable {
//			override def wrapped: LocatableType = lpt
//		}
//	}

//	given Conversion[Actor, Locatable] = (a: Actor) => new Locatable(() => a.getWorldView, () => a.getLocalLocation)
//	given Conversion[TileObject, Locatable] = (to: TileObject) => new Locatable(() => to.getWorldView, () => to.getLocalLocation)
//	given Conversion[Projectile, Locatable] = (to:Projectile) => new Locatable(() => to.getWorldView, () => to.getLocalLocation)
}
