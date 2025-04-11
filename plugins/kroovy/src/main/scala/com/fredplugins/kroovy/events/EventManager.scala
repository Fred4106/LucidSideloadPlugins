package com.fredplugins.kroovy.events

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.common.utils.{TWorldPoint, WorldPointUtils}
import com.fredplugins.kroovy.KroovyPlugin
import com.fredplugins.kroovy.events.KActor.KNpc
import com.fredplugins.kroovy.events.KEvent.KLocalPlayerMoved
import com.fredplugins.kroovy.events.KEvent.KNpcDied
import com.fredplugins.kroovy.events.KEvent.KPlayerMoved
import com.fredplugins.kroovy.events.KTileObject.KGameObject
import com.google.common.base.internal.Finalizer

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Client, DynamicObject, Renderable, WallObject, Actor as RlActor, GameObject as RlGameObject, NPC as RlNpc, Player as RlPlayer, TileObject as RlTileObject, events as rlEvents}
import net.runelite.api.coords.WorldPoint as RlWorldPoint
import net.runelite.api.events.DecorativeObjectDespawned
import net.runelite.api.events.DecorativeObjectSpawned
import net.runelite.api.events.GameObjectDespawned
import net.runelite.api.events.GameObjectSpawned
import net.runelite.api.events.GameTick
import net.runelite.api.events.GroundObjectDespawned
import net.runelite.api.events.GroundObjectSpawned
import net.runelite.api.events.NpcChanged
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.WallObjectDespawned
import net.runelite.api.events.WallObjectSpawned

import scala.collection.mutable
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.PluginChanged
import org.slf4j.Logger

import java.lang.ref.Cleaner
import java.util.function.Consumer
import scala.compiletime.uninitialized
import scala.reflect.ClassTag

object EventManager {
	extension (bus: EventBus) {
		def addTransformation[T: ClassTag as ct, K <: KEvent](
			transform: T => K,
			priority: Float = 0
		)(using
			builder: mutable.Buffer[KEvent],
			subs: mutable.Buffer[EventBus.Subscriber]
		): Unit = {
			subs.addOne(
				bus.register[T](
					ct.runtimeClass.asInstanceOf[Class[T]],
					(e: T) => {
						builder.addOne(transform(e))
						()
					},
					priority
				)
			)
		}
		def addTransformationOpt[T: ClassTag as ct, K <: KEvent](
			transform: T => Option[K],
			priority: Float = 0
		)(using
			builder: mutable.Buffer[KEvent],
			subs: mutable.Buffer[EventBus.Subscriber]
		): Unit = {
			subs.addOne(
				bus.register[T](
					ct.runtimeClass.asInstanceOf[Class[T]],
					(e: T) => {
						transform(e).foreach(builder.addOne(_))
						()
					},
					priority
				)
			)
		}
		def addSubscriber[T: ClassTag as ct](
			callback: T => Any,
			priority: Float = 0
		)(using
			subs:  mutable.Buffer[EventBus.Subscriber]
		): Unit = {
			subs.addOne(
				bus.register[T](
					ct.runtimeClass.asInstanceOf[Class[T]],
					(e: T) => {
						callback(e)
						()
					},
					priority
				)
			)
		}
	}

	trait EventsOp {
		def client: Client
		def clientThread: ClientThread
//		private var enabled: Boolean = true
		protected def enabled: Boolean
		protected def enabled_=(o: Boolean): Unit
		def enable(): Unit  = enabled = true
		def disable(): Unit  = enabled = false
		def isEnabled: Boolean = enabled
//		def handle(events: List[KEvent]): Unit

		def reaction: PartialFunction[KEvent, Unit]
//		def handle[K <: KEvent](evt: K & Matchable): Unit = {
//
//		}
	}

	abstract class EventsOpImpl extends EventsOp {
		override protected var enabled: Boolean = true
	}
}

@Singleton
class EventManager @Inject()(val client: Client, val clientThread: ClientThread, val eventBus: EventBus) {manager =>

	eventBus.register[PluginChanged](classOf[PluginChanged], (e: PluginChanged) => {
		if(e.getPlugin.isInstanceOf[KroovyPlugin]) {
			if(e.isLoaded) eventBus.register(this)
			else eventBus.unregister(this)
		}
	}, 0)

