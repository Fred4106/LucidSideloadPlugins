package net.runelite.client.plugins.coxhelper


import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.ProjectileID
import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.api.WorldRegion.*
import com.fredplugins.common.utils.{ShimUtils, TWorldPoint}
import ethanApiPlugin.lucidplugins.api.utils.{CombatUtils, EquipmentUtils, Reachable}
import net.runelite.api.*
import net.runelite.api.coords.{LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.events.*
import net.runelite.api.gameval.{AnimationID, InterfaceID, ItemID, NpcID}

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class AutoMode(val plugin: CoxPlugin) {
	private val incommingProjectiles: mutable.ListBuffer[(Projectile, CoxProjectile)] = collection.mutable.ListBuffer.empty[(Projectile, CoxProjectile)]

	def getLocalPlayerPos: Option[WorldPoint] = Try(plugin.getClient.getLocalPlayer().getWorldLocation()).toOption
	def getVictims: List[Victim] = plugin.getOlm.getVictims.asScala.toList
	def getNpcs: List[NPC] = plugin.getClient.getTopLevelWorldView.npcs().asScala.toList
	def inRaid: Boolean = plugin.inRaid()
	def atOlm: Boolean = plugin.getOlm.isActive
	def getSuggestedOlmPrayer(): Option[Prayer] = {
		Option(plugin.getOlm.getPrayer)
	}

	def getNpc(ids: Int*): Option[NPC] = {
		getLocalPlayerPos.flatMap { lppos =>
			getNpcs.filter{n =>
				ids.contains(n.getId) && n.getWorldLocation.distanceTo(lppos) < 20
			}.sortBy(n => ids.indexOf(n.getId)).headOption
		}
	}

	def getTekton: Option[NPC] = getNpc(NpcID.RAIDS_TEKTON_WAITING, NpcID.RAIDS_TEKTON_WALKING_STANDARD, NpcID.RAIDS_TEKTON_FIGHTING_STANDARD, NpcID.RAIDS_TEKTON_WALKING_ENRAGED, NpcID.RAIDS_TEKTON_FIGHTING_ENRAGED, NpcID.RAIDS_TEKTON_HAMMERING)
	def getMuttadile: Option[NPC] = getNpc(NpcID.RAIDS_DOGODILE_JUNIOR, NpcID.RAIDS_DOGODILE)
	def getVespula: Option[NPC] = getNpc(NpcID.RAIDS_VESPULA_FLYING, NpcID.RAIDS_VESPULA_ENRAGED, NpcID.RAIDS_VESPULA_WALKING)
	def getVasaNistro: Option[NPC] = getNpc(NpcID.RAIDS_VASANISTIRIO_DORMANT, NpcID.RAIDS_VASANISTIRIO_WALKING, NpcID.RAIDS_VASANISTIRIO_HEALING)

	def projectileSpawned(p: Projectile): Unit = {
		val toMark = List(
			CoxProjectiles.Olm_FallingCrystal,
			CoxProjectiles.Olm_FallingCrystalTrail,
			CoxProjectiles.Olm_Burning,
			CoxProjectiles.Olm_AcidTrail,
			CoxProjectiles.Olm_FireLine,
			CoxProjectiles.Shaman_Aoe,
			CoxProjectiles.IceDemon_Ranged,
			CoxProjectiles.IceDemon_Mage,
			CoxProjectiles.Tekton_Meteor
		)
		Option(p).zip(
			CoxProjectiles.unapply(p).filter(cp => toMark.contains(cp))
		).foreach(incommingProjectiles.addOne(_))
	}

	def dangerousTiles(): java.util.List[DangerousProjectile] = incommingProjectiles.filter{ (p, tpe) => p.getTargetActor == null}.map{ (p, tpe) => DangerousProjectile(p, tpe, p.ticksRemaining + plugin.getClient.getTickCount)}.asJava
	def tick(): Unit = {
		incommingProjectiles.filterInPlace(_._1.hasHit == false)

		val localPlayerPos = getLocalPlayerPos.orNull
		if(localPlayerPos == null) return;

		val wepPrayer = Option(EquipmentUtils.getWepSlotItem.getId).collect {
			case ItemID.ABYSSAL_TENTACLE | ItemID.DRAGON_WARHAMMER | ItemID.RUNE_PICKAXE => Prayer.PIETY
			case ItemID.SHADOWFLAME_QUADRANT => Prayer.AUGURY
			case ItemID.TOXIC_BLOWPIPE_LOADED | ItemID.DRAGON_DART | ItemID.DRAGON_KNIFE =>  Prayer.RIGOUR
		}.orNull

		val protectPrayer = List(
			getSuggestedOlmPrayer(),
			getTekton.map(_ => Prayer.PROTECT_FROM_MELEE),
			getVespula.map(_ => Prayer.PROTECT_FROM_MISSILES),
			getMuttadile.map(n => {
				if(n.getWorldArea.offset(1).contains(localPlayerPos)) Prayer.PROTECT_FROM_MELEE
				else Prayer.PROTECT_FROM_MISSILES
			}),
			getVasaNistro.map(_.getId).map{
				case NpcID.RAIDS_VASANISTIRIO_DORMANT => Prayer.PROTECT_FROM_MAGIC
				case _ => Prayer.PROTECT_FROM_MISSILES
			}
		).filter(_.isDefined).headOption.flatten.orNull

//		if(protectPrayer == null) CombatUtils.deactivatePrayers(true)
		if(protectPrayer != null) CombatUtils.activatePrayer(protectPrayer)
		if(wepPrayer != null && protectPrayer != null) CombatUtils.activatePrayer(wepPrayer)
	}
}
