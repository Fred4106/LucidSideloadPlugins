package com.fredplugins.kroovy.services

import com.fredplugins.kroovy.KroovyConfig
import com.fredplugins.kroovy.services.NpcFilter.NpcInstance
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Client, NPC}
import net.runelite.api.events.{ActorDeath, AnimationChanged, NpcChanged, NpcDespawned, NpcSpawned}
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.game.ItemManager
import scala.util.chaining.*
import scala.collection.mutable
import scala.swing.Publisher
import scala.swing.event.ListChanged

abstract class NpcEventListener {
	@Subscribe
	def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {}
	@Subscribe
	def onNpcDespawned(npcDespawned: NpcDespawned): Unit = {}
	@Subscribe
	def onNpcChanged(npcChanged: NpcChanged): Unit = {}
	@Subscribe
	def onAnimationChanged(animationChanged: AnimationChanged): Unit = {}
	@Subscribe
	def onActorDeath(actorDeath: ActorDeath): Unit = {}
}

object NpcFilter {
	trait NpcInstance {
		def wrapped: NPC
	}
}
trait NpcFilter[Instance](val ids: Int*) extends PartialFunction[NPC, Instance] {
	case class Spawned(result: Instance)
	case class Despawned(result: Instance)

	override def isDefinedAt(x: NPC): Boolean = Option(x).exists(n => ids.contains(n.getId))
	override def apply(v1: NPC): Instance = Option(v1).filter(isDefinedAt).map(transform).get

	def transform(npc: NPC): Instance

	def apply(v1: NpcSpawned): this.Spawned = Spawned(apply(v1.getNpc))
	def apply(v1: NpcDespawned): this.Despawned = Despawned(apply(v1.getNpc))
}

case class NpcFilterAdded(filter: NpcFilter[?], idx: Int) extends scala.swing.event.Event
case class NpcFilterRemoved(filter: NpcFilter[?], idx: Int) extends scala.swing.event.Event
case object NpcFiltersCleared extends scala.swing.event.Event

trait SetWrapped(val wrapped: mutable.SortedSet[NpcFilter[?]]) extends mutable.SortedSet[NpcFilter[?]] with Publisher {
	override def addOne(elem: NpcFilter[_]): SetWrapped.this.type = {
		wrapped.addOne(elem)
		this.publish(NpcFilterAdded(elem, wrapped.toSeq.indexOf(elem)))
		this
	}
	override def subtractOne(elem: NpcFilter[_]): SetWrapped.this.type = {
		val idx = wrapped.toSeq.indexOf(elem)
		wrapped.subtractOne(elem)
		this.publish(NpcFilterRemoved(elem, idx))
		this
	}
	override def clear(): Unit = {
		wrapped.clear()
		this.publish(NpcFiltersCleared)
		this
	}
	override def iteratorFrom(start: NpcFilter[_]): Iterator[NpcFilter[_]] = wrapped.iteratorFrom(start)
	override def contains(elem: NpcFilter[_]): Boolean = wrapped.contains(elem)
	override def iterator: Iterator[NpcFilter[_]] = wrapped.iterator
	override def ordering: Ordering[NpcFilter[_]] = wrapped.ordering
	override def rangeImpl(from: Option[NpcFilter[_]], until: Option[NpcFilter[_]]): mutable.SortedSet[NpcFilter[_]] =
		wrapped.rangeImpl(from, until)
}

@Singleton
class NpcService @Inject()(val client: Client, val eventBus: EventBus, val clientThread: ClientThread, val
config: KroovyConfig) extends ServiceBase {

	private val setOp: mutable.SortedSet[NpcFilter[?]] = new SetWrapped(mutable.SortedSet.apply[NpcFilter[?]]()(using
		Ordering.by((i: NpcFilter[?]) => i.ids.min))) {}

	private val eventListener: NpcEventListener = new NpcEventListener {
		override def onNpcSpawned(spawned: NpcSpawned): Unit = {
			val matched = setOp.toSeq.filter(_.isDefinedAt(spawned.getNpc))
			for (npcFilter <- matched) {
				npcFilter.apply(spawned).tap(s => log.debug("filter {} transformed to {}", npcFilter, s))
			}
		}
		override def onNpcDespawned(despawned: NpcDespawned): Unit = {
			val matched = setOp.toSeq.filter(_.isDefinedAt(despawned.getNpc))
			for (npcFilter <- matched) {
				npcFilter.apply(despawned).tap(s => log.debug("filter {} transformed to {}", npcFilter, s))
			}
		}
	}

	def register(filter: NpcFilter[?]): Unit = {
		setOp.addOne(filter)
	}
	def forget(filter: NpcFilter[?]): Unit = {
		setOp.remove(filter)
	}
	override def init(): Unit = {
		eventBus.register(eventListener)
	}
	override def teardown(): Unit = {
		eventBus.unregister(eventListener)
		setOp.clear()
	}
}
