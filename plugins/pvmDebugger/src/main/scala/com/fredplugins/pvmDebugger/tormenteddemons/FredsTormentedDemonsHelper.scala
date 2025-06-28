package com.fredplugins.pvmDebugger.tormenteddemons

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithPanel
import com.fredplugins.pvmDebugger.tormenteddemons.FredsTormentedDemons.HotkeyAction
import com.fredplugins.pvmDebugger.tormenteddemons.FredsTormentedDemons.TORMENTED_DEMON_IDS
import com.google.inject.Inject
import com.lucidplugins.api.utils.CombatUtils
import net.runelite.api.ChatMessageType
import net.runelite.api.Client
import net.runelite.api.NPC
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
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import org.slf4j.Logger

import java.awt.Color
import java.awt.event.KeyEvent
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
	val TORMENTED_DEMON_IDS: Seq[Int] = List(NpcID.TORMENTED_DEMON_1, NpcID.TORMENTED_DEMON_2)

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
class FredsTormentedDemonsHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsTormentedDemonConfig) extends HelperModule with WithPanel {
	override val moduleName: String = "FredsTormentedDemonsHelper"
//	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	private var targetDemon: Option[NPC] = Option.empty
	private var blockingHotkey: Option[HotkeyAction] = Option.empty[HotkeyAction]
	private def doAction(e: HotkeyAction): Unit = {
		Option(e).collect[(String, String, ChatMessageBuilder)] {
			case HotkeyAction.DodgeFireball => ("Tormented", "Dodge Fireball", new ChatMessageBuilder().append("Moving from ").append(Color.red, "fireball").append("."))
			case HotkeyAction.SwapMelee => ("Tormented", "Swap Melee", new ChatMessageBuilder().append("Swapping to ").append(Color.orange, "melee").append(" gear."))
			case HotkeyAction.SwapRange => ("Tormented", "Swap Range", new ChatMessageBuilder().append("Swapping to ").append(Color.green, "range").append(" gear."))
			case HotkeyAction.SwapMagic => ("Tormented", "Swap Mage", new ChatMessageBuilder().append("Swapping to ").append(Color.blue, "magic").append(" gear."))
		}.map[Runnable] {
			case (group, sender, msg) => () => {
				printMessage(ChatMessageType.FRIENDSCHAT, group, sender)(msg)
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
		parent.getKeyManager.registerKeyListener(internalKeyListener)
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
	@Subscribe
	def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
		Option(npcSpawned.getNpc).filter(n => TORMENTED_DEMON_IDS.contains(n.getId))
			.map(n => n -> FredsTormentedDemons.TormentedDemonData(n.getAnimation, n.getPoseAnimation, n.getWorldLocation)).foreach{
				case (npc, data) =>
					log.debug("Added demons[{}] = {}", npc, data)
			}
	}

	@Subscribe
	def onNpcDespawned(npcDespawned: NpcDespawned): Unit = {
//		val npc = npcDespawned.getNpc
		Option(npcDespawned.getNpc).filter(n => TORMENTED_DEMON_IDS.contains(n.getId)).flatMap(n => demons.remove(n).map(d => n -> d))
			.foreach{
				case (npc, data) => log.debug("Removed demons[{}] = {}", npc, data)
			}
		if(targetDemon.contains(npcDespawned.getNpc)) {
			targetDemon = Option.empty[NPC]
		}
	}

//	@Subscribe
//	def onInteractingChanged(event: InteractingChanged): Unit = {
//		if(event.getSource
//	}

//	var cooldown                 = 0
//	var countdownTillFirstAttack = -1
	@Subscribe
	def onGameTick(e: GameTick): Unit = {
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
}