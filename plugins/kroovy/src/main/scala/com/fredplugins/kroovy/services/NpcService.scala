package com.fredplugins.kroovy.services

import com.fredplugins.kroovy.KroovyConfig
import com.fredplugins.kroovy.api.{LazyPublisher, Publisher, Reactor}
import net.runelite.api.{NPCComposition, Player}
//import com.fredplugins.kroovy.services.NpcEventFilter
import com.fredplugins.kroovy.swing.{ObservableSetEvent, ObservableSortedMap, ObservableSortedSet}
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
type NpcEventFilterType = Set[Int]/*NpcEventFilter[? <: EnumEntry & {def ids: Set[Int]}]*/

trait NpcListener {
	def onSpawned(npc: NPC): Unit = {}
	def onDespawned(npc: NPC): Unit = {}
	def onAnimationChanged(npc: NPC, old: Int, cur: Int): Unit = {}
	def onCompositionChanged(npc: NPC, old: NPCComposition, cur: NPCComposition): Unit = {}
	def onDeath(npc: NPC): Unit = {}
}

trait NpcServiceApi extends ServiceBase {
//	case class NpcFilteredListener(ids: Set[Int], listener: NpcListener) {
//
//	}
	given Ordering[(Set[Int], NpcListener)] = Ordering.by((i: (Set[Int], NpcListener)) => i._1.min)
	protected val observableSortedSet: ObservableSortedSet[(Set[Int], NpcListener)] = ObservableSortedSet[(Set[Int], NpcListener)]()

	private var lookupMap: Map[Int, Set[NpcListener]] = Map.empty

	observableSortedSet.reactions += {
		case e: ObservableSetEvent => {
			log.debug("NpcServiceApi {}", e)
			lookupMap = observableSortedSet.toSet.groupBy(_._1).flatMap {
				case (npcs, listeners) => npcs.map(i => (i, listeners.map(_._2)))
			}
		}
	}

	protected def get(npc: NPC): Set[NpcListener] = lookupMap.getOrElse(npc.getId, Set.empty[NpcListener])

	def register(ids: Set[Int])(listener: NpcListener): NpcListener = {
		assert(!observableSortedSet.exists(_._2 == listener))
		observableSortedSet.addOne(ids -> listener)
		listener
	}

	def forget(listener: NpcListener): Boolean = {
//		assert(observableSortedSet.count(_._2 == listener) == 1)
		val toRemove = observableSortedSet.toSet.filter(_._2 == listener)
		toRemove.forall(tr => observableSortedSet.remove(tr))
		toRemove.nonEmpty
//		observableSortedSet.remove(listener)
	}

	override def teardown(): Unit = {
//		observableSortedSet.clear()
		observableSortedSet.clear()
	}
}

@Singleton
class NpcService @Inject()(val client: Client, val eventBus: EventBus, val clientThread: ClientThread, val config: KroovyConfig) extends NpcServiceApi {

//	val publisher: Publisher[ObservableSetEvent] = new Publisher[ObservableSetEvent]{
//		reactions += {
//			case e => log.debug("npcServiceReaction {}", e)
//		}
//	}

	private object NpcEventListener {
		private var npcToAnimationId: Map[NPC, Int] = Map.empty
		@Subscribe
		def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
			val matched = get(npcSpawned.getNpc)
			if(matched.nonEmpty) {
				npcToAnimationId = npcToAnimationId.toSeq.appended(npcSpawned.getNpc -> npcSpawned.getNpc.getAnimation).toMap
			}
			matched.foreach(l => l.onSpawned(npcSpawned.getNpc))
		}
		@Subscribe
		def onNpcDespawned(npcDespawned: NpcDespawned): Unit = {
			val matched = get(npcDespawned.getNpc)
			if (matched.nonEmpty) {
				npcToAnimationId = npcToAnimationId.removed(npcDespawned.getNpc)
			}
			matched.foreach(l => l.onDespawned(npcDespawned.getNpc))
		}
			@Subscribe
			def onNpcChanged(npcChanged: NpcChanged): Unit = {
				val matched = get(npcChanged.getNpc)
				if(matched.nonEmpty) {
					val old = npcChanged.getOld
					val cur = npcChanged.getNpc.getComposition
					if(old != cur) {
						matched.foreach(l => l.onCompositionChanged(npcChanged.getNpc, old, cur))
					}
				}
			}
			@Subscribe
			def onAnimationChanged(animationChanged: AnimationChanged): Unit = {
				animationChanged.getActor match {
					case npc: NPC => {
						val matched = get(npc)
						val oldAnimation = npcToAnimationId.getOrElse(npc, -1)
						val newAnimation = npc.getAnimation
						npcToAnimationId = npcToAnimationId.updated(npc, newAnimation)
						if(oldAnimation != newAnimation) {
							matched.foreach(l => l.onAnimationChanged(npc, oldAnimation, newAnimation))
						}
					}
					case _ =>
				}
			}
			@Subscribe
			def onActorDeath(actorDeath: ActorDeath): Unit = {
				actorDeath.getActor match {
					case npc: NPC => get(npc).foreach(l => {l.onDeath(npc)})
					case _ =>
				}
			}
	}

	override def init(): Unit = {
		eventBus.register(NpcEventListener)
	}
	override def teardown(): Unit = {
		eventBus.unregister(NpcEventListener)
		super.teardown()
	}
}
