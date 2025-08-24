package com.fredplugins.gearSwapper

import ethanApiPlugin.lucidplugins.api.item.SlottedItem
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.gearSwapper.FredsGearSwapperConfig.CONFIG_GROUP as FREDS_CONFIG_GROUP
import com.fredplugins.gearSwapper.FredsGearSwapperConfig.GearSlot
import com.fredplugins.gearSwapper.FredsGearSwapperPlugin.FILENAME_SPECIAL_CHAR_REGEX
import com.fredplugins.gearSwapper.FredsGearSwapperPlugin.GearSwapState
import com.fredplugins.gearSwapper.FredsGearSwapperPlugin.PRESET_DIR
import com.fredplugins.gearSwapper.FredsGearSwapperPlugin.getSlotFromGearSlotSelected
import com.fredplugins.gearSwapper.FredsGearSwapperPlugin.gson
import com.fredplugins.gearSwapper.FredsGearSwapperPlugin.listsMatch
import com.fredplugins.gearSwapper.FredsGearSwapperPlugin.parseList
import com.fredplugins.gearSwapper.FredsGearSwapperPlugin.slotOrderToCopy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.inject.Inject
import com.google.inject.Provides
import com.google.inject.Singleton
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.lucidplugins.api.utils.EquipmentUtils
import ethanApiPlugin.lucidplugins.api.utils.InteractionUtils
import ethanApiPlugin.lucidplugins.api.utils.InventoryUtils
import ethanApiPlugin.lucidplugins.api.utils.MessageUtils
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.Inventory
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.events.GameTick
import net.runelite.api.widgets.WidgetInfo
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.input.KeyManager
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDependency
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.ui.overlay.OverlayManager
import org.slf4j.Logger
import net.runelite.client.RuneLite.RUNELITE_DIR
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.ConfigChanged
import net.runelite.client.input.KeyListener

import java.awt.Color
import java.awt.event.KeyEvent
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.Failure
import scala.util.Success
import scala.util.Using
import scala.util.chaining.*

object FredsGearSwapperPlugin {
	private val slotOrderToCopy: List[Int] = {
		import net.runelite.api.EquipmentInventorySlot.{WEAPON, LEGS, SHIELD, HEAD, BODY, AMULET, GLOVES, RING, AMMO, BOOTS, CAPE}
		List(WEAPON, SHIELD, HEAD, BODY, LEGS, CAPE, BOOTS, AMULET, GLOVES, RING, AMMO).map(_.getSlotIdx)
	}

	private val PRESET_DIR                 : File        = new File(RUNELITE_DIR, FREDS_CONFIG_GROUP)
	private val FILENAME_SPECIAL_CHAR_REGEX: String      = "[^a-zA-Z\\d:]"
	private val builder                    : GsonBuilder = new GsonBuilder().setPrettyPrinting()
	private val gson                       : Gson        = builder.create()
	enum GearSwapState {
		case TICK_1
		case TICK_2
		case FINISHED
	}
	def parseList(in: String): List[String] = {
		in.split(',').toList
	}

	private def getSlotFromGearSlotSelected(slot: GearSlot) = slot match {
		case GearSlot.GEAR_SLOT_1 => 1
		case GearSlot.GEAR_SLOT_2 => 2
		case GearSlot.GEAR_SLOT_3 => 3
		case GearSlot.GEAR_SLOT_4 => 4
		case GearSlot.GEAR_SLOT_5 => 5
		case GearSlot.GEAR_SLOT_6 => 6
	}