	private val handlers = mutable.HashSet.empty[EventManager.EventsOp]
	private given builder: mutable.ListBuffer[KEvent] = mutable.ListBuffer.empty[KEvent]
	private given subscribers: mutable.ListBuffer[EventBus.Subscriber] = mutable.ListBuffer.empty[EventBus.Subscriber]
	private val cachedNpcAnimations: mutable.HashMap[RlActor, Int]          = mutable.HashMap.empty[RlActor, Int]
	private val cachedNpcLocations : mutable.HashMap[RlActor, RlWorldPoint] = mutable.HashMap.empty[RlActor, RlWorldPoint]

	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
//	import EventManager.{addTransformation, addTransformationOpt, addSubscriber}

	inline def report[K <: KEvent](inline e: K & Matchable): Unit = {		log.debug(s"handling event ${e.getClass.getName} ${e}")
		builder.addOne(e)

	}
	@Subscribe def onEvent(e: NpcSpawned): Unit = {
		cachedNpcLocations.put(e.getActor, e.getActor.getWorldLocation)
		report(
			KEvent.KNpcSpawned(KActor(e.getNpc))
		)
	}
	@Subscribe def onEvent(event: NpcDespawned): Unit = {
		cachedNpcAnimations.remove(event.getActor)
		cachedNpcLocations.remove(event.getActor)
		report(
			KEvent.KNpcDespawned(KNpc(event.getNpc))
		)
	}
	@Subscribe def onEvent(event: NpcChanged): Unit = {
		report(
			KEvent.KNpcChanged(KNpc(event.getNpc), event.getOld.getId, event.getNpc.getComposition.getId)
		)
	}
	@Subscribe def onEvent(event: GameObjectSpawned): Unit = {
		report(
			KEvent.KGameObjectSpawned(KTileObject(event.getGameObject))
		)
	}
	@Subscribe def onEvent(event: GameObjectDespawned): Unit = {
		report(
		KEvent.KGameObjectDespawned(KTileObject(event.getGameObject))
		)
	}
	@Subscribe def onEvent(event: WallObjectSpawned): Unit = {
		report(
		KEvent.KWallObjectSpawned(KTileObject(event.getWallObject))
		)
	}
	@Subscribe def onEvent(event: WallObjectDespawned): Unit = {
		report(
		KEvent.KWallObjectDespawned(KTileObject(event.getWallObject))
		)
	}

	@Subscribe def onEvent(e: DecorativeObjectSpawned): Unit = {
		report(
			KEvent.KDecorativeObjectSpawned(KTileObject(e.getDecorativeObject))
		)
	}
	@Subscribe def onEvent(e: DecorativeObjectDespawned): Unit = {
		report(
			KEvent.KDecorativeObjectDespawned(KTileObject(e.getDecorativeObject))
		)
	}


	@Subscribe def onEvent(e: GroundObjectSpawned): Unit = {
		report(
			KEvent.KGroundObjectSpawned(KTileObject(e.getGroundObject))
		)
	}
	@Subscribe def onEvent(event: GroundObjectDespawned): Unit = {
		report(
		KEvent.KGroundObjectDespawned(KTileObject(event.getGroundObject))
		)
	}


