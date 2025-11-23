package com.fredplugins.scriptMaster

import com.fredplugins.common.utils.ShimUtils
import com.google.inject.Inject
import com.google.inject.Provides
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.Client
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDependency
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.plugins.PluginDescriptor

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import net.runelite.client.{Notifier, RuneLite}
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigItem
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.game.{ItemManager, SpriteManager, WorldService}
import net.runelite.client.plugins.PluginManager
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.{ClientToolbar, NavigationButton}
import net.runelite.client.ui.overlay.OverlayManager

import javax.inject.Singleton
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

@PluginDescriptor(
	name = "<html><font color=\"#CDA400\">Plugin Master</font></html>",
	description = "Provides a scripting environment for runtime loadable mini-plugins",
	tags = Array(
		"scripting", "helper"
		, "utility", "scala"
	),
	enabledByDefault = true
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class ScriptMasterPlugin() extends Plugin with ShimUtils.Logging("DEBUG") {
	@Inject private val client       : Client        = null
	@Inject private val clientThread : ClientThread  = null
	@Inject private val pluginManager: PluginManager = null
	@Inject private val notifier     : Notifier      = null
	@Inject private val configManager: ConfigManager = null
	@Inject private val eventBus      : EventBus       = null
	@Inject private val spriteManager : SpriteManager  = null
	@Inject private val itemManager   : ItemManager    = null
	@Inject private val overlayManager: OverlayManager = null
	@Inject private val worldService : WorldService  = null

	@Inject private val config: ScriptMasterConfig = null

	private var _initializedLock: Int = 0
//	def isInitialized(): Boolean =
	def initialize(): Boolean = {
		if(_initializedLock == 0) {
			_initializedLock = 1
			true
		} else {
			false
		}
	}
	def uninitialize(): Boolean = {
		if(_initializedLock == 1) {
			_initializedLock = 2
			true
		} else {
			false
		}
	}

	lazy val engine: ScriptEngine = {
		ScriptEngineManager().getEngineByName("Scala REPL")
	}
	override def startUp(): Unit = {
		assert(initialize());
		log.debug("startup!");
		engine
	}

	override def shutDown(): Unit = {
		assert(uninitialize());
		log.debug("shutdown!");
	}

	@Subscribe
	def onConfigChanged(configChanged: ConfigChanged): Unit = {
		import ScriptMasterConfig.KEYS._
		Option(configManager.getConfigDescriptor(config)).filter(_.getGroup.value().equals(configChanged.getGroup)).map(_.getItems.asScala.toList)
			.flatMap(_.find(i => i.getItem.keyName() == configChanged.getKey)).map(i => (if (i.getType == classOf[Int]) configManager.getConfiguration[Int](GROUP, i.getItem.keyName, i.getType) else if (i.getType == classOf[Boolean]) configManager.getConfiguration[Boolean](GROUP, i.getItem.keyName(), i.getType) else configManager.getConfiguration[String](GROUP, i.getItem.keyName(), classOf[String])) -> i.getItem.keyName()).map(_.swap)
		match {
			case Some((SOURCE_CODE, src: String)) => {
				log.debug("SOURCE_CODE updated!\n{}", src);
			}
			case Some((DEBUG_STRING, debugStr:String)) => {
				log.debug("DEBUG_STRING updated!\n{}", debugStr);
			}
			case Some(COMPILE_SIGNEL -> true) => {
				configManager.setConfiguration[Boolean](GROUP, COMPILE_SIGNEL, false);
				log.debug("Compile signal detected!");
				log.debug("result {}", engine.eval(config.sourceCode()))
			}
			case _ =>
		}
//		if(configChanged.getGroup == GROUP) {
//			configChanged.getKey match {
//				case COMPILE_SIGNEL if config.compileSignal() == true => {}
//
//			}
//		}
	}

	@Provides
	@Singleton
	def provideConfig(configManager: ConfigManager): ScriptMasterConfig = {
		configManager.getConfig(classOf[ScriptMasterConfig]);
	}

}