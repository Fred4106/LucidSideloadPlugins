package com.fredplugins.kroovy.events

class Interrupt(val source: AnyRef) {
	private var consumedVar: Boolean = false
	def consume(): Unit = consumedVar = true
	def isConsumed:Boolean = consumedVar
}
