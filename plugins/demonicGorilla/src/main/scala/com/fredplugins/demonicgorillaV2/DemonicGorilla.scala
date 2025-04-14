package com.fredplugins.demonicgorillaV2

import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.Client
import net.runelite.api.HeadIcon
import net.runelite.api.NPC
import net.runelite.api.Player
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint

import scala.util.chaining.*
import scala.util.{Try}
import scala.compiletime.uninitialized

case class GorillaFrame(animation: Int, interacting: Player, worldArea: WorldArea, overheadIcon: HeadIcon)
case class DemonicGorillaData(possibleAttackStyles: Seq[AttackStyle & RegularAttack],
															nextAttackTick: Int,
															recentProjectileId: Int,
															attacksUntilSwitch: Int,

															currentAnimation: Int,
															lastAnimation: Int,
															currentWorldArea:WorldArea,
															lastWorldArea: WorldArea,
															currentInteracting: Player,
															lastInteracting: Player,

														 ) {

															 }
class DemonicGorilla(npc: NPC)(using client: Client) {
	private var nextPossibleAttackStyles: Seq[AttackStyle & RegularAttack] = AttackStyle.RegularAttacks.toList
	private var nextAttackTick   : Int                              = -100
	def getNextAttackTick: Int = nextAttackTick
	def setNextAttackTick(value: Int): Unit = nextAttackTick = value

	def index: Int = npc.getIndex
	def localLocation: LocalPoint = npc.getLocalLocation
	private var attacksUntilSwitch: Int                                     = Attacks_per_Switch
	private var recentProjectileId      : Int                               =  -1

	private var takenDamageRecently          : Boolean         = false
	private var changedPrayerThisTick        : Boolean         = false
	private var changedAttackStyleThisTick   : Boolean         = false
	private var changedAttackStyleLastTick   : Boolean         = false
	private var lastTickOverheadIcon         : HeadIcon | Null = null
	private var disabledMeleeMovementForTicks: Int             = 0


	private var lastTickAnimation: Int     = 0
	def getLastTickAnimation: Int = lastTickAnimation
	def setLastTickAnimation(value: Int): Unit = lastTickAnimation = value


	private var cacheAnimationId: Int = uninitialized
	private var cachedAnimationIdOnTick: Int = -1

	def getAnimation: Int = {
		if(cachedAnimationIdOnTick > -1 && client.getTickCount > cachedAnimationIdOnTick) {
			cachedAnimationIdOnTick = -1
		}
		if(cachedAnimationIdOnTick == -1) {
			cachedAnimationIdOnTick = client.getTickCount
			cacheAnimationId =npc.getAnimation
		}
		cacheAnimationId
	}

	private var lastWorldArea: WorldArea|Null   = _
	private var initiatedCombat  : Boolean = false

	private var lastTickInteracting: Player | Null  = 	null

	private var interactingCache: Option[Player | Null] = Option.empty
	private var interactingCachedOnTick: Int = -1
	def getInteracting: Player | Null = {
		if(interactingCachedOnTick > -1 && interactingCachedOnTick != client.getTickCount) {
			interactingCache = Option.empty
			interactingCachedOnTick = -1
		}
		if(interactingCache.isEmpty) {
			interactingCachedOnTick = client.getTickCount
			interactingCache = npc.getInteracting.pipe[Player | Null]( {
				case p: Player => p
				case _ => null
			}).pipe[Option[Player | Null]](Some(_))
		}
		interactingCache.orNull
	}

	private var worldAreaCache       : WorldArea = uninitialized
	private var worldAreaCachedOnTick: Int                   = -1
	def getWorldArea: WorldArea = {
		if (worldAreaCachedOnTick > -1 && worldAreaCachedOnTick != client.getTickCount) {
			worldAreaCachedOnTick = -1
		}
		if (worldAreaCachedOnTick == -1) {
			worldAreaCachedOnTick = client.getTickCount
			worldAreaCache = npc.getWorldArea
		}
		worldAreaCache
	}

	private var worldLocationCache       : WorldPoint = uninitialized
	private var worldLocationCachedOnTick: Int            = -1
	def getWorldLocation: WorldPoint = {
		if (worldLocationCachedOnTick > -1 && worldLocationCachedOnTick != client.getTickCount) {
			worldLocationCachedOnTick = -1
		}
		if (worldLocationCachedOnTick == -1) {
			worldLocationCachedOnTick = client.getTickCount
			worldLocationCache = npc.getWorldLocation
		}
		worldLocationCache
	}
//			npc.getInteracting match {
//				case p: Player => p
//				case _ => null
//			}
//	}
	def isTakenDamageRecently: Boolean = this.takenDamageRecently
	def setTakenDamageRecently(v: Boolean): Unit = this.takenDamageRecently = v

	def getLastTickOverheadIcon: HeadIcon| Null = lastTickOverheadIcon
	def setLastTickOverheadIcon(icon: HeadIcon): Unit = lastTickOverheadIcon = icon
	def getOverheadIcon: HeadIcon | Null = Try(EthanApiPlugin.getHeadIcon(npc)).toOption.orNull
	def decrementAttacksUntilSwitch(): Unit = this.attacksUntilSwitch = attacksUntilSwitch -1
	def setAttacksUntilSwitch(v: Int): Unit = this.attacksUntilSwitch = v
	def getAttacksUntilSwitch: Int = this.attacksUntilSwitch
	def getNextPosibleAttackStyles: Seq[AttackStyle & RegularAttack] = this.nextPossibleAttackStyles
	def filterPossibleAttackStyles(p: AttackStyle => Boolean): Unit = {
		nextPossibleAttackStyles = nextPossibleAttackStyles.filter(p)
	}
	def setNextPosibleAttackStyles(styles: Seq[AttackStyle & RegularAttack]): Unit = this.nextPossibleAttackStyles = styles
	def setRecentProjectileId(id: Int) : Unit= recentProjectileId = id
	def getRecentProjectileId: Int = this.recentProjectileId
	def getLastTickInteracting: Player = lastTickInteracting
	def setLastTickInteracting(p: Player): Unit = lastTickInteracting = p
	def setInitiatedCombat(b: Boolean): Unit = initiatedCombat = b
	def isInitiatedCombat: Boolean = this.initiatedCombat
	def setChangedPrayerThisTick(b: Boolean): Unit = changedPrayerThisTick= b
	def isChangedPrayerThisTick: Boolean = this.changedPrayerThisTick
	def isChangedAttackStyleThisTick: Boolean = this.changedAttackStyleThisTick
	def setChangedAttackStyleThisTick(v: Boolean): Unit = changedAttackStyleThisTick = v
	def isChangedAttackStyleLastTick: Boolean = this.changedAttackStyleLastTick
	def setChangedAttackStyleLastTick(v: Boolean): Unit = changedAttackStyleLastTick = v
	def setLastWorldArea(wa: WorldArea): Unit = this.lastWorldArea = wa
	def getLastWorldArea: WorldArea = this.lastWorldArea
	def setDisabledMeleeMovementForTicks(i: Int): Unit = this.disabledMeleeMovementForTicks = i
	def getDisabledMeleeMovementForTicks: Int = this.disabledMeleeMovementForTicks
	def decrementDisabledMeleeMovementForTicks(): Unit = this.disabledMeleeMovementForTicks -= 1
}
