package com.fredplugins.kroovy.services.npc

import com.fredplugins.kroovy.KroovyConfig
import com.fredplugins.kroovy.api.{LazyPublisher, Publisher, Reactor}
import com.fredplugins.kroovy.services.ServiceBase
import net.runelite.api.{NPCComposition, Player}

import scala.reflect.ClassTag
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

//trait NpcListener {
//	def onSpawned(npc: NPC): Unit = {}
//	def onDespawned(npc: NPC): Unit = {}
//	def onAnimationChanged(npc: NPC, old: Int, cur: Int): Unit = {}
//	def onCompositionChanged(npc: NPC, old: NPCComposition, cur: NPCComposition): Unit = {}
//	def onDeath(npc: NPC): Unit = {}
//}
case class NpcFilteredListener[-E <: SNpcEvent : ClassTag as cTag](ids: Set[Int], op: E => Unit) {
	def matches[E2 <: SNpcEvent : ClassTag as cTag2](npc: NPC): Boolean = {
		cTag2.runtimeClass == cTag.runtimeClass && ids.contains(npc.getId)
	}
}

trait NpcServiceApi extends ServiceBase {
//	given Ordering[(Set[Int], SNpcOp[?])] = Ordering.by((i: (Set[Int], SNpcOp[?])) => i._1.min)
//	protected val observableSortedSet: ObservableSortedSet[(Set[Int], SNpcOp[?])] = ObservableSortedSet[(Set[Int], SNpcOp[?])]()
	private var lookupMap2: Map[Set[Int], Seq[NpcFilteredListener[?]]] = Map.empty

//	private var lookupMap: Seq[NpcFilteredListener[?]] = Seq.empty

//	observableSortedSet.reactions += {
//		case e: ObservableSetEvent => {
//			log.debug("NpcServiceApi {}", e)
//			lookupMap = observableSortedSet.toSet.groupMap(_._1)(_._2)
//		}
//	}

	protected def get[E <: SNpcEvent : ClassTag as cTag](npc: NPC): Set[NpcFilteredListener[E]] = {
		val l = lookupMap2.keySet.filter(_.contains(npc.getId)).flatMap(lookupMap2(_))
			.flatMap {
				case x if x.matches[E](npc) => Seq[NpcFilteredListener[E]](x.asInstanceOf[NpcFilteredListener[E]])
				case _ => Seq.empty
			}
		l
//		lookupMap.flatMap{
//			case x if x.matches[E](npc) => Seq(x.asInstanceOf[NpcFilteredListener[E]])
//			case _ => Seq.empty
//		}
	}

	def register[E <: SNpcEvent : ClassTag as cTag](ids: Set[Int])(op: E => Unit): NpcFilteredListener[E] = {
		val listener = new NpcFilteredListener[E](ids, op)
//		lookupMap = lookupMap.appended(listener)

		lookupMap2 = lookupMap2.updatedWith(ids)(remap => {
			Option(remap match {
				case Some(existing)  => existing.appended(listener)
				case None =>  Seq(listener)
			}).filter(_.nonEmpty)
		})
		listener
	}

	def forget[E <: SNpcEvent : ClassTag as cTag](listener: NpcFilteredListener[E]): Boolean = {
		val containsListener = lookupMap2.exists(_._2.contains(listener))
		if(containsListener) {
			lookupMap2 = lookupMap2.updatedWith(listener.ids)(remap => {
				Option(remap match {
					case Some(existing) => existing.partition(_ == listener)._2
					case None => Seq.empty
				}).filter(_.nonEmpty)
			})
		}
//		val (toRemove, toKeep)  = lookupMap.partition(_ == listener)
//		lookupMap = toKeep
		containsListener// && toRemove.nonEmpty
	}

	override def teardown(): Unit = {
		//lookupMap = Seq.empty
		lookupMap2 = Map.empty
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
		given ClientThread = clientThread
		private var npcToAnimationId: Map[NPC, Int] = Map.empty
		@Subscribe
		def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
			val matched = get[SNpcEvent.Spawned](npcSpawned.getNpc)
			if(matched.nonEmpty) {
				npcToAnimationId = npcToAnimationId.toSeq.appended(npcSpawned.getNpc -> npcSpawned.getNpc.getAnimation).toMap

				val e = SNpcEvent.Spawned(npcSpawned.getNpc)
				matched.foreach(l => l.op(e))
			}
		}
		@Subscribe
		def onNpcDespawned(npcDespawned: NpcDespawned): Unit = {
			val matched = get[SNpcEvent.Despawned](npcDespawned.getNpc)
			if (matched.nonEmpty) {
				npcToAnimationId = npcToAnimationId.removed(npcDespawned.getNpc)
				val e = SNpcEvent.Despawned(npcDespawned.getNpc)
				matched.foreach(l => l.op.apply(e))
			}
		}
		@Subscribe
		def onNpcChanged(npcChanged: NpcChanged): Unit = {
			val n = npcChanged.getNpc
			val matched = get[SNpcEvent.CompositionChanged](n)
			if(matched.nonEmpty) {
				val old = npcChanged.getOld.getId
				val e = SNpcEvent.CompositionChanged(n, old)
				matched.foreach(_.op(e))
			}
		}
			@Subscribe
			def onAnimationChanged(animationChanged: AnimationChanged): Unit = {
				animationChanged.getActor match {
					case npc: NPC => {
						val matched = get[SNpcEvent.AnimationChanged](npc)
						val oldAnimation = npcToAnimationId.getOrElse(npc, -1)
						val newAnimation = npc.getAnimation
						npcToAnimationId = npcToAnimationId.updated(npc, newAnimation)
						if(oldAnimation != newAnimation) {
							val e = SNpcEvent.AnimationChanged(npc, oldAnimation)
							matched.foreach(l => l.op(e))
						}
					}
					case _ =>
				}
			}
			@Subscribe
			def onActorDeath(actorDeath: ActorDeath): Unit = {
				actorDeath.getActor match {
					case npc: NPC => {
						val matched = get[SNpcEvent.Died](npc)
						if(matched.nonEmpty) {
							val e = SNpcEvent.Died(npc)
							matched.foreach(_.op(e))
						}
					}
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
