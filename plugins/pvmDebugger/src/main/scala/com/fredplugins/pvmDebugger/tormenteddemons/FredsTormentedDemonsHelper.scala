package com.fredplugins.pvmDebugger.tormenteddemons

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.lucidplugins.api.utils.CombatUtils
import net.runelite.api.Client
import net.runelite.api.NPC
import net.runelite.api.events.GameTick
import net.runelite.api.events.InteractingChanged
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.gameval.NpcID
import net.runelite.client.eventbus.Subscribe
import org.slf4j.Logger

import scala.jdk.CollectionConverters.*
import java.util.stream.Collectors
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized


class FredsTormentedDemonsHelper(pvmDebuggerPlugin: PvmDebuggerPlugin, client: Client, config: FredsTormentedDemonConfig) {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	private var targetDemon = Option.empty[NPC]

	def shutdown(): Unit = {
		demons.toList.map(n => new NpcDespawned(n)).foreach(onNpcDespawned(_))
		demons.clear()
	}
	def startup(): Unit = {
		val  npcs : List[NPC] = client.getTopLevelWorldView.npcs().asScala.toList
		npcs.map(n => new NpcSpawned(n)).foreach(onNpcSpawned(_))
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

	val TORMENTED_DEMON_IDS = List(NpcID.TORMENTED_DEMON_1, NpcID.TORMENTED_DEMON_2)

	val demons = scala.collection.mutable.HashSet.empty[NPC];

	@Subscribe
	def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
		val npcOpt = Option(npcSpawned.getNpc).filter(n => TORMENTED_DEMON_IDS.contains(n.getId)).filter(n => demons.add(n))
		npcOpt.foreach(log.debug("Added {} from demons", _))
	}

	@Subscribe
	def onNpcDespawned(npcDespawned: NpcDespawned): Unit = {
//		val npc = npcDespawned.getNpc
		val npcOpt = Option(npcDespawned.getNpc).filter(n => TORMENTED_DEMON_IDS.contains(n.getId)).filter(n => demons.remove(n))
//		if(demons.remove(npc)) {

		npcOpt.foreach(log.debug("Removed {} from demons", _))
//		}
	}

//	@Subscribe
//	def onInteractingChanged(event: InteractingChanged): Unit = {
//		if(event.getSource
//	}

//	var cooldown                 = 0
//	var countdownTillFirstAttack = -1
	@Subscribe
	def onGameTick(e: GameTick): Unit = {
		log.debug("tick {}",  client.getTickCount)
		if(targetDemon.isEmpty) {
			val interactingWithOpt = Option(client.getLocalPlayer.getInteracting)
			targetDemon = interactingWithOpt.flatMap(a => Try(a.asInstanceOf[NPC]).toOption).flatMap(interactingWith => {
				demons.find(n => n == interactingWith)
			})
			if(targetDemon.nonEmpty) {
				log.debug("Found new target demon {}", targetDemon.get)
			}
		}
		if(targetDemon.exists(_.isDead)) {
			CombatUtils.deactivatePrayers(false)
			log.debug("Finished killing target demon {}", targetDemon.get)
			targetDemon = Option.empty
		}
//		demons.filter(n => n.getInteracting != null && n.getInteracting.equals(client.getLocalPlayer)).toList.appendedAll(
//			demons.filter(_.getInteracting == null).toList
//		).zipWithIndex.foreach(n => {
//			log.debug("  npc[{}] = {}", n._2, n._1)
//		})
	}
}