	@Subscribe(priority = -100.0f) def onTick(event: GameTick): Unit = {
		client.getNpcs.asScala.toList.foreach(npc => {
			val cur = npc.getWorldLocation //.pipe(wp => if (client.getTopLevelWorldView.isInstance) WorldPointUtils.fromInstance(wp)(using client) else wp)
			val old = cachedNpcLocations.getOrElseUpdate(npc, cur)
			if (old != cur) {
				cachedNpcLocations.put(npc, cur)
				val delta: (Int, Int) = (cur.getX - old.getX) -> (cur.getY - old.getY)
				report(KEvent.KNpcMoved(KNpc(npc), delta, cur))
			}
		})
		client.getPlayers.asScala.toList.foreach(player => {
			val cur = player.getWorldLocation //.pipe(wp => if (client.getTopLevelWorldView.isInstance) WorldPointUtils.fromInstance(wp)(using client) else wp)
			val old = cachedNpcLocations.getOrElseUpdate(player, cur)
			if (old != cur) {
				cachedNpcLocations.put(player, cur)
				val delta: (Int, Int) = (cur.getX - old.getX) -> (cur.getY - old.getY)
					if (player != client.getLocalPlayer) report(KEvent.KPlayerMoved(KActor(player), delta, cur))
					else report(KEvent.KLocalPlayerMoved(delta, cur))

			}
		})

		val events = builder.toList.tap(_ => builder.clear())
		handlers.toSeq.filter(_.isEnabled).foreach(h => events.foreach(h.reaction.lift.apply(_)))
	}

//	eventBus.addTransformation((e: rlEvents.NpcChanged) => KEvent.KNpcChanged(KNpc(e.getNpc), e.getOld.getId, e.getNpc.getComposition.getId))
//	eventBus.addTransformation((e: rlEvents.PlayerSpawned) => {
//		cachedNpcLocations.put(e.getActor, e.getActor.getWorldLocation)
//		KEvent.KPlayerSpawned(KActor(e.getPlayer))
//	})
//	eventBus.addTransformation((e: rlEvents.PlayerDespawned) => {
//		cachedNpcAnimations.remove(e.getActor)
//		cachedNpcLocations.remove(e.getActor)
//		KEvent.KPlayerDespawned(KActor(e.getPlayer))
//	})
//	eventBus.addTransformation((e: rlEvents.ActorDeath) => {
//		e.getActor match {
//			case npc: RlNpc => KEvent.KNpcDied(KActor(npc))
//			case player: RlPlayer => {
//				if (player != client.getLocalPlayer) KEvent.KPlayerDied(KActor(player))
//				else KEvent.KLocalPlayerDied
//			}
//		}
//	})
//
//	eventBus.addTransformationOpt((e: rlEvents.AnimationChanged) => {
//		val cur = e.getActor.getAnimation
//		val old = cachedNpcAnimations.getOrElseUpdate(e.getActor, cur)
//		if (old != cur) {
//			cachedNpcAnimations.put(e.getActor, cur)
//			Option(e.getActor match {
//				case npc: RlNpc => KEvent.KNpcAnimationChanged(KNpc(npc), old, cur)
//				case player: RlPlayer => {
//					if (player != client.getLocalPlayer) KEvent.KPlayerAnimationChanged(KActor(player), old, cur)
//					else KEvent.KLocalPlayerAnimationChanged(old, cur)
//				}
//			})
//		} else Option.empty
//
//	})
//
//	eventBus.addTransformation((e: rlEvents.GameObjectSpawned) => {
//		val kgo: KGameObject = KTileObject(e.getGameObject)
//		KEvent.KGameObjectSpawned(kgo)
//	})
//	eventBus.addTransformation((e: rlEvents.GameObjectDespawned) => {
//		val kgo: KGameObject = KTileObject(e.getGameObject)
//		KEvent.KGameObjectDespawned(kgo)
//	})
//
//	eventBus.addTransformation((e: rlEvents.GroundObjectSpawned) => {
//		val kgo = KTileObject(e.getGroundObject)
//		KEvent.KGroundObjectSpawned(kgo)
//	})
//	eventBus.addTransformation((e: rlEvents.GroundObjectDespawned) => {
//		val kgo: KTileObject.KGroundObject = KTileObject(e.getGroundObject)
//		KEvent.KGroundObjectDespawned(kgo)
//	})
//
//	eventBus.addTransformation((e: rlEvents.WallObjectSpawned) => {
//		val kgo: KTileObject.KWallObject = KTileObject(e.getWallObject)
//		KEvent.KWallObjectSpawned(kgo)
//	})
//
//	eventBus.addTransformation((e: rlEvents.WallObjectDespawned) => {
//		val kgo: KTileObject.KWallObject = KTileObject(e.getWallObject)
//		KEvent.KWallObjectDespawned(kgo)
//	})
//

	def register(op: List[KEvent] => Unit, startEnabled: Boolean = true): EventManager.EventsOp = {
		val toRet = new EventManager.EventsOp {
			override def clientThread: ClientThread = manager.clientThread
			override def client: Client = manager.client
//			override def handle(events: List[KEvent]): Unit = op(events)
			override def reaction: PartialFunction[KEvent, Unit] = {
				case KLocalPlayerMoved(delta, cur) =>
			}
			override protected var enabled: Boolean = startEnabled
		}
		handlers.addOne(toRet)
		toRet
	}

	def register(eventsOp: EventManager.EventsOpImpl): EventManager.EventsOp = {
		handlers.addOne(eventsOp)
		eventsOp
	}

	def unregister(op: EventManager.EventsOp): Unit = {
		handlers.remove(op)
	}
}

