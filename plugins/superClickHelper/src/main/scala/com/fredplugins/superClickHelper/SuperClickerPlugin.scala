package com.fredplugins.superClickHelper

import com.fredplugins.common.InterfaceTab
import com.fredplugins.common.extensions.MenuExtensions.{getNpcOpt, getTileObjectOpt, getWorldLocationOpt, isNpcAction, isRuneliteAction, isTileObjectAction, prettyString}
import com.fredplugins.common.queries.InventoryItemQuery
import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.spells.WidgetInfo
import com.lucidplugins.api.utils.InteractionUtils
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.Inventory
import ethanApiPlugin.collections.query.ItemQuery
import net.runelite.api.ChatMessageType
import net.runelite.api.EnumComposition
import net.runelite.api.EnumID
import net.runelite.api.ItemComposition
import net.runelite.api.ItemContainer
import net.runelite.api.ParamID
import net.runelite.api.ScriptEvent
import net.runelite.api.ScriptID
import net.runelite.api.{Client, DecorativeObject, GameObject, GroundObject, MenuAction, MenuEntry, NPC, Scene, Tile, TileObject, WallObject}
import net.runelite.api.events.PostMenuSort
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.events.ItemContainerChanged
import net.runelite.api.events.PostClientTick
import net.runelite.api.events.ScriptPreFired
import net.runelite.api.events.{GameObjectSpawned, GameTick, MenuEntryAdded, MenuOptionClicked, PostMenuSort}
import net.runelite.api.gameval.InterfaceID
import net.runelite.api.gameval.InventoryID
import net.runelite.api.gameval.ItemID
import net.runelite.api.gameval.VarbitID
import net.runelite.api.widgets.JavaScriptCallback
import net.runelite.api.widgets.Widget
import net.runelite.api.widgets.WidgetItem
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
import net.runelite.api.gameval.ItemID.{ASGARNIAN_HOP_SEED, BARLEY_SEED, BIRDHOUSE_MAGIC, BIRDHOUSE_MAHOGANY, BIRDHOUSE_MAPLE, BIRDHOUSE_NORMAL, BIRDHOUSE_OAK, BIRDHOUSE_REDWOOD, BIRDHOUSE_TEAK, BIRDHOUSE_WILLOW, BIRDHOUSE_YEW, HAMMERSTONE_HOP_SEED, JUTE_SEED, KRANDORIAN_HOP_SEED, WILDBLOOD_HOP_SEED, YANILLIAN_HOP_SEED}
import net.runelite.api.gameval.ObjectID.{BIRDHOUSE_1, BIRDHOUSE_2, BIRDHOUSE_3, BIRDHOUSE_4, BIRDHOUSE_MAGIC_BIRD, BIRDHOUSE_MAGIC_BUILT, BIRDHOUSE_MAGIC_FULL, BIRDHOUSE_MAHOGANY_BIRD, BIRDHOUSE_MAHOGANY_BUILT, BIRDHOUSE_MAHOGANY_FULL, BIRDHOUSE_MAPLE_BIRD, BIRDHOUSE_MAPLE_BUILT, BIRDHOUSE_MAPLE_FULL, BIRDHOUSE_NORMAL_BIRD, BIRDHOUSE_NORMAL_BUILT, BIRDHOUSE_NORMAL_FULL, BIRDHOUSE_NOT_BUILT, BIRDHOUSE_OAK_BIRD, BIRDHOUSE_OAK_BUILT, BIRDHOUSE_OAK_FULL, BIRDHOUSE_REDWOOD_BIRD, BIRDHOUSE_REDWOOD_BUILT, BIRDHOUSE_REDWOOD_FULL, BIRDHOUSE_TEAK_BIRD, BIRDHOUSE_TEAK_BUILT, BIRDHOUSE_TEAK_FULL, BIRDHOUSE_WILLOW_BIRD, BIRDHOUSE_WILLOW_BUILT, BIRDHOUSE_WILLOW_FULL, BIRDHOUSE_YEW_BIRD, BIRDHOUSE_YEW_BUILT, BIRDHOUSE_YEW_FULL}
import net.runelite.api.gameval.ItemID.{RUNE_2H_SWORD, RUNE_ARMOURED_BOOTS, RUNE_AXE, RUNE_AXE_2H, RUNE_BATTLEAXE, RUNE_CHAINBODY, RUNE_CLAWS, RUNE_DAGGER, RUNE_DAGGER_P, RUNE_DAGGER_P_, RUNE_DAGGER_P__, RUNE_FULL_HELM, RUNE_HALBERD, RUNE_KITESHIELD, RUNE_LONGSWORD, RUNE_MACE, RUNE_MED_HELM, RUNE_PICKAXE, RUNE_PLATEBODY, RUNE_PLATELEGS, RUNE_PLATESKIRT, RUNE_SCIMITAR, RUNE_SPEAR, RUNE_SPEAR_P, RUNE_SPEAR_P_, RUNE_SPEAR_P__, RUNE_SQ_SHIELD, RUNE_SWORD, RUNE_THROWNAXE, RUNE_WARHAMMER}

