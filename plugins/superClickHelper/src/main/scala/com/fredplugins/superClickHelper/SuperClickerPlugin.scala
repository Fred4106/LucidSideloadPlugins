package com.fredplugins.superClickHelper

import com.fredplugins.common.InterfaceTab
import com.fredplugins.common.extensions.MenuExtensions.{getNpcOpt, getWorldLocationOpt, isNpcAction, isRuneliteAction, isTileObjectAction}
import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.spells.WidgetInfo
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.ChatMessageType
import net.runelite.api.EnumComposition
import net.runelite.api.EnumID
import net.runelite.api.ItemComposition
import net.runelite.api.ParamID
import net.runelite.api.ScriptEvent
import net.runelite.api.ScriptID
import net.runelite.api.{Client, DecorativeObject, GameObject, GroundObject, MenuAction, MenuEntry, NPC, Scene, Tile, TileObject, WallObject}
import net.runelite.api.events.PostMenuSort
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.events.ScriptPreFired
import net.runelite.api.events.{GameObjectSpawned, GameTick, MenuEntryAdded, MenuOptionClicked, PostMenuSort}
import net.runelite.api.gameval.InterfaceID
import net.runelite.api.gameval.VarbitID
import net.runelite.api.widgets.JavaScriptCallback
import net.runelite.api.widgets.Widget
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread
import net.runelite.client.chat.ChatColorType
import net.runelite.client.chat.ChatMessageBuilder
import net.runelite.client.config.ConfigGroup
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

	@Inject() private val menuManager   : MenuManager    = null
	@Inject() private val clientThread   : ClientThread  = null
	@Inject() private val overlayManager: OverlayManager = null
	@Inject() private val configManager        : ConfigManager  = null
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

	var cachedWidgetValue = Option.empty[Widget]

	def overlays(): Seq[SuperClickHelperOverlay] = List(overlay/*, panel*/)
	var blockTopLevelSwitch: Boolean = false

	override protected def startUp(): Unit = {
		clickedTiles.clear()
		clickedNpcs.clear()
		blockTopLevelSwitch = false

		clientThread.invokeLater(() => this.reinitializeSpellbook())

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

		clientThread.invokeLater(() => this.reinitializeSpellbook())

		clickedNpcs.clear()
		clickedTiles.clear()
	}

	private final inline def ConfigGroupName(): String = config.getClass.getAnnotation[ConfigGroup](classOf[ConfigGroup]).value()
	private final val HIDE_UNHIDE_OP: Int = 6

	inline def getKey(spellbook: Int, spell: Int): String = {
		"spell_hidden_book_" + spellbook + "_" + spell
	}

	def isHidden(spellbook: Int, spell: Int): Boolean = {
		configManager.getConfiguration[Boolean](ConfigGroupName(), getKey(spellbook, spell), classOf[Boolean])
	}

	def setHidden(spellbook: Int, spell: Int, hidden: Boolean): Unit = {
		(hidden match {
			case true => configManager.setConfiguration[Boolean](_, _, true)
			case false => configManager.unsetConfiguration(_, _)
		})(ConfigGroupName(), getKey(spellbook, spell))
	}
	def widgetToNiceString(w: Widget): String ={
		val idxStr = (if (w.getIndex > -1) s" [${w.getIndex}]" else "")
		s"${WidgetInfo.TO_GROUP(w.getId)}.${WidgetInfo.TO_CHILD(w.getId)}${idxStr}"
	}

	def initializeSpells(spellBookEnum: Int): IndexedSeq[(Int, ((Int, ItemComposition), (Int, Widget)))] = {
		val spellbook = client.getEnum(spellBookEnum)
		log.info("initializeSpells({}), spellbook.size() = {}", spellBookEnum, spellbook.size)
		val spellsList = for{
			//i
//			objId = spellbook.getIntValue(i)
			objId <- (0 until spellbook.size()).map(spellbook.getIntValue)
			objDef = client.getItemDefinition(objId)
			component = objDef.getIntValue(ParamID.SPELL_BUTTON)
			w = client.getWidget(component)
		} yield((objId, objDef), (component, w))

		spellsList.map(u => spellBookEnum -> u).tapEach {
			case (i, ((spellObjId, composition), (component, w))) => {
				val newOnOpListener = Option(w.getOnOpListener()).pipe(oldListener => {
					new JavaScriptCallback {
						override def run(e: ScriptEvent): Unit = {
							if (e.getOp == HIDE_UNHIDE_OP + 1) {
								val s                          = e.getSource
								// Spells can be shared between spellbooks, so we can't assume spellBookEnum is the current spellbook.
								// from ~magic_spellbook_redraw
								val subSpellBookId_varbit_book = client.getVarbitValue(VarbitID.SPELLBOOK)
								val subSpellbookId             = client.getEnum(EnumID.SPELLBOOKS_SUB).getIntValue(subSpellBookId_varbit_book)
								log.info("VarbitID.SPELLBOOK({}) => subSpellbookId({})", subSpellBookId_varbit_book, subSpellbookId)
								val spellBookId_varbit_book_sublist = client.getVarbitValue(VarbitID.SPELLBOOK_SUBLIST)
								val spellbookId                     = client.getEnum(subSpellbookId).getIntValue(spellBookId_varbit_book_sublist)
								log.info("VarbitID.SPELLBOOK_SUBLIST({}) => spellbookId({})", spellBookId_varbit_book_sublist, spellbookId)
								var hidden = isHidden(spellbookId, spellObjId)
								hidden = !hidden
								log.debug("Changing {} to hidden: {}", s.getName, hidden)
								setHidden(spellbookId, spellObjId, hidden)
								s.setOpacity(if (hidden) 100 else 0)
								s.setAction(HIDE_UNHIDE_OP, if (hidden) "Unhide" else "Hide")
							} else {
								oldListener.foreach(old => client.runScript(old *))
								//							client.runScript(opListener *)
							}
						}
					}
				})
				w.setOnOpListener(newOnOpListener)
			}
		}

//		var i = 0
//		while (i < spellbook.size) {
//			val spellObjId     = spellbook.getIntValue(i)
//			val spellObjDef       = client.getItemDefinition(spellObjId)
//			val spellComponent = spellObjDef.getIntValue(ParamID.SPELL_BUTTON)
//			val w              = client.getWidget(spellComponent)
//			log.info("spellbook[{}] = (spellObj(id, def)=({}, {}), spell(component, widget)=({}, {})", i, spellObjId, spellObjDef, spellComponent, widgetToNiceString(w))
//			// spells with no target mask have an existing op listener, capture it to
//			// call it later
//			val opListener: Array[AnyRef] = w.getOnOpListener()
//			w.setOnOpListener(
//				new JavaScriptCallback {
//					override def run(e: ScriptEvent): Unit = {
//						if (e.getOp == HIDE_UNHIDE_OP + 1) {
//							val s                          = e.getSource
//							// Spells can be shared between spellbooks, so we can't assume spellBookEnum is the current spellbook.
//							// from ~magic_spellbook_redraw
//							val subSpellBookId_varbit_book = client.getVarbitValue(VarbitID.SPELLBOOK)
//							val subSpellbookId             = client.getEnum(EnumID.SPELLBOOKS_SUB).getIntValue(subSpellBookId_varbit_book)
//							log.info("VarbitID.SPELLBOOK({}) => subSpellbookId({})", subSpellBookId_varbit_book, subSpellbookId)
//							val spellBookId_varbit_book_sublist = client.getVarbitValue(VarbitID.SPELLBOOK_SUBLIST)
//							val spellbookId                     = client.getEnum(subSpellbookId).getIntValue(spellBookId_varbit_book_sublist)
//							log.info("VarbitID.SPELLBOOK_SUBLIST({}) => spellbookId({})", spellBookId_varbit_book_sublist, spellbookId)
//							var hidden = isHidden(spellbookId, spellObjId)
//							hidden = !hidden
//							log.debug("Changing {} to hidden: {}", s.getName, hidden)
//							setHidden(spellbookId, spellObjId, hidden)
//							s.setOpacity(if (hidden) 100 else 0)
//							s.setAction(HIDE_UNHIDE_OP, if (hidden) "Unhide" else "Hide")
//						} else if (opListener != null) {
//							client.runScript(opListener *)
//						}
//					}
//				}
//			)
//			i += 1
//		}
	}

	private def reinitializeSpellbook(): Unit = {
		val w = client.getWidget(InterfaceID.MagicSpellbook.UNIVERSE)
		if (w != null && w.getOnLoadListener() != null){
			client.createScriptEvent(w.getOnLoadListener() *).setSource(w).run()
		}
	}
	val spellsWidgetTable: mutable.ListBuffer[(Int, ((Int, ItemComposition), (Int, Widget)))] = scala.collection.mutable.ListBuffer.empty[(Int, ((Int, ItemComposition), (Int, Widget)))]
	@Subscribe
	def onScriptPreFired(event: ScriptPreFired): Unit = {
		if (event.getScriptId == ScriptID.MAGIC_SPELLBOOK_INITIALISESPELLS) {
			val stack         = client.getIntStack
			val sz            = client.getIntStackSize
			val spellBookEnum = stack(sz - 12) // eg 1982, 5285, 1983, 1984, 1985

//			val i = Integer.toHexString(14286921)
			spellsWidgetTable.filterInPlace(_._1 != spellBookEnum)
			val toCache = clientThread.runOnClientThread[IndexedSeq[(Int, ((Int, ItemComposition), (Int, Widget)))]](() => initializeSpells(spellBookEnum))
			spellsWidgetTable.addAll(toCache)
		} else if (event.getScriptId == 915 && blockTopLevelSwitch) {
			val targetTabOpt = InterfaceTab.values().find(t => t.getId == Int.unbox(event.getScriptEvent.getArguments.apply(1)))
			if (targetTabOpt.contains(InterfaceTab.SPELLBOOK)) {
				event.getScriptEvent.getArguments.update(1, Int.box(InterfaceTab.INVENTORY.getId))
				blockTopLevelSwitch = false
			}
		}
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

			client.addChatMessage(ChatMessageType.FRIENDSCHAT, "SuperClicker", s"SelectedWidget changed from '${cachedWidgetValue.map(widgetToNiceString)}' to '${nWidgetValue.map(widgetToNiceString)}'", "gametick")
			cachedWidgetValue = nWidgetValue
		}

