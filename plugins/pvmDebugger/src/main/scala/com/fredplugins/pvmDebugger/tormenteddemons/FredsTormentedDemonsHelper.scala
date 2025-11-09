package com.fredplugins.pvmDebugger.tormenteddemons

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.fredplugins.pvmDebugger.tormenteddemons.FredsTormentedDemons.HotkeyAction
import com.fredplugins.pvmDebugger.tormenteddemons.FredsTormentedDemons.TORMENTED_DEMON_IDS
import com.fredplugins.pvmDebugger.tormenteddemons.FredsTormentedDemons.TORMENTED_DEMON_REGION_IDS

import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*

import com.google.inject.Inject
import ethanApiPlugin.lucidplugins.api.item.SlottedItem
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.lucidplugins.api.utils.InventoryUtils
import ethanApiPlugin.collections.Inventory
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.ChatMessageType
import net.runelite.api.Client
import net.runelite.api.NPC
import net.runelite.api.Prayer
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.GameTick
import net.runelite.api.events.InteractingChanged
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.gameval.NpcID
import net.runelite.client.RuneLite
import net.runelite.client.chat.ChatMessageBuilder
import net.runelite.client.config.Keybind
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.input.KeyListener
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import org.slf4j.Logger

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.event.KeyEvent
import java.lang.reflect.Modifier
import scala.jdk.CollectionConverters.*
import java.util.stream.Collectors
import scala.collection.mutable
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object FredsTormentedDemons {
	lazy val client: Client = RuneLite.getInjector().getInstance[Client](classOf[Client])
	val TORMENTED_DEMON_IDS: Seq[Int] = List(
		NpcID.TORMENTED_DEMON_1, NpcID.TORMENTED_DEMON_2,
		NpcID.INVISIBLE_TORMENTED_DEMON_1, NpcID.INVISIBLE_TORMENTED_DEMON_2
	)
	val TORMENTED_DEMON_REGION_IDS: Seq[Int] = List(16196, 16197, 16452, 16453)

	enum HotkeyAction(val op: (FredsTormentedDemonConfig => Keybind)) {
		case DodgeFireball extends HotkeyAction(_.dodgeFireballHotkey())
		case SwapMelee extends HotkeyAction(_.swapMeleeGearHotkey())
		case SwapRange extends HotkeyAction(_.swapRangeGearHotkey())
		case SwapMagic extends HotkeyAction(_.swapMageGearHotkey())
	}

	case class TormentedDemonData(animation: Int, poseAnimation: Int, location: WorldPoint) {
		def update(npc: NPC): TormentedDemonData = {
			val n = TormentedDemonData(npc.getAnimation, npc.getPoseAnimation, npc.getWorldLocation)
			if(animation != n.animation) {

			}
			n
		}
	}
}
class FredsTormentedDemonsHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsTormentedDemonConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = "FredsTormentedDemonsHelper"
//	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	private var targetDemon: Option[NPC] = Option.empty
	private var blockingHotkey: Option[HotkeyAction] = Option.empty[HotkeyAction]

	private def swap(itemList: Seq[String], swapFirstHalf: Boolean): Unit = {
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

	private def doAction(e: HotkeyAction): Unit = {
		Option(e).collect[(String, String, ChatMessageBuilder, () => Unit)] {
			case HotkeyAction.DodgeFireball => ("Tormented", "Dodge Fireball", new ChatMessageBuilder().append("Moving from ").append(Color.red, "fireball").append("."), () => {})
			case HotkeyAction.SwapMelee => ("Tormented", "Swap Melee", new ChatMessageBuilder().append("Swapping to ").append(Color.orange, "melee").append(" gear."), () => {if(config.useMeleeStyle()) {
				swap(config.meleeGear().split(','), false)
				if(config.enableOffensivePrayer()) CombatUtils.activatePrayer(Prayer.PIETY)
			}})
			case HotkeyAction.SwapRange => ("Tormented", "Swap Range", new ChatMessageBuilder().append("Swapping to ").append(Color.green, "range").append(" gear."), () => {if(config.useRangeStyle()) {
				swap(config.rangeGear().split(','), false)
				if(config.enableOffensivePrayer()) CombatUtils.activatePrayer(Prayer.EAGLE_EYE)
			}})
			case HotkeyAction.SwapMagic => ("Tormented", "Swap Mage", new ChatMessageBuilder().append("Swapping to ").append(Color.blue, "magic").append(" gear."), () => {if(config.useMagicStyle()) {
				swap(config.magicGear().split(','), false)
				if(config.enableOffensivePrayer()) CombatUtils.activatePrayer(Prayer.MYSTIC_MIGHT)
			}})
		}.map[Runnable] {
			case (group, sender, msg, action) => () => {
				printMessage(ChatMessageType.FRIENDSCHAT, group, sender)(msg)
				action()
			}
		}.foreach(r => parent.getClientThread.invoke(r))
	}

	private val internalKeyListener = new KeyListener {
		override def keyTyped(e: KeyEvent): Unit = {}
		override def keyPressed(e: KeyEvent): Unit = {
			if(blockingHotkey.isEmpty) {
				val foundHotkeyEnum = HotkeyAction.values.find(v => v.op(config).matches(e))
				blockingHotkey = foundHotkeyEnum.tapEach(a => {
					e.consume()
					doAction(a)
				}).headOption
//				if(foundHotkeyEnum.isDefined) {
//					doAction(foundHotkeyEnum.get)
//					e.consume()
//				}
//				blockingHotkey = foundHotkeyEnum
			}
		}
		override def keyReleased(e: KeyEvent): Unit = {
			if(blockingHotkey.exists(_.op(config).matches(e))) {
				e.consume()
				blockingHotkey = Option.empty[HotkeyAction]
			}
		}
	}

	private val demons: mutable.Map[NPC, FredsTormentedDemons.TormentedDemonData] = scala.collection.mutable.HashMap.empty[NPC, FredsTormentedDemons.TormentedDemonData]

	def cleanup(): Unit = {
		demons.clear()
		targetDemon = Option.empty
		parent.getKeyManager.unregisterKeyListener(internalKeyListener)
	}
	def init(): Unit = {
//		val  npcs : List[NPC] = client.getTopLevelWorldView.npcs().asScala.toList
//		npcs.map(n => new NpcSpawned(n)).foreach(onNpcSpawned(_))
		demons.clear()
		targetDemon = Option.empty
		client.getTopLevelWorldView.npcs().asScala.toList.filter(n => TORMENTED_DEMON_IDS.contains(n.getId)).foreach(n => demons.put(n, FredsTormentedDemons.TormentedDemonData(n.getAnimation, n.getPoseAnimation, n.getWorldLocation)))
		findNewTarget()
		if(inRegion()) parent.getKeyManager.registerKeyListener(internalKeyListener)
	}

	def inRegion(): Boolean = {
		Option(client.getLocalPlayer).map(_.getWorldLocation).map(_.getTemplate).map(_.getRegionID)
			.exists(rid => TORMENTED_DEMON_REGION_IDS.contains(rid))
	}

	def findNewTarget(): Unit = {
		val interactingWithOpt   = Option(client.getLocalPlayer).flatMap(lp => Option(lp.getInteracting)).flatMap[NPC](a => Try(a.asInstanceOf[NPC]).toOption)
		val interactingWithDemon = interactingWithOpt.filterNot(_.isDead).flatMap(interactingWith => {
			demons.find(n => n._1 == interactingWith)
		})
		interactingWithDemon.foreach {
			case (npc, data) => {
				log.debug("Found new target demon {} with data {}", npc, data)
				targetDemon = Some(npc)
			}
		}
	}
//	@Subscribe(priority = -9)
//	def onPostMenuSort(postMenuSort: PostMenuSort): Unit = {
//		if (!client.isMenuOpen) {
//			val menu       : Menu            = client.getMenu
//			val menuEntries: List[MenuEntry] = menu.getMenuEntries.toList
//			val (added, stock)               = menuEntries.partition((e: MenuEntry) => {
//				e.getType == MenuAction.RUNELITE && e.getSanitizedOption.endsWith("Disturb")
//			})
//			added.foreach(a => log.debug("Added: {}", a))
//			val newMenuEntries: List[MenuEntry] = stock.appendedAll(added)
//			client.setMenuEntries(newMenuEntries.toArray[MenuEntry])
//		}
//	}

	private lazy val npcIdToNameMap: Map[Int, String] = {
		classOf[net.runelite.api.gameval.NpcID].getDeclaredFields.toList
			.filter(_.getType == Integer.TYPE)
			.filter(_.getModifiers == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL))
			.map(f => {
				f.getInt(null) -> f.getName
			}).toMap
	}
	def getNpcName(id: Int): String = npcIdToNameMap.getOrElse(id, s"Npc(${id})")

	@Subscribe
	def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
		if(inRegion()) {
			Option(npcSpawned.getNpc).filterNot(n => TORMENTED_DEMON_IDS.contains(n.getId))
				.foreach(n => {
					val msg = new ChatMessageBuilder()
						.append(Color.blue, s"${getNpcName(n.getId)}")
						.append(" spawned at ")
						.append(Color.green, s"${n.getWorldLocation}")
						.append(".")
					parent.getClientThread.invoke(() => {
						printMessage(ChatMessageType.FRIENDSCHAT, "Tormented", "NpcSpawned")(msg)
					})
				})

			Option(npcSpawned.getNpc).filter(n => TORMENTED_DEMON_IDS.contains(n.getId))
				.map(n => n -> FredsTormentedDemons.TormentedDemonData(n.getAnimation, n.getPoseAnimation, n.getWorldLocation)).foreach{
					case (npc, data) =>
						demons.put(npc, data)
						log.debug("Added demons[{}] = {}", npc, data)
				}
		}
	}

	@Subscribe
	def onNpcDespawned(npcDespawned: NpcDespawned): Unit = {
		if(inRegion()) {
	//		val npc = npcDespawned.getNpc
			Option(npcDespawned.getNpc).filter(n => TORMENTED_DEMON_IDS.contains(n.getId)).flatMap(n => demons.remove(n).map(d => n -> d))
				.foreach{
					case (npc, data) => log.debug("Removed demons[{}] = {}", npc, data)
				}
			if(targetDemon.contains(npcDespawned.getNpc)) {
				targetDemon = Option.empty[NPC]
			}
		}
	}

