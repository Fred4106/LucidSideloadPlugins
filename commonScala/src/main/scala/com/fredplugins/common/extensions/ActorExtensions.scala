package com.fredplugins.common.extensions

import com.fredplugins.common.utils.TWorldPoint
import com.fredplugins.common.utils.WorldPointUtils
import net.runelite.api.{Actor, Client, NPC, NPCComposition, Player}
import net.runelite.api.coords.{Angle, Direction, WorldPoint}
import net.runelite.api.events.{ActorDeath, AnimationChanged}
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread
import net.runelite.client.game.NPCManager

import javax.annotation.Nullable
import scala.util.chaining.scalaUtilChainingOps
import scala.reflect.Selectable.reflectiveSelectable
object ActorExtensions {
//	given Conversion[Actor, `
	private val npcManager = RuneLite.getInjector.getInstance(classOf[NPCManager])
	extension (e: Actor) {
		def templateLocation: WorldPoint = TWorldPoint.get(e.getWorldLocation)
		def isInInstance: Boolean = e.getWorldView.isInstance
		def region: Int = e.getWorldLocation.getRegionID
		def templateRegion: Int = templateLocation.getRegionID

		def direction: Direction = Angle(e.getOrientation).getNearestDirection
	}

	extension (n: NPC)(using client: Client) {
		def actor: Actor = n//.asInstanceOf[Actor].templateLocation//{templateLocation, isInInstance, region, templateRegion}
//		export templateLocation
//		def templateLocation: WorldPoint = TWorldPoint.get(n.getWorldLocation)
//		def isInInstance: Boolean = n.getWorldView.isInstance
//		def region: Int = n.getWorldLocation.getRegionID
//		def templateRegion: Int = templateLocation.getRegionID

		def health: Int = {
			val ratio = n.getHealthRatio
			val scale = n.getHealthScale
			(npcManager.getHealth(n.getId).toDouble * (ratio.toDouble / scale.toDouble)).toInt
		}

		def healthPercent: Int = {
			val ratio = n.getHealthRatio
			val scale = n.getHealthScale
			(
				if(ratio  == -1 || scale == -1) 100
				else ((ratio.toDouble / scale.toDouble) * 100).toInt
			).min(100).max(0)
		}

		def baseId: Int = {
			n.getComposition.getId
		}

		def isImpostor: Boolean = {
			n.getId != baseId
		}

		def impostorComposition: Option[NPCComposition] = {
			Option.when(isImpostor){
				client.getNpcDefinition(n.getId)
			}
		}

		def niceString: String = {
			val morphString = Option(baseId).filter(_ != n.getId).map(i => s", base=${i}").getOrElse("")
			s"Npc(id=${n.getId}${morphString}, idx=${n.getIndex}, sLoc=${n.getLocalLocation.pipe(ll => s"(${ll.getSceneX}, ${ll.getSceneY})")}, tLoc=${n.templateLocation})"
		}
	}

	extension (p: Player) {
		def actor: Actor = p
		//		export ActorExtensions.{templateLocation, isInInstance, region, templateRegion}

		//		def templateLocation: WorldPoint = TWorldPoint.get(p.getWorldLocation)
		//		def isInInstance: Boolean = p.getWorldView.isInstance
		//		def region: Int = p.getWorldLocation.getRegionID
		//		def templateRegion: Int = templateLocation.getRegionID

		def isLocal(using c: Client): Boolean = c.getLocalPlayer.getId == p.getId

		def niceString: String = {
			s"Player(id=${p.getId}, name=${p.getName}, level=${p.getCombatLevel}, sLoc=${p.getLocalLocation.pipe(ll => s"(${ll.getSceneX}, ${ll.getSceneY})")}, tLoc=${p.templateLocation})"
		}
	}

//	extension (e: ActorDeath) {
//		@Nullable
//		def getNpc: NPC = Option(e.getActor).collect {
//			case n: NPC => n
//		}.orNull
//
//		def niceString: String = {
//			s"Player(id=${p.getId}, name=${p.getName}, level=${p.getCombatLevel}, sLoc=${p.getLocalLocation.pipe(ll => s"(${ll.getSceneX}, ${ll.getSceneY})")}, tLoc=${p.templateLocation})"
//		}
//	}

	type HasGetActorMethod = AnyRef & { def getActor(): Actor }
//	type HasGetNpcMethod = AnyRef & { def getNpc(): NPC }
//	type HasGetPlayerMethod = AnyRef & { def getPlayer(): Player }
	extension (e: HasGetActorMethod) {
		def getAsNpc(): Option[NPC] = Option(e.getActor()).collect {
			case n: NPC => n
		}

		def getAsPlayer(): Option[Player] = Option(e.getActor()).collect {
			case n: Player => n
		}
	}
	extension (e: Actor) {
		def getAsNpc(): Option[NPC] = Option(e).collect {
			case n: NPC => n
		}

		def getAsPlayer(): Option[Player] = Option(e).collect {
			case n: Player => n
		}
	}
}
