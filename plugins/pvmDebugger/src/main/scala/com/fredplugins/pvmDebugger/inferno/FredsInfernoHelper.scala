package com.fredplugins.pvmDebugger.inferno

import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.fredplugins.pvmDebugger.inferno.displaymodes.InfernoNamingDisplayMode
import com.fredplugins.pvmDebugger.inferno.displaymodes.InfernoPrayerDisplayMode
import com.fredplugins.pvmDebugger.inferno.displaymodes.InfernoWaveDisplayMode
import com.google.inject.Inject
import com.google.inject.Singleton
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.NPCs
import ethanApiPlugin.collections.query.NPCQuery
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.Actor
import net.runelite.api.ChatMessageType
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.NPC
import net.runelite.api.Perspective
import net.runelite.api.Player
import net.runelite.api.Point
import net.runelite.api.Prayer
import net.runelite.api.Projectile
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.AnimationChanged
import net.runelite.api.events.ChatMessage
import net.runelite.api.events.GameStateChanged
import net.runelite.api.events.GameTick
import net.runelite.api.events.GraphicsObjectCreated
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.PostHealthBarConfig
import net.runelite.api.events.ProjectileMoved
import net.runelite.api.events.VarbitChanged
import net.runelite.api.gameval.ItemID
import net.runelite.api.gameval.NpcID
import net.runelite.api.gameval.ObjectID1
import net.runelite.api.gameval.SpotanimID
import net.runelite.api.gameval.VarbitID
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.ConfigChanged
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.ProgressPieComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import packets.MovementPackets

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Rectangle
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import scala.annotation.unused
import scala.collection.mutable
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.jdk.OptionConverters.RichOptional
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class FredsInfernoHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsInfernoConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = FredsInfernoConfig.GROUP
	private def clientThread  = parent.getClientThread

	given Client = client
	given FredsInfernoConfig = config

	val infernoNpcs                                                 = mutable.ListBuffer.empty[InfernoNpc]
	val upcomingAttacks                                             = mutable.HashMap.empty[Int, mutable.HashMap[InfernoNpcAttack, Int]]
	val obstacles        : mutable.ListBuffer[WorldPoint]           = mutable.ListBuffer.empty[WorldPoint]
	// 0 = total safespot
	// 1 = pray melee
	// 2 = pray range
	// 3 = pray magic
	// 4 = pray melee, range
	// 5 = pray melee, magic
	// 6 = pray range, magic
	// 7 = pray all
	val safeSpotMap      : mutable.Map[WorldPoint, Int]             = mutable.HashMap.empty
	val safeSpotAreas    : mutable.Map[Int, List[WorldPoint]]       = mutable.HashMap.empty
	val blobDeathSpots   : mutable.ListBuffer[InfernoBlobDeathSpot] = mutable.ListBuffer.empty[InfernoBlobDeathSpot]

	var lastLocation     : WorldPoint                               = new WorldPoint(0, 0, 0)
	var currentWaveNumber: Int                                      = -1
	var closestAttack: InfernoNpcAttack = InfernoNpcAttack.UNKNOWN

	var finalPhase           : Boolean    = false
	var finalPhaseTick       : Boolean    = false
	var ticksSinceFinalPhase : Int        = 0
	var zukShield            : NPC        = null
	var zuk                  : NPC        = null
	var zukShieldLastPosition: WorldPoint = null
	var zukShieldBase        : WorldPoint = null
	var zukShieldCornerTicks : Int        = -2

	var zukShieldNegativeXCoord   : Int = -1
	var zukShieldPositiveXCoord   : Int = -1
	var zukShieldLastNonZeroDelta : Int = 0
	var zukShieldLastDelta        : Int = 0
	var zukShieldTicksLeftInCorner: Int = -1

	var centralNibbler: InfernoNpc = null

	var lastTick: Long = 0
	var spawnTimerInfoBox: InfernoSpawnTimerInfobox = null
	var jadInfoBox: InfernoJadInfobox = null

	def getPlayerRegionID: Int = WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation).getRegionID

	def isInInferno: Boolean = {
		getPlayerRegionID == InfernoData.INFERNO_REGION || true
	}

	def reset(): Unit = {
		Option(spawnTimerInfoBox).foreach(parent.getInfoBoxManager.removeInfoBox)
		spawnTimerInfoBox = null

		Option(jadInfoBox).foreach(parent.getInfoBoxManager.removeInfoBox)
		jadInfoBox = null
		safeSpotMap.clear()
		safeSpotAreas.clear()
		blobDeathSpots.clear()
		infernoNpcs.clear()
		upcomingAttacks.clear()
		obstacles.clear()

		lastLocation = new WorldPoint(0, 0, 0)
		currentWaveNumber = -1
		closestAttack = InfernoNpcAttack.UNKNOWN

		finalPhase = false
		finalPhaseTick = false
		ticksSinceFinalPhase = 0
		zukShield = null
		zuk = null
		zukShieldLastPosition = null
		zukShieldBase = null
		zukShieldCornerTicks = -2

		zukShieldNegativeXCoord    = -1
		zukShieldPositiveXCoord    = -1
		zukShieldLastNonZeroDelta  = 0
		zukShieldLastDelta         = 0
		zukShieldTicksLeftInCorner = -1

		centralNibbler = null

		lastTick = 0
	}

	override def init(): Unit = {
		reset()
		if(isInInferno && client.getGameState == GameState.LOGGED_IN) {
			spawnTimerInfoBox = new InfernoSpawnTimerInfobox(this)
			jadInfoBox = new InfernoJadInfobox(this)
			List(spawnTimerInfoBox, jadInfoBox).foreach(parent.getInfoBoxManager.addInfoBox)
		}
	}

	override def cleanup(): Unit = {
		reset()
	}
