package com.fredplugins.kroovy.events

import ethanApiPlugin.EthanApiPlugin

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import net.runelite.api.{HeadIcon, Actor as RlActor, NPC as RlNpc, Player as RlPlayer}

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.reflect.Selectable.reflectiveSelectable
import scala.reflect.{TypeTest, Typeable}

sealed trait KActor {
	type Wrapped <: RlActor & Matchable
	def wrapped: Wrapped
	def id: Int = {
		wrapped match {
			case n: RlNpc => n.getId
			case p: RlPlayer=> p.getId
		}
	}
	def level: Int = wrapped.getCombatLevel// = wrapped.getCombatLevel
	def name: String = wrapped.getName// = wrapped.getName
}

object KActor {
	private val actorToKActorMap: scala.collection.mutable.HashMap[RlActor, KActor] = mutable.HashMap.empty[RlActor, KActor]

	sealed case class KNpc(wrapped: RlNpc) extends KActor {
		override type Wrapped = RlNpc
		def index: Int = wrapped.getIndex

		override def toString: String = s"Npc[${index -> id}](name=${name})"
	}
	sealed case class KPlayer(wrapped: RlPlayer) extends KActor {
		override type Wrapped = RlPlayer
		def team: Int = wrapped.getTeam

		override def toString: String = s"Player[${id}](name=\"${name}\", team=${team})"
	}

	inline transparent def apply[X <: RlActor & Matchable](inline rlActor: X): KNpc | KPlayer = {
		inline rlActor match {
			case npc: RlNpc => actorToKActorMap.getOrElseUpdate(npc, KNpc(npc)).asInstanceOf[KNpc]
			case player: RlPlayer => actorToKActorMap.getOrElseUpdate(player, KPlayer(player)).asInstanceOf[KPlayer]
			//			case u => log.warn("")
		}
	}
}