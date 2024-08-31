package com.fredplugins.dt2


import com.fredplugins.dt2
import com.fredplugins.dt2.ForsakenAssassin.{ForsakenAssassinId, ForsakenCloud, ForsakenProjectile}

import scala.collection.mutable.ListBuffer
import com.google.inject.{Inject, Singleton}
import com.lucidplugins.api.utils.GameObjectUtils
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.{Client, NPC, Projectile, TileObject}
import net.runelite.api.events.{AnimationChanged, GameObjectDespawned, GameObjectSpawned, GameTick, NpcDespawned, NpcSpawned, ProjectileMoved}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import org.slf4j.Logger

import java.awt.Color
import java.util
import scala.util.chaining.*
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.annotation.unused
import scala.collection.mutable
import com.fredplugins.dt2.{MyEnumEntry, MyEnum}
import enumeratum._
import com.fredplugins.dt2.ForsakenAssassin.Enums.{CloudType, ForsakenAnimation, ForsakenProjectileType}


object ForsakenAssassin {
	object Enums {

		sealed trait ForsakenAnimation(val id: Int) extends MyEnumEntry

		object ForsakenAnimation extends MyEnum[ForsakenAnimation] {
			case object BasicMelee extends ForsakenAnimation(406)

			case object BasicRange extends ForsakenAnimation(426)

			case object ThrowAcid extends ForsakenAnimation(385)

			case object Throw3Jars extends ForsakenAnimation(7617)

			val values: IndexedSeq[ForsakenAnimation] = findValues
		}

		sealed trait ForsakenProjectileType(val id: Int, val color: Color) extends MyEnumEntry

		object ForsakenProjectileType extends MyEnum[ForsakenProjectileType] {
			case object AcidJar extends ForsakenProjectileType(2383, Color.GREEN)
			case object White extends ForsakenProjectileType(2295, Color.WHITE)
			case object Pink extends ForsakenProjectileType(2300, Color.PINK)

			val values: IndexedSeq[ForsakenProjectileType] = findValues
		}

		sealed trait CloudType(val id: Int, val color: Color) extends MyEnumEntry
		object CloudType extends MyEnum[CloudType] {
			case object White extends CloudType(46622, Color.WHITE)
			case object Pink extends CloudType(47505, Color.PINK)

			val values: IndexedSeq[CloudType] = findValues
		}
	}
	val ForsakenAssassinId: Int = 12328
	class ForsakenProjectile(val p: Projectile) {
		assert(ForsakenProjectileType.find(p.getId).isDefined)
		val tpe: ForsakenProjectileType = ForsakenProjectileType.find(p.getId).get

		def localPoint: LocalPoint = {
			val x: Int = p.getX.toInt
			val y: Int = p.getY.toInt
			new LocalPoint(x, y, destinationPoint.getWorldView)
		}

		def localTile: Client => WorldPoint = (client) => WorldPoint.fromLocal(client, localPoint)

		def destinationPoint: LocalPoint = {
			p.getTarget
		}

		def destinationTile: Client => WorldPoint = (client) => WorldPoint.fromLocal(client, destinationPoint)
	}

	class ForsakenCloud(val c: TileObject) {
		assert(CloudType.find(c.getId).isDefined)
		val tpe: CloudType = CloudType.find(c.getId).get
	}
}
@Singleton
class ForsakenAssassin @Inject()(val client: Client, val eventBus: EventBus) {
	val log: Logger = org.slf4j.LoggerFactory.getLogger(classOf[ForsakenAssassin])

	private object State {
		var target: NPC = null
		var animation: Option[ForsakenAnimation]                            = None
		var vials        : List[ForsakenProjectile] = List.empty//.from(projectiles)
		var clouds       : List[ForsakenCloud]      = List.empty//.from(

		val newVials     : mutable.ListBuffer[ForsakenProjectile] = mutable.ListBuffer.empty[ForsakenProjectile]
		val addedClouds  : mutable.ListBuffer[ForsakenCloud]      = mutable.ListBuffer.empty[ForsakenCloud]
		val removedClouds: mutable.ListBuffer[TileObject]         = mutable.ListBuffer.empty[TileObject]
	}

	def getClouds: util.List[ForsakenCloud] = {
		State.clouds.asJava
	}

	def getVials: util.List[ForsakenProjectile] = {
		State.vials.asJava
	}

	def getNpc: Option[NPC] = Option(State.target)

