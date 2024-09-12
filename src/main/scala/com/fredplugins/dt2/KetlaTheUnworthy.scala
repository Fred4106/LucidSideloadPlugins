package com.fredplugins.dt2

import com.fredplugins.dt2.KetlaTheUnworthy.Enums.KetlaAnimation
import com.fredplugins.dt2.KetlaTheUnworthy.NpcWrapper
import com.google.inject.{Inject, Singleton}
import net.runelite.api.events.{AnimationChanged, GameObjectDespawned, GameObjectSpawned, GameTick, NpcDespawned, NpcSpawned, ProjectileMoved}
import net.runelite.api.{Client, NPC, NpcID, Projectile}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import org.slf4j.Logger

import java.util
import scala.annotation.unused
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

object KetlaTheUnworthy {
	object Enums {
		sealed trait KetlaAnimation(val id: Int) extends MyEnumEntry
		object KetlaAnimation extends MyEnum[KetlaAnimation] {
			case object HitByChargedShotMaybe extends KetlaAnimation(100)
			case object DeathAnimationMaybe extends KetlaAnimation(102)
			case object AttackShotMaybe extends KetlaAnimation(7863)
			case object SummonClonesMaybe extends KetlaAnimation(10108)
			case object PrimaryDeathAnimationMaybe extends KetlaAnimation(10164)
			val values: IndexedSeq[KetlaAnimation] = findValues
		}
//		sealed trait ForsakenProjectileType(val id: Int, val color: Color) extends MyEnumEntry
//		object ForsakenProjectileType extends MyEnum[ForsakenProjectileType] {
//			case object AcidJar extends ForsakenProjectileType(2383, Color.GREEN)
//			case object White extends ForsakenProjectileType(2295, Color.WHITE)
//			case object Pink extends ForsakenProjectileType(2300, Color.PINK)
//			val values: IndexedSeq[ForsakenProjectileType] = findValues
//		}
//
//		sealed trait CloudType(val id: Int, val color: Color) extends MyEnumEntry
//		object CloudType extends MyEnum[CloudType] {
//			case object White extends CloudType(46622, Color.WHITE)
//			case object Pink extends CloudType(47505, Color.PINK)
//			val values: IndexedSeq[CloudType] = findValues
//		}
	}
	class NpcWrapper(val npc: NPC) {
		def animation: Option[KetlaTheUnworthy.Enums.KetlaAnimation] = KetlaTheUnworthy.Enums.KetlaAnimation.find(npc.getAnimation)
		private var lastAnimation: Option[KetlaTheUnworthy.Enums.KetlaAnimation] = animation
		def tick(): Unit = {
			lastAnimation = animation
		}
		def getLastAnimation: Option[KetlaAnimation] = lastAnimation
	}
}
@Singleton
class KetlaTheUnworthy @Inject()(val client: Client, val eventBus: EventBus) {
	val log: Logger = org.slf4j.LoggerFactory.getLogger(classOf[KetlaTheUnworthy])

	private object State {
		var target: NpcWrapper = null
//		var animation: Option[KetlaAnimation]                            = None
		var minions: List[NpcWrapper] = List.empty
		var projectiles        : List[Projectile] = List.empty//.from(projectiles)
//		var clouds       : List[ForsakenCloud]      = List.empty//.from(

		val newProjectiles     : mutable.ListBuffer[Projectile] = mutable.ListBuffer.empty[Projectile]
//		val addedClouds  : mutable.ListBuffer[ForsakenCloud]      = mutable.ListBuffer.empty[ForsakenCloud]
//		val removedClouds: mutable.ListBuffer[TileObject]         = mutable.ListBuffer.empty[TileObject]
	}

	def getProjectiles: util.List[Projectile] = {
		State.projectiles.asJava
	}

	def getTarget: Option[NpcWrapper] = Option(State.target)
	def getMinions: util.List[NpcWrapper] = {
		State.minions.asJava
	}

	def init(): Unit = {
		ethanApiPlugin.collections.NPCs.search().withId(12329).nearestToPlayer().toScala.map(new NpcSpawned(_)).foreach(onNpcSpawned)
	}

	def reset(): Unit = {
		getTarget.map(s => new NpcDespawned(s.npc)).foreach(onNpcDespawned)
	}

