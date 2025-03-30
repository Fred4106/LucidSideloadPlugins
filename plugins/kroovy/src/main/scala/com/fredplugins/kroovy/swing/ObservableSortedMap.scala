package com.fredplugins.kroovy.swing

import com.fredplugins.kroovy.api.{LazyPublisher, Publisher}

import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized


sealed trait ObservableMapEvent {}
object ObservableMapEvents {
	case class Added[K, V](key: K, value: V) extends ObservableMapEvent {}
	case class Removed[K, V](key: K, value: V) extends ObservableMapEvent {}
	case object Cleared extends ObservableMapEvent {}
}

object ObservableSortedMap {
	def apply[K: Ordering as ord, V](source: (K, V) *): ObservableSortedMap[K, V] =  new ObservableSortedMap[K, V] {
		override protected val wrapped: mutable.SortedMap[K, V] = mutable.SortedMap(source *)
	}
}
sealed abstract class ObservableSortedMap[K, V] extends mutable.SortedMap[K, V] with Publisher[ObservableMapEvent] {
	protected def wrapped: mutable.SortedMap[K, V]

	import ObservableMapEvents._
	def all(): Seq[(K, V)] = wrapped.toSeq
	override def addOne(elem: (K, V)): ObservableSortedMap.this.type = {
		wrapped.addOne(elem)
		publish(Added(elem._1, elem._2))
		this
	}
	override def subtractOne(elem: K): ObservableSortedMap.this.type = {
		wrapped.remove(elem).map(v => {
			Removed(elem, v)
		}).foreach(publish)
		this
	}
	override def clear(): Unit = {
		wrapped.clear()
		publish(Cleared)
	}
	override def keysIteratorFrom(start: K): Iterator[K] = wrapped.keysIteratorFrom(start)
	override def get(key: K): Option[V] = wrapped.get(key)
	override def ordering: Ordering[K] = wrapped.ordering
	override def rangeImpl(from: Option[K], until: Option[K]): mutable.SortedMap[K, V] = wrapped.rangeImpl(from, until)
	override def iteratorFrom(start: K): Iterator[(K, V)] = wrapped.iteratorFrom(start)
	override def iterator: Iterator[(K, V)] = wrapped.iterator

}

