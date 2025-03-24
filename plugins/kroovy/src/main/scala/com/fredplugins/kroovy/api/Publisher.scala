/*
 * scala-swing (https://www.scala-lang.org)
 *
 * Copyright EPFL, Lightbend, Inc., contributors
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package com.fredplugins.kroovy.api

import scala.collection.mutable
import scala.ref.{Reference, WeakReference}

/**
 * Used by reactors to let clients register custom event reactions.
 */
trait Reactions[E] extends PartialFunction[E, Unit] {
  /**
   * Add a reaction.
   */
  def +=(r: PartialFunction[E, Unit]): this.type

  /**
   * Remove the given reaction.
   */
  def -=(r: PartialFunction[E, Unit]): this.type
}

trait Reactor[E] {
  /**
   * All reactions of this reactor.
   */
  val reactions: Reactions[E] = new Reactions[E] {
    private val parts: mutable.Buffer[PartialFunction[E, Unit]] = new mutable.ListBuffer[PartialFunction[E, Unit]]
    def isDefinedAt(e: E): Boolean = parts.exists(_ isDefinedAt e)
    def +=(r: PartialFunction[E, Unit]): this.type = {parts += r; this}
    def -=(r: PartialFunction[E, Unit]): this.type = {parts -= r; this}
    def apply(e: E): Unit = {
      for (p <- parts) if (p isDefinedAt e) p(e)
    }
  }
  /**
   * Listen to the given publisher as long as <code>deafTo</code> isn't called for
   * them.
   */
  def listenTo(ps: Publisher[_ <: E]*): Unit = for (p <- ps) p.subscribe(reactions)
  /**
   * Installed reaction won't receive events from the given publisher anylonger.
   */
  def deafTo(ps: Publisher[_ <: E]*): Unit = for (p <- ps) p.unsubscribe(reactions)
}

/** <p>
 *    Notifies registered reactions when an event is published. Publishers are
 *    also reactors and listen to themselves per default as a convenience.
 *  </p>
 */
trait Publisher[E] extends Reactor[E]{

  protected val listeners: RefSet[PartialFunction[E, Unit]] = new RefSet[PartialFunction[E, Unit]] {
    protected val underlying: mutable.Set[Reference[PartialFunction[E, Unit]]] =
      new mutable.HashSet[Reference[PartialFunction[E, Unit]]]

    protected def Ref(a: PartialFunction[E, Unit]): Ref[PartialFunction[E, Unit]] = a match {
      case _                     => new WeakReference[PartialFunction[E, Unit]](a, referenceQueue) with super.Ref[PartialFunction[E, Unit]]
    }
  }

  def subscribe  (listener: PartialFunction[E, Unit]): Unit = listeners += listener
  def unsubscribe(listener: PartialFunction[E, Unit]): Unit = listeners -= listener

  /**
   * Notify all registered reactions.
   */
  def publish(e: E): Unit = for (l <- listeners) if (l.isDefinedAt(e)) l(e)

  listenTo(this)
}

/**
 * A publisher that subscribes itself to an underlying event source not before the first
 * reaction is installed. Can unsubscribe itself when the last reaction is uninstalled.
 */
trait LazyPublisher[E] extends Publisher[E] {

  protected def onFirstSubscribe (): Unit
  protected def onLastUnsubscribe(): Unit

  override def subscribe(listener: PartialFunction[E, Unit]): Unit = {
    if (listeners.size == 1) onFirstSubscribe()
    super.subscribe(listener)
  }

  override def unsubscribe(listener: PartialFunction[E, Unit]): Unit = {
    super.unsubscribe(listener)
    if (listeners.size == 1) onLastUnsubscribe()
  }
}



import scala.ref._

trait SingleRefCollection[A <: AnyRef] extends Iterable[A] { self =>

  trait Ref[+B <: AnyRef] extends Reference[B] {
    override def hashCode(): Int = get match {
      case Some(x)  => x.##
      case _        => 0
    }
    override def equals(that: Any): Boolean = that match {
      case that: ReferenceWrapper[_] =>
        val v1 = this.get
        val v2 = that.get
        v1 == v2
      case _ => false
    }
  }

  //type Ref <: Reference[A] // TODO: could use higher kinded types, but currently crashes
  protected[this] def Ref(a: A): Ref[A]
  protected[this] val referenceQueue = new ReferenceQueue[A]

  protected val underlying: Iterable[Reference[A]]

  def purgeReferences(): Unit = {
    var ref = referenceQueue.poll
    while (ref.isDefined) {
      removeReference(ref.get)
      ref = referenceQueue.poll
    }
  }

  protected[this] def removeReference(ref: Reference[A]): Unit

  def iterator: Iterator[A] = new Iterator[A] {
    private val elems = self.underlying.iterator
    private var hd: A = _
    private var ahead: Boolean = false
    private def skip(): Unit =
      while (!ahead && elems.hasNext) {
        // make sure we have a reference to the next element,
        // otherwise it might be garbage collected
        val next = elems.next().get
        ahead = next.isDefined
        if (ahead) hd = next.get
      }
    def hasNext: Boolean = { skip(); ahead }
    def next(): A =
      if (hasNext) { ahead = false; hd }
      else throw new NoSuchElementException("next on empty iterator")
  }
}

abstract class SetWrapper[A] extends mutable.Set[A] {
  /** The collection passed to `addAll` and `subtractAll` */
  type MoreElem[+B] = IterableOnce[B]

  /** Cross-version way for creating an iterator from `MoreElem`. */
  final protected def mkIterator[B](xs: MoreElem[B]): Iterator[B] = xs.iterator

  override def clear(): Unit = iterator.toList.foreach(remove)
}
abstract class RefSet[A <: AnyRef] extends SetWrapper[A] with SingleRefCollection[A] { self =>
  protected val underlying: mutable.Set[Reference[A]]

  override def addOne     (el: A): this.type = { purgeReferences(); underlying += Ref(el); this }
  override def subtractOne(el: A): this.type = { underlying -= Ref(el); purgeReferences(); this }

  override def clear(): Unit = { underlying.clear(); purgeReferences() }

  def contains(el: A): Boolean = { purgeReferences(); underlying.contains(Ref(el)) }

  override def size: Int = { purgeReferences(); underlying.size }

  protected[this] def removeReference(ref: Reference[A]): Unit = { underlying -= ref }
}
