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
import com.fredplugins.common.extensions.GeneralExtensions
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.pvmDebugger.tormenteddemons.FredsTormentedDemons.AttackStyle
import com.fredplugins.pvmDebugger.tormenteddemons.FredsTormentedDemons.AttackStyle.MAGE
import com.fredplugins.pvmDebugger.tormenteddemons.FredsTormentedDemons.AttackStyle.MELEE
import com.fredplugins.pvmDebugger.tormenteddemons.FredsTormentedDemons.AttackStyle.RANGE
import com.fredplugins.pvmDebugger.tormenteddemons.FredsTormentedDemons.TormentedDemonData
import com.google.inject.Inject
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.lucidplugins.api.item.SlottedItem
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.lucidplugins.api.utils.InventoryUtils
import ethanApiPlugin.collections.Inventory
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.ChatMessageType
import net.runelite.api.Client
import net.runelite.api.HeadIcon
import net.runelite.api.NPC
import net.runelite.api.Prayer
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.AnimationChanged
import net.runelite.api.events.GameTick
import net.runelite.api.events.InteractingChanged
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.gameval.AnimationID
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


	enum AttackStyle(val protectionPrayer: Prayer) {
		case RANGE extends AttackStyle(Prayer.PROTECT_FROM_MISSILES)
		case MAGE extends AttackStyle(Prayer.PROTECT_FROM_MAGIC)
		case MELEE extends AttackStyle(Prayer.PROTECT_FROM_MELEE)
	}

	private def swap(itemList: Array[String], swapFirstHalf: Boolean): Unit = {
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

	enum HotkeyAction(val op: (FredsTormentedDemonConfig => Keybind), val color: Color, val action: (FredsTormentedDemonConfig => (() => Unit))) {
		case DodgeFireball extends HotkeyAction(_.dodgeFireballHotkey(), Color.red, config => () => {})
		case SwapMelee extends HotkeyAction(_.swapMeleeGearHotkey(), Color.orange, config => () => {
			if (config.useMeleeStyle()) {
				swap(config.meleeGear().split(','), false)
				if (config.enableOffensivePrayer()) CombatUtils.activatePrayer(Prayer.PIETY)
			}
		})
		case SwapRange extends HotkeyAction(_.swapRangeGearHotkey(), Color.green, config => () => {
			if (config.useRangeStyle()) {
				swap(config.rangeGear().split(','), false)
				if (config.enableOffensivePrayer()) CombatUtils.activatePrayer(Prayer.EAGLE_EYE)
			}
		})
		case SwapMagic extends HotkeyAction(_.swapMageGearHotkey(), Color.blue, config => () => {
			if (config.useMagicStyle()) {
				swap(config.magicGear().split(','), false)
				if (config.enableOffensivePrayer()) CombatUtils.activatePrayer(Prayer.MYSTIC_MIGHT)
			}
		})

		private def message: ChatMessageBuilder = {
			new ChatMessageBuilder().append("Running action \"").append(this.color, s"${this}").append("\".")
		}

		def run(module: FredsTormentedDemonsHelper): Unit = {
			val actionFunc = action(module.config)
			module.parent.getClientThread.invoke(() => {
				actionFunc()
				module.printMessage(ChatMessageType.FRIENDSCHAT, "Tormented", "Action")(message)
			})
		}
	}

	case class TormentedDemonData(attackCount: Int, ticksUntilAttack: Int, protectingStyle: AttackStyle, attackStyle: Set[AttackStyle], animationId: Int) {
	}
}
class FredsTormentedDemonsHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsTormentedDemonConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = "FredsTormentedDemonsHelper"
//	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	private var targetDemon: Option[NPC] = Option.empty
	private var blockingHotkey: Option[HotkeyAction] = Option.empty[HotkeyAction]

	def buildData(n: NPC): TormentedDemonData = {
		FredsTormentedDemons.TormentedDemonData(
			0, 7, EthanApiPlugin.getHeadIcon(n) match {
				case HeadIcon.RANGED => AttackStyle.RANGE
				case HeadIcon.MAGIC => AttackStyle.MAGE
				case _ => AttackStyle.MELEE
			}, Set(AttackStyle.MAGE, AttackStyle.RANGE, AttackStyle.MELEE), n.getAnimation)
	}

	private val internalKeyListener = new KeyListener {
		override def keyTyped(e: KeyEvent): Unit = {}
		override def keyPressed(e: KeyEvent): Unit = {
			if(blockingHotkey.isEmpty) {
				val foundHotkeyEnum = HotkeyAction.values.find(v => v.op(config).matches(e))
				blockingHotkey = foundHotkeyEnum.tapEach(a => {
					e.consume()
					a.run(FredsTormentedDemonsHelper.this)
//					Option(a).collect[(String, String, ChatMessageBuilder)] {
//						case HotkeyAction.DodgeFireball => ("Tormented", "Dodge Fireball", new ChatMessageBuilder().append("Moving from ").append(Color.red, "fireball").append("."))
//						case HotkeyAction.SwapMelee => ("Tormented", "Swap Melee", new ChatMessageBuilder().append("Swapping to ").append(Color.orange, "melee").append(" gear."))
//						case HotkeyAction.SwapRange => ("Tormented", "Swap Range", new ChatMessageBuilder().append("Swapping to ").append(Color.green, "range").append(" gear."))
//						case HotkeyAction.SwapMagic => ("Tormented", "Swap Mage", new ChatMessageBuilder().append("Swapping to ").append(Color.blue, "magic").append(" gear."))
//					}.zip(Option(a).map(_.action(config))).map[Runnable] {
//						case ((group, sender, msg), action) => () => {
//							printMessage(ChatMessageType.FRIENDSCHAT, group, sender)(msg)
//						}
//					}.foreach(r => parent.getClientThread.invoke(r))
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
		client.getTopLevelWorldView.npcs().asScala.toList.filter(n => TORMENTED_DEMON_IDS.contains(n.getId)).foreach(n => demons.put(n, buildData(n)))
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
	private lazy val animationIdToNameMap: Map[Int, String] = {
		classOf[net.runelite.api.gameval.AnimationID].getDeclaredFields.toList
			.filter(_.getType == Integer.TYPE)
			.filter(_.getModifiers == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL))
			.map(f => {
				f.getInt(null) -> f.getName
			}).toMap
	}
	def getAnimationName(id: Int): String = animationIdToNameMap.getOrElse(id, s"Animation(${id})")

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
				.map(n => n -> buildData(n)).foreach{
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

	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
		Option(e.getActor).collect {
			case npc: NPC if demons.contains(npc) => npc -> demons(npc)
		}.tap(x => {
			x.foreach {
				case (npc, data) => {
					val msg = new ChatMessageBuilder()
						.append("Animation changed from ")
						.append(Color.blue, s"${getAnimationName(data.animationId)}")
						.append(" to ")
						.append(Color.green, s"${getAnimationName(npc.getAnimation)}")
						.append(".")
					parent.getClientThread.invoke(() => {
						printMessage(ChatMessageType.FRIENDSCHAT, "Tormented", "AnimationChanged")(msg)
					})
				}
			}
		}).foreach {
			case (npc, data) if npc.getAnimation ==  AnimationID.LUC2_UNDEAD_DEMON_MELEE=> {
				demons.update(npc, data.copy(attackCount = data.attackCount + 1, ticksUntilAttack = 7, attackStyle = Set(MELEE)))
			}
			case (npc, data) if npc.getAnimation == AnimationID.LUC2_UNDEAD_DEMON_SPARE_RIBS => {
				demons.update(npc, data.copy(attackCount = data.attackCount + 1, ticksUntilAttack = 7, attackStyle = Set(RANGE)))
			}
			case (npc, data) if npc.getAnimation == AnimationID.LUC2_UNDEAD_DEMON_FIREY_BALLS => {
				demons.update(npc, data.copy(attackCount = data.attackCount + 1, ticksUntilAttack = 7, attackStyle = Set(MAGE)))
			}
			case (npc, data) if npc.getAnimation == AnimationID.LUC2_UNDEAD_DEMON_EXPLOSION_FIRE => {
				demons.update(npc, data.copy(attackCount = 0, ticksUntilAttack = 7, attackStyle = AttackStyle.values.toSet.diff(data.attackStyle)))
			}
			case (npc, data) =>
		}
	}
	
	@Subscribe
	def onGameTick(e: GameTick): Unit = {
		if(inRegion()) {
			demons.mapValuesInPlace {
				case (npc, data) => {
//						val x = Option(npc.getAnimation).filterNot(_ == data.animationId).collect {
//							case AnimationID.LUC2_UNDEAD_DEMON_MELEE => data.attackStyle.filter(_ == AttackStyle.MELEE)
//							case _ => data.attackStyle.filter(_ == AttackStyle.RANGE)
//							case _ => data.attackStyle.filter(_ == AttackStyle.MAGE)
//							case _ => AttackStyle.values.filterNot(s => data.attackStyle.contains(s)).toSet
//							case _ =>
//						}
					//val n = buildData(npc)//TormentedDemonData(npc.getAnimation, npc.getPoseAnimation, npc.getWorldLocation)
					data.copy(ticksUntilAttack = Math.max(data.ticksUntilAttack-1, 0), animationId = npc.getAnimation, protectingStyle = EthanApiPlugin.getHeadIcon(npc) match {
						case HeadIcon.RANGED => AttackStyle.RANGE
						case HeadIcon.MAGIC => AttackStyle.MAGE
						case _ => AttackStyle.MELEE
					})
				}
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
					val text = s"attackCount: ${data.attackCount}, ticksUntilAttack: ${data.ticksUntilAttack}, anim: ${getAnimationName(data.animationId)}, protecting: ${data.protectingStyle}"
					val color = if(targetDemon.contains(npc)) Color.GREEN else Color.RED
					renderNpcOverlay(npc, text, color, 0)
				}
			}
		}
		null.asInstanceOf[Dimension]
	}
}