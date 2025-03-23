package com.fredplugins.kroovy

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.SEventBus.{OwnerType, SubscriberType}
import com.google.inject.{Inject, Singleton}
import io.bullet.spliff.Diff.{Op, Patch}
import net.runelite.client.callback.ClientThread
import org.slf4j.Logger

import java.util
import java.util.{Comparator}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.reflect.{ClassTag, TypeTest, Typeable, classTag}
import scala.reflect.*
import scala.swing.{Publisher, Reactor}

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

		override def toString: String = s"SubscriberType[${eClazz.getSimpleName}, ${priority}, ${group}](owner=\"${owner.toString}\", priority=${priority})[${Integer.toHexString(hashCode)}]"
	}


	sealed trait SEventBusEvent extends scala.swing.event.Event with Product {

	}

	case class Refresh() extends SEventBusEvent {

	}
//	case class Move(idx: Int, count: Int, step: Int) extends SEventBusEvent
}

@Singleton
class SEventBus @Inject()(val clientThread: ClientThread) extends Publisher {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	private var internal: List[SubscriberType[?, ?, ?]] = List.empty

	def all(): Seq[SubscriberType[?, ?, ?]] = internal
	def events(): Set[Class[?]] = internal.map(_.eClazz).distinct.sortBy(_.getName).toSet
	def owners(): Set[OwnerType] = internal.map(_.owner).distinct.sortBy(_.getClass.getName).toSet
	def groups(a: OwnerType): Set[String] = internal.filter(_.owner == a).map(_.group).distinct.sorted.toSet

	private def updateInternal(in: List[SubscriberType[?, ?,? ]]): Unit = {
		val comparator: Comparator[SubscriberType[?, ?, ?]] = Comparator.comparingInt[SubscriberType[?, ?, ?]](_.priority).thenComparing[String](_.owner.getClass.getName)
			.thenComparing(s => s.ownerAndGroupStr)
		val updated = in.sortWith((s1, s2) => {				comparator.compare(s1, s2) < 0			})
		import io.bullet.spliff.Diff

		val (unchanged, added) = updated.partition(u => internal.contains(u))
		val removed = internal.filterNot(ii => updated.contains(ii))
		def prettyString(st: SubscriberType[?, ?, ?]): String = {
			s"SubscriberType[${st.eClazz.getSimpleName}, ${st.priority}, ${st.group}](${st.owner.getClass.getSimpleName}[${Integer.toHexString(st.owner.hashCode())}])(${Integer.toHexString(st.hashCode())})"
		}

		val toPrint = added.map(a => s"    added - ${prettyString(a)}").appendedAll(
			removed.map(a => s"  removed - ${prettyString(a)}")
		).mkString(s"updateInternal[\n", "\n", "\n]")

		log.debug(toPrint)

//		val diff = Diff(internal.toIndexedSeq, updated.toIndexedSeq)
//		val ops = diff
//
//		val opsStr = ops.steps.map(_.toString).map(s => s"\t$s").mkString("[\n", "\n", "\n]")
//		val internalStr = internal.map(_.toString).map(s => s"\t$s").mkString("[\n", "\n", "\n]")
//		val updatedStr = updated.map(_.toString).map(s => s"\t$s").mkString("[\n", "\n", "\n]")

//		ops.steps.toList.foreach {
//			case Patch.Delete(baseIdx, count) => {
//				log.debug("Patch.Delete(baseIdx: {}, count: {})", baseIdx, count)
////					Option(publisher).foreach(_.publish(SEventBus.Delete(baseIdx, internal.slice(baseIdx, baseIdx + count))))
//			}
//			case Patch.Move(origIdx, destIdx, count) => {
//				val elements = internal.slice(origIdx, origIdx + count)
//				log.debug("Patch.Move(origIdx: {}, destIdx: {}, count: {}) => step: {}, children {}", origIdx, destIdx, count, destIdx - origIdx - count, elements.mkString("[",  ", ", "]"))
////				Option(publisher).foreach(_.publish(
////					SEventBus.Delete(origIdx, elements)))
////				Option(publisher).foreach(_.publish(
////					SEventBus.Add(destIdx - count - origIdx, elements)
////				))
////				SEventBus.Move.apply(origIdx, count, destIdx - count - origIdx)
////								)
//			}
//			case Patch.Insert(baseIdx, values) => {
//				log.debug("Patch.Insert(baseIdx: {}, values: {})", baseIdx, values.mkString("[",  ", ", "]"))
////				Option(publisher).foreach(_.publish(
////					SEventBus.Add(baseIdx, values)
////				))
//			}
//			case x => {
//				log.debug("reacting to {}", x)
//			}
//		}
		internal = updated
		publish(SEventBus.Refresh())
	}

