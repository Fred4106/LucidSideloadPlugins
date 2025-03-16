package com.fredplugins.kroovy.jfx

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.ApiDelegatingScript
import com.fredplugins.kroovy.jfx.ui.KPanel
import com.sun.javafx.collections.ObservableMapWrapper
import groovy.lang.Binding
import javafx.beans.property.{ReadOnlyBooleanWrapper, ReadOnlyMapProperty, ReadOnlyMapWrapper}
import net.runelite.client.callback.ClientThread

import javax.swing.JPanel
import scala.collection.mutable
import scala.jdk.CollectionConverters.*


trait PluginIntf {
	def kPanel: Option[JPanel]
}

trait SPackageT extends ShimUtils.Logging {
	def plugin: PluginIntf
	val onStart: ScriptTuple = new ScriptTuple(this)
	val onStop: ScriptTuple = new ScriptTuple(this)
	def self: this.type = this

	//	private var _running: Boolean = false

	private val running = new ReadOnlyBooleanWrapper(this, "running", false) {}

	def setRunning(nVal: Boolean): this.type = {
		if (nVal != isRunning) {
			runStartStop(nVal)
			running.setValue(nVal)
		}
		this
	}

	private val _binding = new Binding()
	private val nodesWrapper: ReadOnlyMapWrapper[String, SNode] = new ReadOnlyMapWrapper[String, SNode](new ObservableMapWrapper[String, SNode](mutable.HashMap.empty[String, SNode].asJava))
	def nodes: ReadOnlyMapProperty[String, SNode] = nodesWrapper.getReadOnlyProperty

	def clientThread: ClientThread
	def isRunning: Boolean = running.get
	def runningProperty = running.getReadOnlyProperty
	def binding(): Binding = _binding
	def compile(source: String): ApiDelegatingScript

	final def runStartStop(boolean: Boolean): Unit = {
		val hook = if (boolean) onStart else onStop
		if (!boolean) {
			nodes.get().asScala.filter(_._2.isEnabled).foreach(_._2.runDisabled())
		}
		hook.invoke()
		if (boolean) {
			nodes.get().asScala.filter(_._2.isEnabled).foreach(_._2.runEnabled())
		}
	}
	def addNode[E <: SNode](name: String, node: E): this.type = {
		if (node.pkg == this && !containsNode(name) && getNodeName(node).isEmpty) {
			nodesWrapper.put(name, node)
			//			publish(SPackageNodeAddedEvent(this, node))
			if (this.isRunning && node.isEnabled) {
				node.runEnabledDisabled(true)
			}
		} else {

		}
		this
	}
	def containsNode(name: String): Boolean = {
		nodes.get().containsKey(name)
	}
	def deleteNode(name: String): SPackageT = {
		Option(nodesWrapper.get(name)) match {
			case Some(node) =>
				if (this.isRunning && node.isEnabled) node.runDisabled()
			case _ =>
		}
		nodesWrapper.remove(name)
		this
	}
	//noinspection AccessorLikeMethodIsEmptyParen
	def getName(): Option[String]

	def getNodeName(node: SNode): Option[(String, String)]

	def getNode(name: String): Option[SNode] = {
		Option(nodes.get(name))
	}
}