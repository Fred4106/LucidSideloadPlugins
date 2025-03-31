package com.fredplugins.kroovy.api

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

import scala.compiletime.uninitialized
import scala.util.NotGiven
//Map A -> Gauntlet
//Map A -> Scurrius
//Map B -> Zulrah
//Map C -> Gauntlet
//Get[A] = Set(Gauntlet, Scurrius)
//Get[Gauntlet] = Set(A, B)
//Put(key: KEY)(values: VALUE *): Unit =
//Remove(key: KEY)(values: VALUE *): Unit =
//RemoveAll(key: KEY): Unit
trait MultiBiMapOps[Key, Value](using ev: NotGiven[Key =:= Value], ev2: NotGiven[Key <:< Value], ev3: NotGiven[Value <:< Key]) {
	def keys: Set[Key]
	def values: Set[Value]
	def forwards: Map[Key, Set[Value]]
	def reverse: Map[Value, Set[Key]]
//	def get(key: Value): Set[Value]
//	def getValue(value: Value): Set[Key]
	def put(key: Key)(values: Value *): Boolean
	def remove(key: Key)(values: Value *): Boolean
	def removeAll(key: Key): Boolean
	def removeIf(key: Key)(cond: Value => Boolean): Boolean
}
class MultiBiMap[Key, Value] extends MultiBiMapOps[Key, Value] {
	private val backing = scala.collection.mutable.HashSet.empty[(Key, Value)]
	def entries: Set[(Key, Value)] = backing.toSet

	override def forwards: Map[Key, Set[Value]] = entries.groupMap(_._1)(_._2)
	override def reverse: Map[Value, Set[Key]] = entries.groupMap(_._2)(_._1)

	override def put(key: Key)(values: Value*): Boolean = {
//		assert(!entries.exists(e => e._1 == key && values.contains(e._2)))
		values.distinct.map(v => key -> v).map(backing.add).reduce(_ || _)
	}
	override def remove(key: Key)(values: Value*): Boolean = {
//		assert(entries.exists(e => e._1 == key && values.contains(e._2)))
		values.distinct.map(v => key -> v).map(backing.remove).reduce(_ || _)
	}
	override def removeAll(key: Key): Boolean = {
		entries.collect{
			case e@(`key`,  _) => e
		}.forall(backing.remove)
	}
	override def removeIf(key: Key)(cond: Value => Boolean): Boolean = {
		entries.collect {
			case e@(`key`, value) if cond(value) => e
		}.forall(backing.remove)
	}
	override def keys: Set[Key] = entries.map(_._1)
	override def values: Set[Value] = entries.map(_._2)
}
