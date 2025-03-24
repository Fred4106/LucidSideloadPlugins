package com.fredplugins.kroovy.swing

import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.swing.Publisher

object ObservableSortedSet {
	def apply[E: Ordering as ord](publisher: Publisher)(source: E *): ObservableSortedSet[E] =  new ObservableSortedSet[E](publisher) {
		override protected val wrapped: mutable.SortedSet[E] = mutable.SortedSet(source *)
	}
}
sealed abstract class ObservableSortedSet[I](publisher: Publisher) extends mutable.SortedSet[I] {
	protected def wrapped: mutable.SortedSet[I]

	def all(): Seq[I] = wrapped.toSeq

	sealed trait ObservableSetEvent extends swing.event.Event {}
	case class Added(element: I, idx: Int) extends  ObservableSetEvent {}
	case class Removed(element: I, idx: Int) extends ObservableSetEvent {}
	case object Cleared extends ObservableSetEvent {}

	override def addOne(elem: I): ObservableSortedSet.this.type  = {
		wrapped.addOne(elem)
		publisher.publish(Added(elem, wrapped.toSeq.indexOf(elem)))
		this
	}
	override def subtractOne(elem: I): ObservableSortedSet.this.type = {
		val idx = wrapped.toSeq.indexOf(elem)
		wrapped.subtractOne(elem)
		publisher.publish(Removed(elem, idx))
		this
	}
	override def clear(): Unit = {
		wrapped.clear()
		publisher.publish(Cleared)
	}
	override def iteratorFrom(start: I): Iterator[I] = wrapped.iteratorFrom(start)
	override def contains(elem: I): Boolean = wrapped.contains(elem)
	override def iterator: Iterator[I] = wrapped.iterator
	override def ordering: Ordering[I] = wrapped.ordering
	override def rangeImpl(from: Option[I], until: Option[I]): mutable.SortedSet[I] =
		wrapped.rangeImpl(from, until)
}

