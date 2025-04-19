package com.fredplugins.demonicgorillaV2

import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.Actor
import net.runelite.api.Client
import net.runelite.api.HeadIcon
import net.runelite.api.NPC
import net.runelite.api.Player
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint

import scala.util.chaining.*
import scala.util.Try
import scala.compiletime.uninitialized

//case class GorillaFrame(animation: Int, interacting: Player, worldArea: WorldArea, overheadIcon: HeadIcon)
//case class DemonicGorillaData(possibleAttackStyles: Seq[AttackStyle & RegularAttack],
//															nextAttackTick: Int,
//															recentProjectileId: Int,
//															attacksUntilSwitch: Int,
//
//															currentAnimation: Int,
//															lastAnimation: Int,
//															currentWorldArea:WorldArea,
//															lastWorldArea: WorldArea,
//															currentInteracting: Player,
//															lastInteracting: Player,
//
//														 ) {
//
//															 }
case class MemorizedNpcData(
	index:       Int,
	id:          Int,
	animationId: Int,
	interactingWith: Player,
	overheadIcon: HeadIcon,
	worldLoc:    WorldPoint,
	localPoint:  LocalPoint
) {
	def worldArea: WorldArea = new WorldArea(worldLoc, 1, 1)
}
case class MemorizedStateData(
	nextPossibleAttackStyles: Seq[AttackStyle & RegularAttack] = AttackStyle.RegularAttacks,
	nextAttackTick:           Int = -100,
	attacksUntilSwitch: Int = Attacks_per_Switch,
	recentProjectileId: Int = -1,
	initiatedCombat: Boolean          = false,
	takenDamageRecently          : Boolean = false,
	disabledMeleeMovementForTicks: Int     = 0,
	changedPrayerThisTick     : Boolean = false,
	changedAttackStyleThisTick: Boolean = false,
	changedAttackStyleLastTick: Boolean         = false,
	lastTickOverheadIcon      : HeadIcon | Null = null,
	lastTickAnimation         : Int             = -1,
	lastWorldArea  : WorldArea | Null = null,
	lastTickInteracting: Player | Null = null
)
class DemonicGorilla(npc: NPC)(using client: Client) {

	private var npcData: MemorizedNpcData = MemorizedNpcData(-1, -1, -1, Option(npc.getInteracting).collect {
		case n: Player => n
	}.orNull, null, null, null)

	private var stateData = MemorizedStateData()

	override def toString: String = {
		def printProduct(cc: Product, indent: Int): String = {
				cc.productElementNames.zip(cc.productIterator).map(a => s"  ${a._1} = ${a._2}")
					.mkString(s"${cc.productPrefix}(\n", ",\n", "\n)").linesIterator.toList.map(_.prependedAll(" ".repeat(indent))).mkString("\n").stripPrefix(" ".repeat(indent))
		}
		s"""DemonicGorilla($npc) {
			 |  npcData = ${printProduct(npcData, 12)}
			 |  stateData = ${printProduct(stateData, 12)}
			 |}
			 |""".stripMargin
	}
	//	override def toString: String = {
	//		s"""DemonicGorilla(${npc}) {
	//			 |  index = ${index}
	//			 |  nextPossibleAttackStyles = ${nextPossibleAttackStyles}
	//			 |  nextAttackTick = ${nextAttackTick}
	//			 |  attacksUntilSwitch = ${attacksUntilSwitch}
	//			 |  recentProjectileId = ${recentProjectileId}
	//			 |  takenDamageRecently = ${takenDamageRecently}
	//			 |  disabledMeleeMovementForTicks = ${disabledMeleeMovementForTicks}
	//			 |  changedPrayer = ${changedPrayerThisTick}
	//			 |  thisTick = {
	//			 |    changedAttackStyle = ${changedAttackStyleThisTick}
	//			 |    animation = ${getAnimation}
	//			 |    overhead = ${getOverheadIcon}
	//			 |    interacting = ${getInteracting}
	//			 |  }
	//			 |  lastTick = {
	//			 |    changedAttackStyle = ${changedAttackStyleLastTick}
	//			 |    animation = ${lastTickAnimation}
	//			 |    overhead = ${lastTickOverheadIcon}
	//			 |    interacting = ${lastTickInteracting}
	//			 |  }
	//			 |}
	//			 |""".stripMargin
	//	}
	//	private var nextPossibleAttackStyles: Seq[AttackStyle & RegularAttack] = AttackStyle.RegularAttacks
	//	private var nextAttackTick          : Int                              = -100
		def getNextAttackTick: Int = stateData.nextAttackTick
		def setNextAttackTick(value: Int): Unit = stateData = stateData.copy(nextAttackTick = value)

	//	def index: Int = npc.getIndex
	//	def localLocation: LocalPoint = npc.getLocalLocation

//	private var attacksUntilSwitch: Int = Attacks_per_Switch
//	private var recentProjectileId: Int = -1
//
//	private var takenDamageRecently          : Boolean = false
//	private var disabledMeleeMovementForTicks: Int     = 0
//
//	private var changedPrayerThisTick     : Boolean = false
//	private var changedAttackStyleThisTick: Boolean = false
//
//	private var changedAttackStyleLastTick: Boolean         = false
//	private var lastTickOverheadIcon      : HeadIcon | Null = null
//	private var lastTickAnimation         : Int             = 0



