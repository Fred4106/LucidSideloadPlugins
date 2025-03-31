package com.fredplugins.kroovy.services.npc

import com.fredplugins.kroovy.KroovyConfig
import com.fredplugins.kroovy.api.{LazyPublisher, Publisher, Reactor}
import com.fredplugins.kroovy.services.ServiceBase
import com.fredplugins.kroovy.services.npc.NpcServiceApi.NpcServiceEvent
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.GameTick
import net.runelite.api.{NPCComposition, Player}

import javax.swing.SwingUtilities
import scala.reflect.ClassTag
//import com.fredplugins.kroovy.services.NpcEventFilter
import com.fredplugins.kroovy.swing.{ObservableSetEvent, ObservableSortedMap, ObservableSortedSet}
import enumeratum.EnumEntry
import net.runelite.client.RuneLite
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Client, NPC}
import net.runelite.api.events.{ActorDeath, AnimationChanged, NpcChanged, NpcDespawned, NpcSpawned}
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
case class NpcFilteredListener[E <: SNpcEvent : ClassTag as cTag](ids: Set[Int], op: E => Unit) {
	val tag: ClassTag[E] = cTag
//	def matches[E2 <: SNpcEvent : ClassTag as cTag2](npc: NPC): Option[NpcFilteredListener[E2]] = {
//		Option.when(
//			cTag2.runtimeClass == cTag.runtimeClass && ids.contains(npc.getId)
//		)(this.asInstanceOf[NpcFilteredListener[E2]])
//	}
	def tryCast[E2 <: SNpcEvent : ClassTag as cTag2]: Option[NpcFilteredListener[E2]] = {
		Option.when(
			cTag2.runtimeClass == cTag.runtimeClass
		)(asInstanceOf[NpcFilteredListener[E2]])
	}
}
object NpcServiceApi {
	sealed trait NpcServiceEvent extends swing.event.Event {}
	case object ListChanged extends NpcServiceEvent
	case class Log(str: String) extends NpcServiceEvent
}

sealed trait NpcServiceApi extends ServiceBase with swing.Publisher {
	private var lookupMap2: Map[Set[Int], Seq[NpcFilteredListener[? <: SNpcEvent]]] = Map.empty

	protected inline def get[E <: SNpcEvent : ClassTag as cTag](npc: NPC): Set[NpcFilteredListener[E]] = {
		val l = lookupMap2.keySet.filter(_.contains(npc.getId)).flatMap(lookupMap2(_))
			.flatMap(
				x => x.tryCast[E]
			)
		l
	}

	def all: Seq[NpcFilteredListener[? <: SNpcEvent]] = lookupMap2.values.flatten.toSeq.distinct

	def register[E <: SNpcEvent : ClassTag as cTag](ids: Set[Int], op: E => Unit): NpcFilteredListener[E] = {
		val listener = new NpcFilteredListener[E](ids, op)
		publish(NpcServiceApi.Log(s"Registering ${listener}"))
//		lookupMap = lookupMap.appended(listener)
		lookupMap2 = lookupMap2.updatedWith(ids)(remap => {
			Option(remap match {
				case Some(existing)  => existing.appended(listener)
				case None =>  Seq(listener)
			}).filter(_.nonEmpty)
		})
		publish(NpcServiceApi.ListChanged)
		listener
	}

	def forget[E <: SNpcEvent : ClassTag as cTag](listener: NpcFilteredListener[E]): Boolean = {
		publish(NpcServiceApi.Log(s"Forgetting ${listener}"))
		val containsListener = lookupMap2.exists(_._2.contains(listener))
		if(containsListener) {
			lookupMap2 = lookupMap2.updatedWith(listener.ids)(remap => {
				Option(remap match {
					case Some(existing) => existing.partition(_ == listener)._2
					case None => Seq.empty
				}).filter(_.nonEmpty)
			})
			publish(NpcServiceApi.ListChanged)
		}
//		val (toRemove, toKeep)  = lookupMap.partition(_ == listener)
//		lookupMap = toKeep
		containsListener// && toRemove.nonEmpty
	}

	override def teardown(): Unit = {
		//lookupMap = Seq.empty
		publish(NpcServiceApi.Log("Clearing lookupMap2"))
		lookupMap2 = Map.empty
		publish(NpcServiceApi.ListChanged)
	}
}

@Singleton
class NpcService @Inject()(val client: Client, val eventBus: EventBus, val clientThread: ClientThread) extends NpcServiceApi {

//	val publisher: Publisher[ObservableSetEvent] = new Publisher[ObservableSetEvent]{
//		reactions += {
//			case e => log.debug("npcServiceReaction {}", e)
//		}
//	}

	private object NpcEventListener {
		given ClientThread = clientThread
		private var npcToAnimationId: Map[NPC, Int]       = Map.empty
		private var npcToWorldPoint: Map[NPC, WorldPoint] = Map.empty
		@Subscribe
		def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
			npcToAnimationId = npcToAnimationId.toSeq.appended(npcSpawned.getNpc -> npcSpawned.getNpc.getAnimation).toMap
			npcToWorldPoint = npcToWorldPoint.toSeq.appended(npcSpawned.getNpc -> npcSpawned.getNpc.getWorldLocation).toMap
			val matched = get[SNpcEvent.Spawned](npcSpawned.getNpc)
			if(matched.nonEmpty) {
				val e = SNpcEvent.Spawned(npcSpawned.getNpc)
				publish(NpcServiceApi.Log(s"$e"))
				matched.foreach(l => l.op(e))
			}
		}
		@Subscribe
		def onNpcDespawned(npcDespawned: NpcDespawned): Unit = {
			val matched = get[SNpcEvent.Despawned](npcDespawned.getNpc)
			npcToAnimationId = npcToAnimationId.removed(npcDespawned.getNpc)
			npcToWorldPoint = npcToWorldPoint.removed(npcDespawned.getNpc)
			if (matched.nonEmpty) {
				val e = SNpcEvent.Despawned(npcDespawned.getNpc)
				publish(NpcServiceApi.Log(s"$e"))
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
				publish(NpcServiceApi.Log(s"$e"))
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
							publish(NpcServiceApi.Log(s"$e"))
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
							publish(NpcServiceApi.Log(s"$e"))
							matched.foreach(_.op(e))
						}
					}
					case _ =>
				}
			}
			@Subscribe
			def onGameTick(gt: GameTick): Unit = {
//				val updatedNpcToWorldPoint = this.npcToWorldPoint.keys.map(k => k -> (npcToWorldPoint(k), k.getWorldLocation))
				val matchedToEventSeq = npcToWorldPoint.toSeq.collect {
					case (npc, oldWorldPoint) if oldWorldPoint != npc.getWorldLocation => get[SNpcEvent.Moved](npc) -> SNpcEvent.Moved(npc, oldWorldPoint)
				}.filter(_._1.nonEmpty)
//				publish(NpcServiceApi.Log(s"$e"))
				matchedToEventSeq.foreach {
					case (matched, e) => {
						publish(NpcServiceApi.Log(s"$e"))
						matched.foreach(_.op(e))
					}
				}
//				matched.foreach(m => get[SNpcEvent.Moved](m.npc).foreach(l => l.op(m)))
				npcToWorldPoint = matchedToEventSeq.map(_._2).foldLeft(npcToWorldPoint)((a, b) => a.updated(b.npc, b.cur))
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
