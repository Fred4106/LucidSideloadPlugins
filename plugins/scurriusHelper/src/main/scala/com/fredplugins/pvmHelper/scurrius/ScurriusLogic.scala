package com.fredplugins.pvmHelper.scurrius

import com.fredplugins.common.utils.{SInteractionUtils, ShimUtils}
import com.fredplugins.pvmHelper.{BossToolTrait, FredsPvmHelperConfig, FredsPvmHelperPanel}
import com.google.gson.JsonObject
import com.lucidplugins.api.utils.CombatUtils
import com.lucidplugins.api.utils.{CombatUtils, InteractionUtils, NpcUtils}
import interactionApi.PrayerInteraction
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.*
import net.runelite.api.*
import net.runelite.client.config.*
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayPanel
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, TitleComponent}

import java.awt.Color
import scala.reflect.Selectable.reflectiveSelectable
import scala.util.Try
import scala.util.chaining.*
//import com.fredplugins.pvmHelper.helpers.ScurriusLogic.{DURATION, FALLING_CEILING_GRAPHIC, SCURRIUS, SCURRIUS_PUBLIC}
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.{CombatUtils, InteractionUtils, NpcUtils}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{TileObjects, Widgets}
import net.runelite.api.{ChatMessageType, Client, GameState, GraphicsObject, InventoryID, Item, ItemContainer, NPC, Prayer, Projectile, TileObject}
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.OverlayManager
import org.slf4j.Logger

import java.awt.Font
import java.util
import java.util.stream.Collectors
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.IntAccumulator
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.StreamHasToScala
import scala.util.chaining.*
import scala.util.{Random, Try}

