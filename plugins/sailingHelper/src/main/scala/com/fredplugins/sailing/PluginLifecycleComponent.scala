package com.fredplugins.sailing

trait PluginLifecycleComponent {
	def isEnabled: Boolean = true
	def startUp(): Unit = {}
	def shutDown(): Unit = {}
}