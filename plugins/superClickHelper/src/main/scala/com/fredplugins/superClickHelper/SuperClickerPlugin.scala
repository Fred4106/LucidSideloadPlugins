package com.fredplugins.superClickHelper

import com.fredplugins.common.extensions.MenuExtensions.{getNpcOpt, getWorldLocationOpt, isNpcAction, isRuneliteAction, isTileObjectAction}
import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.spells.WidgetInfo
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.ChatMessageType
import net.runelite.api.{Client, DecorativeObject, GameObject, GroundObject, MenuAction, MenuEntry, NPC, Scene, Tile, TileObject, WallObject}
import net.runelite.api.events.PostMenuSort
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.events.{GameObjectSpawned, GameTick, MenuEntryAdded, MenuOptionClicked, PostMenuSort}
import net.runelite.api.widgets.Widget
import net.runelite.client.RuneLite
import net.runelite.client.chat.ChatColorType
import net.runelite.client.chat.ChatMessageBuilder
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.externalplugins.ExternalPluginManager
import net.runelite.client.menus.MenuManager
import net.runelite.client.plugins.PluginManager
import net.runelite.client.plugins.emojis.EmojiPlugin
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.util.ColorUtil
import net.runelite.client.util.Text
import org.slf4j.Logger

import java.awt.Color
import scala.collection.mutable
import scala.util.chaining.*
import scala.util.Try
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Super Clicker</html>",
	description = "Adds various click menus to some tedious tasks",
	tags = Array("widget", "interface", "click", "helper", "fred4106"),
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class SuperClickerPlugin() extends Plugin {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	@Inject() private val eventBus: EventBus = null
	@Inject() private val client  : Client   = null
	given Client = client

	@Inject() private val menuManager   : MenuManager                   = null
	@Inject() private val overlayManager: OverlayManager                = null
	@Inject() private val config        : SuperClickHelperConfig  = null
	@Inject() private val overlay       : SuperClickHelperOverlay = null
//	@Inject() private val panel       : SuperClickHelperPanel = null

	private val clickedTiles: mutable.ListBuffer[(Int, WorldPoint)] = mutable.ListBuffer.empty
	private val clickedNpcs: mutable.ListBuffer[(Int, NPC)] = mutable.ListBuffer.empty
	def getClickedState: (List[(Int, NPC)], List[(Int, WorldPoint)]) = {
		clickedNpcs.toList -> clickedTiles.toList
	}

	@Provides
	def getConfig(configManager: ConfigManager): SuperClickHelperConfig = {
		configManager.getConfig[SuperClickHelperConfig](classOf[SuperClickHelperConfig])
	}

	object SrValue {
		case class SrType(group: Int, child: Int) {
			def packed: Int = WidgetInfo.PACK(group, child)
		}
		private lazy val field = client.getClass.getClassLoader.loadClass("ph").getDeclaredField("sr").tap(_.setAccessible(true))
		private val readKey = 2100281859
		private val writeKey = -2068285269
		private inline def readField: SrType = Option(field.get(null).asInstanceOf[Integer]).map(_.intValue() * readKey).fold[SrType](SrType(-1, -1))(x => SrType(WidgetInfo.TO_GROUP(x), WidgetInfo.TO_CHILD(x)))
		private var cachedValue: SrType = readField

		def get(): SrType = if(cachedValue != null) cachedValue else readField.tap(rv => cachedValue = rv)

		def update(): Option[(SrType, SrType)] = {
			readField.pipe(nv => Option(cachedValue -> nv).filter(a => a._1 != a._2)).tapEach {
				case (oldV, newV) => cachedValue = newV
			}.headOption
		}

		def set(n: SrType): Unit = {
			if(n != get()) field.set(null, n.packed * writeKey)
		}
	}
	object SbValue {
//		case class SbType(group: Int, child: Int) {
//			def packed: Int = WidgetInfo.PACK(group, child)
//		}
		private lazy val field    = client.getClass.getClassLoader.loadClass("client").getDeclaredField("sb").tap(_.setAccessible(true))
		private      val readKey  = -1805685543
		private      val writeKey = -438152343
		private inline def readField: Int = Option(field.get(null).asInstanceOf[Integer]).map(_.intValue() * readKey).fold(-1)(x => x)
		private var cachedValue: Int = readField

		def get(): Int = cachedValue

		def update(): Option[(Int, Int)] = {
			readField.pipe(nv => Option(cachedValue -> nv).filter(a => a._1 != a._2)).tapEach {
				case (oldV, newV) => cachedValue = newV
			}.headOption
		}

		def set(n: Int): Unit = {
			if (n != get()) field.set(null, n * writeKey)
		}
	}
	var cachedWidgetValue = Option.empty[Widget]

	def overlays(): Seq[SuperClickHelperOverlay] = List(overlay/*, panel*/)

	override protected def startUp(): Unit = {
		clickedTiles.clear()
		clickedNpcs.clear()

		overlays().foreach(o => {
			eventBus.register(o)
			overlayManager.add(o)
		})
	}
	override protected def shutDown(): Unit = {
		overlays().foreach(o => {
			overlayManager.remove(o)
			eventBus.unregister(o)
		})


		clickedNpcs.clear()
		clickedTiles.clear()
	}

	@Subscribe
	def onGameTick(event: GameTick): Unit = {
		clickedTiles.flatMapInPlace {
			case (i, point) if i < 20 => Some((i + 1, point))
			case (_, point) => None
		}
		clickedNpcs.flatMapInPlace {
			case (i, point) if i < 20 => Some((i + 1, point))
			case (_, point) => None
		}
//		SrValue.update().foreach {
//			case (o, n) => client.addChatMessage(ChatMessageType.FRIENDSCHAT, "SuperClicker", s"ph.sr changed from '${o}' to '${n}'", "gametick")
//		}
//		SbValue.update().foreach {
//			case (o, n) => client.addChatMessage(ChatMessageType.FRIENDSCHAT, "SuperClicker", s"client.sb changed from '${o}' to '${n}'", "gametick")
//		}
		val nWidgetValue = EthanApiPlugin.getSelectedWidget.toScala
		if(cachedWidgetValue != nWidgetValue) {
			def widgetToStr(w: Widget): ((Int, Int), Int) = {
					(
						WidgetInfo.TO_GROUP(w.getId),
						WidgetInfo.TO_CHILD(w.getId)
					) -> w.getIndex
			}
			client.addChatMessage(ChatMessageType.FRIENDSCHAT, "SuperClicker", s"SelectedWidget changed from '${cachedWidgetValue.map(widgetToStr)}' to '${nWidgetValue.map(widgetToStr)}'", "gametick")
			cachedWidgetValue = nWidgetValue
		}

//		cachedWidgetValue = EthanApiPlugin.getSelectedWidget.toScala
	}

	@Subscribe
	def onMenuEntryAdded(menuOptionAdded: MenuEntryAdded): Unit = {
//		val targetOpt = MenuEntryTarget(menuOptionAdded)
//		targetOpt.foreach(met => log.info("Transformed {} into {}", menuOptionAdded, met))
//		if (targetOpt.isEmpty) {
//			log.debug("Cant handle {} yet", menuOptionAdded)
//		}
	}

	@Subscribe(priority = -15)
	def onPostMenuSort(postMenuSort: PostMenuSort): Unit = {
		if (!client.isMenuOpen) {
			val menuEntries   : List[MenuEntry] = client.getMenuEntries.toList
			val (added, stock)                  = menuEntries.partition(e => {
				false
			})
			val newMenuEntries: List[MenuEntry] = stock.appendedAll(added)
			client.setMenuEntries(newMenuEntries.toArray[MenuEntry])
		}
	}

	extension (s: String) {
		def colored(c: Color): String = s"${ColorUtil.colorTag(c)}$s${ColorUtil.CLOSING_COLOR_TAG}"
		def icon(iconId:Int): String = s"<img=${iconId}>"
//		def emoji(e: ): String = s"<${}>"
	}

	@Subscribe
	def onMenuOptionClicked(menuOptionClicked: MenuOptionClicked): Unit = {
		if(config.isDebugMenu) {
			val paramColor = new Color(0xC06A09)
			val messageParts = Seq(
				menuOptionClicked.getMenuAction.name().colored(Color.blue).appendedAll("("),
				"id".colored(paramColor).appendedAll("=").appendedAll(s"${menuOptionClicked.getId}".colored(Color.GREEN)).appendedAll(", "),
				"params".colored(paramColor).appendedAll("=(").appendedAll(s"${menuOptionClicked.getParam0}".colored(Color.CYAN)).appendedAll(", ").appendedAll(s"${menuOptionClicked.getParam1}".colored(Color.CYAN)).appendedAll("), "),
				"option".colored(paramColor).appendedAll("=").appendedAll(s"${Text.escapeJagex(menuOptionClicked.getMenuOption)}".colored(Color.MAGENTA)).appendedAll(", "),
				"target".colored(paramColor).appendedAll("=").appendedAll(s"${Text.escapeJagex(menuOptionClicked.getMenuTarget)}".colored(new Color(100, 100, 200))).appendedAll(")")
			)

//			val message = " id=`${menuOptionClicked.getId}`, params=`${menuOptionClicked.pipe(j => j.getParam0 -> j.getParam1)}`, option=`${menuOptionClicked.getMenuOption}`, target=`${menuOptionClicked.getMenuTarget}`)"
			val message = messageParts.fold("")(_.appendedAll(_))
			log.debug(Text.removeFormattingTags(message))
			client.addChatMessage(ChatMessageType.FRIENDSCHAT, "SuperClicker", message , s"clicked${client.getTickCount}")
		}
//		val targetOpt = MenuEntryTarget(menuOptionClicked)
//		targetOpt.foreach(met => log.info("Transformed {} into {}", menuOptionClicked, met))
//		if(targetOpt.isEmpty) {
//			log.debug("Cant handle {} yet", menuOptionClicked)
//		}

		val me = menuOptionClicked.getMenuEntry
		me.getWorldLocationOpt.foreach(
			worldPoint => {
				clickedTiles.filterInPlace {
					case (_, point) => !point.equals(worldPoint)
				}.addOne((0, worldPoint))
			}
		)
		me.getNpcOpt.foreach(
			npc => {
				clickedNpcs.filterInPlace {
					case (_, n) => !n.equals(npc)
				}.addOne((0, npc))
			}
		)
	}

}

