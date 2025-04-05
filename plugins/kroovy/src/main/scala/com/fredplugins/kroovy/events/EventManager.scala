package com.fredplugins.kroovy.events

import com.fredplugins.common.utils.{TWorldPoint, WorldPointUtils}
import com.fredplugins.kroovy.events.KEvent.*

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Client, Actor as RlActor, NPC as RlNpc, Player as RlPlayer, events as rlEvents}
import net.runelite.api.coords.WorldPoint as RlWorldPoint

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
			builder: mutable.Seq[KEvent],
			subs: mutable.Seq[EventBus.Subscriber]
		): Unit = {
			subs.appended(
				bus.register[T](
					ct.runtimeClass.asInstanceOf[Class[T]],
					(e: T) => {
						builder.appended(transform(e))
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
			builder: mutable.Seq[KEvent],
			subs: mutable.Seq[EventBus.Subscriber]
		): Unit = {
			subs.appended(
				bus.register[T](
					ct.runtimeClass.asInstanceOf[Class[T]],
					(e: T) => {
						transform(e).foreach(builder.appended(_))
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
			subs:  mutable.Seq[EventBus.Subscriber]
		): Unit = {
			subs.appended(
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

	private val cachedAnimations: mutable.HashMap[RlActor, Int]        = mutable.HashMap.empty[RlActor, Int]
	private val cachedLocations: mutable.HashMap[RlActor, RlWorldPoint] = mutable.HashMap.empty[RlActor, RlWorldPoint]

	def start()(using eventBus: EventBus): Unit = {
		eventBus.addTransformation((e: rlEvents.NpcSpawned) => {
			cachedLocations.put(e.getActor, e.getActor.getWorldLocation.pipe(wp => {
				if(client.getTopLevelWorldView.isInstance) WorldPointUtils.fromInstance(wp)(using client)
				else wp
			}))
			KEvent.KNpcSpawned(KNpc(e.getNpc))
		})
		eventBus.addTransformation((e: rlEvents.NpcDespawned) => {
			cachedAnimations.remove(e.getActor)
			cachedLocations.remove(e.getActor)
			KEvent.KNpcDespawned(KNpc(e.getNpc))
		})
		eventBus.addTransformation((e: rlEvents.NpcChanged) => KEvent.KNpcChanged(KNpc(e.getNpc), e.getOld.getId, e.getNpc.getComposition.getId))
		eventBus.addTransformation((e: rlEvents.PlayerSpawned) => {
			cachedLocations.put(e.getActor, e.getActor.getWorldLocation.pipe(wp => {
				if (client.getTopLevelWorldView.isInstance) WorldPointUtils.fromInstance(wp)(using client)
				else wp
			}))
			KEvent.KPlayerSpawned(KPlayer(e.getPlayer))
		})
		eventBus.addTransformation((e: rlEvents.PlayerDespawned) => {
			cachedAnimations.remove(e.getActor)
			cachedLocations.remove(e.getActor)
			KEvent.KPlayerDespawned(KPlayer(e.getPlayer))
		})
		eventBus.addTransformation((e: rlEvents.ActorDeath) => {
			e.getActor match {
				case npc: RlNpc => KEvent.KNpcDied(KNpc(npc))
				case player: RlPlayer => {
					if (player != client.getLocalPlayer) KEvent.KPlayerDied(KPlayer(player))
					else KEvent.KLocalPlayerDied

				}
			}
		})
		eventBus.addTransformationOpt((e: rlEvents.AnimationChanged) => {
			val old = cachedAnimations.getOrElseUpdate(e.getActor, -1)
			val cur = e.getActor.getAnimation
			Option.when(old != cur) {
				cachedAnimations.update(e.getActor, cur)
				e.getActor match {
					case npc: RlNpc => KEvent.KNpcAnimationChanged(KNpc(npc), old, cur)
					case player: RlPlayer => {
						if(player != client.getLocalPlayer) KEvent.KPlayerAnimationChanged(KPlayer(player), old, cur)
						else KEvent.KLocalPlayerAnimationChanged(old, cur)
					}
				}
			}
		})

		eventBus.addSubscriber((e: rlEvents.GameTick) => {
			client.getNpcs.asScala.toList.foreach(npc => {
				val cur = npc.getWorldLocation.pipe(wp => if (client.getTopLevelWorldView.isInstance) WorldPointUtils.fromInstance(wp)(using client) else wp)
				val old = cachedLocations.getOrElseUpdate(npc, cur)
				if (old != cur) {
					cachedLocations.put(npc, cur)
					val delta: (Int, Int) = (cur.getX - old.getX) -> (cur.getY - old.getY)
					builder.addOne(KEvent.KNpcMoved(KNpc(npc), delta, cur))
				}
			})
			client.getPlayers.asScala.toList.foreach(player => {
				val cur = player.getWorldLocation.pipe(wp => if (client.getTopLevelWorldView.isInstance) WorldPointUtils.fromInstance(wp)(using client) else wp)
				val old = cachedLocations.getOrElseUpdate(player, cur)
				if (old != cur) {
					cachedLocations.put(player, cur)
					val delta: (Int, Int) = (cur.getX - old.getX) -> (cur.getY - old.getY)
					builder.addOne(
						if(player != client.getLocalPlayer) KEvent.KPlayerMoved(KPlayer(player), delta, cur)
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