	def register[ET: ClassTag as ct, Priority <: Int & scala.Singleton : ValueOf as pVal, Group <: String & scala.Singleton : ValueOf as gVal](owner: OwnerType)(op: ET => Unit): SubscriberType[ET, Priority, Group] = {
		val toAdd = new SubscriberType[ET, Priority, Group](owner) {
			override def handle(e: ET): Unit = op(e)
		}
//		internal2.add(toAdd)
		updateInternal(internal.appended(toAdd))
		toAdd
	}

	def unregisterByOwner(owner: OwnerType): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => h.owner == owner)
//		log.debug(s"unregisterByOwner(${owner}) => toRemove: ${toRemove}, toKeep: ${toKeep}")
//		internal2.removeAll(toRemove *)
		updateInternal(toKeep)
		toRemove.nonEmpty
	}
	def unregisterByEvent[ET: ClassTag as ct](): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => h.eClazz == ct.runtimeClass)
//		internal2.removeAll(toRemove *)
		updateInternal(toKeep)
		toRemove.nonEmpty
	}
	def unregisterByOwnerAndGroup[Group <:String & scala.Singleton: ValueOf as gVal](owner: OwnerType): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => h.owner == owner && h.group .equals(gVal.value))
//		internal2.removeAll(toRemove *)
		updateInternal(toKeep)
		toRemove.nonEmpty
	}
	def unregisterByOwnerAndEvent[ET: ClassTag as ct](owner: OwnerType): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => h.owner == owner && h.eClazz == ct.runtimeClass)
//		internal2.removeAll(toRemove *)
		updateInternal(toKeep)
		toRemove.nonEmpty
	}
	def unregisterByOwnerGroupAndEvent[ET: ClassTag as ct, Group <: String & scala.Singleton : ValueOf as gVal](owner: OwnerType): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => h.owner == owner && h.group.equals(gVal.value) && h.eClazz == ct.runtimeClass)
//		internal2.removeAll(toRemove *)
		updateInternal(toKeep)
		toRemove.nonEmpty
	}
	def unregisterAll(): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => true)
//		internal2.removeAll(toRemove *)
		updateInternal(toKeep)
		toRemove.nonEmpty
	}

	def unregister(subscriber: SubscriberType[?, ?, ?]): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => h == subscriber)
//		internal2.removeAll(toRemove *)
		updateInternal(toKeep)
		toRemove.nonEmpty
	}

	def post[ET: ClassTag as classTag](e: ET): Unit = {
		internal.foreach(h => {
			if(h.eClazz == classTag.runtimeClass) {
				log.debug(s"Dispatching event ${e} to ${h.toString}")
				h.asInstanceOf[SubscriberType[ET, ?, ?]].handle(e)
			}
		})
	}

	def debug(): Unit = {
		internal.zipWithIndex.foreach(h => {
			log.debug(s"internal[${h._2}] = ${h._1}")
		})

		val z: Set[(OwnerType, String, Seq[SubscriberType[?, ?, ?]])] = for{
			owner <- owners()
			group <- groups(owner)
		} yield {
			val seq = internal.filter(h => {
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
				log.debug(s"owner: ${owner.getClass.getSimpleName}")
				remainder.foreach {
					case (g, remainder) => {
						log.debug(s"\tgroup: ${g}")
						remainder.groupBy(_.eClazz).foreach {
							case (eClass, seq) => {
								log.debug(s"\t\tevent: ${eClass.getSimpleName}")
								seq.foreach{ h =>
									log.debug(s"\t\t\tsubscriber[${h.priority}] = ${h}")
								}
							}
						}
					}
				}
			}
		}
	}
}
