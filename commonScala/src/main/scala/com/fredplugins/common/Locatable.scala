package com.fredplugins.common

import net.runelite.api.{Actor, ActorSpotAnim, Client, GameObject, IterableHashTable, Model, NPC, Node, Player, Point, Projectile, Scene, SpritePixels, TileObject, WorldView}
import net.runelite.api.coords.{LocalPoint, WorldArea, WorldPoint}

import java.awt.{Graphics2D, Polygon, Shape}
import java.awt.image.BufferedImage
import scala.swing.ListView.Renderer.Wrapped
import scala.util.chaining.*

//trait Locatable {
//	def worldPoint: WorldPoint
//}

object Locatable {
	type LocatableType = Actor | TileObject | Projectile
	extension (n: LocatableType)(using client: Client) {
		def findWorldView: Option[WorldView] = {
			Option(n).collect {
				case a: Actor => {
					a.getWorldView
				}
				case p: Projectile => {
					client.getWorldView(client.getScene.getWorldViewId)
				}
				case to: TileObject => {
					to.getWorldView
				}
			}
		}
		def findLocalCord: Option[LocalPoint] = {
			Option(n).collect {
				case a: Actor => {
					a.getLocalLocation
				}
				case p: Projectile if(p.findWorldView.isDefined) => {
					val x: Int = p.getX.toInt
					val y: Int = p.getY.toInt
					new LocalPoint(x, y, p.findWorldView.get)
				}
				case to: TileObject => {
					val x: Int = to.getX
					val y: Int = to.getY
					new LocalPoint(x, y, to.getWorldView)
				}
			}.filter(_.isInScene)
		}
		def findWorldCord: Option[WorldPoint] = {
			n.findLocalCord.map(lc => {
				WorldPoint.fromLocalInstance(client, lc, client.getPlane)
			})
		}
		def findSceneCord: Option[(Int, Int)] = {
			n.findLocalCord.map(lc=>lc.getSceneX -> lc.getSceneY)
		}

		def distanceTo(o: LocatableType): Int = {
			n.findWorldCord.zip(o.findWorldCord).map{
				case (np, op) => np.distanceTo(op)
			}.getOrElse(-1)
		}
	}
//	given Conversion[Actor, Locatable] = (a: Actor) => new Locatable(() => a.getWorldView, () => a.getLocalLocation)
//	given Conversion[TileObject, Locatable] = (to: TileObject) => new Locatable(() => to.getWorldView, () => to.getLocalLocation)
//	given Conversion[Projectile, Locatable] = (to:Projectile) => new Locatable(() => to.getWorldView, () => to.getLocalLocation)
}
