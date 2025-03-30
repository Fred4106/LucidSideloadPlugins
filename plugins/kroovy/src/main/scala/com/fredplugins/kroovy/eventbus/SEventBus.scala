package com.fredplugins.kroovy.eventbus

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.eventbus.SEventBus.{AddedSubs, DeletedSubs, OwnerType, SubscriberType}
import com.google.inject.Singleton
import org.slf4j.Logger

import java.util.Comparator
import scala.collection.{immutable, mutable}
import scala.reflect.ClassTag
import scala.swing.Publisher

object SEventBus {
	type OwnerType = AnyRef

	abstract class SubscriberType[ET: ClassTag as ct, Priority <: Int & scala.Singleton : ValueOf as pVal, Group <: String & scala.Singleton : ValueOf as gVal](val owner: OwnerType) {
		val valueOfParams: (ValueOf[Priority], ValueOf[Group]) = pVal -> gVal

		private var _enabled: Boolean = false
		def getEnabled: Boolean = _enabled
		def setEnabled_=(v: Boolean): Unit = _enabled = v
		val priority: Int    = pVal.value
		val group   : String = gVal.value

		val ownerAndGroupStr: String       = owner.getClass.getName + "." + Integer.toHexString(owner.hashCode()) + "." + group
		val eTag            : ClassTag[ET] = ct
		val eClazz          : Class[ET]    = eTag.runtimeClass.asInstanceOf[Class[ET]]

		def handle(e: ET): Unit

//		override def toString: String = s"SubscriberType[${eClazz.getSimpleName}, ${priority}, ${group}](owner=\"${owner.toString}\", priority=${priority})[${Integer.toHexString(hashCode)}]"
		override def toString: String = s"SubscriberType[${eClazz.getSimpleName}, ${priority}, ${group}](${owner.getClass.getSimpleName}@${Integer.toHexString(owner.hashCode)}, ${Integer.toHexString(hashCode)}) = ${getEnabled}"

	}


	sealed trait SEventBusEvent extends scala.swing.event.Event with Product {	}
	case class AddedSubs(added: Set[SubscriberType[?, ?, ?]], all: scala.collection.immutable.SortedSet[SubscriberType[?, ?, ?]]) extends SEventBusEvent
	case class DeletedSubs(deleted: Set[SubscriberType[?, ?, ?]], all: scala.collection.immutable.SortedSet[SubscriberType[?, ?, ?]]) extends SEventBusEvent
	case class Record(str: String) extends SEventBusEvent

	given Ordering[SubscriberType[?, ?, ?]] = Ordering.comparatorToOrdering[SubscriberType[?, ?,  ?]](
		Comparator.comparingInt[SubscriberType[?, ?, ?]](_.priority).thenComparing[String](_.owner.getClass.getName).thenComparing(s => s.ownerAndGroupStr)
	)


}
@Singleton
class SEventBus() extends Publisher {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

//	private var internal: List[SubscriberType[?, ?, ?]] = List.empty
	private val internal2 = mutable.SortedSet.apply[SubscriberType[?, ?, ?]]()

	def all(): Seq[SubscriberType[?, ?, ?]] = internal2.toSeq
	def events(): Set[Class[?]] = all().map(_.eClazz).distinct.sortBy(_.getName).toSet
	def owners(): Set[OwnerType] = all().map(_.owner).distinct.sortBy(_.getClass.getName).toSet
	def groups(a: OwnerType): Set[String] =all().filter(_.owner == a).map(_.group).distinct.sorted.toSet

	private def add(added: Set[SubscriberType[?, ?, ?]]): Unit = {
		internal2.addAll(added)
		publish(AddedSubs(added, immutable.SortedSet.from(internal2)))
	}
	private def delete(removed: Set[SubscriberType[?, ?, ?]]): Unit = {
		internal2.subtractAll(removed)
		publish(DeletedSubs(removed, immutable.SortedSet.from(internal2)))
	}

	def register[ET: ClassTag as ct, Priority <: Int & scala.Singleton : ValueOf as pVal, Group <: String & scala.Singleton : ValueOf as gVal](owner: OwnerType)(op: ET => Unit): SubscriberType[ET, Priority, Group] = {
		val toAdd = new SubscriberType[ET, Priority, Group](owner) {
			override def handle(e: ET): Unit = op(e)
		}
//		internal2.add(toAdd)
		add(Set(toAdd))
//		updateInternal(internal.appended(toAdd))
		toAdd
	}

