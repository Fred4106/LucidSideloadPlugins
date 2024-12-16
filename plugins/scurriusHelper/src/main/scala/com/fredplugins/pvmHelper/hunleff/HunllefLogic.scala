package com.fredplugins.pvmHelper.hunleff

import com.fredplugins.common.Locatable.given
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmHelper.{BossToolTrait, FredsPvmHelperPanel, hunleff}
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.CombatUtils
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

import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
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

	inline def localPlayer: Player = client.getLocalPlayer
	inline def getLocalPlayerWorldPoint: WorldPoint = WorldPoint.fromLocalInstance(client, localPlayer.getLocalLocation)

	override def inArea(): Boolean = {
		isGauntletVarbitSet || isHunllefVarbitSet
	}

	object State {
		var inGauntlet: Boolean = false
		var inHunllef: Boolean = false
		var lastSwitchTick: Int = 0
		var lastAttackTick: Int = 0
		var lastDodgeTick: Int = -1
		var lastSafeTile: WorldPoint = uninitialized
		var secondLastSafeTile: WorldPoint = uninitialized
		var hunllef: Hunllef = null
		var missile: Missile = null
		var tornadoes: Set[Tornado] = Set.empty
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

		lastSafeTile = null
		secondLastSafeTile = null

		hunllef = null
		missile = null
		tornadoes = Set.empty

		wrongAttackStyle = false
		switchWeapon = false
	}

	override def layoutPanel(): Seq[LayoutableRenderableEntity] = {
		import State.*

		Seq("inGauntlet" -> State.inGauntlet,
		"inHunllef" -> State.inHunllef,
		"lastSwitchTick" -> State.lastSwitchTick,
		"lastAttackTick" -> State.lastAttackTick,
		"lastDodgeTick" -> State.lastDodgeTick,
		"lastSafeTile" -> State.lastSafeTile,
		"secondLastSafeTile" -> State.secondLastSafeTile,
		"hunllef" -> State.hunllef,
		"missile" -> State.missile,
		"tornadoes" -> State.tornadoes,
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

	@Subscribe private def onVarbitChanged(event: VarbitChanged): Unit = {
		import State.{inHunllef, inGauntlet}
		if (isHunllefVarbitSet) {if (!inHunllef) State.inHunllef = true}
		else if (isGauntletVarbitSet) {if (!inGauntlet) State.inGauntlet = true}
		else if (inGauntlet || inHunllef) resetState()
	}

	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
		if (!inArea() || event.getNpc == null) return
		log.debug(s"Spawned ${event.getNpc.pipe(p => p.getId -> p.getName)}")
		if (Hunllef.validIds.contains(event.getNpc.getId)) {
			State.hunllef = Hunllef(event.getNpc)(skillIconManager.getSkillImage(Skill.MAGIC), skillIconManager.getSkillImage(Skill.RANGED), config.hunllefAttackStyleIconSize)
		} else if (Tornado.validIds.contains(event.getNpc.getId)) {
			State.tornadoes = State.tornadoes + new Tornado(event.getNpc)
		}
	}

	@Subscribe
	private def onNpcDespawned(event: NpcDespawned): Unit = {
		if (!inArea() || event.getNpc == null) return
		log.debug(s"Despawned ${event.getNpc.pipe(p => p.getId -> p.getName)}")
	}

	@Subscribe
	private def onAnimationChanged(event: AnimationChanged): Unit = {
		if (!event.getActor.isInstanceOf[NPC]) return
		if (!inArea()) return
		val npc = event.getActor.asInstanceOf[NPC]
	}


//	@Subscribe
//	private def onProjectileMoved(event: ProjectileMoved): Unit = {
//		val projectile = event.getProjectile
//		val scurrius = NpcUtils.getNearestNpc("Scurrius")
//		val local = client.getLocalPlayer
//		if (!isScurrius(scurrius) || (projectile.getInteracting != local) || (projectile.getRemainingCycles != (projectile.getEndCycle - projectile.getStartCycle))) return
//
//		log.info(s"Projectile {} has {} cycles remaining", projectile.getId, projectile.getRemainingCycles)
//		if (projectile.getId != 2642 && projectile.getId != 2640) return
//
//		if (!attacks.contains(projectile)) {
//			attacks = attacks.appended(projectile)
//			if (config.autoPray) CombatUtils.deactivatePrayer(Prayer.PROTECT_FROM_MELEE)
//		}
//	}

//	@Subscribe
//	private def onHitsplatApplied(event: HitsplatApplied): Unit = {
//		if(!inArea()) return
//		if(event.getActor != localPlayer) return
//		if(!event.getHitsplat.isMine) return
//		if(requestedJadPrayer.isDefined) {
//			requestedJadPrayer = None
//		}
//	}

	@Subscribe
	private def onGameTick(event: GameTick): Unit = {
		if (!inArea()) return
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
