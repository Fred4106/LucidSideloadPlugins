package com.fredplugins.pvmDebugger.kraken

import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.common.extensions.MenuExtensions.{*, given}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.lucidplugins.api.item.SlottedItem
import com.lucidplugins.api.utils.CombatUtils
import com.lucidplugins.api.utils.EquipmentUtils
import com.lucidplugins.api.utils.InteractionUtils
import com.lucidplugins.api.utils.InventoryUtils
import com.lucidplugins.api.utils.NpcUtils
import ethanApiPlugin.collections.Inventory
import ethanApiPlugin.collections.query.NPCQuery
import interactionApi.InventoryInteraction
import net.runelite.api.Client
import net.runelite.api.Menu
import net.runelite.api.MenuAction
import net.runelite.api.MenuEntry
import net.runelite.api.NPC
import net.runelite.api.Skill
import net.runelite.api.events.GameTick
import net.runelite.api.events.MenuEntryAdded
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.PostMenuSort
import net.runelite.api.gameval.ItemID
import net.runelite.api.gameval.NpcID
import net.runelite.api.widgets.WidgetUtil
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.util.ColorUtil
import net.runelite.client.util.Text
import org.slf4j.Logger

import java.awt.Color
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class KrakenHelper(pvmDebuggerPlugin: PvmDebuggerPlugin, client:  Client, config: KrakenConfig) {
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Subscribe(priority = -9)
	def onPostMenuSort(postMenuSort: PostMenuSort): Unit = {
		if (!client.isMenuOpen) {
			val menu   : Menu = client.getMenu
			val menuEntries   : List[MenuEntry] = menu.getMenuEntries.toList
			val (added, stock)                  = menuEntries.partition((e: MenuEntry) => {
				e.getType == MenuAction.RUNELITE && e.getSanitizedOption.endsWith("Disturb")
			})
			added.foreach(a => log.debug("Added: {}", a))
			val newMenuEntries: List[MenuEntry] = stock.appendedAll(added)
			client.setMenuEntries(newMenuEntries.toArray[MenuEntry])
		}
	}

	@Subscribe
	def onMenuEntryAdded(menuEntryAdded: MenuEntryAdded): Unit = {
		val entry = menuEntryAdded.getMenuEntry
		if(entry.isNpcAction) {
			val npc = entry.getNpc
			val explosivesWidgetOpt = Inventory.search().withId(ItemID.FISHING_EXPLOSIVE).first().toScala
			if(npc.getId == NpcID.SLAYER_KRAKEN_BOSS_WHIRLPOOL &&
				entry.getSanitizedOption.equals("Disturb") &&
				explosivesWidgetOpt.isDefined) {
				val explosivesW = explosivesWidgetOpt.get
				val menu = entry.getParentMenu
				menu.createMenuEntry(-1)
						.setOption(ColorUtil.prependColorTag("Disturb", Color.BLUE))
						.setType(MenuAction.RUNELITE)
						.onClick(e => {
							InteractionUtils.useWidgetOnNPC(explosivesW, npc)
//							setNpcHighlightColor(npc.getId(), c);
//							clientThread.invokeLater(this :: rebuild);
						});
				//add throw explosives entry
			}
		}
	}
//	def onNpcSpawned(npcSpawned: NpcSpawned): Unit = {
//		val npc = npcSpawned.getNpc
//		if(npc.getId == NpcID.SLAYER_KRAKEN_BOSS_WHIRLPOOL) {
//			kraken = Some(npc)
////			if(Inventory.getItemAmount(6664) > 0)
////			InteractionUtils.useItemOnNPC(6664, kraken)
//		}
//	}
//
//	def onNpcDespawned(npcDespawned: NpcDespawned): Unit = {
//		val npc = npcDespawned.getNpc
//		if(kraken.contains(npc)) {
//			kraken = Option.empty
//		}
//	}

	var cooldown = 0
	var countdownTillFirstAttack = -1
	@Subscribe
	def onGameTick(e: GameTick): Unit = {
		if(cooldown > 0) {
			cooldown = cooldown - 1
		} else {
			countdownTillFirstAttack = Math.max(countdownTillFirstAttack - 1, -1)
			val krakenNpcOpt = NpcUtils.search().withId(NpcID.SLAYER_KRAKEN_BOSS_WHIRLPOOL, NpcID.SLAYER_KRAKEN_BOSS).first().toScala
			if(krakenNpcOpt.isDefined) {
				val localPlayer = client.getLocalPlayer
				val kraken = krakenNpcOpt.get
				val myHp = client.getBoostedSkillLevel(Skill.HITPOINTS)
				if (countdownTillFirstAttack > 0) {

				}
				else if(countdownTillFirstAttack == 0) {
					NpcUtils.attackNpc(kraken)
					cooldown = 12
				}
				else if(myHp < config.lowerHpThreshold() && EquipmentUtils.getWepSlotItem.getId != ItemID.MAGICTRAINING_WAND_MASTER) {
					cooldown = if(InventoryUtils.wieldItem(ItemID.MAGICTRAINING_WAND_MASTER)) 2 else 0
				}
				else if (myHp > config.upperHpThreshold() && EquipmentUtils.getWepSlotItem.getId != ItemID.WARPED_SCEPTRE) {
					cooldown = if(InventoryUtils.wieldItem(ItemID.WARPED_SCEPTRE)) 2 else 0
				}
				else if (localPlayer.getInteracting == null || localPlayer.getInteracting != kraken) {
					if(kraken.getId == NpcID.SLAYER_KRAKEN_BOSS) {
						countdownTillFirstAttack = 1
					} else if(config.autoExplosive() && InventoryUtils.contains(ItemID.FISHING_EXPLOSIVE) && localPlayer.getInteracting == null) {
						InteractionUtils.useItemOnNPC(ItemID.FISHING_EXPLOSIVE, kraken)
						countdownTillFirstAttack = Random.nextInt(3)+1
						cooldown = 4
					}
				}
			}
		}
	}
}
