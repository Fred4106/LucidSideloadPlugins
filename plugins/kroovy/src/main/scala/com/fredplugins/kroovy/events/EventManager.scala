package com.fredplugins.kroovy.events

import com.fredplugins.common.utils.{TWorldPoint, WorldPointUtils}
import com.fredplugins.kroovy.events.KActor.KNpc
import com.fredplugins.kroovy.events.KTileObject.KGameObject

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Client, DynamicObject, Renderable, WallObject, Actor as RlActor, GameObject as RlGameObject, NPC as RlNpc, Player as RlPlayer, TileObject as RlTileObject, events as rlEvents}
import net.runelite.api.coords.WorldPoint as RlWorldPoint
import net.runelite.api.events.GameObjectSpawned

import scala.collection.mutable
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.{EventBus, Subscribe}

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
		def handle(events: List[KEvent]): Unit
	}

	abstract class EventsOpImpl extends EventsOp {
		override protected var enabled: Boolean = true
	}
}

@Singleton
class EventManager @Inject()(val client: Client, val clientThread: ClientThread) {manager =>
//	given Client = client
//	given ClientThread = clientThread
	import EventManager.{addTransformation, addTransformationOpt, addSubscriber}
//	val builder =
	private val handlers = mutable.HashSet.empty[EventManager.EventsOp]
	private given builder: mutable.ListBuffer[KEvent] = mutable.ListBuffer.empty[KEvent]
	private given subscribers: mutable.ListBuffer[EventBus.Subscriber] = mutable.ListBuffer.empty[EventBus.Subscriber]

	private val cachedNpcAnimations: mutable.HashMap[RlActor, Int]        = mutable.HashMap.empty[RlActor, Int]
	private val cachedNpcLocations: mutable.HashMap[RlActor, RlWorldPoint] = mutable.HashMap.empty[RlActor, RlWorldPoint]

	private val cacheObjAnimations: mutable.HashMap[RlTileObject, Int]        = mutable.HashMap.empty[RlTileObject, Int]
	private val cacheObjLocations: mutable.HashMap[RlTileObject, RlWorldPoint] = mutable.HashMap.empty[RlTileObject, RlWorldPoint]