//	@Subscribe
//	def onInteractingChanged(event: InteractingChanged): Unit = {
//		if(event.getSource
//	}

//	@Subscribe
//	def onAnimationChanged(e: AnimationChanged)
	
	@Subscribe
	def onGameTick(e: GameTick): Unit = {
		if(inRegion()) {
			demons.mapValuesInPlace {
				case (npc, data) => data.update(npc)
			}

			if(targetDemon.exists(_.isDead)) {
				CombatUtils.deactivatePrayers(false)
				log.debug("Finished killing target demon {} with data {}", targetDemon.get, demons(targetDemon.get))
				targetDemon = Option.empty
			}

			if(targetDemon.isEmpty) {
				findNewTarget()
			}
		}
	}
	@Subscribe
	def onRegionChanged(event: LocalRegionChanged): Unit = {
		if(!TORMENTED_DEMON_REGION_IDS.contains(event.getCurRegion) && !TORMENTED_DEMON_REGION_IDS.contains(event.getOldRegion)) {
			return
		}
		val msg = new ChatMessageBuilder()
			.append("Region changed from ")
			.append(Color.blue, s"${event.getOldRegion}")
			.append(" to ")
			.append(Color.green, s"${event.getCurRegion}")
			.append(".")
		parent.getClientThread.invoke(() => {
			printMessage(ChatMessageType.FRIENDSCHAT, "Tormented", "Region")(msg)
		})
		if(!TORMENTED_DEMON_REGION_IDS.contains(event.getCurRegion)) {
			targetDemon = None
			demons.clear()
			parent.getKeyManager.unregisterKeyListener(internalKeyListener)
		}
		if(!TORMENTED_DEMON_REGION_IDS.contains(event.getOldRegion) && TORMENTED_DEMON_REGION_IDS.contains(event.getCurRegion)) {
			parent.getKeyManager.registerKeyListener(internalKeyListener)
		}
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		Option(demons.toList.map(d => {
				LineComponent.builder().left(s"${d._1}").right(s"${d._2}")
					.rightColor(
						if(targetDemon.contains(d._1))
							new Color(200, 0, 100)
						else
							new Color(100, 100, 50)
					)
					.build()
			})).filter(_.nonEmpty).map(_.prepended{
				LineComponent.builder().left(s"target")
					.right(targetDemon.flatMap(t => demons.get(t).map(d => t -> d)).fold("empty"){
						case (n, d) => s"$n=$d"
					})
					.build()
			}).getOrElse(Seq.empty[LayoutableRenderableEntity])
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		def renderNpcOverlay(n: NPC, text: String, color: Color, zoffset: Int): Unit = {
			parent.getModelOutlineRenderer.drawOutline(n, 2, color, 4)
			val poly = n.getCanvasTilePoly
			if (poly != null) OverlayUtil.renderPolygon(g, poly, color)
			val textLocation = n.getCanvasTextLocation(g, text, n.getLogicalHeight + zoffset)
			if (textLocation != null) OverlayUtil.renderTextLocation(g, textLocation, text, color)
		}
		if(demons.nonEmpty) {
			demons.foreach {
				case (npc, data) => {
					val text = s"anim: ${data.animation}, pose: ${data.poseAnimation}"
					val color = if(targetDemon.contains(npc)) Color.GREEN else Color.RED
					renderNpcOverlay(npc, text, color, 0)
				}
			}
		}
		null.asInstanceOf[Dimension]
	}
}