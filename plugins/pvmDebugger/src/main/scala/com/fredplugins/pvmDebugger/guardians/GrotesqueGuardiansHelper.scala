package com.fredplugins.pvmDebugger.guardians

import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.guardians.GrotesqueGuardiansConfig.AttackStyle
import com.fredplugins.pvmDebugger.guardians.Guardian.Variant
import net.runelite.api.NPC
import net.runelite.api.Projectile
import net.runelite.client.eventbus.Subscribe

import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.common.extensions.MenuExtensions.{*, given}
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import ethanApiPlugin.lucidplugins.api.item.SlottedItem
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.lucidplugins.api.utils.EquipmentUtils
import ethanApiPlugin.lucidplugins.api.utils.InteractionUtils
import ethanApiPlugin.lucidplugins.api.utils.InventoryUtils
import ethanApiPlugin.lucidplugins.api.utils.NpcUtils
import ethanApiPlugin.collections.Inventory
import ethanApiPlugin.collections.query.NPCQuery
import ethanApiPlugin.interactionApi.InventoryInteraction
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

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
object Guardian {
	private val DUSK_PHASE_1_ANIMATION_MELEE = 7785

	private val DUSK_PHASE_2_ANIMATION_MELEE_7786 = 7786
	private val DUSK_PHASE_2_ANIMATION_MELEE_7788 = 7788
	val DUSK_PHASE_2_ECLIPSE_EXPLOSION = 7802

	private val DUSK_PHASE_3_ANIMATION_MELEE_7785 = 7785
	private val DUSK_PHASE_3_ANIMATION_MELEE_7787 = 7787

	private val DUSK_PHASE_4_ANIMATION_MELEE = 7800
	private val DUSK_PHASE_4_ANIMATION_RANGE = 7801

	val ECHO_DUSK_PHASE_2_TRANSITION = 7799

	private val DUSK_ATTACK_TICK_SPEED                = 6
	private val DEFINITELY_NOT_DUSK_ATTACK_TICK_SPEED = 12

	private val DAWN_PROJECTILE_STONE_ORB     = 1445
	private val DAWN_PROJECTILE_RANGED_ATTACK = 1444

	private val DAWN_ANIMATION_STONE_ORB     = 7771
	private val DAWN_ANIMATION_RANGED_ATTACK = 7770
	private val DAWN_ANIMATION_MELEE_ATTACK  = 7769

	private val DAWN_ATTACK_TICK_SPEED      = 6
	private val ECHO_DAWN_ATTACK_TICK_SPEED = 12