	@Subscribe
	def onNpcSpawned(event: NpcSpawned): Unit = {
		if(event.getNpc.getId!=12329 && event.getNpc.getId!=12330) return;
		if(State.target != null && event.getNpc.getId == 12330) {
			State.minions = State.minions.appended(new NpcWrapper(event.getNpc))
		} else if(State.target == null && event.getNpc.getId == NpcID.KETLA_THE_UNWORTHY) {
			State.target = new NpcWrapper(event.getNpc)
			State.projectiles = client.getTopLevelWorldView.getProjectiles.asScala.toList
			State.minions = ethanApiPlugin.collections.NPCs.search().withId(12330).results.asScala.toList.map(new NpcWrapper(_))
//			State.clouds = GameObjectUtils.search().filter(go => CloudType.find(go.getId).isDefined).result().asScala.toList.map(go => new ForsakenCloud(go))
//			State.addedClouds.clear()
//			State.removedClouds.clear()
			State.newProjectiles.clear()
		}
	}

	@Subscribe
	def onNpcDespawned(event: NpcDespawned): Unit = {
		if(State.target == null) return
		if (event.getNpc.getId != 12329 && event.getNpc.getId != 12330) return

		if (event.getNpc.getId == 12329) {
			State.target = null
			State.projectiles = List.empty
			State.minions = List.empty
			State.newProjectiles.clear()
		} else if (event.getNpc.getId == 12330) {
			State.minions = State.minions.filterNot(m => m.npc == event.getNpc)
		}
	}
	@Subscribe
	def onProjectileMoved(event: ProjectileMoved): Unit = {
		if(State.target == null) return
		val currentProjectiles = State.projectiles
		Option(event.getProjectile).filterNot(p => currentProjectiles.contains(p)).foreach(toAdd => {
			State.newProjectiles.addOne(toAdd)
		})
	}

	@Subscribe def onGameTick(@unused event: GameTick): Unit = {
		if (State.target == null) return
		log.info("GameTick: {}", client.getTickCount)
//		val currentAnimation: Option[KetlaAnimation] = Option(State.target).map(_.getAnimation).flatMap(anim => ForsakenAnimation.find(anim))
		if(State.target.animation != State.target.getLastAnimation) {
			log.info("Animation changing from {} to {}", State.target.getLastAnimation, State.target.animation)
			State.target.tick()
		}
		State.minions.foreach(m => {
			if(m.animation != m.getLastAnimation) {
				log.info("Animation changing from {} to {}", m.getLastAnimation, m.animation)
				m.tick()
			}
		})
		val projectilesSnapshot = State.projectiles
		val projectilesToAdd    = State.newProjectiles.toList
		if(projectilesToAdd.nonEmpty) {
			State.projectiles = State.projectiles.appendedAll(projectilesToAdd)
			State.newProjectiles.clear()
		}
		val projectilesToRemove = State.projectiles.filterNot(v => v.getRemainingCycles > 0)
		if(projectilesToRemove.nonEmpty) {
			State.projectiles = State.projectiles.filterNot(v => projectilesToRemove.contains(v))
		}
		if(projectilesSnapshot != State.projectiles) {
			log.info("+{} - {} = {}", projectilesToAdd, projectilesToRemove, State.projectiles)
		}
//		if(State.addedClouds.nonEmpty || State.removedClouds.nonEmpty) {
//			val cloudsSnapshot = State.clouds
//			State.clouds = State.clouds.appendedAll(State.addedClouds.toList).filter(c => !State.removedClouds.toList.contains(c.c))
//			if(cloudsSnapshot != State.clouds) {
//				log.info("+{} - {} = {}", State.addedClouds.toList, State.removedClouds.toList, State.clouds)
//			}
//			State.addedClouds.clear()
//			State.removedClouds.clear()
//		}
	}

	@Subscribe def onAnimationChanged(event: AnimationChanged): Unit = {
		if (State.target == null) return
		event.getActor match {
			case eventNpc: NPC => {
				val toSearch: List[NpcWrapper] = State.minions.appendedAll(getTarget.toList)
				toSearch.filter(_.npc == eventNpc).foreach(w => {
					log.info("Npc {}[{}] had animation changed from {} to {}[{}]", w.npc, w.npc.getId, w.getLastAnimation, KetlaAnimation.find(eventNpc.getAnimation), eventNpc.getAnimation)
				})
			}
			case _ =>
		}
	}

	@Subscribe def onGameObjectSpawned(event: GameObjectSpawned): Unit = {
		if (State.target == null) return
//		if (CloudType.find(event.getGameObject.getId).isEmpty) return
//		if (State.addedClouds.exists(_.c == event.getGameObject)) return
//		State.addedClouds.addOne(new ForsakenCloud(event.getGameObject))
	}

	@Subscribe def onGameObjectDespawned(event: GameObjectDespawned): Unit = {
		if (State.target == null) return
//		if (CloudType.find(event.getGameObject.getId).isEmpty) return
//		if (State.removedClouds.contains(event.getGameObject)) return
//		State.removedClouds.addOne(event.getGameObject)
	}
}