@PluginDescriptor(
	name = "<html><font color=\"#A1004B\">Freds</font> PVM Helper - Scurrius</html>",
	description = "Dodges Scurrius' falling ceiling attack and re-attacks",
	tags = Array("pvm", "scurrius", "prayer", "helper", "maps"),
	conflicts = Array("<html><font color=\"#32CD32\">Lucid </font>Scurrius Helper</html>")
	)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class ScurriusLogic() extends Plugin with BossToolTrait {
	@Inject val client      : Client         = null
	@Inject val clientThread: ClientThread   = null
	@Inject val config      : ScurriusConfig = null
	@Inject val notifier    : Notifier       = null
	private         val log                    : Logger                             = ShimUtils.getLogger(
		this
			.getClass
			.getName, "DEBUG")
	@Inject private val eventBus               : EventBus                           = null
	@Inject private val overlayManager         : OverlayManager                     = null
	@Inject private val configManager          : ConfigManager                      = null
	//	@Inject private val overlay: FredsPvmHelperOverlay = null
	private         val panel                  : FredsPvmHelperPanel[ScurriusLogic] = new FredsPvmHelperPanel(this) {}
	private         val FALLING_CEILING_GRAPHIC: Int                                = 2644
	private         val SCURRIUS               : Int                                = 7222
	private         val SCURRIUS_PUBLIC        : Int                                = 7221
	private         val SCURRIUS_GIANT_RAT     : Int                                = 7223
	private         val DURATION               : Int                                = 9
	private var bossNpc                        : NPC                                = uninitialized
	private var justDodged                     : Boolean                            = false
	private var lastDodgeTick                  : Int                                = 0
	private var lastRatTick                    : Int                                = 0
	private var lastActivateTick               : Int                                = 0
	private var fallingCeilingToTicks          : Map[GraphicsObject, Int]           = Map.empty //new HashMap<>();
	private var attacks                        : List[Projectile]                   = List.empty

	given Client = client

	@Provides
	def getConfig(configManager: ConfigManager): ScurriusConfig = {
		configManager.getConfig[ScurriusConfig](classOf[ScurriusConfig])
	}

	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if (e.getGroup == ScurriusConfig.GroupName) {
			e.getKey match {
				case u => {
					log.debug(
						"Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e
							.getNewValue)
				}
			}
		}
	}

	override def layoutPanel(): Seq[LayoutableRenderableEntity] = {
		Seq(
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

	inline def getRegionId: Int = Try(getLocalPlayerWorldPoint.getRegionID).getOrElse(-1)

	override protected def startUp(): Unit = {
		resetState()
		overlayManager.add(panel)
		//		overlayManager.add(overlay)
	}

	override def resetState(): Unit = {
		bossNpc = null
		justDodged = false
		lastDodgeTick = 0
		lastRatTick = 0
		lastActivateTick = 0
		attacks = List.empty
		fallingCeilingToTicks = Map.empty
	}

	override protected def shutDown(): Unit = {
		overlayManager.remove(panel)
		//		overlayManager.remove(overlay)
		resetState()
	}

	@Subscribe
	private def onGraphicsObjectCreated(event: GraphicsObjectCreated): Unit = {
		if (!inArea()) return
		val graphicsObject = event.getGraphicsObject
		val id             = graphicsObject.getId
		if (id == FALLING_CEILING_GRAPHIC) {
			fallingCeilingToTicks = fallingCeilingToTicks.updated(graphicsObject, DURATION)
		}
	}

	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
		if (!inArea()) return
		if (config.attackOnSpawn && isScurrius(event.getNpc)) {
			lastDodgeTick = client.getTickCount
		}
	}

	@Subscribe
	private def onAnimationChanged(event: AnimationChanged): Unit = {
		if (!event.getActor.isInstanceOf[NPC]) return
		if (!inArea()) return
		val npc = event.getActor.asInstanceOf[NPC]
		if (isScurrius(npc) && npc.getAnimation == 10705 && NpcUtils.getNearestNpc("Giant rat") == null) {
			if (config.autoPray) {
				CombatUtils.deactivatePrayers(false)
			}
		}
	}

	inline def isScurrius(npc: NPC): Boolean = npc != null && (npc.getId == SCURRIUS || npc.getId == SCURRIUS_PUBLIC)
	inline def isGiantRat(npc: NPC): Boolean = npc != null && npc.getId == SCURRIUS_GIANT_RAT

	@Subscribe
	private def onProjectileMoved(event: ProjectileMoved): Unit = {
		val projectile = event.getProjectile
		val scurrius   = NpcUtils.getNearestNpc("Scurrius")
		val local      = client.getLocalPlayer
		if (!isScurrius(scurrius) || (projectile.getInteracting != local) || (projectile.getRemainingCycles != (projectile
			.getEndCycle - projectile.getStartCycle))) {
			return
		}
		log.info(s"Projectile {} has {} cycles remaining", projectile.getId, projectile.getRemainingCycles)
		if (projectile.getId != 2642 && projectile.getId != 2640) return

		if (!attacks.contains(projectile)) {
			attacks = attacks.appended(projectile)
			if (config.autoPray) CombatUtils.deactivatePrayer(Prayer.PROTECT_FROM_MELEE)
		}
	}

	@Subscribe
	private def onGameTick(event: GameTick): Unit = {
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
			if (config.attackAfterDodge && (client.getLocalPlayer.getInteracting ne scurrius)) {
				val tSinceLastDodge = client.getTickCount - lastDodgeTick
				if (tSinceLastDodge < 3) {
					if (scurrius != null) {
						if (!config
							.prioritizeRats || getEligibleRat == null) {
							NpcUtils.attackNpc(scurrius)
						}
					}
				}
			}
		}
		var attackRat = true
		if (scurrius != null) {
			val ratio           = scurrius.getHealthRatio
			val scale           = scurrius.getHealthScale
			val targetHpPercent = ratio.toDouble / scale.toDouble * 100
			if (targetHpPercent > 0) attackRat = false
		}
		if (justDodged) return
		if (config.attackRats && attackRat || config.prioritizeRats) {
			val giantRat = getEligibleRat
			if (giantRat != null && (giantRat ne client.getLocalPlayer.getInteracting)) {
				NpcUtils.attackNpc(giantRat)
				lastRatTick = client.getTickCount
			}
			else if (config.prioritizeRats && giantRat == null) {
				val tSinceLatRatHit = client.getTickCount - lastRatTick
				if (scurrius != null && tSinceLatRatHit < 8 && (client
					.getLocalPlayer
					.getInteracting ne scurrius)) {
					NpcUtils.attackNpc(scurrius)
				}
			}
		}
	}

	private def handlePrayers(): Unit = {
		if (!config.autoPray) return
		var prayer: Prayer = null
		//		import scala.collection.JavaConversions._
		for (projectile <- attacks) {
			val cyclesToTicks = Math.floor(projectile.getRemainingCycles / 30.0F).toInt
			if (cyclesToTicks <= 2) {
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
			val scurriusFilter: NPC => Boolean = (npc: NPC) =>
				isScurrius(npc) &&
					npc.getPoseAnimation == gameval.AnimationID.NPC_RAT_BOSS_IDLE_01 &&
					npc.getAnimation != gameval.AnimationID.NPC_RAT_BOSS_DEATH_01

			val giantRatFilter: NPC => Boolean = (npc: NPC) => (npc.getName != null && npc.getName == "Giant rat")
			///*

			val targetingMe = NpcUtils.getNearestNpc(n => scurriusFilter(n)) //(npc: NPC) => (npc.getName != null && npc.getName == "Giant rat") || )
			if (targetingMe != null) {
				if(attacks.isEmpty) {
					CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MELEE)
					lastActivateTick = client.getTickCount
				}
//				else if(client.getTickCount - lastActivateTick > 3) {
//					CombatUtils.deactivatePrayers(true)
//				}
			} else if(CombatUtils.getActiveOverhead != null && client.getTickCount - lastActivateTick > 3) {
				CombatUtils.deactivatePrayers(true);
			}
//			*/
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
					var safeTile    = Option.empty[WorldPoint]
					if (config.stayMelee) {
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
			val ratio           = npc.getHealthRatio
			val scale           = npc.getHealthScale
			val targetHpPercent = ratio.toDouble / scale.toDouble * 100
			npc.getName != null && npc.getName == "Giant rat" && targetHpPercent > 0
		}
	})

	override def inArea(): Boolean = {
		getLocalPlayerWorldPoint.pipe(x => x.getRegionID == 13210 && x.getRegionX >= 23)
	}

	inline def getLocalPlayerWorldPoint: WorldPoint = WorldPoint.fromLocalInstance(
		client, client
			.getLocalPlayer
			.getLocalLocation)
}
