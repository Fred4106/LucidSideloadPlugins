package com.fredplugins.pvmHelper.hunleff

import com.fredplugins.common.Locatable
import com.fredplugins.common.Locatable.given
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmHelper.{BossToolTrait, FredsPvmHelperPanel, hunleff}
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.{CombatUtils, MessageUtils}
import ethanApiPlugin.EthanApiPlugin
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.*
import net.runelite.api.*
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.game.SkillIconManager
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent}
import org.slf4j.Logger

import java.awt.Color
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.util.Try
import scala.util.chaining.*
@PluginDescriptor(
	name = "<html><font color=\"#A1004B\">Freds</font> PVM Helper - Hunllef</html>",
	description = "Helps fight the echo Hunllef",
	tags = Array("pvm", "hunllef", "prayer", "helper", "maps", "gauntlet")
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class HunllefLogic() extends Plugin with BossToolTrait {
	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val skillIconManager: SkillIconManager = null
	@Inject val config: HunleffConfig = null
	@Inject val notifier: Notifier = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject private val eventBus: EventBus = null
	@Inject private val overlayManager: OverlayManager = null
	private val panel: FredsPvmHelperPanel[HunllefLogic] = new FredsPvmHelperPanel(this) {}

	given Client = client
	given SkillIconManager = skillIconManager

	@Provides
	def getConfig(configManager: ConfigManager): HunleffConfig = {
		configManager.getConfig[HunleffConfig](classOf[HunleffConfig])
	}

	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if (e.getGroup == hunleff.HunleffConfig.GroupName) {
			e.getKey match {
				case u => log.debug("Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e.getNewValue)
			}
		}
	}


	inline def isGauntletVarbitSet: Boolean = client.getVarbitValue(9178) == 1
	inline def isHunllefVarbitSet: Boolean = client.getVarbitValue(9177) == 1
//
//	inline def localPlayer: Player = client.getLocalPlayer
//	inline def getLocalPlayerWorldPoint: WorldPoint = WorldPoint.fromLocalInstance(client, localPlayer.getLocalLocation)

	override def inArea(): Boolean = {
		State.inGauntlet || State.inHunllef
	}

	sealed trait HunllefCycle {}

	case class Range(couldBeInverted: Boolean = false) extends HunllefCycle {}
	case object Mage extends HunllefCycle {}

	object State {
		var inGauntlet: Boolean = false
		var inHunllef: Boolean = false
		var lastSwitchTick: Int = 0
		var lastAttackTick: Int = 0
		var lastDodgeTick: Int = -1
		var lastSafeTile: WorldPoint = uninitialized
		var secondLastSafeTile: WorldPoint = uninitialized

		var projectilesSpawnedThisTick = Set.empty[Int]

		object HunllefState {
			/**
			 * The boss has an attack speed of 4.
			 * It initially prays against all three combat styles at once. Using the crystal dagger's special attack will disable this temporarily. At 75%, 50%, and 25%, if this prayer has been disabled, Hunllef will begin praying against all three combat styles again.
			 * Every 4th ranged attack, there is a ~50% chance for the boss to instead launch a projectile that will deal significant damage, disable all active prayers, and drain prayer if the player's overhead protection prayers are enabled when it lands.
			 * Blue tornadoes are present in the room from the beginning, moving clockwise around the edges of the room and diagonally from the corners. One is present at the beginning of the fight, with an additional tornado spawning when Hunllef is at 75%, 50%, and 25% health, for a total of four by the end of the fight.
			 * Each auto-attack hits twice.
			 */
			//0, 1, 2, 3 are range
			//4, 5, 6, 7 are mage
			//any attack on mage cycle can be either magic or prayer disabling
			//the last attack on range cycle can be either a range attack or a prayer inversion attack which equires overhead prayers to be off
//			private val CYCLE_SEQ: List[HunllefCycle] = List(Range, Range, Range, RangeOrPrayerInvert, MageOrPrayerDisable, MageOrPrayerDisable, MageOrPrayerDisable, MageOrPrayerDisable)
			private var cycleV: Int = 0
			private var ticksUntilNextAttackV: Int = 4

			def getNextCycle: HunllefCycle = {
				cycleV match {
					case y if y < 4 => Range(y == 3)
					case y if y < 8 => Mage
				}
			}

			def getLastCycle: HunllefCycle = {
				(((cycleV + 7) % 8)) match {
						case y if y < 4 => Range(y == 3)
						case y if y < 8 => Mage
				}
			}
			def getTicksUntilNextAttack: Int = {
				math.max(math.min(4, ticksUntilNextAttackV), 1)
			}
			def onHunllefAttack(): Unit = {
				cycleV = (cycleV+1) % 8
				ticksUntilNextAttackV=4
			}

			def onPlayerAttack(): Unit = {

			}

			def onTick(): Unit = {
				ticksUntilNextAttackV = math.max(math.min(4, ticksUntilNextAttackV-1), 1)
			}

			def reset(): Unit = {
				cycleV = 0
				ticksUntilNextAttackV = 4
			}
		}

//		var missile: Missile = null
//		var tornadoes: Set[Tornado] = Set.empty

		var wrongAttackStyle: Boolean = false
		var switchWeapon: Boolean = false
	}

	override def resetState(): Unit = {
		import State.*
		inGauntlet = false
		inHunllef = false
		lastSwitchTick = 0
		lastAttackTick = 0
		lastDodgeTick = -1

		projectilesSpawnedThisTick = Set.empty[Int]
		HunllefState.reset()

		lastSafeTile = null
		secondLastSafeTile = null

		wrongAttackStyle = false
		switchWeapon = false
	}

	override def layoutPanel(): Seq[LayoutableRenderableEntity] = {
		import State.*


		Seq("inGauntlet" -> State.inGauntlet,
		"inHunllef" -> State.inHunllef,
		"hunllef(cycle, ticksTillAttack)" -> (State.HunllefState.getNextCycle, State.HunllefState.getTicksUntilNextAttack),
		"lastSwitchTick" -> State.lastSwitchTick,
		"lastAttackTick" -> State.lastAttackTick,
		"lastDodgeTick" -> State.lastDodgeTick,
		"lastSafeTile" -> State.lastSafeTile,
		"secondLastSafeTile" -> State.secondLastSafeTile,
		"wrongAttackStyle" -> State.wrongAttackStyle,
		"switchWeapon" -> State.switchWeapon)
			.map(d => LineComponent.builder.left(d._1).right(Option(d._2).map(_.toString).getOrElse("None")).build())
	}

	override protected def startUp(): Unit = {
		resetState()
		overlayManager.add(panel)
		//		overlayManager.add(overlay)
	}


	override protected def shutDown(): Unit = {
		overlayManager.remove(panel)
		//		overlayManager.remove(overlay)
		resetState()
	}


	var didLogThisTick = false
	private inline def addGametick(s: String): String = s"Tick[${client.getTickCount}] | ${s}"

	def logTick(var1: String, var2: Any*): Unit  = {
		didLogThisTick  = true
		log.debug(addGametick(var1), var2 *)
	}


	@Subscribe
	private def onVarbitChanged(event: VarbitChanged): Unit = {
		(isGauntletVarbitSet, isHunllefVarbitSet,  (State.inGauntlet || State.inHunllef)) match {
			case (a, b, c) if((a || b) != c && c) => resetState()
			case (a, b, c) => {
				State.inHunllef = b
				State.inGauntlet = a
			}
		}
	}

	def isHunllef(n: NPC): Boolean = {
		Set(NpcID.CRYSTALLINE_HUNLLEF, NpcID.CRYSTALLINE_HUNLLEF_9022,
			NpcID.CRYSTALLINE_HUNLLEF_9023, NpcID.CRYSTALLINE_HUNLLEF_9024,
			NpcID.CORRUPTED_HUNLLEF, NpcID.CORRUPTED_HUNLLEF_9036,
			NpcID.CORRUPTED_HUNLLEF_9037, NpcID.CORRUPTED_HUNLLEF_9038).contains(n.getId)
	}

	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
		if (!inArea() || event.getNpc == null) return
		var shouldLog = true

		if (isHunllef(event.getNpc)) {
			State.HunllefState.reset()
		}
		/* else if (Tornado.validIds.contains(event.getNpc.getId)) {
			State.tornadoes = State.tornadoes + new Tornado(event.getNpc)
		}*/ else {
			shouldLog = false
		}

		if (shouldLog) {
			logTick(s"Spawned npc[${event.getNpc.getIndex}] = {id=${event.getNpc.getId}, name=${event.getNpc.getName}}")
		}
	}

	@Subscribe
	private def onNpcDespawned(event: NpcDespawned): Unit = {
		if (!inArea() || event.getNpc == null) return
		var shouldLog: Boolean = true
		if (isHunllef(event.getNpc)) {
			State.HunllefState.reset()
		}/* else if (State.tornadoes.exists(_.npc == event.getNpc)) {
			State.tornadoes = State.tornadoes.filter(t => t.npc != event.getNpc)
		}*/ else {shouldLog = false}

		if(shouldLog) {
			logTick(s"Despawned npc[${event.getNpc.getIndex}] = {id=${event.getNpc.getId}, name=${event.getNpc.getName}}")
		}
	}

	val HUNLLEF_TORNADO = 8418
	val HUNLLEF_ATTACK_ANIM = 8419
	val HUNLLEF_STYLE_SWITCH_TO_MAGE = 8754
	val HUNLLEF_STYLE_SWITCH_TO_RANGE = 8755

	@Subscribe
	private def onAnimationChanged(event: AnimationChanged): Unit = {
		if (!inArea()) return
		val animationId = event.getActor.getAnimation

		event.getActor match {
			case npc: NPC if (isHunllef(npc) && Set(HUNLLEF_ATTACK_ANIM, HUNLLEF_TORNADO).contains(animationId)) => {
				logTick(s"onHunllefAttack {id=${npc.getId}, name=${npc.getName}, animation=${npc.getAnimation}}")
				State.HunllefState.onHunllefAttack()
			}
//			case player: Player if (player == client.getLocalPlayer) => {
//				logTick(s"AnimationChanged localPlayer = {id=${player.getId}, name=${player.getName}, animation=${player.getAnimation}}")
//			}
			case _ => {}
		}
	}


	@Subscribe
	private def onProjectileMoved(event: ProjectileMoved): Unit = {
		if (!inArea()) return

		val projectile = event.getProjectile
		val justSpawned = projectile.getRemainingCycles() == (projectile.getEndCycle() - projectile.getStartCycle())

		import State.projectilesSpawnedThisTick
		if(justSpawned && projectile.getInteracting == client.getLocalPlayer && !projectilesSpawnedThisTick.contains(projectile.getId)) {
			projectilesSpawnedThisTick = projectilesSpawnedThisTick + projectile.getId
		}
	}

//	@Subscribe
//	private def onHitsplatApplied(event: HitsplatApplied): Unit = {
//		if(!inArea()) return
//		if(event.getActor != localPlayer) return
//		if(!event.getHitsplat.isMine) return
//		if(requestedJadPrayer.isDefined) {
//			requestedJadPrayer = None
//		}
//	}
	@Subscribe private def onChatMessage(event: ChatMessage): Unit = {
		val `type` = event.getType
		logTick(s"onChatMessage: \"${event.getMessage}\"")
		if (event.getMessage.contains("prayers have been disabled")) {
			val x = Prayer.values.flatMap ( p =>
				Option.when(client.isPrayerActive(p))(
					p.name()
				)
			).mkString("[", ", ", "]")
			val protectPrayerReq = State.HunllefState.getNextCycle match {
				case Range(couldBeInverted) => Prayer.PROTECT_FROM_MISSILES
				case Mage => Prayer.PROTECT_FROM_MAGIC
			}
			MessageUtils.addMessage(s"Chat message: ${event.getMessage.replace("prayers have been disabled", "disabled prayers")}" + "[" + protectPrayerReq + "] [" + "null" + "] " + x, Color.BLUE)
			if (config.autoPray()) CombatUtils.togglePrayer(protectPrayerReq)
		}
	}

//	var oldHunleffCycle: HunllefCycle = State.HunllefState.getNextCycle
	@Subscribe
	private def onGameTick(event: GameTick): Unit = {
		if (!inArea()) {
		} else {
			import State.projectilesSpawnedThisTick
			if(State.inHunllef) {
				val currentCycle = State.HunllefState.getNextCycle
//				val oldHunleffCycle = State.HunllefState.getLastCycle

				if(projectilesSpawnedThisTick.nonEmpty) {
					logTick(s"hunllef spawned these projectiles ${projectilesSpawnedThisTick.mkString("Set(", ", ", ")")}")
					projectilesSpawnedThisTick = Set.empty
				}
//				if(oldHunleffCycle != currentCycle) {
//					logTick("hunllef cycle changed from {} to {}", oldHunleffCycle, currentCycle)
				if(client.getProjectiles.asScala.toList.filter(_.getInteracting == client.getLocalPlayer).exists(p => p.getId == 3164)) {
					CombatUtils.deactivatePrayers(true)
				} else {
					currentCycle match {
						case Range(couldBeInverted) => CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MISSILES)
						case Mage => CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MAGIC)
					}
				}

				State.HunllefState.onTick()
				projectilesSpawnedThisTick = Set.empty
			}
		}

		if(didLogThisTick) {
			logTick ("GameTick\n")
			didLogThisTick = false
		}

			//		attacks = attacks.filter((proj: Projectile) => proj.getRemainingCycles > 30)
