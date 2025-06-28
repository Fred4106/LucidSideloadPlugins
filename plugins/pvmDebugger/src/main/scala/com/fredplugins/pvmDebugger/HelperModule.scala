package com.fredplugins.pvmDebugger

import com.fredplugins.common.utils.ShimUtils
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
import org.slf4j.Logger

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
	val config: Config & {
		def enabled(): Boolean
	}// = parent.getConfigManager.getConfig[C](classTag[C].runtimeClass.asInstanceOf[Class[C]])
	val moduleName: String
	protected final lazy val log: Logger = ShimUtils.getLogger(moduleName, "DEBUG")
	val configGroup: String = parent.getConfigManager.getConfigDescriptor(config).getGroup.value()/*config.getAnnotation(classOf[ConfigGroup]).value()*/
	def isRegistered(): Boolean = parent.getEventBus.isRegistered(this)
	def isEnabled(): Boolean = {
//		if(enabledValue == null) parent.getConfigManager.setConfiguration(configGroup, "enabled", classOf[java.lang.Boolean])
//		parent.getConfigManager.getConfiguration(configGroup, "enabled", classOf[java.lang.Boolean])
		parent.getConfigManager.getConfiguration(configGroup, "enabled").toBooleanOption.getOrElse {
			false.tap(b => parent.getConfigManager.setConfiguration(configGroup, "enabled", b.toString))
		}
	}

	protected def init(): Unit
	protected def cleanup(): Unit

	final def startup(): Unit = {
		if(!isRegistered() && isEnabled()) {
			init()
			log.debug(s"Initializing ${moduleName}")
			Option(this).collect {
				case swp: WithOverlay => swp.overlay
			}.foreach(o => parent.getOverlayManager.add(o))
			Option(this).collect {
				case swp: WithPanel => swp.panel
			}.foreach(o => parent.getOverlayManager.add(o))

			parent.getEventBus.register(this)
		}
	}

	final def shutdown(): Unit = {
		if (isRegistered()) {
			parent.getEventBus.unregister(this)
			Option(this).collect {
				case swp: WithOverlay => swp.overlay
			}.foreach(o => parent.getOverlayManager.remove(o))
			Option(this).collect {
				case swp: WithPanel => swp.panel
			}.foreach(o => parent.getOverlayManager.remove(o))
			cleanup()
			log.debug(s"Cleaning up ${moduleName}")
		}
	}

	def printMessage(tpe: ChatMessageType, sender: String)(msg: ChatMessageBuilder): Unit = {
		val queuedMessage = QueuedMessage.builder().`type`(tpe).sender(sender).name(sender).runeLiteFormattedMessage(msg.build()).build()
//		val msg = (new ChatMessageBuilder).append("Can't initialize, unable to find barriers. Found: " + barriers.size).build
		parent.getChatMessageManager.queue(queuedMessage)
	}

	def printMessage(tpe: ChatMessageType, sender: String, name: String)(msg: ChatMessageBuilder): Unit = {
		val queuedMessage = QueuedMessage.builder().`type`(tpe).sender(sender).name(name).runeLiteFormattedMessage(msg.build()).build()
		//		val msg = (new ChatMessageBuilder).append("Can't initialize, unable to find barriers. Found: " + barriers.size).build
		parent.getChatMessageManager.queue(queuedMessage)
	}
}
