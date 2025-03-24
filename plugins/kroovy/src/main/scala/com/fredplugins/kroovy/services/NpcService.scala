package com.fredplugins.kroovy.services

import com.fredplugins.kroovy.KroovyConfig
import com.fredplugins.kroovy.api.{LazyPublisher, Publisher}
import com.fredplugins.kroovy.services.NpcEventFilter
import com.fredplugins.kroovy.swing.{ObservableSetEvent, ObservableSortedSet}
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Client, NPC}
import net.runelite.api.events.{ActorDeath, AnimationChanged, NpcChanged, NpcDespawned, NpcSpawned}
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.game.ItemManager

import scala.Ordering
import scala.util.chaining.*
import scala.collection.mutable
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
	given Ordering[NpcEventFilter] = Ordering.by((i: NpcEventFilter) => i.ids.min)
	protected val observableSortedSet: ObservableSortedSet[NpcEventFilter] = ObservableSortedSet.apply[NpcEventFilter]()

	def register(filter: NpcEventFilter): Unit = {
		observableSortedSet.addOne(filter)
	}
	def forget(filter: NpcEventFilter): Unit = {
		observableSortedSet.remove(filter)
	}
	override def teardown(): Unit = {
		observableSortedSet.clear()
	}
}

@Singleton
class NpcService @Inject()(val client: Client, val eventBus: EventBus, val clientThread: ClientThread, val config: KroovyConfig) extends NpcServiceApi {

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
//	}

	private object NpcEventListener extends Publisher[FilterEvent]{
		@Subscribe
		def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
			val idToFind = npcSpawned.getNpc.getId
			val matched = observableSortedSet.all().filter(_.ids.contains(idToFind))
			matched.flatMap(m => {
				m.Instance.unapply(npcSpawned.getNpc).map(mi => Spawned(m, mi))
			}).foreach(spawned => publish(spawned))
		}
		@Subscribe
		def onNpcDespawned(npcDespawned: NpcDespawned): Unit = {
			val idToFind = npcDespawned.getNpc.getId
			val matched = observableSortedSet.all().filter(_.ids.contains(idToFind))
			matched.flatMap(m => {
				m.Instance.unapply(npcDespawned.getNpc).map(m1 => Despawned(m, m1))
			}).foreach(despawned => publish(despawned))
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