	def gameTick(): Unit = {
		val oldNpcData = npcData

		this.npcData = MemorizedNpcData(
			npc.getIndex,
			npc.getId,
			npc.getAnimation,
			Option(npc.getInteracting).collect {
				case n: Player => n
			}.orNull,
			EthanApiPlugin.getHeadIcon(npc),
			npc.getWorldLocation,
			npc.getLocalLocation
		)

		stateData = stateData.copy(
//			nextPossibleAttackStyles = ,
//			nextAttackTick = ,
//			attacksUntilSwitch = ,
//			recentProjectileId = ,
//			initiatedCombat = ,
//			takenDamageRecently = ,
//			disabledMeleeMovementForTicks = ,
//			changedPrayerThisTick = ,
//			changedAttackStyleThisTick = ,
//			changedAttackStyleLastTick = stateData.changedAttackStyleThisTick,
			lastTickAnimation = oldNpcData.animationId,
			lastTickOverheadIcon = oldNpcData.overheadIcon,
			lastTickInteracting = oldNpcData.interactingWith,
			lastWorldArea = oldNpcData.worldArea
		)

//		this.stateData = .lastTickAnimation = oldNpcData.animationId
		this.stateData.lastTickOverheadIcon
	}

	def getIndex: Int = npcData.index
	def getWorldArea: WorldArea = npcData.worldArea
	def getAnimationId: Int = npcData.animationId

	def getLastTickAnimation: Int = stateData.lastTickAnimation
	def setLastTickAnimation(value: Int): Unit = {
		stateData = stateData.copy(lastTickAnimation = value)
	}

	def getInteracting: Player | Null = {
		npcData.interactingWith
	}

	def isMatching(actor: Actor): Boolean = {
		actor match {
			case n: NPC => (n == this.npc)
			case _ => false
		}
	}

	def getWorldLocation: WorldPoint = npcData.worldLoc
	def getLocalLocation: LocalPoint = npcData.localPoint

	def isTakenDamageRecently: Boolean = stateData.takenDamageRecently
	def setTakenDamageRecently(v: Boolean): Unit = stateData = stateData.copy(takenDamageRecently = v)

	def getLastTickOverheadIcon: HeadIcon | Null = stateData.lastTickOverheadIcon
	def setLastTickOverheadIcon(icon: HeadIcon): Unit = stateData = stateData.copy(lastTickOverheadIcon = icon)

	def getOverheadIcon: HeadIcon | Null = npcData.overheadIcon
	def decrementAttacksUntilSwitch(): Unit = {
		setAttacksUntilSwitch(getAttacksUntilSwitch - 1)
	}
	def setAttacksUntilSwitch(v: Int): Unit = stateData = stateData.copy(attacksUntilSwitch = v)
	def getAttacksUntilSwitch: Int = stateData.attacksUntilSwitch

	def getNextPosibleAttackStyles: Seq[AttackStyle & RegularAttack] = stateData.nextPossibleAttackStyles

	def setNextPosibleAttackStyles(styles: Seq[AttackStyle & RegularAttack]): Unit = stateData = stateData.copy(
		nextPossibleAttackStyles = styles,
	)

	def filterPossibleAttackStyles(p: AttackStyle => Boolean): Unit = {
		val newAttackStyles = stateData.nextPossibleAttackStyles.filter(p)
		stateData = stateData.copy(
			nextPossibleAttackStyles = newAttackStyles
		)
	}

	def setRecentProjectileId(id: Int): Unit = stateData = stateData.copy(recentProjectileId = id)
	def getRecentProjectileId: Int = stateData.recentProjectileId
	def getLastTickInteracting: Player = stateData.lastTickInteracting
	def setLastTickInteracting(p: Player): Unit = stateData = stateData.copy(lastTickInteracting = p)
	def setInitiatedCombat(b: Boolean): Unit = stateData = stateData.copy(initiatedCombat = b)
	def isInitiatedCombat: Boolean = stateData.initiatedCombat
	def setChangedPrayerThisTick(b: Boolean): Unit = stateData = stateData.copy(changedPrayerThisTick = b)
	def isChangedPrayerThisTick: Boolean = stateData.changedPrayerThisTick


	def isChangedAttackStyleThisTick: Boolean = stateData.changedAttackStyleThisTick
	def setChangedAttackStyleThisTick(v: Boolean): Unit = stateData = stateData.copy(changedAttackStyleThisTick = v)
	def isChangedAttackStyleLastTick: Boolean = stateData.changedAttackStyleLastTick
	def setChangedAttackStyleLastTick(v: Boolean): Unit = stateData = stateData.copy(changedAttackStyleLastTick = v)
	def setLastWorldArea(wa: WorldArea): Unit = stateData = stateData.copy(lastWorldArea = wa)
	def getLastWorldArea: WorldArea = stateData.lastWorldArea
	def setDisabledMeleeMovementForTicks(i: Int): Unit = stateData = stateData.copy(disabledMeleeMovementForTicks = i)
	def getDisabledMeleeMovementForTicks: Int = stateData.disabledMeleeMovementForTicks
	def decrementDisabledMeleeMovementForTicks(): Unit = setDisabledMeleeMovementForTicks(getDisabledMeleeMovementForTicks-1)

}
