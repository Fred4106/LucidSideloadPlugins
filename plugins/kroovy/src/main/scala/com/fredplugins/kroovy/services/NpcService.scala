package com.fredplugins.kroovy.services

import com.fredplugins.kroovy.KroovyConfig
import com.fredplugins.kroovy.api.{LazyPublisher, Publisher, Reactor}
import com.fredplugins.kroovy.services.NpcEventFilter
import com.fredplugins.kroovy.swing.{ObservableSetEvent, ObservableSortedSet}
import com.google.inject.{Inject, Singleton}
import enumeratum.EnumEntry
import net.runelite.api.{Client, NPC}
import net.runelite.api.events.{ActorDeath, AnimationChanged, NpcChanged, NpcDespawned, NpcSpawned}
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.game.ItemManager

import scala.Ordering
import scala.util.chaining.*
import scala.collection.mutable
import scala.language.reflectiveCalls
import scala.swing.event.ListChanged
import scala.util.Try
//
//trait NpcFilter[Instance](val ids: Int*) extends PartialFunction[NPC, Instance] {
//	sealed trait NpcFilterEvent extends scala.swing.event.Event
//	case class Spawned(result: Instance) extends NpcFilterEvent
//	case class Despawned(result: Instance) extends NpcFilterEvent
//
//	override def isDefinedAt(x: NPC): Boolean = Option(x).exists(n => ids.contains(n.getId))
//	override def apply(v1: NPC): Instance = Option(v1).filter(isDefinedAt).map(transform).get
//
//	def transform(npc: NPC): Instance
//
//	def apply(v1: NpcSpawned): this.Spawned = Spawned(apply(v1.getNpc))
//	def apply(v1: NpcDespawned): this.Despawned = Despawned(apply(v1.getNpc))
//}

trait NpcServiceApi extends ServiceBase {
	given Ordering[NpcEventFilter[? <: EnumEntry & {def ids: Set[Int]}]] = Ordering.by((i: NpcEventFilter[_ <: EnumEntry with {def ids: Set[Int]}]) => i.source.values.minBy(_.ids.min).ids.min)
	protected val observableSortedSet: ObservableSortedSet[NpcEventFilter[? <: EnumEntry & {def ids: Set[Int]}]] = ObservableSortedSet.apply[NpcEventFilter[_ <: EnumEntry with {def ids: Set[Int]}]]()


	def register(filter: NpcEventFilter[_ <: EnumEntry with {def ids: Set[Int]}]): Unit = {
		observableSortedSet.addOne(filter)
	}
	def forget(filter: NpcEventFilter[_ <: EnumEntry with {def ids: Set[Int]}]): Unit = {
		observableSortedSet.remove(filter)
	}
	override def teardown(): Unit = {
		observableSortedSet.clear()
	}
}

@Singleton
class NpcService @Inject()(val client: Client, val eventBus: EventBus, val clientThread: ClientThread, val config: KroovyConfig) extends NpcServiceApi {

	sealed trait FilterEvent {}
	case class DespawnTagged(despawned: TaggedNpc[?]) extends FilterEvent {}
	case class SpawnTagged(spawned: TaggedNpc[?]) extends FilterEvent {}


	val publisher: Publisher[ObservableSetEvent | FilterEvent] = new Publisher[ObservableSetEvent | FilterEvent]{
		reactions += {
			case e => log.debug("npcServiceReaction {}", e)
		}
	}
//	def publish(e: AnyRef): Unit = {
//		e match {
//			case a: (ObservableSetEvent | FilterEvent) =>  publisher.publish(a)
//			case x => log.error("Cant handle {}", x)
//		}
//	}

//	observableSortedSet.reactions += {
//		case u => publisher.publish(u)
//}

	private object NpcEventListener extends Publisher[FilterEvent]{
		@Subscribe
		def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
			val idToFind = npcSpawned.getNpc.getId
			val matched = observableSortedSet.all().find(_.allIds.contains(idToFind))
			matched.flatMap(m => {
				m.transform(npcSpawned.getNpc)
			}).foreach(spawned => publish(SpawnTagged(spawned)))
		}
		@Subscribe
		def onNpcDespawned(npcDespawned: NpcDespawned): Unit = {
			val idToFind = npcDespawned.getNpc.getId
			val matched = observableSortedSet.all().find(_.allIds.contains(idToFind))
			matched.flatMap(m => {
				m.transform(npcDespawned.getNpc)
			}).foreach(despawned => publish(DespawnTagged(despawned)))
		}
		//		@Subscribe
		//		def onNpcChanged(npcChanged: NpcChanged): Unit = {}
		//		@Subscribe
		//		def onAnimationChanged(animationChanged: AnimationChanged): Unit = {}
		//		@Subscribe
		//		def onActorDeath(actorDeath: ActorDeath): Unit = {}
	}

	publisher.listenTo(observableSortedSet)
	publisher.listenTo(NpcEventListener)

	override def init(): Unit = {
		eventBus.register(NpcEventListener)
	}
	override def teardown(): Unit = {
		eventBus.unregister(NpcEventListener)
		super.teardown()
	}
}
