package com.fredplugins.pvmHelper.helpers

import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.pvmHelper.helpers.ScurriusConfig.ConfigObj
import com.fredplugins.pvmHelper.{BossToolTrait, FredsPvmHelperConfig}
import com.google.gson.JsonObject
import com.lucidplugins.api.utils.{CombatUtils, InteractionUtils, NpcUtils}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.{AnimationChanged, GameTick, GraphicsObjectCreated, NpcSpawned, ProjectileMoved}
import net.runelite.api.{Client, GameObject, GraphicsObject, NPC, Prayer, Projectile}
import net.runelite.client.config.{Config, ConfigGroup, ConfigItem, ConfigManager, ConfigSection}
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayPanel
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, TitleComponent}
import scala.reflect.Selectable.reflectiveSelectable

import java.awt.Color
import scala.util.Try
import scala.util.chaining.*

//@ConfigGroup("fredspvmhelper")
abstract class ConfigPropValue[T] {
	def value: T
	def value_=(value: T): Unit
}

given Conversion[String, Boolean] = (_.toBoolean)
given Conversion[Boolean, String] = (_.toString)
//object ConfigPropFactory {
//	def createKey[T](encode: T => String, decode: String => T, group: String, subGroup: String, key: String)(defaultValue: T)(using cm: ConfigManager): ConfigPropValue[T] = {
//
//	}
//}

//trait ScurriusConfig {
//	def stayMelee: ConfigPropValue[Boolean]
////	def stayMelee_=(melee: Boolean): Unit
//
//	def attackAfterDodge: ConfigPropValue[Boolean]
//	def attackOnSpawn: ConfigPropValue[Boolean]
//	def attackRats: ConfigPropValue[Boolean]
//	def prioritizeRats: ConfigPropValue[Boolean]
//	def autoPray: ConfigPropValue[Boolean]
//}
object ScurriusConfig {
	type ConfigObj = Object {val stayMelee: ConfigPropValue[Boolean]; val attackRats: ConfigPropValue[Boolean]; val autoPray: ConfigPropValue[Boolean]; val attackOnSpawn: ConfigPropValue[Boolean]; val attackAfterDodge: ConfigPropValue[Boolean]; val prioritizeRats: ConfigPropValue[Boolean]}
	def load(using configManager: ConfigManager): ConfigObj = {
		inline def makeKey[T](inline keyName: String, inline iv: T)(using decode: Conversion[String, T], encode: Conversion[T, String]): ConfigPropValue[T] = {
			new ConfigPropValue[T] {
				override def value_=(value: T): Unit = {
					encode(value).pipe(ev => configManager.setConfiguration(FredsPvmHelperConfig.GroupName, s"scurrius-${keyName}", ev))
				}

				override def value: T = configManager.getConfiguration(FredsPvmHelperConfig.GroupName, s"scurrius-${keyName}").pipe(decode)
			}.tap(cpv => cpv.value = iv)
		}


		new {
			val stayMelee: ConfigPropValue[Boolean] = makeKey("stayMelee", false)
			val attackAfterDodge: ConfigPropValue[Boolean] = makeKey("attackAfterDodge", false)
			val attackOnSpawn: ConfigPropValue[Boolean] = makeKey("attackOnSpawn", false)
			val attackRats: ConfigPropValue[Boolean] = makeKey(" qattackRats", false)
			val prioritizeRats: ConfigPropValue[Boolean] = makeKey("prioritizeRats", false)
			val autoPray: ConfigPropValue[Boolean] = makeKey("autoPray", false)
		}
	}
}
class ScurriusLogic(using client: Client, configManager: ConfigManager) extends BossToolTrait {
	private val FALLING_CEILING_GRAPHIC: Int = 2644
	private val SCURRIUS: Int = 7222
	private val SCURRIUS_PUBLIC: Int = 7221
	private val DURATION: Int = 9

	private var bossNpc: NPC = _
	private var justDodged: Boolean = false
	private var lastDodgeTick: Int = 0
	private var lastRatTick: Int = 0
	private var lastActivateTick: Int = 0
	private var fallingCeilingToTicks: Map[GraphicsObject, Int] = Map.empty //new HashMap<>();
	private var attacks: List[Projectile] = List.empty

	private val config: ConfigObj = ScurriusConfig.load