	sealed trait Variant(val id: Int, val echo: Boolean, val attackAnimationIds:Set[Int], val projectileIds: Set[Int], val attackSpeed: Int, val attackStyle: AttackStyle) {
	}
	object Variants {
		case object Dusk_Phase_1 extends Variant(NpcID.GARGBOSS_DUSK_PHASE1_DEFENSIVE, false, Set(DUSK_PHASE_1_ANIMATION_MELEE), Set(), DUSK_ATTACK_TICK_SPEED, AttackStyle.MELEE)
		case object Dusk_Phase_2 extends Variant(NpcID.GARGBOSS_DUSK_PHASE2_ATTACKING, false, Set(DUSK_PHASE_2_ANIMATION_MELEE_7786, DUSK_PHASE_2_ANIMATION_MELEE_7788, DUSK_PHASE_2_ECLIPSE_EXPLOSION), Set(), DUSK_ATTACK_TICK_SPEED, AttackStyle.MELEE)
		case object Dusk_Phase_3 extends Variant(NpcID.GARGBOSS_DUSK_PHASE3_DEFENSIVE, false, Set(DUSK_PHASE_3_ANIMATION_MELEE_7785, DUSK_PHASE_3_ANIMATION_MELEE_7787), Set(), DUSK_ATTACK_TICK_SPEED, AttackStyle.MELEE)
		case object Dusk_Phase_4 extends Variant(NpcID.GARGBOSS_DUSK_PHASE4, false, Set(DUSK_PHASE_4_ANIMATION_MELEE, DUSK_PHASE_4_ANIMATION_RANGE), Set(DAWN_PROJECTILE_RANGED_ATTACK), DUSK_ATTACK_TICK_SPEED, AttackStyle.RANGE)
		case object Dusk_Despawn extends Variant(NpcID.GARGBOSS_DUSK_DEATH, false, Set(), Set(), DUSK_ATTACK_TICK_SPEED, AttackStyle.UNKNOWN)
		case object Dawn_Phase_1 extends Variant(NpcID.GARGBOSS_DAWN_PHASE1, false, Set(DAWN_ANIMATION_MELEE_ATTACK, DAWN_ANIMATION_RANGED_ATTACK, DAWN_ANIMATION_STONE_ORB), Set(DAWN_PROJECTILE_RANGED_ATTACK, DAWN_PROJECTILE_STONE_ORB), DAWN_ATTACK_TICK_SPEED, AttackStyle.RANGE)
		case object Dawn_Phase_3 extends Variant(NpcID.GARGBOSS_DAWN_PHASE3, false, Set(DAWN_ANIMATION_MELEE_ATTACK, DAWN_ANIMATION_RANGED_ATTACK, DAWN_ANIMATION_STONE_ORB), Set(DAWN_PROJECTILE_RANGED_ATTACK, DAWN_PROJECTILE_STONE_ORB), DAWN_ATTACK_TICK_SPEED, AttackStyle.RANGE)
		case object Dawn_Despawn extends Variant(NpcID.GARGBOSS_DAWN_DEATH, false, Set(), Set(), DAWN_ATTACK_TICK_SPEED, AttackStyle.UNKNOWN)
		case object Echo_Dusk extends Variant(NpcID.GARGBOSS_DUSK_PHASE4, true, Set(DUSK_PHASE_4_ANIMATION_RANGE), Set(DAWN_PROJECTILE_RANGED_ATTACK), DUSK_ATTACK_TICK_SPEED, AttackStyle.RANGE)
		case object Echo_Dawn extends Variant(NpcID.GARGBOSS_DAWN_PHASE1, true, Set(DAWN_ANIMATION_STONE_ORB), Set(DAWN_PROJECTILE_STONE_ORB), ECHO_DAWN_ATTACK_TICK_SPEED, AttackStyle.RANGE)
		case object Definitely_not_dusk extends Variant(NpcID.GARGBOSS_DUSK_PHASE2_ATTACKING, true, Set(DUSK_PHASE_2_ECLIPSE_EXPLOSION), Set(), DEFINITELY_NOT_DUSK_ATTACK_TICK_SPEED, AttackStyle.UNKNOWN)

		val values: Seq[Variant] = Seq(
			Dusk_Phase_1,
				Dusk_Phase_2,
				Dusk_Phase_3,
				Dusk_Phase_4,
				Dusk_Despawn,
				Dawn_Phase_1,
				Dawn_Phase_3,
				Dawn_Despawn,
				Echo_Dusk,
				Echo_Dawn,
				Definitely_not_dusk
			)

		def of(id: Int, echoVariant: Boolean): Variant = {
			values.find(v => v.id == id && v.echo == echoVariant).getOrElse(new Variant(id, echoVariant, Set.empty, Set.empty, (if (echoVariant) ECHO_DAWN_ATTACK_TICK_SPEED else DUSK_ATTACK_TICK_SPEED), AttackStyle
				.UNKNOWN){

			})
		}
	}
}
class Guardian(val npc: NPC, val echoVariant: Boolean) {
	val npcId = npc.getId
	val npcName = npc.getName
	val variant: Variant = Guardian.Variants.of(npcId, echoVariant)
	var ticksUntilNextAttack: Int = -1;

	private val attackAnimations: Set[Int] = null
	private val projectileIds: Set[Int] = null
	private val attackTickSpeed: Int = 0
	private var attackStyle: AttackStyle = null
	private var lastAttackProjectile: Projectile = null

	private var echoVariantPhased: Boolean = false
}

class GrotesqueGuardiansHelper(pvmDebuggerPlugin: PvmDebuggerPlugin, client:  Client, config: GrotesqueGuardiansConfig) {
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
	}
}
