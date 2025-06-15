package com.fredplugins.pvmDebugger

import net.runelite.api.ChatMessageType
import net.runelite.api.Client
import net.runelite.api.events.ChatMessage
import net.runelite.api.gameval.InterfaceID.Chatbox
import net.runelite.client.chat.ChatMessageBuilder
import net.runelite.client.chat.QueuedMessage
import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.events.ConfigChanged
import net.runelite.client.ui.overlay.OverlayPanel
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity

import java.awt.Dimension
import java.awt.Graphics2D
import java.lang.ref.Cleaner.Cleanable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.reflect.ClassTag
import scala.reflect.classTag

trait WithPanel {
	self: HelperModule =>
	val panel: HelperOverlayPanel = {
		HelperOverlayPanel.create(self) {createPanelElements()}
	}

	protected def createPanelElements(): Seq[LayoutableRenderableEntity]
}
trait WithOverlay {
	self: HelperModule =>
	def renderOverlay(g: Graphics2D): Dimension
	val overlay: HelperOverlay = HelperOverlay.create(self)(renderOverlay)
}
abstract class HelperModule/*[C <: Config :ClassTag]*/ {
	val parent: PvmDebuggerPlugin
	val client: Client
//	type C <: Config
	val config: Config// = parent.getConfigManager.getConfig[C](classTag[C].runtimeClass.asInstanceOf[Class[C]])

	val configGroup: String = parent.getConfigManager.getConfigDescriptor(config).getGroup.value()/*config.getAnnotation(classOf[ConfigGroup]).value()*/
	def isRegistered(): Boolean = parent.getEventBus.isRegistered(this)
	def isEnabled(): Boolean = parent.getConfigManager.getConfiguration(configGroup, "enabled", classOf[java.lang.Boolean])

	protected def init(): Unit
	protected def cleanup(): Unit

	final def startup(): Unit = {
		if(!isRegistered() && isEnabled()) {
			init()
			Option(this).collect {
				case swp: HelperModule with WithOverlay => swp.overlay
			}.foreach(o => parent.getOverlayManager.add(o))
			Option(this).collect {
				case swp: HelperModule with WithPanel => swp.panel
			}.foreach(o => parent.getOverlayManager.add(o))

			parent.getEventBus.register(this)
		}
	}

	final def shutdown(): Unit = {
		if (isRegistered()) {
			parent.getEventBus.unregister(this)
			Option(this).collect {
				case swp: HelperModule with WithOverlay => swp.overlay
			}.foreach(o => parent.getOverlayManager.remove(o))
			Option(this).collect {
				case swp: HelperModule with WithPanel => swp.panel
			}.foreach(o => parent.getOverlayManager.remove(o))
			cleanup()
		}
	}

	def printMessage(tpe: ChatMessageType, sender: String, name: String)(msg: ChatMessageBuilder): Unit = {
		val queuedMessage = QueuedMessage.builder().`type`(tpe).sender(sender).name(name).runeLiteFormattedMessage(msg.build()).build()
//		val msg = (new ChatMessageBuilder).append("Can't initialize, unable to find barriers. Found: " + barriers.size).build
		parent.getChatMessageManager.queue(queuedMessage)
	}
}