import net.runelite.api.gameval.ItemID.{ADAMANT_2H_SWORD, ADAMANT_ARMOURED_BOOTS, ADAMANT_AXE, ADAMANT_AXE_2H, ADAMANT_BATTLEAXE, ADAMANT_CHAINBODY, ADAMANT_CLAWS, ADAMANT_DAGGER, ADAMANT_DAGGER_P, ADAMANT_DAGGER_P_, ADAMANT_DAGGER_P__, ADAMANT_FULL_HELM, ADAMANT_HALBERD, ADAMANT_KITESHIELD, ADAMANT_LONGSWORD, ADAMANT_MACE, ADAMANT_MED_HELM, ADAMANT_PICKAXE, ADAMANT_PLATEBODY, ADAMANT_PLATELEGS, ADAMANT_PLATESKIRT, ADAMANT_SCIMITAR, ADAMANT_SPEAR, ADAMANT_SPEAR_P, ADAMANT_SPEAR_P_, ADAMANT_SPEAR_P__, ADAMANT_SQ_SHIELD, ADAMANT_SWORD, ADAMNT_THROWNAXE, ADAMNT_WARHAMMER}

import net.runelite.api.gameval.ItemID.{MITHRIL_2H_SWORD, MITHRIL_ARMOURED_BOOTS, MITHRIL_AXE, MITHRIL_AXE_2H, MITHRIL_BATTLEAXE, MITHRIL_CHAINBODY, MITHRIL_CLAWS, MITHRIL_DAGGER, MITHRIL_DAGGER_P, MITHRIL_DAGGER_P_, MITHRIL_DAGGER_P__, MITHRIL_FULL_HELM, MITHRIL_HALBERD, MITHRIL_KITESHIELD, MITHRIL_LONGSWORD, MITHRIL_MACE, MITHRIL_MED_HELM, MITHRIL_PICKAXE, MITHRIL_PLATEBODY, MITHRIL_PLATELEGS, MITHRIL_PLATESKIRT, MITHRIL_SCIMITAR, MITHRIL_SPEAR, MITHRIL_SPEAR_P, MITHRIL_SPEAR_P_, MITHRIL_SPEAR_P__, MITHRIL_SQ_SHIELD, MITHRIL_SWORD, MITHRIL_THROWNAXE, MITHRIL_WARHAMMER}
import net.runelite.api.gameval.ItemID.{
	MAPLE_SHORTBOW, MAPLE_LONGBOW, YEW_SHORTBOW, YEW_LONGBOW, MAGIC_LONGBOW,
	AIR_BATTLESTAFF, FIRE_BATTLESTAFF, EARTH_BATTLESTAFF, WATER_BATTLESTAFF
}
import net.runelite.api.gameval.ItemID.{
	ARROW_SHAFT, FEATHER,
	HEADLESS_ARROW, SLAYER_BROAD_ARROWHEAD,
	MAHOGANY_LOGS, TEAK_LOGS,
	UNSTRUNG_OPAL_AMULET, UNSTRUNG_JADE_AMULET, UNSTRUNG_TOPAZ_AMULET,
	UNSTRUNG_GOLD_AMULET, UNSTRUNG_SAPPHIRE_AMULET, UNSTRUNG_EMERALD_AMULET,
	UNSTRUNG_RUBY_AMULET, UNSTRUNG_DIAMOND_AMULET, UNSTRUNG_DRAGONSTONE_AMULET,
	UNSTRUNG_ONYX_AMULET, UNSTRUNG_ZENYTE_AMULET,
	OPAL_RING, OPAL_NECKLACE, STRUNG_OPAL_AMULET, OPAL_BRACELET,
	JADE_RING, JADE_NECKLACE, STRUNG_JADE_AMULET, JADE_BRACELET,
	TOPAZ_RING, TOPAZ_NECKLACE, STRUNG_TOPAZ_AMULET, TOPAZ_BRACELET
}

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



	extension (s: Any) {
		def colored(c: Color): String = s"${ColorUtil.colorTag(c)}${s.toString}${ColorUtil.CLOSING_COLOR_TAG}"
//		def icon(iconId:Int): String = s"<img=${iconId}>"
//		def emoji(e: ): String = s"<${}>"
	}

	def sendChatMessage(name: String)(message: => String): Unit = {
		client.addChatMessage(ChatMessageType.FRIENDSCHAT, name, s"${ColorUtil.CLOSING_COLOR_TAG}${message}", "SuperClicker", false)
	}


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

	var cookingHelper: CookingHelper = null

	override protected def startUp(): Unit = {
		cookingHelper = new CookingHelper(this, client, clientThread)

		clickedTiles.clear()
		clickedNpcs.clear()
		blockTopLevelSwitch = false
		spellsWidgetTable.clear()
		priorityMenuEntries.clear()
		clientThread.invokeLater(() => this.reinitializeSpellbook())

		overlays().foreach(o => {
			eventBus.register(o)
			overlayManager.add(o)
		})
		eventBus.register(cookingHelper)
	}

	override protected def shutDown(): Unit = {
		overlays().foreach(o => {
			overlayManager.remove(o)
			eventBus.unregister(o)
		})

		blockTopLevelSwitch = false
		spellsWidgetTable.clear()
		priorityMenuEntries.clear()
		clientThread.invokeLater(() => this.reinitializeSpellbook())

		clickedNpcs.clear()
		clickedTiles.clear()

		eventBus.unregister(cookingHelper)
		cookingHelper=  null
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
		val idxStr = (if (w.getIndex > -1) s" [${w.getIndex.colored(Color.BLUE)}]" else "")
		s"${WidgetInfo.TO_GROUP(w.getId).colored(Color.GREEN)}.${WidgetInfo.TO_CHILD(w.getId).colored(Color.GREEN)}${idxStr}"
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
	}

	private def reinitializeSpellbook(): Unit = {
		val w = client.getWidget(InterfaceID.MagicSpellbook.UNIVERSE)
		if (w != null && w.getOnLoadListener() != null){
			client.createScriptEvent(w.getOnLoadListener() *).setSource(w).run()
		}
	}
	val spellsWidgetTable: mutable.ListBuffer[(Int, ((Int, ItemComposition), (Int, Widget)))] = scala.collection.mutable.ListBuffer.empty[(Int, ((Int, ItemComposition), (Int, Widget)))]
	val priorityMenuEntries: mutable.ListBuffer[MenuEntry] = scala.collection.mutable.ListBuffer.empty[MenuEntry]


	def search(ids: Int*): Option[WidgetItem] = {
		new InventoryItemQuery().idEquals(ids *).result(client).asScala.toList
			.sortBy(u => (u.getId.min(65535).max(0) << 8) | (u.getWidget.getIndex.min(255).max(0))).headOption
	}
	private val processedGameObjects: mutable.ListBuffer[TileObject] = mutable.ListBuffer.empty
	private var birdhouseItem_c: WidgetItem = null
	private var birdhouseSeed_c: WidgetItem = null
	def birdhouseItem(): WidgetItem = {
		if (birdhouseItem_c == null) {
			birdhouseItem_c = search(BIRDHOUSE_REDWOOD, BIRDHOUSE_MAGIC, BIRDHOUSE_YEW, BIRDHOUSE_MAHOGANY, BIRDHOUSE_MAPLE, BIRDHOUSE_TEAK, BIRDHOUSE_WILLOW, BIRDHOUSE_OAK, BIRDHOUSE_NORMAL).orNull
		}
		birdhouseItem_c
	}

	def birdhouseSeed(): WidgetItem = {
		if (birdhouseSeed_c == null) {
			birdhouseSeed_c = search(BARLEY_SEED, JUTE_SEED, HAMMERSTONE_HOP_SEED, ASGARNIAN_HOP_SEED, YANILLIAN_HOP_SEED, KRANDORIAN_HOP_SEED, WILDBLOOD_HOP_SEED).orNull
		}
		birdhouseSeed_c
	}

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

		log.debug(
			"Found seeds {} and birdhouses {}", Option(birdhouseSeed()).map(w => s"${w.getId} @ ${w.getWidget.getIndex}"),
			Option(birdhouseItem()).map(w => s"${w.getId} @ ${w.getWidget.getIndex}")
		)
		val nWidgetValue = EthanApiPlugin.getSelectedWidget.toScala
		if(cachedWidgetValue != nWidgetValue && config.isDebugSelectedWidget) {
			sendChatMessage("SelectedWidget"){
				s"Changed from ${cachedWidgetValue.map(widgetToNiceString)} to ${nWidgetValue.map(widgetToNiceString)}"
			}
		}
		cachedWidgetValue = nWidgetValue
	}

	def findSpell(group: Int, id: Int): Option[(Int, ((Int, ItemComposition), (Int, Widget)))] = {
		spellsWidgetTable.toList.find(e => {
			val (egroup, eid) = e._2._2._2.getId.pipe(eid => WidgetInfo.TO_GROUP(eid) -> WidgetInfo.TO_CHILD(eid))
			egroup == group && eid == id
		})
	}


	object OPAL_JEWELRY {
		def unapply(id: Int): Boolean = {
					List(OPAL_RING, OPAL_NECKLACE, STRUNG_OPAL_AMULET, OPAL_BRACELET).contains(id)
		}
	}
	object JADE_JEWELRY {
		def unapply(id: Int): Boolean = {
			List(JADE_RING, JADE_NECKLACE, STRUNG_JADE_AMULET, JADE_BRACELET).contains(id)
		}
	}

	object TOPAZ_JEWELRY {
		def unapply(id: Int): Boolean = {
			List(TOPAZ_RING, TOPAZ_NECKLACE, STRUNG_TOPAZ_AMULET, TOPAZ_BRACELET).contains(id)
		}
	}

	object Alchable {
		inline def bowAlches: List[Int] = List(MAPLE_SHORTBOW, MAPLE_LONGBOW, YEW_SHORTBOW, YEW_LONGBOW, MAGIC_LONGBOW)
		inline def staffAlches: List[Int] = List(AIR_BATTLESTAFF, FIRE_BATTLESTAFF, EARTH_BATTLESTAFF, WATER_BATTLESTAFF)
		inline def mithrilAlches: List[Int] =List(MITHRIL_2H_SWORD, MITHRIL_ARMOURED_BOOTS, MITHRIL_AXE, MITHRIL_AXE_2H, MITHRIL_BATTLEAXE, MITHRIL_CHAINBODY, MITHRIL_CLAWS, MITHRIL_DAGGER, MITHRIL_DAGGER_P, MITHRIL_DAGGER_P_, MITHRIL_DAGGER_P__, MITHRIL_FULL_HELM, MITHRIL_HALBERD, MITHRIL_KITESHIELD, MITHRIL_LONGSWORD, MITHRIL_MACE, MITHRIL_MED_HELM, MITHRIL_PICKAXE, MITHRIL_PLATEBODY, MITHRIL_PLATELEGS, MITHRIL_PLATESKIRT, MITHRIL_SCIMITAR, MITHRIL_SPEAR, MITHRIL_SPEAR_P, MITHRIL_SPEAR_P_, MITHRIL_SPEAR_P__, MITHRIL_SQ_SHIELD, MITHRIL_SWORD, MITHRIL_THROWNAXE, MITHRIL_WARHAMMER)
		inline def adamantAlches: List[Int] = List(ADAMANT_2H_SWORD, ADAMANT_ARMOURED_BOOTS, ADAMANT_AXE, ADAMANT_AXE_2H, ADAMANT_BATTLEAXE, ADAMANT_CHAINBODY, ADAMANT_CLAWS, ADAMANT_DAGGER, ADAMANT_DAGGER_P, ADAMANT_DAGGER_P_, ADAMANT_DAGGER_P__, ADAMANT_FULL_HELM, ADAMANT_HALBERD, ADAMANT_KITESHIELD, ADAMANT_LONGSWORD, ADAMANT_MACE, ADAMANT_MED_HELM, ADAMANT_PICKAXE, ADAMANT_PLATEBODY, ADAMANT_PLATELEGS, ADAMANT_PLATESKIRT, ADAMANT_SCIMITAR, ADAMANT_SPEAR, ADAMANT_SPEAR_P, ADAMANT_SPEAR_P_, ADAMANT_SPEAR_P__, ADAMANT_SQ_SHIELD, ADAMANT_SWORD, ADAMNT_THROWNAXE, ADAMNT_WARHAMMER)
		inline def runeAlches: List[Int] =List(RUNE_2H_SWORD, RUNE_ARMOURED_BOOTS, RUNE_AXE, RUNE_AXE_2H, RUNE_BATTLEAXE, RUNE_CHAINBODY, RUNE_CLAWS, RUNE_DAGGER, RUNE_DAGGER_P, RUNE_DAGGER_P_, RUNE_DAGGER_P__, RUNE_FULL_HELM, RUNE_HALBERD, RUNE_KITESHIELD, RUNE_LONGSWORD, RUNE_MACE, RUNE_MED_HELM, RUNE_PICKAXE, RUNE_PLATEBODY, RUNE_PLATELEGS, RUNE_PLATESKIRT, RUNE_SCIMITAR, RUNE_SPEAR, RUNE_SPEAR_P, RUNE_SPEAR_P_, RUNE_SPEAR_P__, RUNE_SQ_SHIELD, RUNE_SWORD, RUNE_THROWNAXE, RUNE_WARHAMMER)

		private lazy val listOfAlches: List[Int] = List(bowAlches, staffAlches, mithrilAlches, adamantAlches, runeAlches).flatten
		private lazy val listOfAlchesNoted: List[Int] = listOfAlches.map(client.getItemDefinition).flatMap(x => Option(x.getLinkedNoteId).filter(_ != -1))
//			.flatMap(laid => laid.flatMap(aid =>
//				Seq(aid,client.getItemDefinition(aid).getLinkedNoteId).filter(_ != -1)
//			))
		def unapply(id: Int): Boolean = {
			(
				if (!config.isAlchNotesOnly) {
					listOfAlches
				} else {
					Seq.empty[Int]
				}
			).appendedAll(listOfAlchesNoted).contains(id)
		}
	}

	object UNSTRUNG_JEWELRY {
		def unapply(id: Int): Boolean = {
			List(
				UNSTRUNG_GOLD_AMULET,
				UNSTRUNG_SAPPHIRE_AMULET,
				UNSTRUNG_EMERALD_AMULET,
				UNSTRUNG_RUBY_AMULET,
				UNSTRUNG_DIAMOND_AMULET,
				UNSTRUNG_DRAGONSTONE_AMULET,
				UNSTRUNG_ONYX_AMULET,
				UNSTRUNG_ZENYTE_AMULET,
				UNSTRUNG_OPAL_AMULET,
				UNSTRUNG_JADE_AMULET,
				UNSTRUNG_TOPAZ_AMULET
			).contains(id)
		}
	}


	@Subscribe
	def onMenuEntryAdded(menuOptionAdded: MenuEntryAdded): Unit = {
		val me = menuOptionAdded.getMenuEntry
		if(!client.getMenu.getMenuEntries.contains(me) || blockTopLevelSwitch) return
		if (me.getType == MenuAction.WIDGET_TARGET && me.getParam1 == InterfaceID.Inventory.ITEMS) {
			Option(me.getItemId).collect{
//				case ItemID.PAYDIRT =>
				case MAHOGANY_LOGS | TEAK_LOGS => findSpell(218, 133)
				case OPAL_JEWELRY() => findSpell(218, 13)
				case JADE_JEWELRY() => findSpell(218, 24)
				case TOPAZ_JEWELRY() => findSpell(218, 37)
				case UNSTRUNG_JEWELRY() => findSpell(218, 126)
				case Alchable() => findSpell(218, 44)
			}.flatten.map {
				case (_, (_, (cid, w))) => {
					client.getMenu.createMenuEntry(-1)
						.setOption("Cast".colored(Color.BLUE))
						.setType(MenuAction.WIDGET_TARGET)
						.setIdentifier(0)
						.setParam0(-1)
						.setParam1(cid)
						.onClick(e => {
							blockTopLevelSwitch = true
						}).tap(m => priorityMenuEntries.addOne(m));
				}
			}
		}
	}

	@Subscribe
	def onWidgetItemMenuEntryAdded(menuOptionAdded: MenuEntryAdded): Unit = {
		val me = menuOptionAdded.getMenuEntry
		if (!client.getMenu.getMenuEntries.contains(me)) return
		if (me.getType == MenuAction.WIDGET_TARGET && me.getParam1 == InterfaceID.Inventory.ITEMS) {
			Option(me.getItemId).collect {
				//				case ItemID.PAYDIRT =>
				case ARROW_SHAFT | FEATHER => search(FEATHER).zip(search(ARROW_SHAFT))//(218, 133)
				case SLAYER_BROAD_ARROWHEAD | HEADLESS_ARROW => search(SLAYER_BROAD_ARROWHEAD).zip(search(HEADLESS_ARROW))
			}.flatten.map((a, b) => a.getWidget -> b.getWidget).map {
				case (a, b) => client.getMenu.createMenuEntry(-1)
					.setOption("Make".colored(Color.GREEN))
					.setType(MenuAction.RUNELITE)
					.setIdentifier(0)
					.onClick((e) => {
						clientThread.invokeLater(() => {
							InteractionUtils.useWidgetOnWidget(a, b)
						})
					}).tap(m => priorityMenuEntries.addOne(m));
			}
		}
	}

	object BIRDHOUSE_SEED {
		def unapply(id: Int): Boolean = {
			List(BARLEY_SEED, JUTE_SEED, HAMMERSTONE_HOP_SEED, ASGARNIAN_HOP_SEED, YANILLIAN_HOP_SEED, KRANDORIAN_HOP_SEED, WILDBLOOD_HOP_SEED).contains(id)
		}
	}
	object BIRDHOUSE_ITEM {
		def unapply(id: Int): Boolean = {
			List(		BIRDHOUSE_NORMAL,BIRDHOUSE_OAK,BIRDHOUSE_WILLOW,BIRDHOUSE_TEAK,BIRDHOUSE_MAPLE,BIRDHOUSE_MAHOGANY,BIRDHOUSE_YEW,BIRDHOUSE_MAGIC,BIRDHOUSE_REDWOOD).contains(id)
		}
	}

	object BIRDHOUSE_FULL{
		def unapply(id: Int): Boolean = {
				List(BIRDHOUSE_NORMAL_FULL, BIRDHOUSE_OAK_FULL, BIRDHOUSE_WILLOW_FULL, BIRDHOUSE_TEAK_FULL, BIRDHOUSE_MAPLE_FULL, BIRDHOUSE_MAHOGANY_FULL, BIRDHOUSE_YEW_FULL, BIRDHOUSE_MAGIC_FULL, BIRDHOUSE_REDWOOD_FULL).contains(id)
		}
	}
	object BIRDHOUSE_EMPTY{
		def unapply(id: Int): Boolean = {
				List(BIRDHOUSE_NORMAL_BUILT, BIRDHOUSE_OAK_BUILT, BIRDHOUSE_WILLOW_BUILT, BIRDHOUSE_TEAK_BUILT, BIRDHOUSE_MAPLE_BUILT, BIRDHOUSE_MAHOGANY_BUILT, BIRDHOUSE_YEW_BUILT, BIRDHOUSE_MAGIC_BUILT, BIRDHOUSE_REDWOOD_BUILT).contains(id)
		}
	}
	object BIRDHOUSE_BIRD {
		def unapply(id: Int): Boolean = {
			List(BIRDHOUSE_NORMAL_BIRD, BIRDHOUSE_OAK_BIRD, BIRDHOUSE_WILLOW_BIRD, BIRDHOUSE_TEAK_BIRD, BIRDHOUSE_MAPLE_BIRD, BIRDHOUSE_MAHOGANY_BIRD, BIRDHOUSE_YEW_BIRD, BIRDHOUSE_MAGIC_BIRD, BIRDHOUSE_REDWOOD_BIRD).contains(id)
		}
	}

	object BIRDHOUSE_SPOT {
		def unapply(tileObject: TileObject): Option[(Int, TileObject)] = {
			Option(tileObject).filter(_.getId.pipe(List(BIRDHOUSE_1, BIRDHOUSE_2, BIRDHOUSE_3, BIRDHOUSE_4).contains(_))).flatMap(to => {
				Option(client.getObjectDefinition(to.getId)).map(d => scala.util.Try(d.getImpostor).getOrElse(d)).map(oc => (oc.getId, to))
			})
		}
	}


		//		List(

