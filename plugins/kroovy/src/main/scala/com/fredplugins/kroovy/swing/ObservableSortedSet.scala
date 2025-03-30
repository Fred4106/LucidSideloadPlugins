package com.fredplugins.kroovy.swing

import com.fredplugins.kroovy.api.{LazyPublisher, Publisher}

import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized


sealed trait ObservableSetEvent {}
object ObservableSetEvents {
	case class Added[I](element: I, idx: Int) extends ObservableSetEvent {}
	case class Removed[I](element: I, idx: Int) extends ObservableSetEvent {}
	case object Cleared extends ObservableSetEvent {}
}

object ObservableSortedSet {
	def apply[E: Ordering as ord](source: E *): ObservableSortedSet[E] =  new ObservableSortedSet[E] {
		override protected val wrapped: mutable.SortedSet[E] = mutable.SortedSet(source *)
	}
}
sealed abstract class ObservableSortedSet[I] extends mutable.SortedSet[I] with Publisher[ObservableSetEvent] {
//	import ObservableSetEvent._
	protected def wrapped: mutable.SortedSet[I]

	def all(): Seq[I] = wrapped.toSeq

	override def addOne(elem: I): ObservableSortedSet.this.type  = {
		wrapped.addOne(elem)
		publish(ObservableSetEvents.Added(elem, wrapped.toSeq.indexOf(elem)))
		this
	}
	override def subtractOne(elem: I): ObservableSortedSet.this.type = {
		val evt = ObservableSetEvents.Removed(elem, wrapped.toSeq.indexOf(elem))
		wrapped.subtractOne(elem)
		publish(evt)
		this
	}
	override def clear(): Unit = {
		wrapped.clear()
		publish(ObservableSetEvents.Cleared)
	}
	override def iteratorFrom(start: I): Iterator[I] = wrapped.iteratorFrom(start)
	override def contains(elem: I): Boolean = wrapped.contains(elem)
	override def iterator: Iterator[I] = wrapped.iterator
	override def ordering: Ordering[I] = wrapped.ordering
	override def rangeImpl(from: Option[I], until: Option[I]): mutable.SortedSet[I] =
		wrapped.rangeImpl(from, until)
}

