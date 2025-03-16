package com.fredplugins.kroovy

import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Singleton}
import net.runelite.client.callback.ClientThread
import org.slf4j.Logger

import java.util.{Comparator, Objects}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.reflect.{ClassTag, TypeTest, Typeable, classTag}
import scala.reflect.*

@Singleton
class SEventBus @Inject()(val clientThread: ClientThread) {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	type OwnerType = AnyRef

	abstract class SubscriberType[ET: ClassTag as ct, Priority <:Int & scala.Singleton : ValueOf as pVal, Group <: String  & scala.Singleton : ValueOf as gVal](val owner: OwnerType) {
		val valueOfParams: (ValueOf[Priority], ValueOf[Group]) = pVal -> gVal

		val priority: Int = pVal.value
		val group: String = gVal.value

		val ownerAndGroupStr: String = owner.getClass.getName + "." + Integer.toHexString(owner.hashCode()) + "." + group
		val eTag: ClassTag[ET] = ct
		val eClazz: Class[ET] = eTag.runtimeClass.asInstanceOf[Class[ET]]

		def handle(e: ET): Unit

		override def toString: String = s"SubscriberType[${eClazz.getSimpleName}, ${priority}, ${group}](owner=\"${owner.toString}\", priority=${priority})[${Integer.toHexString(hashCode)}]"
	}

	private var internal: Seq[SubscriberType[?, ?, ?]] = Seq.empty

	def events(): Set[Class[?]] = internal.map(_.eClazz).distinct.sortBy(_.getName).toSet
	def owners(): Set[OwnerType] = internal.map(_.owner).distinct.sortBy(_.getClass.getName).toSet
	def groups(a: OwnerType): Set[String] = internal.filter(_.owner == a).map(_.group).distinct.sorted.toSet

	private def updateInternal(in: Seq[SubscriberType[?, ?,? ]]): Unit = {
		val comparator: Comparator[SubscriberType[?, ?, ?]] = Comparator.comparingInt[SubscriberType[?, ?, ?]](_.priority).thenComparing[String](_.owner.getClass.getName)
			.thenComparing(s => s.ownerAndGroupStr)
		internal = in.sortWith((s1, s2) => {				comparator.compare(s1, s2) < 0			})
	}

	def register[ET: ClassTag as ct, Priority <: Int & scala.Singleton : ValueOf as pVal, Group <: String & scala.Singleton : ValueOf as gVal](owner: OwnerType)(op: ET => Unit): SubscriberType[ET, Priority, Group] = {
		val toAdd = new SubscriberType[ET, Priority, Group](owner) {
			override def handle(e: ET): Unit = op(e)
		}
		updateInternal(internal.appended(toAdd))
		toAdd
	}

	def unregisterByOwner(owner: OwnerType): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => h.owner == owner)
		log.debug(s"unregisterByOwner(${owner}) => toRemove: ${toRemove}, toKeep: ${toKeep}")
		updateInternal(toKeep)
		toRemove.nonEmpty
	}
	def unregisterByEvent[ET: ClassTag as ct](): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => h.eClazz == ct.runtimeClass)
		updateInternal(toKeep)
		toRemove.nonEmpty
	}
	def unregisterByOwnerAndGroup[Group <:String & scala.Singleton: ValueOf as gVal](owner: OwnerType): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => h.owner == owner && h.group .equals(gVal.value))
		updateInternal(toKeep)
		toRemove.nonEmpty
	}
	def unregisterByOwnerAndEvent[ET: ClassTag as ct](owner: OwnerType): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => h.owner == owner && h.eClazz == ct.runtimeClass)
		updateInternal(toKeep)
		toRemove.nonEmpty
	}
	def unregisterByOwnerGroupAndEvent[ET: ClassTag as ct, Group <: String & scala.Singleton : ValueOf as gVal](owner: OwnerType): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => h.owner == owner && h.group.equals(gVal.value) && h.eClazz == ct.runtimeClass)
		updateInternal(toKeep)
		toRemove.nonEmpty
	}
	def unregisterAll(): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => true)
		updateInternal(toKeep)
		toRemove.nonEmpty
	}

	def unregister(subscriber: SubscriberType[?, ?, ?]): Boolean = {
		val (toRemove, toKeep) = internal.partition(h => h == subscriber)
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