//	private def calculateUpcomingAttacks(): Unit = {
//		infernoNpcs.foreach(infernoNPC => {
//			infernoNPC.gameTick(client, lastLocation, finalPhase, ticksSinceFinalPhase)
//			if ((infernoNPC.tpe eq InfernoNpcType.ZUK) && (zukShieldCornerTicks eq -1)) {
//				infernoNPC.updateNextAttack(InfernoNpcAttack.UNKNOWN, 12) // TODO: Could be 10 or 11. Test!
//
//				zukShieldCornerTicks = 0
//			}
//			// Map all upcoming attacks and their priority + determine which NPC is about to attack next
//			if (infernoNPC.getTicksTillNextAttack > 0 && isPrayerHelper(infernoNPC) && ((infernoNPC.getNextAttack ne InfernoNpcAttack.UNKNOWN) || (config.indicateBlobDetectionTick && (infernoNPC.getType eq InfernoNPC.Type.BLOB) && infernoNPC.getTicksTillNextAttack >= 4))) {
//				upcomingAttacks.getOrElseUpdate(infernoNPC.getTicksTillNextAttack, mutable.HashMap.empty)
//				if (config.indicateBlobDetectionTick && (infernoNPC.tpe == InfernoNpcType.BLOB) && infernoNPC.getTicksTillNextAttack >= 4) {
//					upcomingAttacks.computeIfAbsent(infernoNPC.getTicksTillNextAttack - 3, (k) => new util.HashMap[K, V])
//					upcomingAttacks.computeIfAbsent(infernoNPC.getTicksTillNextAttack - 4, (k) => new util.HashMap[K, V])
//					// If there's already a magic attack on the detection tick, group them
//					if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 3).containsKey(InfernoNpcAttack.MAGIC)) if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 3).get(InfernoNPC.Attack.MAGIC) > InfernoNPC.Type.BLOB.getPriority) upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 3).put(InfernoNPC.Attack.MAGIC, InfernoNPC.Type.BLOB.getPriority)
//					else if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 3).containsKey(InfernoNpcAttack.RANGED)) if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 3).get(InfernoNPC.Attack.RANGED) > InfernoNPC.Type.BLOB.getPriority) upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 3).put(InfernoNPC.Attack.RANGED, InfernoNPC.Type.BLOB.getPriority)
//					else if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack).containsKey(InfernoNpcAttack.MAGIC) || upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 4).containsKey(InfernoNPC.Attack.MAGIC)) if (!upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 3).containsKey(InfernoNPC.Attack.RANGED) || upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 3).get(InfernoNPC.Attack.RANGED) > InfernoNPC.Type.BLOB.getPriority) upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 3).put(InfernoNPC.Attack.RANGED, InfernoNPC.Type.BLOB.getPriority)
//					else if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack).containsKey(InfernoNpcAttack.RANGED) || upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 4).containsKey(InfernoNPC.Attack.RANGED)) if (!upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 3).containsKey(InfernoNPC.Attack.MAGIC) || upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 3).get(InfernoNPC.Attack.MAGIC) > InfernoNPC.Type.BLOB.getPriority) upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 3).put(InfernoNPC.Attack.MAGIC, InfernoNPC.Type.BLOB.getPriority)
//					else upcomingAttacks.get(infernoNPC.getTicksTillNextAttack - 3).put(InfernoNpcAttack.MAGIC, InfernoNpcType.BLOB.getPriority)
//				}
//				else {
//					val attack   = infernoNPC.getNextAttack
//					val priority = infernoNPC.tpe.getPriority
//					if (!upcomingAttacks(infernoNPC.getTicksTillNextAttack).contains(attack) || upcomingAttacks.get(infernoNPC.getTicksTillNextAttack).get(attack) > priority) upcomingAttacks.get(infernoNPC.getTicksTillNextAttack.put(attack, priority)
//				}
//			}
//		})
//	}
//	private def calculateClosestAttack(): Unit = {
//		if ((config.prayerDisplayMode == InfernoPrayerDisplayMode.PRAYER_TAB) || (config.prayerDisplayMode == InfernoPrayerDisplayMode.BOTH)) {
//			var closestTick     = 999
//			var closestPriority = 999
//			upcomingAttacks.foreach((tick, attackPriority) => {
//				attackPriority.foreach((currentAttack, currentPriority) => {
//					if (tick < closestTick || ((tick eq closestTick) && currentPriority < closestPriority)) {
//						closestAttack = currentAttack
//						closestPriority = currentPriority
//						closestTick = tick
//					}
//				})
//			})
//		}
//	}
//	@Subscribe
//	private def onGameTick(event: GameTick): Unit = {
//		if (!isInInferno) return
//		lastTick = System.currentTimeMillis
//		upcomingAttacks.clear
//		calculateUpcomingAttacks
//		closestAttack = null
//		calculateClosestAttack()
//		safeSpotMap.clear
//		calculateSafespots
//		safeSpotAreas.clear
//		calculateSafespotAreas
//		obstacles.clear
//		calculateObstacles
//		centralNibbler = null
//		calculateCentralNibbler
//		calculateSpawnTimerInfobox
//		manageBlobDeathLocations
//		//if finalPhaseTick, we will skip incrementing because we already did it in onNpcSpawned
//		if (finalPhaseTick) finalPhaseTick = false
//		else if (finalPhase) ticksSinceFinalPhase += 1
//	}

	def getNextWaveNumber: Int = if (currentWaveNumber == -1 || currentWaveNumber == 69) -1 else currentWaveNumber + 1
	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		if(isInInferno) {
			val curWaveLine: Seq[LayoutableRenderableEntity] = Option.when(
				List(InfernoWaveDisplayMode.CURRENT, InfernoWaveDisplayMode.BOTH).contains(config.waveDisplay())
			) {
				val titleComponent = TitleComponent.builder()
					.text("Current Wave (Wave " + currentWaveNumber + ")")
					.color(config.getWaveOverlayHeaderColor)
					.build()

				val npcComponents: Seq[LineComponent] = waveMapping.getOrElse(currentWaveNumber, List.empty[Int])
					.pipe(
						x => x.distinct.map(y => y -> x.count(_ == y))
					)
					.map((monsterLevel, monsterCount) => {
						val name = npcNameMappings.getOrElse(monsterLevel, ("null", "null")).pipe(n => {
							if (config.npcNaming() == InfernoNamingDisplayMode.SIMPLE) n._1 else n._2
						}).pipe(x => {
							x + (if(config.npcLevels()) s" ($monsterLevel)" else "")
						})

						LineComponent.builder().left(name).leftColor(config.getWaveTextColor).right(s"${monsterCount}x").build()
					})
				npcComponents.prepended(titleComponent)
			}.getOrElse(Seq.empty[LayoutableRenderableEntity])
			val nextWaveLine: Seq[LayoutableRenderableEntity] = Option.when(
						List(InfernoWaveDisplayMode.NEXT, InfernoWaveDisplayMode.BOTH).contains(config.waveDisplay())
					) {
					val titleComponent = TitleComponent.builder()
						.text("Next Wave (Wave " + getNextWaveNumber + ")")
						.color(config.getWaveOverlayHeaderColor)
						.build()

					val npcComponents: Seq[LineComponent] = waveMapping.getOrElse(getNextWaveNumber, List.empty[Int])
						.pipe(
							x => x.distinct.map(y => y -> x.count(_ == y))
						)
						.map((monsterLevel, monsterCount) => {
							val name = npcNameMappings.getOrElse(monsterLevel, ("null", "null")).pipe(n => {
								if (config.npcNaming() == InfernoNamingDisplayMode.SIMPLE) n._1 else n._2
							}).pipe(x => {
								x + (if (config.npcLevels()) s" ($monsterLevel)" else "")
							})

							LineComponent.builder().left(name).leftColor(config.getWaveTextColor).right(s"${monsterCount}x").build()
						})
					npcComponents.prepended(titleComponent)
				}.getOrElse(Seq.empty[LayoutableRenderableEntity])
			curWaveLine ++ nextWaveLine
		} else Seq.empty[LayoutableRenderableEntity]
	}

	override def renderOverlay(g: Graphics2D): Dimension = {
		null.asInstanceOf[Dimension]
	}
}
