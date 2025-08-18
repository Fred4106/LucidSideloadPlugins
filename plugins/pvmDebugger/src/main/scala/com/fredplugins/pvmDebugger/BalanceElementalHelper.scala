package com.fredplugins.pvmDebugger


import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger.BalanceElementalIds.AttackStyle
import com.fredplugins.pvmDebugger.BalanceElementalIds.BalanceElementalNpc
import com.fredplugins.pvmDebugger.BalanceElementalIds.BalanceElementalType
import com.fredplugins.pvmDebugger.BalanceElementalIds.BalanceElementalType.unapply
import com.google.inject.Inject
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.lucidplugins.api.utils.EquipmentUtils
import ethanApiPlugin.lucidplugins.api.utils.InteractionUtils
import ethanApiPlugin.lucidplugins.api.utils.InventoryUtils
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.ETileItem
import ethanApiPlugin.collections.Inventory
import net.runelite.api.Client
import net.runelite.api.HeadIcon
import net.runelite.api.NPC
import net.runelite.api.NPCComposition
import net.runelite.api.Player
import net.runelite.api.Prayer
import net.runelite.api.Projectile
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.GameTick
import net.runelite.api.events.ItemDespawned
import net.runelite.api.events.ItemSpawned
import net.runelite.api.events.MenuOptionClicked
import net.runelite.api.events.NpcChanged
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.OverheadTextChanged
import net.runelite.api.events.ProjectileMoved
import net.runelite.api.gameval.AnimationID
import net.runelite.api.gameval.NpcID
import net.runelite.client.eventbus.Subscribe

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object BalanceElementalIds {
//	val RangeNpcId    = 13528
//	val CrushNpcId    = 13529
//	val MageNpcId    = 13530

	object BalanceElementalNpc {
		def unapply(npc: NPC): Option[(NPC, BalanceElementalType)] = {
			Option(npc).filter(_.getId == NpcID.WGS_BALANCE_ELEMENTAL).zip(
				BalanceElementalType.unapply(npc.getPoseAnimation)
			)
		}
	}

	sealed trait BalanceElementalType {
		def poseAnimationIds: Seq[Int]
	}
	object BalanceElementalType {
		case object RangeType extends BalanceElementalType {
			override def poseAnimationIds: Seq[Int] = Seq(
				AnimationID.NPC_BALANCE_ELEMENTAL_FIRE_IDLE01,
				AnimationID.NPC_BALANCE_ELEMENTAL_FIRE_WALK01)
		}
		case object CrushType extends BalanceElementalType{
			override def poseAnimationIds: Seq[Int] = Seq(
				AnimationID.NPC_BALANCE_ELEMENTAL_AIR_IDLE01,
				AnimationID.NPC_BALANCE_ELEMENTAL_AIR_WALK01)
		}
		case object MageType extends BalanceElementalType{
			override def poseAnimationIds: Seq[Int] = Seq(
				AnimationID.NPC_BALANCE_ELEMENTAL_WATER_IDLE01,
				AnimationID.NPC_BALANCE_ELEMENTAL_WATER_WALK01)
		}
		def values: Seq[BalanceElementalType] = Seq(RangeType, CrushType, MageType)

		def unapply(poseAnimation: Int): Option[BalanceElementalType] ={
			values.find(_.poseAnimationIds.contains(poseAnimation))
		}
	}

	sealed trait AttackStyle {}
	object AttackStyle {
		case object Mage extends AttackStyle
		case object Range extends AttackStyle
		case object Melee extends AttackStyle
		val values: Seq[AttackStyle] = Seq(Mage, Range, Melee)
	}
}
class BalanceElementalHelper (val client: Client) extends ShimUtils.Logging("DEBUG") {
	var balanceNpc: NPC = null
	var balanceOverhead: HeadIcon = null

	def reset(): Unit = {
		this.balanceOverhead = null
		this.balanceNpc = null
	}

	@Subscribe
	def onMenuEntryClicked(event: MenuOptionClicked): Unit = {
		if(balanceNpc != null) {
			log.debug(s"Balanced Elemental Clicked: ${event}")
		}
	}

	@Subscribe
	def onNpcSpawned(event: NpcSpawned): Unit = {
		Option(event.getNpc).collect {
			case BalanceElementalNpc(nm, tpe) => {
				this.balanceNpc = nm
				log.debug(s"Spawned Balanced Elemental ${tpe} ${Integer.toHexString(nm.hashCode())} ${balanceNpc.getIndex -> balanceNpc.getId}")
			}
		}
	}
	@Subscribe
	def onNpcDespawned(event: NpcDespawned): Unit = {
		Option(event.getNpc).collect{
			case BalanceElementalNpc(nm, tpe) => {
				log.debug(s"Despawned ${if(nm != balanceNpc) "Unknown " else ""}Balanced Elemental ${tpe} ${Integer.toHexString(nm.hashCode())}")
				balanceNpc = null
			}
		}
	}

	@Subscribe
	def onGameTick(event: GameTick): Unit = {
		if(balanceNpc != null) {

			Option(balanceNpc.getPoseAnimation).collect {
				case BalanceElementalType(BalanceElementalType.RangeType) => Prayer.PROTECT_FROM_MISSILES
				case BalanceElementalType(BalanceElementalType.CrushType) => Prayer.PROTECT_FROM_MELEE
				case BalanceElementalType(BalanceElementalType.MageType) => Prayer.PROTECT_FROM_MAGIC
			}.filterNot(client.isPrayerActive).foreach(CombatUtils.activatePrayer)

			val playerAttackStyle = Option(EquipmentUtils.getWepSlotItem).map(_.getId).collect {
				case 9185 => AttackStyle.Range
				case 28585 => AttackStyle.Mage
				case 24699 => AttackStyle.Melee
			}.orNull


			val oldHeadIcon = balanceOverhead
			val curHeadIcon = EthanApiPlugin.getHeadIcon(balanceNpc)
			val validAttackStyles = curHeadIcon match {
				case HeadIcon.RANGED => Seq(AttackStyle.Mage, AttackStyle.Melee)
				case HeadIcon.MAGIC => Seq(AttackStyle.Range, AttackStyle.Melee)
				case HeadIcon.MELEE => Seq(AttackStyle.Mage, AttackStyle.Range)
				case HeadIcon.RANGE_MAGE => Seq(AttackStyle.Melee)
				case HeadIcon.RANGE_MELEE => Seq(AttackStyle.Mage)
				case HeadIcon.MAGE_MELEE => Seq(AttackStyle.Range)
				case _ => Seq(AttackStyle.Mage, AttackStyle.Range, AttackStyle.Melee)
			}
			val wepToEquip  = validAttackStyles.head match {
				case AttackStyle.Mage => 28585
				case AttackStyle.Range => 9185
				case AttackStyle.Melee => 24699
			}
			val attackPrayer = validAttackStyles.head match {
				case AttackStyle.Mage => Prayer.MYSTIC_MIGHT
				case AttackStyle.Range => Prayer.EAGLE_EYE
				case AttackStyle.Melee => Prayer.PIETY
			}
			InventoryUtils.wieldItem(wepToEquip)
			CombatUtils.activatePrayer(attackPrayer)
			balanceOverhead = curHeadIcon
		}
	}
}
