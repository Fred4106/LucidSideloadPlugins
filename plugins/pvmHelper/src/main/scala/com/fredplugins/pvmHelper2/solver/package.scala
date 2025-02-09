package com.fredplugins.pvmHelper2

import net.runelite.api.{Actor, NPC, Player}
import net.runelite.api.events.{AnimationChanged, NpcChanged, NpcDespawned, NpcSpawned}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import org.slf4j.Logger

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

package object solver {
	type NpcRuneliteEvent = NpcSpawned | NpcDespawned | NpcChanged | AnimationChanged

	trait NpcEventType {
		this: Product =>
		def name: String = this.productPrefix.stripPrefix("Npc").stripSuffix("Event")
		type RuneliteEvent
		def extract(e: RuneliteEvent): Option[(this.type, NPC)]
	}

	case object NpcSpawnedEvent extends NpcEventType {
		override type RuneliteEvent = NpcSpawned
		override def extract(e: NpcSpawned): Option[(NpcSpawnedEvent.type, NPC)] = Option((this, e.getNpc))
	}

	case object NpcDespawnedEvent extends NpcEventType {
		override type RuneliteEvent = NpcDespawned
		override def extract(e: NpcDespawned): Option[(NpcDespawnedEvent.type, NPC)] = Option((this, e.getNpc))
	}

	case object NpcAnimationEvent extends NpcEventType {
		override type RuneliteEvent = AnimationChanged
		override def extract(e: AnimationChanged): Option[(NpcAnimationEvent.type, NPC)] = {
			e.getActor match {
				case npc: NPC => Some((this, npc))
				case _ => None
			}
		}
	}

	trait NpcFilterTrait {
		this: Product =>
		def ids: Seq[Int]
		def name: String
		def filter[E <: NpcRuneliteEvent](event: E): Option[(String, NpcEventType, NPC)]
	}

	class NpcFilterSingle(val ids: Int *) extends NpcFilterTrait {
		self: Product =>
		def name: String = this.productPrefix
		def filter[E <: NpcRuneliteEvent](event: E): Option[(String, NpcEventType, NPC)] = {
			Option(event).collect {
				case e: NpcSpawnedEvent.RuneliteEvent => NpcSpawnedEvent.extract(e)
				case e: NpcDespawnedEvent.RuneliteEvent => NpcDespawnedEvent.extract(e)
				case e: NpcAnimationEvent.RuneliteEvent => NpcAnimationEvent.extract(e)
			}.flatten.filter(j => ids.contains(j._2.getId)).map(x => {
				(name, x._1, x._2)
			})
		}
	}

	class NpcFilterMulti(val children: NpcFilterTrait *) extends NpcFilterTrait {
		this: Product =>
		override def ids: Seq[Int] = children.flatMap(_.ids).distinct
		override def name: String = this.productPrefix + children.map(_.name).mkString("[", ", ", "]")
		override def filter[E <: NpcRuneliteEvent](event: E): Option[(String, NpcEventType, NPC)] = {
			children.flatMap(f => f.filter[E](event)).headOption
		}
	}
}