	def start()(using eventBus: EventBus): Unit = {
		eventBus.addTransformation((e: rlEvents.NpcSpawned) => {
			cachedNpcLocations.put(e.getActor, e.getActor.getWorldLocation)
			val x: KActor.KNpc = KActor[RlNpc](e.getNpc)
			KEvent.KNpcSpawned(x)
		})

		eventBus.addTransformation((e: rlEvents.NpcDespawned) => {
			cachedNpcAnimations.remove(e.getActor)
			cachedNpcLocations.remove(e.getActor)
			KEvent.KNpcDespawned(KNpc(e.getNpc))
		})
		eventBus.addTransformation((e: rlEvents.NpcChanged) => KEvent.KNpcChanged(KNpc(e.getNpc), e.getOld.getId, e.getNpc.getComposition.getId))
		eventBus.addTransformation((e: rlEvents.PlayerSpawned) => {
			cachedNpcLocations.put(e.getActor, e.getActor.getWorldLocation)
			KEvent.KPlayerSpawned(KActor(e.getPlayer))
		})
		eventBus.addTransformation((e: rlEvents.PlayerDespawned) => {
			cachedNpcAnimations.remove(e.getActor)
			cachedNpcLocations.remove(e.getActor)
			KEvent.KPlayerDespawned(KActor(e.getPlayer))
		})
		eventBus.addTransformation((e: rlEvents.ActorDeath) => {
			e.getActor match {
				case npc: RlNpc => KEvent.KNpcDied(KActor(npc))
				case player: RlPlayer => {
					if (player != client.getLocalPlayer) KEvent.KPlayerDied(KActor(player))
					else KEvent.KLocalPlayerDied
				}
			}
		})

		eventBus.addTransformationOpt((e: rlEvents.AnimationChanged) => {
			val cur = e.getActor.getAnimation
			val old = cachedNpcAnimations.getOrElseUpdate(e.getActor, cur)
			if(old != cur) {
				cachedNpcAnimations.put(e.getActor, cur)
				Option(e.getActor match {
					case npc: RlNpc => KEvent.KNpcAnimationChanged(KNpc(npc), old, cur)
					case player: RlPlayer => {
						if(player != client.getLocalPlayer) KEvent.KPlayerAnimationChanged(KActor(player), old, cur)
						else KEvent.KLocalPlayerAnimationChanged(old, cur)
					}
				})
			} else Option.empty

		})

		eventBus.addTransformation((e: rlEvents.GameObjectSpawned) => {
			val kgo: KGameObject = KTileObject(e.getGameObject)
			KEvent.KGameObjectSpawned(kgo)
		})
		eventBus.addTransformation((e: rlEvents.GameObjectDespawned) => {
			val kgo: KGameObject = KTileObject(e.getGameObject)
			KEvent.KGameObjectDespawned(kgo)
		})

		eventBus.addTransformation((e: rlEvents.GroundObjectSpawned) => {
			val kgo = KTileObject(e.getGroundObject)
			KEvent.KGroundObjectSpawned(kgo)
		})
		eventBus.addTransformation((e: rlEvents.GroundObjectDespawned) => {
			val kgo: KTileObject.KGroundObject = KTileObject(e.getGroundObject)
			KEvent.KGroundObjectDespawned(kgo)
		})

		eventBus.addTransformation((e: rlEvents.WallObjectSpawned) => {
			val kgo: KTileObject.KWallObject = KTileObject(e.getWallObject)
			KEvent.KWallObjectSpawned(kgo)
		})

		eventBus.addTransformation((e: rlEvents.WallObjectDespawned) => {
			val kgo: KTileObject.KWallObject = KTileObject(e.getWallObject)
			KEvent.KWallObjectDespawned(kgo)
		})

		eventBus.addTransformation((e: rlEvents.DecorativeObjectSpawned) => {
			val kgo: KTileObject.KDecorativeObject = KTileObject(e.getDecorativeObject)
			KEvent.KDecorativeObjectSpawned(kgo)
		})

		eventBus.addTransformation((e: rlEvents.DecorativeObjectDespawned) => {
			val kgo: KTileObject.KDecorativeObject = KTileObject(e.getDecorativeObject)
			KEvent.KDecorativeObjectDespawned(kgo)
		})

		eventBus.addSubscriber((e: rlEvents.GameTick) => {
			client.getNpcs.asScala.toList.foreach(npc => {
				val cur = npc.getWorldLocation//.pipe(wp => if (client.getTopLevelWorldView.isInstance) WorldPointUtils.fromInstance(wp)(using client) else wp)
				val old = cachedNpcLocations.getOrElseUpdate(npc, cur)
				if (old != cur) {
					cachedNpcLocations.put(npc, cur)
					val delta: (Int, Int) = (cur.getX - old.getX) -> (cur.getY - old.getY)
					builder.addOne(KEvent.KNpcMoved(KNpc(npc), delta, cur))
				}
			})
			client.getPlayers.asScala.toList.foreach(player => {
				val cur = player.getWorldLocation//.pipe(wp => if (client.getTopLevelWorldView.isInstance) WorldPointUtils.fromInstance(wp)(using client) else wp)
				val old = cachedNpcLocations.getOrElseUpdate(player, cur)
				if (old != cur) {
					cachedNpcLocations.put(player, cur)
					val delta: (Int, Int) = (cur.getX - old.getX) -> (cur.getY - old.getY)
					builder.addOne(
						if(player != client.getLocalPlayer) KEvent.KPlayerMoved(KActor(player), delta, cur)
						else KEvent.KLocalPlayerMoved(delta, cur)
					)
				}
			})

			val events = builder.toList.tap(_ => builder.clear())
			handlers.toSeq.filter(_.isEnabled).foreach(_.handle(events))
		}, -100)
	}

	def stop()(using eventBus: EventBus): Unit = {
		builder.clear()
		subscribers.foreach(eventBus.unregister)
		subscribers.clear()
		handlers.clear()
	}

	def register(op: List[KEvent] => Unit, startEnabled: Boolean = true): EventManager.EventsOp = {
		val toRet = new EventManager.EventsOp {
			override def clientThread: ClientThread = manager.clientThread
			override def client: Client = manager.client
			override def handle(events: List[KEvent]): Unit = op(events)
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

