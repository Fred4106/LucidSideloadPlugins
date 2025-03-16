package com.fredplugins.kroovy

import com.fredplugins.common.utils.ShimUtils
import com.google.common.eventbus.EventBus
import com.google.inject.{Inject, Singleton}
import net.runelite.client.callback.ClientThread
import org.slf4j.Logger

import java.util.Comparator
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.reflect.{ClassTag, TypeTest, Typeable, classTag}
import scala.reflect._
abstract class SSubDef{
	//summon[TypeTest[AnyRef, ET]]
	type ET
	def tag: ClassTag[ET]/* = classTag[ET]*/
	//using tag: ClassTag[ET]
	def owner: AnyRef
	def priority: Int
	def handle: ET => Unit
}

//case class SSubDef[E](owner: AnyRef, eType: ClassTag[E], priority: Int, handler: E => Unit) {
//
//}

@Singleton
class SEventBus @Inject()(val clientThread: ClientThread) {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")


	private var handlers: Seq[SSubDef] = Seq.empty[SSubDef]
//	private def setHandlers(nVals: SSubDef[?] *): Unit = {
//		handlers = nVals
//	}

	private def addHandlers(toAdd: SSubDef *): Unit = {
		handlers = (handlers ++ toAdd).sortWith(comparator.compare(_, _) < 0)
	}
	private def removeHandlers(toRemove: SSubDef *): Unit = {
		handlers = handlers.collect{
			case h if !toRemove.contains(h) => h
		}.sortWith(comparator.compare(_, _) < 0)
	}

	def getAll: Seq[SSubDef] = handlers
//	def getHandlers(owner: AnyRef): Seq[SSubDef[?]] = getAll.collect {
//		case h@SSubDef(o, eType, priority, handler) if(o ==  owner) => h
////		case h: SSubDef[E] if (h.owner == owner && h.eType.runtimeClass == eType) => h
//	} //.filter(_.owner == owner).filter(_.eType == eType)//.sortWith(comparator.compare(_, _) < 0)
//	def getHandlers[E: ClassTag](owner: AnyRef)(using eType: ClassTag[E]): Seq[SSubDef[E]] = getAll.collect {
//		case h: SSubDef[E] if (h.owner == owner && h.eType == eType) => h
//	} //.filter(_.owner == owner).filter(_.eType == eType)//.sortWith(comparator.compare(_, _) < 0)
//	def getHandlers[E: ClassTag]()(using eType: ClassTag[E]): Seq[SSubDef[E]] = getAll.collect {
//		case h: SSubDef[E] if(h.eType == eType) => h
//	}//.filter(_.owner == owner).filter(_.eType == eType)//.sortWith(comparator.compare(_, _) < 0)

	def owners(): Seq[AnyRef] = getAll.map(e => e.owner).distinct
	def events(): Seq[ClassTag[?]] = getAll.map(x => x.tag).distinct

//	def events(owner: AnyRef): Seq[ClassTag[?]] = getAll.filter(_.owner == owner).map(_.eType).distinct

	def owners[E](tag: ClassTag[E]): Seq[AnyRef] = {
		getAll.filter(h => h.tag == tag).map(e => e.owner).distinct
	}

//	def getHandlers(ownerFilter: Option[AnyRef] = None): Seq[SSubDef[?]] = {
//		val f1 = ownerFilter match {
//			case Some(ownerMatch) => ((_: SSubDef[_]).owner == ownerMatch)
//			case None => ((_: SSubDef[_]) => true)
//		}
//		handlers.filter(f1)
//	}
//
//	def getHandlers[E: ClassTag](using classTag: ClassTag[E]): Seq[SSubDef[E]] = {
//		handlers.collect {
//			case s: SSubDef[E] if s.eType == classTag => s
//		}
//	}

	private val comparator: Comparator[SSubDef] = Comparator.comparingInt[SSubDef](_.priority).reversed()
		.thenComparing(s => s.owner.getClass.getName).thenComparing(s => s.tag.runtimeClass.getName)

	def register[E: ClassTag](o: AnyRef, p: Int = 0)(h: E => Unit)(using classTag:ClassTag[E]): SSubDef = {
		val toRegister = new SSubDef {
			override type ET = E
			override def tag: ClassTag[E] = classTag
			override def owner: AnyRef = o
			override def priority: Int = p
			override def handle: E => Unit = h
		}
		addHandlers(toRegister)
		toRegister
	}
	def unregisterAll(): Boolean = {
		val toRemove = getAll
		removeHandlers(toRemove *)
		toRemove.nonEmpty
	}

	def unregisterAll(owner: AnyRef): Boolean = {
		val toRemove = getAll.filter(_.owner == owner)
		removeHandlers(toRemove*)
		toRemove.nonEmpty
	}
	def unregisterAll[E: ClassTag](using classTag: ClassTag[E]): Boolean = {
		val toRemove = getAll.filter(_.tag == classTag)
		removeHandlers(toRemove*)
		toRemove.nonEmpty
	}

	def unregisterAll[E: ClassTag](owner: AnyRef)(using classTag: ClassTag[E]): Boolean = {
		val toRemove = getAll.filter(i => i.tag == classTag && i.owner == owner)
		removeHandlers(toRemove*)
		toRemove.nonEmpty
	}

	def unregister(remove: SSubDef): Boolean = {
		val toRemove = getAll.filter(_ == remove)
		removeHandlers(toRemove *)
		toRemove.nonEmpty
	}

	def post[E: ClassTag](e: E)(using classTag: ClassTag[E]): Unit = {
		getAll.filter(_.tag == classTag).sortBy(_.priority).reverse.foreach(h => {
			h.tag.unapply(e).foreach(h.handle(_))
		})
	}

	def debug(): Unit = {
		events().foreach(eClazz => {
			println(eClazz.runtimeClass.getName)
			owners(eClazz).foreach(o => {
				println(s"\t${o.toString}")
				getAll.filter(h => h.tag == eClazz && h.owner == o).foreach(h => {
					println(s"\t\t${h.toString}")
				})
//				getHandler(o, e.runtimeClass).foreach(h => {
//
//				})
			})
		})
//		val map: Map[(AnyRef, ClassTag[?]), Seq[(Int, Function[?, Unit])]] = getAll.groupMap(i =>(i.owner, i.eType))(i => (i.priority, i.handler))

//		val eventsSeq = getAll.groupMap(_.eType)(a => a)
//		val ownerSeq = getAll.groupMap(_.owner)(a=>a).toMap.get
	}
}