	def unregisterByOwner(owner: OwnerType): Boolean = {
		val (toRemove, toKeep) = all().partition(h => h.owner == owner)
//		log.debug(s"unregisterByOwner(${owner}) => toRemove: ${toRemove}, toKeep: ${toKeep}")
//		internal2.removeAll(toRemove *)
		delete(toRemove.toSet)
//		delete(toRemove.toSet)
//		updateInternal(toKeep, true)
		toRemove.nonEmpty
	}
	def unregisterByEvent[ET: ClassTag as ct](): Boolean = {
		val (toRemove, toKeep) = all().partition(h => h.eClazz == ct.runtimeClass)
//		internal2.removeAll(toRemove *)
//		updateInternal(toKeep, true)
		delete(toRemove.toSet)

		toRemove.nonEmpty
	}
	def unregisterByOwnerAndGroup[Group <:String & scala.Singleton: ValueOf as gVal](owner: OwnerType): Boolean = {
		val (toRemove, toKeep) = all().partition(h => h.owner == owner && h.group .equals(gVal.value))
//		internal2.removeAll(toRemove *)
//		updateInternal(toKeep, true)
		delete(toRemove.toSet)
		toRemove.nonEmpty
	}
	def unregisterByOwnerAndEvent[ET: ClassTag as ct](owner: OwnerType): Boolean = {
		val (toRemove, toKeep) = all().partition(h => h.owner == owner && h.eClazz == ct.runtimeClass)
//		internal2.removeAll(toRemove *)
//		updateInternal(toKeep, true)
		delete(toRemove.toSet)

		toRemove.nonEmpty
	}
	def unregisterByOwnerGroupAndEvent[ET: ClassTag as ct, Group <: String & scala.Singleton : ValueOf as gVal](owner: OwnerType): Boolean = {
		val (toRemove, toKeep) = all().partition(h => h.owner == owner && h.group.equals(gVal.value) && h.eClazz == ct.runtimeClass)
//		internal2.removeAll(toRemove *)
//		updateInternal(toKeep, true)
		delete(toRemove.toSet)
		toRemove.nonEmpty
	}
	def unregisterAll(): Boolean = {
		val (toRemove, toKeep) = all().partition(h => true)
//		internal2.removeAll(toRemove *)
		delete(toRemove.toSet)
//		updateInternal(toKeep, true)
		toRemove.nonEmpty
	}

	def unregister(subscriber: SubscriberType[?, ?, ?]): Boolean = {
		val (toRemove, toKeep) = all().partition(h => h == subscriber)
//		internal2.removeAll(toRemove *)
//		updateInternal(toKeep, true)
		delete(toRemove.toSet)
		toRemove.nonEmpty
	}

	def post[ET: ClassTag as classTag](e: ET): Unit = {
		all().foreach(h => {
			if(h.eClazz == classTag.runtimeClass) {
//				log.debug(s"Dispatching event ${e} to ${h.toString}")
				h.asInstanceOf[SubscriberType[ET, ?, ?]].handle(e)
			}
		})
	}

	def debug(op: String => Unit): Unit = {
		all().zipWithIndex.foreach(h => {
			op(s"internal[${h._2}] = ${h._1}")
		})

		val z: Set[(OwnerType, String, Seq[SubscriberType[?, ?, ?]])] = for{
			owner <- owners()
			group <- groups(owner)
		} yield {
			val seq = all().filter(h => {
				h.owner == owner &&
				h.group == group
			})
			(owner, group, seq)
		}

		val zMap = z.groupMap(_._1)(y => y._2 -> y._3).map{
			case (a, b) => a -> b.toMap
		}.toMap
		zMap.foreach{
			case (owner, remainder) => {
				op(s"owner: ${owner.getClass.getSimpleName}")
				remainder.foreach {
					case (g, remainder) => {
						op(s"\tgroup: ${g}")
						remainder.groupBy(_.eClazz).foreach {
							case (eClass, seq) => {
								op(s"\t\tevent: ${eClass.getSimpleName}")
								seq.foreach{ h =>
									op(s"\t\t\tsubscriber[${h.priority}] = ${h}")
								}
							}
						}
					}
				}
			}
		}
	}
}