	def init(): Unit = {
		ethanApiPlugin.collections.NPCs.search().withId(ForsakenAssassinId).nearestToPlayer().toScala.map(new NpcSpawned(_)).foreach(onNpcSpawned)
	}

	def reset(): Unit = {
		getNpc.map(s => new NpcDespawned(s)).foreach(onNpcDespawned)
	}

	@Subscribe
	def onNpcSpawned(event: NpcSpawned): Unit = {
		if(State.target != null || event.getNpc.getId != ForsakenAssassinId) {
		} else {
			State.target = event.getNpc
			State.vials = client.getTopLevelWorldView.getProjectiles.asScala.toList.filter(p => ForsakenProjectileType.find(p.getId).isDefined).map(p => new ForsakenProjectile(p))
			State.clouds = GameObjectUtils.search().filter(go => CloudType.find(go.getId).isDefined).result().asScala.toList.map(go => new ForsakenCloud(go))

			State.animation = None
			State.addedClouds.clear()
			State.removedClouds.clear()
			State.newVials.clear()
		}
	}

	@Subscribe
	def onNpcDespawned(event: NpcDespawned): Unit = {
		if (State.target == null || event.getNpc.getId != ForsakenAssassinId) {}
		else {
			State.target = null
			State.vials = List.empty
			State.clouds = List.empty
			State.animation = None

			State.addedClouds.clear()
			State.removedClouds.clear()
			State.newVials.clear()
		}
	}
	@Subscribe
	def onProjectileMoved(event: ProjectileMoved): Unit = {
		if(State.target == null) return
		val currentVials = State.vials
		Option(event.getProjectile).filter(p => ForsakenProjectileType.find(p.getId).isDefined).filterNot(p => currentVials.exists(_.p == p)).foreach(toAdd => {
			State.newVials.addOne(new ForsakenProjectile(toAdd))
		})
	}

	@Subscribe def onGameTick(@unused event: GameTick): Unit = {
		if (State.target == null) return
		log.info("GameTick: {}", client.getTickCount)
		val currentAnimation: Option[ForsakenAnimation] = Option(State.target).map(_.getAnimation).flatMap(anim => ForsakenAnimation.find(anim))
		if(currentAnimation != State.animation) {
			log.info("Animation changing from {} to {}", State.animation, currentAnimation)
			State.animation = currentAnimation
		}
		val vialsSnapshot = State.vials
		val vialsToAdd    = State.newVials.toList
		if(vialsToAdd.nonEmpty) {
			State.vials = State.vials.appendedAll(vialsToAdd)
			State.newVials.clear()
		}
		val vialsToRemove = State.vials.filterNot(v => v.p.getRemainingCycles > 0)
		if(vialsToRemove.nonEmpty) {
			State.vials = State.vials.filterNot(v => vialsToRemove.contains(v))
		}
		if(vialsSnapshot != State.vials) {
			log.info("+{} - {} = {}", vialsToAdd, vialsToRemove, State.vials)
		}
		if(State.addedClouds.nonEmpty || State.removedClouds.nonEmpty) {
			val cloudsSnapshot = State.clouds
			State.clouds = State.clouds.appendedAll(State.addedClouds.toList).filter(c => !State.removedClouds.toList.contains(c.c))
			if(cloudsSnapshot != State.clouds) {
				log.info("+{} - {} = {}", State.addedClouds.toList, State.removedClouds.toList, State.clouds)
			}
			State.addedClouds.clear()
			State.removedClouds.clear()
		}
	}

	@Subscribe def onAnimationChanged(event: AnimationChanged): Unit = {
		if (State.target == null) return
		log.info("Animation Changed to {}[{}] from {}", ForsakenAnimation.find(event.getActor.getAnimation), event.getActor.getAnimation, State.animation)
	}

	@Subscribe def onGameObjectSpawned(event: GameObjectSpawned): Unit = {
		if (State.target == null) return
		if (CloudType.find(event.getGameObject.getId).isEmpty) return
		if (State.addedClouds.exists(_.c == event.getGameObject)) return
		State.addedClouds.addOne(new ForsakenCloud(event.getGameObject))
	}

	@Subscribe def onGameObjectDespawned(event: GameObjectDespawned): Unit = {
		if (State.target == null) return
		if (CloudType.find(event.getGameObject.getId).isEmpty) return
		if (State.removedClouds.contains(event.getGameObject)) return
		State.removedClouds.addOne(event.getGameObject)
	}
}