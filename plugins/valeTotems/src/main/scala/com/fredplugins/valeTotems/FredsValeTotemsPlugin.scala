package com.fredplugins.valeTotems

import com.fredplugins.common.extensions.MenuExtensions.{getNpcOpt, getWorldLocationOpt, isNpcAction, isRuneliteAction, isTileObjectAction}
import com.fredplugins.common.utils.ShimUtils
import com.google.gson.Gson
import com.google.inject.{Inject, Provides, Singleton}
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.ChatMessageType
import net.runelite.api.GameState
import net.runelite.api.Skill
import net.runelite.api.{Client, DecorativeObject, GameObject, GroundObject, MenuAction, MenuEntry, NPC, Scene, Tile, TileObject, WallObject}
import net.runelite.api.events.PostMenuSort
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.events.GameStateChanged
import net.runelite.api.events.InteractingChanged
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.StatChanged
import net.runelite.api.events.VarbitChanged
import net.runelite.api.events.{GameObjectSpawned, GameTick, MenuEntryAdded, MenuOptionClicked, PostMenuSort}
import net.runelite.api.gameval.VarbitID
import net.runelite.api.gameval.VarbitID.{NTK_CURRENT_ROOM_LEVEL, NTK_DOOR1_STATE, NTK_DOOR2_STATE, NTK_DOOR3_STATE, NTK_DOOR4_STATE, NTK_GOLDEN_CHEST_STATE, NTK_OUTSIDE_DOOR1_STATE, NTK_OUTSIDE_DOOR2_STATE, NTK_OUTSIDE_DOOR3_STATE, NTK_OUTSIDE_DOOR4_STATE, NTK_PLAYED_BEFORE, NTK_PLAYER_TIMER_COUNT, NTK_ROOM_NUMBER, NTK_SARCOPHAGUS_PUSH, NTK_SARCOPHAGUS_STATE, NTK_TRAP_ACTIVE, NTK_URN10_STATE, NTK_URN11_STATE, NTK_URN12_STATE, NTK_URN13_STATE, NTK_URN14_STATE, NTK_URN15_STATE, NTK_URN1_STATE, NTK_URN2_STATE, NTK_URN3_STATE, NTK_URN4_STATE, NTK_URN5_STATE, NTK_URN6_STATE, NTK_URN7_STATE, NTK_URN8_STATE, NTK_URN9_STATE}
import net.runelite.api.widgets.Widget
import net.runelite.client.chat.ChatColorType
import net.runelite.client.chat.ChatMessageBuilder
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.menus.MenuManager
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import java.awt.Color
import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.util.chaining.*
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*


@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Totem Fletching</html>",
	description = "A plugin to support fletching in Ashenvale.",
	tags = Array("Ashenvale","fletching","fletchtodt","minigame","totem","vale","valetotems","vale totem", "fred4106")
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsValeTotemsPlugin() extends Plugin {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	@Inject() private val eventBus: EventBus = null
	@Inject() private val client  : Client   = null
	given Client = client

	@Inject() private val menuManager   : MenuManager                  = null
	@Inject() private val overlayManager: OverlayManager               = null
	@Inject() private val config        : FredsValeTotemsConfig        = null
	@Inject() private val overlay       : FredsValeTotemsOverlay = null
	@Inject() private val GSON: Gson                                       = null
	@Inject() private val totemService: TotemService                                       = null

	@Provides
	def getConfig(configManager: ConfigManager): FredsValeTotemsConfig = {
		configManager.getConfig[FredsValeTotemsConfig](classOf[FredsValeTotemsConfig])
	}

	def buildMessage(header: String, parts: (String, Any)*): String = {
		def add(func: ChatMessageBuilder => ChatMessageBuilder)(chatMessageBuilder: ChatMessageBuilder => ChatMessageBuilder) = chatMessageBuilder
			.andThen(func)

		parts.foldLeft(new ChatMessageBuilder().append(ChatColorType.NORMAL).append(header + "\n"))((a, b) => {
			(b._2 match {
				case (c: Color, s: Any) => Option(add(_.append(c, s.toString)))
				case null => Option.empty
				case x => Option(add(_.append(Color.BLUE, x.toString)))
			}).map(_.apply(_.append("[").append(b._1).append(": ")).andThen(_.append(ChatColorType.NORMAL).append("]")))
				.map(_.apply(a))
				.getOrElse(a)
		}).build().stripTrailing().stripSuffix(",").stripSuffix("<br>")
	}

	override protected def startUp(): Unit = {
		eventBus.register(overlay)
		overlayManager.add(overlay)
	}
	override protected def shutDown(): Unit = {
		overlayManager.remove(overlay)
		eventBus.unregister(overlay)
	}

	@Subscribe
	def onGameTick(e: GameTick): Unit = {
		totemService.updateClosestTotem(client.getLocalPlayer())
	}

	@Subscribe
	def onVarbitChanged(event: VarbitChanged): Unit = {
		totemService.onVarbitChanged(event)
	}
}