//		cachedWidgetValue = EthanApiPlugin.getSelectedWidget.toScala
	}

	def findSpell(group: Int, id: Int): Option[(Int, ((Int, ItemComposition), (Int, Widget)))] = {
		spellsWidgetTable.toList.find(e => {
			val (egroup, eid) = e._2._2._2.getId.pipe(eid => WidgetInfo.TO_GROUP(eid) -> WidgetInfo.TO_CHILD(eid))
			egroup == group && eid == id
		})
	}

	@Subscribe
	def onMenuEntryAdded(menuOptionAdded: MenuEntryAdded): Unit = {
		val me = menuOptionAdded.getMenuEntry
		val meData = (me.getType, me.getParam0, me.getParam1, me.getIdentifier)
		if(!blockTopLevelSwitch) {
			if(client.getMenu.getMenuEntries.contains(me) && me.getType == MenuAction.WIDGET_TARGET && me.getParam1 == InterfaceID.Inventory.ITEMS) {
//				val subSpellbookId            = client.getEnum(EnumID.SPELLBOOKS_SUB).getIntValue(client.getVarbitValue(VarbitID.SPELLBOOK))
//				val widgetsTable: Seq[Widget] = client.getEnum(subSpellbookId).getIntVals.toList.flatMap(spellbookId => {//.getIntValue(client.getVarbitValue(VarbitID.SPELLBOOK_SUBLIST))
//					val spellbook: EnumComposition = client.getEnum(spellbookId)
//					for {
//						i <- 0 until spellbook.size()
//					} yield {
//						val spellObj = client.getItemDefinition(spellbook.getIntValue(i))
//						val w        = client.getWidget(spellObj.getIntValue(ParamID.SPELL_BUTTON))
//						w
//					}
//				})
//				widgetsTable.zipWithIndex.foreach((xxx) => log.debug("widget[{}] = (({}, {}), {})", xxx._2, WidgetInfo.TO_GROUP(xxx._1.getId), WidgetInfo.TO_CHILD(xxx._1.getId), xxx._1.getIndex))

				val spellSearchOpt = (if(me.getItemId == 12011) {
					findSpell(218, 133)
				} else if(me.getItemId == 6332) {
					findSpell(218, 133)
//					widgetsTable.find(w => w.getId == InterfaceID.MagicSpellbook.PLANK_MAKE).foreach(spellWidget => {
//						client.getMenu.createMenuEntry(-1)
//							.setOption("Cast".colored(Color.BLUE))
//							.setType(MenuAction.WIDGET_TARGET)
//							.setIdentifier(0)
//							.setParam0(-1)
//							.setParam1(spellWidget.getId)
//							.onClick(e => {
//								blockTopLevelSwitch = true
//							});
//					})
				} else if(me.getWidget.getItemId == 21111 || me.getWidget.getItemId == 1639) {
					findSpell(218, 24)
//					widgetsTable.find(w => w.getId == InterfaceID.MagicSpellbook.ENCHANT_2).foreach(spellWidget => {
//						client.getMenu.createMenuEntry(-1)
//							.setOption("Cast".colored(Color.BLUE))
//							.setType(MenuAction.WIDGET_TARGET)
//							.setIdentifier(0)
//							.setParam0(-1)
//							.setParam1(spellWidget.getId)
//							.onClick(e => {
//								blockTopLevelSwitch = true
//							});
//					})
				} else Option.empty)

				spellSearchOpt.foreach{
					case (_, (_, (cid, w))) => {
						client.getMenu.createMenuEntry(-1)
							.setOption("Cast".colored(Color.BLUE))
							.setType(MenuAction.WIDGET_TARGET)
							.setIdentifier(0)
							.setParam0(-1)
							.setParam1(cid)
							.onClick(e => {
								blockTopLevelSwitch = true
							});
					}
				}
			}
		}
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

