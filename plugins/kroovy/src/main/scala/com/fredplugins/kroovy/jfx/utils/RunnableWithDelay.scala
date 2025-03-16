package com.fredplugins.kroovy.jfx.utils


import com.fredplugins.kroovy.jfx.RunnableImplicits

import scala.collection.mutable

sealed trait RunnableTypeTrait {
		def dProvider: () => Int
		def action: () => Unit
		def runnable: Runnable = RunnableImplicits.runnable(action)
		def delay: Int = 0
		def tick(): Boolean = false
}

case class RunnableWithGameTickDelay(dProvider: () => Int, action: () => Unit) extends RunnableTypeTrait {
	private var $delay: Int = dProvider()
	override def delay: Int = $delay

	override def tick(): Boolean = {
		if (delay <= 0) {
			runnable.run()
			true
		}
		else {
			$delay -= 1
			false
		}
	}
}

case class RunnableWithClientTickDelay(dProvider: () => Int, action: () => Unit) extends RunnableTypeTrait {
	override val delay: Int = dProvider()
}
//
//object RunnableWithDelayBuilder {
//	type Constructor[J] = (() => Int) => (() => Unit) => J
////	implicit val gameTickConstructor: Constructor[RunnableWithGameTickDelay] = {
////		val z = ((a: () => Int)(b: () => Unit) => RunnableWithGameTickDelay(a, b))
////		RunnableWithGameTickDelay.curried.asInstanceOf[Constructor[RunnableWithGameTickDelay]]
////	}
////	implicit val clientTickConstructor: Constructor[RunnableWithClientTickDelay] = {
////		RunnableWithClientTickDelay.curried
////	}
//}

class RunnableWithDelayBuilder[I <: RunnableTypeTrait](implicit val constructor: Constructor[I]) {
	private val actions: mutable.ListBuffer[() => Unit] = collection.mutable.ListBuffer.empty
	private var delayProvider: () => Int = () => 0

	def addAction(action: () => Unit): this.type = {
		actions.addOne(action)
		this
	}

	def setDelay(func: () => Int): this.type = {
		delayProvider = func
		this
	}

	def setDelay(min: Int, max: Int): this.type = setDelay(() => (Math.random * (max - min + 1) + min).toInt)

	def flush(): List[I] = {
		val toRet = actions.toList.map(constructor.apply(delayProvider))
		actions.clear()
		toRet
	}
}