	override def resetState(): Unit = {
		bossNpc = null
		justDodged = false
		lastDodgeTick = 0
		lastRatTick = 0
		lastActivateTick = 0
		attacks = List.empty
		fallingCeilingToTicks = Map.empty
	}


	@Subscribe
	private def onGraphicsObjectCreated(event: GraphicsObjectCreated): Unit = {
		if (!inArea()) return
		val graphicsObject = event.getGraphicsObject
		val id = graphicsObject.getId
		if (id == FALLING_CEILING_GRAPHIC) {
			fallingCeilingToTicks = fallingCeilingToTicks.updated(graphicsObject, DURATION)
		}
	}

	inline def isScurrius(npc: NPC): Boolean = npc != null && (npc.getId == SCURRIUS || npc.getId == SCURRIUS_PUBLIC)

	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
		if (!inArea()) return
		if (config.attackOnSpawn.value && isScurrius(event.getNpc)) {
			lastDodgeTick = client.getTickCount
		}
	}

	@Subscribe
	private def onAnimationChanged(event: AnimationChanged): Unit = {
		if (!event.getActor.isInstanceOf[NPC]) return
		if (!inArea()) return
		val npc = event.getActor.asInstanceOf[NPC]
		if (isScurrius(npc) && npc.getAnimation == 10705 && NpcUtils.getNearestNpc("Giant rat") == null) {
			if (config.autoPray.value) {
				CombatUtils.deactivatePrayers(false)
			}
		}
	}

	@Subscribe
	private def onProjectileMoved(event: ProjectileMoved): Unit = {
		val projectile = event.getProjectile
		val scurrius = NpcUtils.getNearestNpc("Scurrius")
		val local = client.getLocalPlayer
		if (!isScurrius(scurrius) || (projectile.getInteracting != local) || (projectile.getRemainingCycles != (projectile.getEndCycle - projectile.getStartCycle))) return

		log.info(s"Projectile {} has {} cycles remaining", projectile.getId, projectile.getRemainingCycles)
		if (projectile.getId != 2642 && projectile.getId != 2640) return

		if (!attacks.contains(projectile)) {
			attacks = attacks.appended(projectile)
			if (config.autoPray.value) CombatUtils.deactivatePrayer(Prayer.PROTECT_FROM_MELEE)
		}
	}

	@Subscribe
	private def onGameTick(event: GameTick): Unit = {
		//		val instancePoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation)
		if (!inArea()) return //instancePoint.getRegionID != 13210 || instancePoint.getRegionX < 23) return
		handlePrayers()
		attacks = attacks.filter((proj: Projectile) => proj.getRemainingCycles > 30)
		justDodged = false
		if (fallingCeilingToTicks.nonEmpty) {
			dodgeFallingCeiling()
			fallingCeilingToTicks = (fallingCeilingToTicks.toList.map(in => in._1 -> (in._2 - 1)).filter(_._2 > 0)).toMap
			//			fallingCeilingToTicks.filterInPlace((_: GraphicsObject, v: Int) => v > 0)
		}
		val scurrius = NpcUtils.getNearestNpc("Scurrius")
		if (!justDodged) {
			if (config.attackAfterDodge.value && (client.getLocalPlayer.getInteracting ne scurrius)) {
				val tSinceLastDodge = client.getTickCount - lastDodgeTick
				if (tSinceLastDodge < 3) if (scurrius != null) if (!config.prioritizeRats.value || getEligibleRat == null) NpcUtils.attackNpc(scurrius)
			}
		}
		var attackRat = true
		if (scurrius != null) {
			val ratio = scurrius.getHealthRatio
			val scale = scurrius.getHealthScale
			val targetHpPercent = ratio.toDouble / scale.toDouble * 100
			if (targetHpPercent > 0) attackRat = false
		}
		if (justDodged) return
		if (config.attackRats.value && attackRat || config.prioritizeRats.value) {
			val giantRat = getEligibleRat
			if (giantRat != null && (giantRat ne client.getLocalPlayer.getInteracting)) {
				NpcUtils.attackNpc(giantRat)
				lastRatTick = client.getTickCount
			}
			else if (config.prioritizeRats.value && giantRat == null) {
				val tSinceLatRatHit = client.getTickCount - lastRatTick
				if (scurrius != null && tSinceLatRatHit < 8 && (client.getLocalPlayer.getInteracting ne scurrius)) NpcUtils.attackNpc(scurrius)
			}
		}
	}

	private def handlePrayers(): Unit = {
		if (!config.autoPray.value) return
		var prayer: Prayer = null
		//		import scala.collection.JavaConversions._
		for (projectile <- attacks) {
			val cyclesToTicks = Math.floor(projectile.getRemainingCycles / 30.0F).toInt
			if (cyclesToTicks <= 1) {
				if (projectile.getId == 2642) {
					prayer = Prayer.PROTECT_FROM_MISSILES
				} else {
					prayer = Prayer.PROTECT_FROM_MAGIC
				}
			}
		}
		if (prayer != null) {
			CombatUtils.activatePrayer(prayer)
		} else {
			val targetingMe = NpcUtils.getNearestNpc((npc: NPC) => (npc.getName != null && npc.getName == "Giant rat") || (npc.getName != null && npc.getName == "Scurrius" && npc.getPoseAnimation == 10687 && npc.getAnimation != 10705))
			if (targetingMe != null) {
				if (attacks.size == 0) {
					CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MELEE)
					lastActivateTick = client.getTickCount
				}
				else {
					if (client.isPrayerActive(Prayer.PROTECT_FROM_MELEE) && client.getTickCount - lastActivateTick < 3 || attacks.size > 0) return
					CombatUtils.deactivatePrayers(true)
				}
			}
		}
	}

	private def dodgeFallingCeiling(): Unit = {
		for (fallingCeiling <- fallingCeilingToTicks.toSet) {
			val unsafeTile = fallingCeiling._1.getLocation
			val playerTile = client.getLocalPlayer.getLocalLocation
			if (unsafeTile.getX == playerTile.getX && unsafeTile.getY == playerTile.getY) {
				val scurrius = NpcUtils.getNearestNpc("Scurrius")
				if (scurrius != null) {
					val unsafeTiles = fallingCeilingToTicks.keys.map(_.getLocation).toList
					var safeTile = Option.empty[WorldPoint]
					if (config.stayMelee.value) {
						safeTile = SInteractionUtils.getClosestSafeLocationInNPCMeleeDistance(unsafeTiles, scurrius)
					} else {
						safeTile = SInteractionUtils.getClosestSafeLocationNotInNPCMeleeDistance(unsafeTiles, scurrius)
					}
					if (safeTile.isDefined) {
						InteractionUtils.walk(safeTile.get)
						justDodged = true
						lastDodgeTick = client.getTickCount
					}
				}
			}
		}
	}

	private def getEligibleRat: NPC = NpcUtils.getNearestNpc((npc: NPC) => {
		if (npc == null) {
			false
		} else {
			val ratio = npc.getHealthRatio
			val scale = npc.getHealthScale
			val targetHpPercent = ratio.toDouble / scale.toDouble * 100
			npc.getName != null && npc.getName == "Giant rat" && targetHpPercent > 0
		}
	})

	override def layoutPanel(): Seq[LayoutableRenderableEntity] = {
		Seq(
			//			LineComponent.builder
			//				.left("region")
			//				.right(s"inArea(${getRegion}, ${getRegionX}, ${getRegionY}) = ${inArea()}")
			//				.build,
			LineComponent.builder
				.left("Npc")
				.right(s"${bossNpc}")
				.build,
			LineComponent.builder
				.left("justDodged")
				.right(s"${justDodged}")
				.build,
			LineComponent.builder
				.left("lastDodgeTick")
				.right(s"${lastDodgeTick}")
				.build,
			LineComponent.builder
				.left("lastRatTick")
				.right(s"${lastRatTick}")
				.build,
			LineComponent.builder
				.left("lastActivateTick")
				.right(s"${lastActivateTick}")
				.build
		)
	}

	inline def getLocalPlayerWorldPoint: WorldPoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation)

	inline def getRegionId: Int = Try(getLocalPlayerWorldPoint.getRegionID).getOrElse(-1)


	override def inArea(): Boolean = {
		val toRet = getLocalPlayerWorldPoint.pipe(x => x.getRegionID == 13210 && x.getRegionX >= 23)
		toRet
	}
}
