package com.fredplugins.kroovy.services

import enumeratum.EnumEntry
import net.runelite.api.NPC
import net.runelite.api.coords.WorldPoint

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.language.reflectiveCalls

abstract class TaggedNpc[T <: EnumEntry with {def ids: Set[Int]}](val tag: T, wrapped: NPC, val source: enumeratum.Enum[_ <: T]) {
	def group: enumeratum.Enum[_ <: T] = source
	def worldLocation: WorldPoint = wrapped.getWorldLocation
	def index: Int = wrapped.getIndex
	def animation: Int = wrapped.getAnimation
	def name: String = wrapped.getName

	override def toString(): String = {
		s"TaggedNpc[${tag.entryName}](wrapped = ${Integer.toHexString(wrapped.hashCode())})"
	}
}


abstract class NpcEventFilter[T <: EnumEntry with {def ids: Set[Int]}](val source: enumeratum.Enum[_<:T]) {
	def getTag(in: Int): Option[EnumEntry with {def ids: Set[Int]}] = source.values.find(_.ids.contains(in))
//	source.values.find(_.ids.contains(

	//	def IdToTag =
	val allIds: Seq[Int] = source.values.flatMap(_.ids).sorted.distinct

	def transform(npc: NPC): Option[TaggedNpc[_ <: EnumEntry with {def ids: Set[Int]}]] = {
		getTag(npc.getId).map(e => new TaggedNpc(e, npc, source) {

		})
	}
}