	def listsMatch(list1: List[String], list2: List[String]): Boolean = {
		if (list1.size != list2.size) return false
		val list2Copy = ArrayBuffer.from(list2)
		list1.foreach(element => {
			Option(list2Copy.indexOf(element))
				.filterNot(_ == -1)
				.foreach(list2Copy.remove)
		}
									)
		list2Copy.isEmpty
	}
}

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Gear Swapper</html>",
	description = "Set-up up to 6 custom gear swaps with customizable hotkeys or trigger them via weapon equip",
	tags = Array("gear", "swap", "swapper", "hotkey")
	)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsGearSwapperPlugin() extends Plugin with KeyListener {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")

	@Inject val client        : Client                 = null
	@Inject val clientThread  : ClientThread           = null
	@Inject val config        : FredsGearSwapperConfig = null
	@Inject val configManager : ConfigManager          = null
	@Inject val keyManager    : KeyManager             = null
	@Inject val overlayManager: OverlayManager         = null

	private val configs           = new Array[String](6)
	private var gearSwapState     = GearSwapState.TICK_1
	private var gearSwapSelected  = -1
	private var lastSwapSelected  = -1
	private val lastItemsEquipped = ListBuffer.empty[Int]
	private val lastEquipmentList = ListBuffer.empty[String]

	@Provides def getConfig(configManager: ConfigManager): FredsGearSwapperConfig = configManager.getConfig(classOf[FredsGearSwapperConfig])

	protected override def startUp(): Unit = {
		clientThread.invoke(() => {
			keyManager.registerKeyListener(this)
			parseSwaps()
		}
												)
	}

	protected override def shutDown(): Unit = {
		keyManager.unregisterKeyListener(this)
	}

	@Subscribe
	private def onConfigChanged(event: ConfigChanged): Unit = {
		if (!event.getGroup.equals(FREDS_CONFIG_GROUP)) return
		parseSwaps()
	}

	@Subscribe
	private def onGameTick(event: GameTick): Unit = {
		getEquipmentChanges()
		if (lastItemsEquipped.nonEmpty) {
			var i = 0
			while (i < configs.length) {
				if (isActivateOnFirstItem(i) && configs(i) != null) {
					val configList = parseList(configs(i))
					if (configList != null && configList.nonEmpty) {
						val itemString = configList(0).strip
						val firstItem  = EquipmentUtils.getAll.asScala.toList.filter {
							item => client.getItemDefinition(item.getItem.getId).getName.contains(itemString)
						}
						if (firstItem.nonEmpty) {
							if (lastItemsEquipped.contains(firstItem.head.getItem.getId) && (gearSwapSelected == -1)) {
								if (isSlotEnabled(i)) gearSwapSelected = i
							}
						}
					}
				}
				i += 1
			}
			lastItemsEquipped.clear
		}

		if (gearSwapState == GearSwapState.FINISHED) {
			log.debug("lastSwapSelected: {}, gearSwapSelected {}", lastSwapSelected, gearSwapSelected)
			if (shouldActivateSpec() != CombatUtils.isSpecEnabled) CombatUtils.toggleSpec()
			gearSwapSelected = -1
			gearSwapState = GearSwapState.TICK_1
		}

		if (gearSwapSelected != -1) {
			if (gearSwapState == GearSwapState.TICK_1) {
				if (config.oneTickSwap) {
					swap(gearSwapSelected, false)
					gearSwapState = GearSwapState.FINISHED
					lastSwapSelected = gearSwapSelected
				}
				else {
					swap(gearSwapSelected, true)
					gearSwapState = GearSwapState.TICK_2
					lastSwapSelected = -1
				}
			} else if (gearSwapState == GearSwapState.TICK_2) {
				swap(gearSwapSelected, false)
				gearSwapState = GearSwapState.FINISHED
				lastSwapSelected = gearSwapSelected
			}
		}
	}

	private def getEquipmentChanges(): Unit = {
		val bankWidget = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER)
		if (bankWidget != null && !bankWidget.isSelfHidden) return
		val equippedItems = EquipmentUtils.getAll.asScala.toList
		val itemsMapped   = equippedItems.map(
			item => client.getItemDefinition(item.getItem.getId).getName
			)
		if (!listsMatch(itemsMapped, lastEquipmentList.toList)) {
			for (slottedItem <- equippedItems) {
				val name = client.getItemDefinition(slottedItem.getItem.getId).getName
				if (!lastEquipmentList.contains(name)) {
					if (gearSwapSelected == -1) lastItemsEquipped.addOne(slottedItem.getItem.getId)
				}
			}
			lastEquipmentList.clear
			lastEquipmentList.addAll(itemsMapped)
		}
	}
	private def isActivateOnFirstItem(configIndex: Int): Boolean = {
		configIndex match {
			case 0 => config.equipFirstItem1()
			case 1 => config.equipFirstItem2()
			case 2 => config.equipFirstItem3()
			case 3 => config.equipFirstItem4()
			case 4 => config.equipFirstItem5()
			case 5 => config.equipFirstItem6()
		}
	}

	private def isSlotEnabled(slot: Int): Boolean = {
		slot match {
			case 0 => config.swap1Enabled()
			case 1 => config.swap2Enabled()
			case 2 => config.swap3Enabled()
			case 3 => config.swap4Enabled()
			case 4 => config.swap5Enabled()
			case 5 => config.swap6Enabled()
		}
	}
	private def shouldActivateSpec(): Boolean = {
		//		val specEnergy =
		(lastSwapSelected match {
			case 0 => config.activateSpec1() -> config.specThreshold1()
			case 1 => config.activateSpec2() -> config.specThreshold2()
			case 2 => config.activateSpec3() -> config.specThreshold3()
			case 3 => config.activateSpec4() -> config.specThreshold4()
			case 4 => config.activateSpec5() -> config.specThreshold5()
			case 5 => config.activateSpec6() -> config.specThreshold6()
			case _ => false -> -1
		}).pipe {
			case (activateSpec, specThreshold) => activateSpec && CombatUtils.getSpecEnergy >= specThreshold
		}
	}

	private def parseSwaps(): Unit = {
		configs(0) = config.swap1String
		configs(1) = config.swap2String
		configs(2) = config.swap3String
		configs(3) = config.swap4String
		configs(4) = config.swap5String
		configs(5) = config.swap6String
	}

	override def keyPressed(e: KeyEvent): Unit = {
		if (config.loadPresetHotkey.matches(e)) clientThread.invoke(() => loadPreset())

		if (config.savePresetHotkey.matches(e)) clientThread.invoke(() => savePreset())

		if (config.copyGearHotkey.matches(e)) {
			if (client == null || (client.getGameState != GameState.LOGGED_IN)) return
			val slotSelected = getSlotFromGearSlotSelected(config.slotToCopyTo())
			if (slotSelected != 0) {
				clientThread.invoke(() => {
					//				val equippedGear =
					val equippedItemsString = EquipmentUtils.getAll.asScala.toList.sortBy(item => slotOrderToCopy.indexOf(item
																																																									.getSlot
																																																								)
																																								)
																									.map(slottedItem => client
																										.getItemDefinition(slottedItem.getItem.getId)
																										.getName
																											 ).mkString(",")
					val key                 = "swap" + slotSelected + "String"
					configManager.setConfiguration("lucid-gear-swapper", key, equippedItemsString)
					MessageUtils.addMessage("Copied Equipment to Preset Slot " + slotSelected, Color.RED)

				}
														)
			}
		}

		if (config.swap1Hotkey.matches(e) && config.swap1Enabled) {
			clientThread.invoke(() => {
				if (client.getGameState == GameState.LOGGED_IN) {
					if (config.oneTickSwap) {
						swap(0, false)
						gearSwapState = GearSwapState.TICK_1
					} else {
						gearSwapSelected = 0
						swap(0, true)
						gearSwapState = GearSwapState.TICK_2
					}
					lastSwapSelected = 0
				}
			})
		}
		if (config.swap2Hotkey.matches(e) && config.swap2Enabled) {
			clientThread.invoke(() => {
				if (client.getGameState == GameState.LOGGED_IN) {
					if (config.oneTickSwap) {
						swap(1, false)
						gearSwapState = GearSwapState.TICK_1
					} else {
						gearSwapSelected = 1
						swap(1, true)
						gearSwapState = GearSwapState.TICK_2
					}
					lastSwapSelected = 1
				}
			})
		}
		if (config.swap3Hotkey.matches(e) && config.swap3Enabled) {
			clientThread.invoke(() => {
				if (client.getGameState == GameState.LOGGED_IN) {
					if (config.oneTickSwap) {
						swap(2, false)
						gearSwapState = GearSwapState.TICK_1
					}
					else {
						gearSwapSelected = 2
						swap(2, true)
						gearSwapState = GearSwapState.TICK_2
					}
					lastSwapSelected = 2
				}
			}
													)
		}
		if (config.swap4Hotkey.matches(e) && config.swap4Enabled) {
			clientThread.invoke(() => {

				if (client.getGameState == GameState.LOGGED_IN) {
					if (config.oneTickSwap) {
						swap(3, false)
						gearSwapState = GearSwapState.TICK_1
					}
					else {
						gearSwapSelected = 3
						swap(3, true)
						gearSwapState = GearSwapState.TICK_2
					}
					lastSwapSelected = 3
				}
			}
													)
		}
		if (config.swap5Hotkey.matches(e) && config.swap5Enabled) {
			clientThread.invoke(() => {
				if (client.getGameState == GameState.LOGGED_IN) {
					if (config.oneTickSwap) {
						swap(4, false)
						gearSwapState = GearSwapState.TICK_1
					} else {
						gearSwapSelected = 4
						swap(4, true)
						gearSwapState = GearSwapState.TICK_2
					}
					lastSwapSelected = 4
				}
			}
													)
		}
		if (config.swap6Hotkey.matches(e) && config.swap6Enabled) {
			clientThread.invoke(() => {
				if (client.getGameState == GameState.LOGGED_IN) {
					if (config.oneTickSwap) {
						swap(5, false)
						gearSwapState = GearSwapState.TICK_1
					}
					else {
						gearSwapSelected = 5
						swap(5, true)
						gearSwapState = GearSwapState.TICK_2
					}
					lastSwapSelected = 5
				}
			})
		}
		if (gearSwapSelected != -1) e.consume()
	}

	override def keyTyped(e: KeyEvent): Unit = {}
	override def keyReleased(e: KeyEvent): Unit = {}
	private def swap(swapId: Int, swapFirstHalf: Boolean): Unit = {
		val itemList                     = parseList(configs(swapId))
		val validItems: Seq[SlottedItem] = for {
			item <- itemList
			slottedItem <- Inventory.search.nameContains(item.strip)
															.first().toScala
															.map(w => new SlottedItem(w.getItemId, w.getItemQuantity, w.getIndex))
		} yield slottedItem

		def interactWith(item: SlottedItem): Unit = {
			if (InventoryUtils.itemHasAction(item.getItem.getId, "Wield")) {
				InventoryUtils.itemInteract(item.getItem.getId, "Wield")
			} else if (InventoryUtils.itemHasAction(item.getItem.getId, "Wear")) {
				InventoryUtils.itemInteract(item.getItem.getId, "Wear")
			} else if (InventoryUtils.itemHasAction(item.getItem.getId, "Equip")) {
				InventoryUtils.itemInteract(item.getItem.getId, "Equip")
			}
		}

		if (validItems.nonEmpty) {
			if (swapFirstHalf) {
				var i = 0
				while (i < validItems.size / 2) {
					val item: SlottedItem = validItems(i)
					interactWith(item)
					i += 1
				}
			} else {
				for (item <- validItems) {
					interactWith(item)
				}
			}
		}
	}

	private def savePreset(): Unit = {
		//val presetName          = config.presetName
		val presetNameFormatted = config.presetName()
																		.replaceAll(FILENAME_SPECIAL_CHAR_REGEX, "")
																		.replaceAll(" ", "_")
																		.toLowerCase
		if (presetNameFormatted.isEmpty) return
		val exportableConfig = new ExportableConfig()
		exportableConfig.setSwap(
			0, config.swap1Enabled, config.swap1String, config.swap1Hotkey, config
				.equipFirstItem1, config.activateSpec1, config.specThreshold1
			)
		exportableConfig.setSwap(
			1, config.swap2Enabled, config.swap2String, config.swap2Hotkey, config
				.equipFirstItem2, config.activateSpec2, config.specThreshold2
			)
		exportableConfig.setSwap(
			2, config.swap3Enabled, config.swap3String, config.swap3Hotkey, config
				.equipFirstItem3, config.activateSpec3, config.specThreshold3
			)
		exportableConfig.setSwap(
			3, config.swap4Enabled, config.swap4String, config.swap4Hotkey, config
				.equipFirstItem4, config.activateSpec4, config.specThreshold4
			)
		exportableConfig.setSwap(
			4, config.swap5Enabled, config.swap5String, config.swap5Hotkey, config
				.equipFirstItem5, config.activateSpec5, config.specThreshold5
			)
		exportableConfig.setSwap(
			5, config.swap6Enabled, config.swap6String, config.swap6Hotkey, config
				.equipFirstItem6, config.activateSpec6, config.specThreshold6
			)
		if (!PRESET_DIR.exists) PRESET_DIR.mkdirs
		val saveFile     = new File(PRESET_DIR, presetNameFormatted + ".json")
		val configString = gson.toJson(exportableConfig)

		Using.apply(new FileWriter(saveFile))(_.write(configString)) match {
			case Failure(e) => {
				InteractionUtils.showNonModalMessageDialog(e.getMessage, "Save Preset Error")
				log.error(e.getMessage)
			}
			case Success(_) => {
				InteractionUtils.showNonModalMessageDialog(
					"Successfully saved preset '" + presetNameFormatted + "' at " + saveFile
						.getAbsolutePath, "Preset Save Success"
					)
			}
		}
	}

	private def loadPreset(): Unit = {
		//		FILENAME_SPECIAL_CHAR_REGEX.replaceAllIn(presetName, "").
		val presetNameFormatted = config.presetName()
																		.replaceAll(FILENAME_SPECIAL_CHAR_REGEX, "")
																		.replaceAll(" ", "_")
																		.toLowerCase
		if (presetNameFormatted.isEmpty) return
		Using(new BufferedReader(new FileReader(PRESET_DIR.getPath + "/" + presetNameFormatted + ".json"))) { br =>
			gson.fromJson(br, classOf[ExportableConfig])
		} match {
			case Failure(e) => {
				InteractionUtils.showNonModalMessageDialog(e.getMessage, "Preset Load Error")
				log.error(e.getMessage)
			}
			case Success(loadedConfig) => {
				log.info("Loaded preset: " + presetNameFormatted)
				for (i <- 0 until 6) {
					val swap      = loadedConfig.getSwap(i)
					val keyValues = List(
						s"swap${i + 1}Enabled" -> swap.enabled,
						s"swap${i + 1}String" -> swap.itemsString,
						s"swap${i + 1}Hotkey" -> swap.hotkey,
						s"equipFirstItem${i + 1}" -> swap.equipFirstItem,
						s"activateSpec${i + 1}" -> swap.toggleSpecOnActivation,
						s"specThreshold${i + 1}" -> swap.specThreshold
						)
					keyValues.foreach {
						case (key, value) => configManager.setConfiguration(FREDS_CONFIG_GROUP, key, value)
					}
				}
				InteractionUtils.showNonModalMessageDialog("Successfully loaded preset '" + presetNameFormatted + "'", "Preset Load Success")
			}
		}
	}
}