//		List()

//	def birdhouseItem(query: ItemQuery, filter: Int => Boolean): Option[Widget] = query.withIdFilter {
//		filter(_)
//	}.result().asScala.toList.sortBy(u => (u.getItemId.min(65535).max(0) << 8) | (u.getIndex.min(255).max(0))).headOption

//	extension(c: ItemContainer) {
//		def findAny(ids: Int *): Option[Int] = {
//			ids.foldLeft(Option.empty[Int])((acm, id) => {
//				acm.orElse{
//					Option(c.find(id)).filter(_ > -1)
//				}
//			})
//		}
//	}




	@Subscribe
	def onGameObjectMenuEntryAdded(menuOptionAdded: MenuEntryAdded): Unit = {
		val me = menuOptionAdded.getMenuEntry
		if(!(client.getMenu.getMenuEntries.contains(me) && me.isTileObjectAction)) return

		val targetedTileObject = me.getTileObjectOpt.flatMap(to => {
			BIRDHOUSE_SPOT.unapply(to).filterNot(_._2.pipe(processedGameObjects.contains(_)))
		}).tap(tto => {
			tto.map(_._2).foreach(processedGameObjects.addOne)
			val birdSeenLocation = tto.map(_._2).map(_.getLocalLocation.pipe(ll => ll.getSceneX -> ll.getSceneY)).getOrElse((-1, -1))
			val targetTileObject = tto.map(_._2).get

			tto.collect({
				case (BIRDHOUSE_BIRD(), to) => {
					//GAME_OBJECT_THIRD_OPTION(id=30567, params=(48, 49), option=Empty, target=<lt>col=ffff<gt>Magic birdhouse)
					(_: MenuEntry)
						.setOption("Harvest".colored(Color.GREEN.darker()))
						.setType(MenuAction.GAME_OBJECT_THIRD_OPTION)
						.setIdentifier(to.getId)
						.setParam0(birdSeenLocation._1)
						.setParam1(birdSeenLocation._2)
						.onClick(e => {
							sendChatMessage("birdhouses"){s"harvesting bird @ ${birdSeenLocation}"}
						})
				}
				case (30552, to) if(birdhouseItem() != null) => {
					//GAME_OBJECT_FIRST_OPTION(id=30567, params=(48, 49), option=Build, target=<lt>col=ffff<gt>Space)
					(_: MenuEntry)
						.setOption("Build".colored(Color.GREEN.darker()))
						.setType(MenuAction.GAME_OBJECT_FIRST_OPTION)
						.setIdentifier(to.getId)
						.setParam0(birdSeenLocation._1)
						.setParam1(birdSeenLocation._2)
						.onClick(e => {
							sendChatMessage(s"birdhouses"){
								s"building a birdhouse @ ${birdSeenLocation} with item ${birdhouseItem()}"
							}
						})
//					log.debug("Added menu to object {} with impostorId {} @ {}", to, impostorId, to.getLocalLocation.pipe(ll => ll.getSceneX -> ll.getSceneY))
				}
//				case (BIRDHOUSE_EMPTY(), to) if(Option(client.getSelectedWidget()).exists(_.getItemId.pipe(BIRDHOUSE_SEED.unapply(_)))) => {
//					(_: MenuEntry)
//						.setOption("Use")
//						.setType(MenuAction.WIDGET_TARGET_ON_GAME_OBJECT)
//						.setIdentifier(to.getId)
//						.setParam0(birdSeenLocation._1)
//						.setParam1(birdSeenLocation._2)
//						.onClick(e => {
//							//WIDGET_TARGET(id=0, params=(26, 9764864), option=Use, target=<lt>col=ff9040<gt>Jute seed<lt>/col<gt>)
//							//[Client] DEBUG c.f.superClickHelper.SuperClickerPlugin - WIDGET_TARGET_ON_GAME_OBJECT(id=30567, params=(48, 49), option=Use, target=<lt>col=ff9040<gt>Jute seed<lt>/col<gt><lt>col=ffffff<gt> -<gt> <lt>col=ffff<gt>Magic birdhouse (empty))
//							client.addChatMessage(ChatMessageType.FRIENDSCHAT, "Entry Clicked", s"Filling birdhouse @ ${birdSeenLocation} with seeds ${client.getSelectedWidget.getItemId}", s"birdhouses")
//						})
////					log.debug("Added menu to object {} with impostorId {} @ {}", to, impostorId, to.getLocalLocation.pipe(ll => ll.getSceneX -> ll.getSceneY))
//				}
				case (BIRDHOUSE_EMPTY(), to) if (birdhouseSeed() != null) => {
					(_: MenuEntry)
						.setOption("Selecting seeds")
						.setType(MenuAction.RUNELITE)
						.setIdentifier(0)
						.onClick((e) => {
							val w = birdhouseSeed().getWidget
							clientThread.invokeLater(() => {
								InteractionUtils.useWidgetOnTileObject(w, targetTileObject)
							})
						})
				}
			}).foreach(b => b.apply(client.getMenu.createMenuEntry(-1)).tap(priorityMenuEntries.addOne(_)))
		})
//		def birdhouseSeed(query: ItemQuery): List[Widget] = query.withIdFilter {
//			BIRDHOUSE_SEED.unapply(_)
//		}.result().asScala.toList.sortBy(u => (u.getItemId.min(65535).max(0) << 8) | (u.getIndex.min(255).max(0))).headOption

//		targetedTileObject.zip
//			.collect {
//				case (BIRDHOUSE_EMPTY(), to) => birdhouseItem(Inventory.search(), BIRDHOUSE_ITEM.unapply).map(w => w -> to)
//				case (BIRDHOUSE_FULL(), to) => to
//				case (BIRDHOUSE_BIRD(), to) => to
//				case (_, to) => ItemQuery
//			}
	}

	@Subscribe
	def onPostGameTick(event: ItemContainerChanged): Unit = {
		processedGameObjects.clear()
		if(event.getItemContainer.getId == InventoryID.INV) {
			birdhouseSeed_c = null
			birdhouseItem_c = null
		}
	}

	@Subscribe(priority = -15)
	def onPostMenuSort(postMenuSort: PostMenuSort): Unit = {
		processedGameObjects.clear()
		if (!client.isMenuOpen) {
			val menuEntries   : List[MenuEntry] = client.getMenuEntries.toList
			val (added, stock)                  = menuEntries.partition(e => {
				priorityMenuEntries.contains(e)
			})
			val newMenuEntries: List[MenuEntry] = stock.appendedAll(added)//.sortBy(a => priorityMenuEntries.indexOf(a)))
			client.setMenuEntries(newMenuEntries.toArray[MenuEntry])
		}
		priorityMenuEntries.clear()
	}

	@Subscribe
	def onMenuOptionClicked(menuOptionClicked: MenuOptionClicked): Unit = {
		if(config.isDebugMenu) {
//			val paramColor = new Color(0xC06A09)
//			val messageParts = Seq(
//				menuOptionClicked.getMenuAction.name().colored(Color.blue).appendedAll("("),
//				"id".colored(paramColor).appendedAll("=").appendedAll(s"${menuOptionClicked.getId}".colored(Color.GREEN)).appendedAll(", "),
//				"params".colored(paramColor).appendedAll("=(").appendedAll(s"${menuOptionClicked.getParam0}".colored(Color.CYAN)).appendedAll(", ").appendedAll(s"${menuOptionClicked.getParam1}".colored(Color.CYAN)).appendedAll("), "),
//				"option".colored(paramColor).appendedAll("=").appendedAll(s"${Text.escapeJagex(menuOptionClicked.getMenuOption)}".colored(Color.MAGENTA)).appendedAll(", "),
//				"target".colored(paramColor).appendedAll("=").appendedAll(s"${Text.escapeJagex(menuOptionClicked.getMenuTarget)}".colored(new Color(100, 100, 200))).appendedAll(")")
//			)


			sendChatMessage("MenuClicked"){
				menuOptionClicked.getMenuEntry.prettyString()
//				messageParts.fold("")(_.appendedAll(_))
			}
		}

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