//		justDodged = false
//		if (fallingCeilingToTicks.nonEmpty) {
//			dodgeFallingCeiling()
//			fallingCeilingToTicks = (fallingCeilingToTicks.toList.map(in => in._1 -> (in._2 - 1)).filter(_._2 > 0)).toMap
//			//			fallingCeilingToTicks.filterInPlace((_: GraphicsObject, v: Int) => v > 0)
//		}
//		val scurrius = NpcUtils.getNearestNpc("Scurrius")
//		if (!justDodged) {
//			if (config.attackAfterDodge && (client.getLocalPlayer.getInteracting ne scurrius)) {
//				val tSinceLastDodge = client.getTickCount - lastDodgeTick
//				if (tSinceLastDodge < 3) if (scurrius != null) if (!config.prioritizeRats || getEligibleRat == null) NpcUtils.attackNpc(scurrius)
//			}
//		}
//		var attackRat = true
//		if (scurrius != null) {
//			val ratio = scurrius.getHealthRatio
//			val scale = scurrius.getHealthScale
//			val targetHpPercent = ratio.toDouble / scale.toDouble * 100
//			if (targetHpPercent > 0) attackRat = false
//		}
//		if (justDodged) return
//		if (config.attackRats && attackRat || config.prioritizeRats) {
//			val giantRat = getEligibleRat
//			if (giantRat != null && (giantRat ne client.getLocalPlayer.getInteracting)) {
//				NpcUtils.attackNpc(giantRat)
//				lastRatTick = client.getTickCount
//			}
//			else if (config.prioritizeRats && giantRat == null) {
//				val tSinceLatRatHit = client.getTickCount - lastRatTick
//				if (scurrius != null && tSinceLatRatHit < 8 && (client.getLocalPlayer.getInteracting ne scurrius)) NpcUtils.attackNpc(scurrius)
//			}
//		}
	}

//	def shouldPrayAgainst(tzMob: TzMob): Boolean = {
//		val v3 = (tzMob.tpe == KetZek && config.autoPrayMage() && tzMob.distanceTo(localPlayer) < 18)
//		val v1 = (tzMob.tpe == TokXil && config.autoPrayRange() && tzMob.distanceTo(localPlayer) < 18)
//		val v2 = (tzMob.tpe == YtMejKot && config.autoPrayMelee() && tzMob.distanceTo(localPlayer) < 3)
//		v1 || v2 || v3
//	}
}
