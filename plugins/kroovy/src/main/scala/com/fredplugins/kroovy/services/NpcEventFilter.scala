package com.fredplugins.kroovy.services

import net.runelite.api.NPC
import net.runelite.api.coords.WorldPoint

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

trait RootNpcInstance {
	val wrapped: NPC
	def worldLocation: WorldPoint = wrapped.getWorldLocation
	def index: Int = wrapped.getIndex
	def animation: Int = wrapped.getAnimation
	def name: String = wrapped.getName
}


//case class Despawned(instance: NpcEventFilter#Instance) extends NpcFilterEvent

abstract class NpcEventFilter(val ids: Int *) {filter =>
	type Instance <: RootNpcInstance
	def transform(npc: NPC): Instance
//	sealed class Instance(override val wrapped: NPC) extends RootNpcInstance {}

	sealed trait FilterEvent extends scala.swing.event.Event {
		def source: NpcEventFilter
		def element: filter.Instance
	}
	case class Spawned(source: NpcEventFilter, element: Instance) extends FilterEvent {}
	case class Despawned(source: NpcEventFilter, element: Instance) extends FilterEvent {}

	def unapply(in: NPC): Boolean = {
		ids.contains(in.getId)
	}

	object Instance {
		def unapply(in: NPC): Option[Instance] = Option.when(ids.contains(in.getId))(transform(in))
	}
}
