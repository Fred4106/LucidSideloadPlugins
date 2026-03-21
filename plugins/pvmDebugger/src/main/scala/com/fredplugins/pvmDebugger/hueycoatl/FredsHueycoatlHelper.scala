package com.fredplugins.pvmDebugger.hueycoatl

import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.utils.ReflectionUtils
import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.fredplugins.pvmDebugger.hueycoatl.HueycoatlData.*
import com.google.inject.Inject
import com.google.inject.Singleton
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.NPCs
import ethanApiPlugin.collections.query.NPCQuery
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.Actor
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
import net.runelite.api.events.GameTick
import net.runelite.api.events.GraphicsObjectCreated
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.PostHealthBarConfig
import net.runelite.api.events.ProjectileMoved
import net.runelite.api.events.VarbitChanged
import net.runelite.api.gameval.NpcID
import net.runelite.api.gameval.ObjectID1
import net.runelite.api.gameval.SpotanimID
import net.runelite.api.gameval.VarbitID
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.Subscribe
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

object HueycoatlData {
	val HueyRegion                          = 5939
	val HueyProjectileIds: Map[Int, Prayer] = List(
		SpotanimID.VFX_HUEY_ATTACK_RANGED_PROJANIM_01 -> Prayer.PROTECT_FROM_MISSILES,
		SpotanimID.VFX_HUEY_ATTACK_MAGIC_PROJANIM_01 -> Prayer.PROTECT_FROM_MAGIC,
		SpotanimID.VFX_HUEY_ATTACK_MELEE_PROJANIM_01 -> Prayer.PROTECT_FROM_MELEE
	).toMap
	val HueyObjectIds    : Seq[Int]         = List(
		ObjectID1.HUEY_P2_BARRIER_RESPAWN,
		ObjectID1.HUEY_P2_BARRIER,
		ObjectID1.HUEY_P2_BARRIER_RETRACTED,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_BRAZIER_OFF,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_BRAZIER_OFF,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_BRAZIER_OFF,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_BRAZIER_ON,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_BRAZIER_ON,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_BRAZIER_ON
	)
	val HueyDecorativeObjectIds: Seq[Int] = List(
			ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_EMPTY,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S1,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S2,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S3,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S4,
		ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S5,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_EMPTY,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S1,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S2,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S3,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S4,
		ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S5,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_EMPTY,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S1,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S2,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S3,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S4,
		ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S5
	)


	val HueyNpcIds: Seq[Int] = List(
		NpcID.HUEY_BODY_PART, NpcID.HUEY_BODY_PART_BROKEN,
		NpcID.HUEY_HEAD, NpcID.HUEY_HEAD_INVULNERABLE, NpcID.HUEY_HEAD_DEFEATED, NpcID.HUEY_HEAD_RESPAWN_PLACEHOLDER,
		NpcID.HUEY_TAIL_BROKEN, NpcID.HUEY_TAIL,
	)

	val HueyHeadIds: Seq[Int] = List(
		NpcID.HUEY_HEAD
		, NpcID.HUEY_HEAD_INVULNERABLE
		, NpcID.HUEY_HEAD_DEFEATED
		, NpcID.HUEY_HEAD_RESPAWN_PLACEHOLDER
	)
	
	val HueyShockwaveIds = List(
	SpotanimID.VFX_HUEY_TAIL_SLAM_SHOCKWAVE_SOUTH,
	SpotanimID.	VFX_HUEY_TAIL_SLAM_SHOCKWAVE_WEST,
	SpotanimID.	VFX_HUEY_TAIL_SLAM_SHOCKWAVE_NORTH,
	SpotanimID.	VFX_HUEY_TAIL_SLAM_SHOCKWAVE_EAST,
	SpotanimID.	VFX_HUEY_TAIL_SLAM_SHOCKWAVE_SOUTHEAST,
	SpotanimID.	VFX_HUEY_TAIL_SLAM_SHOCKWAVE_SOUTHWEST,
	SpotanimID.	VFX_HUEY_TAIL_SLAM_SHOCKWAVE_NORTHWEST,
			SpotanimID.VFX_HUEY_TAIL_SLAM_SHOCKWAVE_NORTHEAST
	)

	sealed trait PillarTrait(val wp: WorldPoint, val scale: Seq[Int], val prayer: Prayer) extends enumeratum.EnumEntry {
		def getLevel(using client: Client): Int = {
			wp.getTile.map(_.getDecorativeObject.getId).map(scale.indexOf(_)).getOrElse(-1)
		}

		def getColor: Color = {
			Option(prayer).collect {
				case Prayer.PROTECT_FROM_MAGIC => Color.BLUE
				case Prayer.PROTECT_FROM_MISSILES => Color.GREEN
				case Prayer.PROTECT_FROM_MELEE => Color.RED
			}.getOrElse(Color.WHITE)
		}
	}
	object Pillars extends enumeratum.Enum[PillarTrait] {
		case object Mage extends PillarTrait(
			new WorldPoint(1504, 3287, 0),
			Seq(
				ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_EMPTY,
				ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S1,
				ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S2,
				ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S3,
				ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S4,
				ObjectID1.HUEY_PRAYER_PILLAR_MAGIC_S5
			),
			Prayer.PROTECT_FROM_MAGIC
		) {}

		case object Range extends PillarTrait(
			new WorldPoint(1505, 3289, 0),
			Seq(
				ObjectID1.HUEY_PRAYER_PILLAR_RANGED_EMPTY,
				ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S1,
				ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S2,
				ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S3,
				ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S4,
				ObjectID1.HUEY_PRAYER_PILLAR_RANGED_S5
			),
			Prayer.PROTECT_FROM_MISSILES
		) {}

		case object Melee extends PillarTrait(
			new WorldPoint(1507, 3290, 0),
			Seq(
				ObjectID1.HUEY_PRAYER_PILLAR_MELEE_EMPTY,
				ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S1,
				ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S2,
				ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S3,
				ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S4,
				ObjectID1.HUEY_PRAYER_PILLAR_MELEE_S5
			),
			Prayer.PROTECT_FROM_MELEE
		) {}

		override def values: IndexedSeq[PillarTrait] = findValues
	}

//	sealed trait HueyBody(val wp: WorldPoint) extends enumeratum.EnumEntry {
//		def getNpc(using client: Client): Option[NPC] = {
//			NPCs.search().withId(NpcID.HUEY_BODY_PART, NpcID.HUEY_BODY_PART_BROKEN).atLocation(wp).first().toScala
//		}
//
//		def isDead(using client: Client): Boolean = {
//			getNpc.exists(_.getId == NpcID.HUEY_BODY_PART_BROKEN)
//		}
//	}

	val HueyBodyLocations = Seq(
				WorldPoint(1520,3273, 0),
				WorldPoint(1524,3277, 0),
				WorldPoint(1527,3273, 0),
				WorldPoint(1530,3276, 0),
				WorldPoint(1524,3270, 0)
		)
}
trait DangerousTile{
	def location: WorldPoint
//	def dangerousOnTick: Int
	def spawnCycle: Int
	def finishedCycle: Int
}
case class WaveTile(location: WorldPoint, spawnCycle: Int, finishedCycle: Int, spawnTick: Int) extends DangerousTile
case class LightingTile(location: WorldPoint, spawnCycle: Int, finishedCycle: Int, spawnTick: Int) extends DangerousTile

@Singleton
class FredsHueycoatlHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsHueycoatlConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = FredsHueycoatlConfig.GROUP
	private def clientThread  = parent.getClientThread
	given Client = client
	given FredsHueycoatlConfig = config

	case class HueycoatlState(projectile: Option[Projectile], pillars: Map[PillarTrait, Int], dangerousTiles: Seq[DangerousTile], npcs: Seq[NPC]) {
		lazy val hueyHead  : Option[NPC] = npcs.find(_.getId.pipe(HueyHeadIds.contains(_)))
		lazy val hueyBodies: Seq[NPC]    = npcs.filter(_.getId == NpcID.HUEY_BODY_PART)
		lazy val hueyTail  : Option[NPC] = npcs.find(_.getId.pipe(Seq(NpcID.HUEY_TAIL, NpcID.HUEY_TAIL_BROKEN).contains(_)))

		lazy val stage: Int = hueyHead.map(_.getId).collect {
			case NpcID.HUEY_HEAD_RESPAWN_PLACEHOLDER | NpcID.HUEY_HEAD_DEFEATED => 0
			case NpcID.HUEY_HEAD_INVULNERABLE => 3
			case NpcID.HUEY_HEAD if hueyBodies.nonEmpty => 1
			case NpcID.HUEY_HEAD if hueyTail.isEmpty => 2
			case NpcID.HUEY_HEAD if hueyTail.isDefined => 4
		}.getOrElse(0)

		lazy val reachableTiles: Seq[WorldPoint] = {
			SInteractionUtils.reachableTiles.map(_.getTemplate).filterNot(wp => dangerousTiles.exists(dt => dt.location == wp && dt.isInstanceOf[LightingTile]))
		}

		lazy val priorityTiles : Seq[WorldPoint] = {
			Option(stage).collect {
				case 1 => hueyBodies
				case 2 | 4 => hueyHead.toList
				case 3 => hueyTail.toList
			}.map(_.map(_.getWorldArea).flatMap(wa => {
				wa.offset(1).pipe(x => x.tiles.diff(x.corners))
					.diff(wa.tiles)
			})).getOrElse(Seq.empty[WorldPoint])
				.map(_.getTemplate)
				.filter(reachableTiles.contains(_))
				.filter(_.distanceTo(client.getLocalPlayer.getWorldLocation.getTemplate) < 5)
		}

		lazy val protectionPrayer: Option[Prayer] = {
			val pprayer = projectile.filter(_ => stage > 0).filter(_.ticksRemaining <= 3).flatMap(_.getId.pipe(HueyProjectileIds.get))
			val cprayer = Option(config.campPrayer().getPrayer).filter(_ => stage > 1)
			pprayer.orElse(cprayer)
		}

		def withDangerousTile(dt: DangerousTile): HueycoatlState = {
			this.copy(dangerousTiles = dangerousTiles.appended(dt))
		}
		def addNpc(npc: NPC): HueycoatlState = {
			Option(npc).filter(_.getId.pipe(HueyNpcIds.contains))
				.fold(this)(n => this.copy(npcs = npcs.appended(n)))
		}
		def removeNpc(npc: NPC): HueycoatlState = {
			Option(npc).filter(npcs.contains(_))
				.fold(this)(n => this.copy(npcs = npcs.filterNot(_ == n)))
		}
		def withProjectile(nProjectile: Projectile): HueycoatlState = {
			Option.when(nProjectile != null && HueyProjectileIds.contains(nProjectile.getId) && !projectile.contains(nProjectile)) {
				this.copy(projectile = Option(nProjectile))
			}.getOrElse(this)
		}

		def inFight: Boolean = clientThread.runOnClientThread(() => client.getVarbitValue(VarbitID.HUEY_IN_AREA)) == 1

		def tickState(using client: Client): HueycoatlState = {
			val cycle = client.getGameCycle
			copy(
				pillars = Pillars.values.map(p => p -> p.getLevel).toMap,
				dangerousTiles = dangerousTiles.filter(_.finishedCycle > cycle),
				projectile = projectile.filterNot(p => p.hasHit && p.ticksRemaining <= 0),
			)
		}
	}

//	private def curRegion: Int = Option(client.getLocalPlayer).map(_.templateLocation).map(_.getRegionID).getOrElse(-1)
//	private def inRegion: Boolean = HueyRegion == curRegion

	def createState: Option[HueycoatlState] = {
		Option.when(client.isClientThread && client.getGameState == GameState.LOGGED_IN && client.getLocalPlayer.getWorldLocation.getTemplate.getRegionID == HueyRegion) {
			HueycoatlState(
				client.getProjectiles.asScala.toList.filter(p => HueyProjectileIds.contains(p.getId) && Option(p.getTargetActor).contains(client.getLocalPlayer)).sortBy(_.getEndCycle).headOption,
				Pillars.values.map { p =>
					p -> p.getLevel
				}.toMap,
/*				client.getTopLevelWorldView.getGraphicsObjects.asScala.toList.flatMap(go => {
					val swo = Option.when(HueyShockwaveIds.contains(go.getId)) {
						WaveTile(go.templateLocation, client.getGameCycle, go.getStartCycle, client.getTickCount)
					}
					val lso = Option.when(go.getId == SpotanimID.VFX_HUEYCOATL_PRAYER_02){
						LightingTile(go.templateLocation, client.getGameCycle, go.getStartCycle, client.getTickCount)
					}
					swo.orElse(lso)
				})*/
				Seq.empty[DangerousTile],
				NPCs.search().withId(HueyNpcIds *).result().asScala.toList
			)
		}
	}

	private var state: Option[HueycoatlState] = Option.empty

	override def init(): Unit = {
		state = clientThread.runOnClientThread(() => createState)
	}

	override def cleanup(): Unit = {
		state = Option.empty
	}

	@Subscribe
	def onGraphicsObjectCreated(e: GraphicsObjectCreated): Unit = {
		val graphicsObject = e.getGraphicsObject
		val name           = ReflectionUtils.getSpotAnimationName(graphicsObject.getId)
		if (!name.startsWith("VFX_HUEY")) return
		if(graphicsObject.templateLocation.getRegionID == HueyRegion) {
			//ticksSinceDangerousTiles = 5
			Option(
				if(graphicsObject.getId == SpotanimID.VFX_HUEYCOATL_PRAYER_02) {
					LightingTile(graphicsObject.templateLocation, client.getGameCycle, graphicsObject.getStartCycle, client.getTickCount)
				} else if (HueyShockwaveIds.contains(graphicsObject.getId)) {
					val goDuration = (graphicsObject.getStartCycle- client.getGameCycle)
					val fakeDuration = Math.min(120, goDuration)
					val fakeSpawnCycle = graphicsObject.getStartCycle - fakeDuration
					val fakeSpawnTick = ((goDuration - fakeDuration) / 30.0).floor.toInt + client.getTickCount
					WaveTile(graphicsObject.templateLocation, fakeSpawnCycle, graphicsObject.getStartCycle, fakeSpawnTick)
				} else null
			).foreach(dt=> {
				state = state.map(_.withDangerousTile(dt))
			})
		}
		log.info(s"GraphicsObject \"${name}\" created on tick ${client.getTickCount} at ${graphicsObject.templateLocation} with start cycle ${graphicsObject.getStartCycle} on cycle ${client.getGameCycle} animation ${Option(graphicsObject.getAnimation).map(a => a.getId -> a.getDuration).getOrElse(-1 -> 0)}")
	}

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		state = state.map(_.tickState)
		state.foreach(s => {
			if(!s.inFight) CombatUtils.deactivatePrayers()
			else {
				if(s.stage == 0) CombatUtils.deactivatePrayer(Prayer.PIETY)
				else Option.when(config.autoPrayPiety())(Prayer.PIETY).foreach(CombatUtils.activatePrayer(_))

				s.protectionPrayer.fold {
					() => CombatUtils.deactivatePrayers(true)
				}(a => () => CombatUtils.activatePrayer(a)).apply()

				val destinationTile = Option(client.getLocalDestinationLocation).map(_.getTemplate).getOrElse(client.getLocalPlayer.templateLocation)
				if (s.dangerousTiles.exists(dt => dt.location == destinationTile && dt.isInstanceOf[LightingTile])) {
					val playerPos = client.getLocalPlayer.templateLocation
					Option(s.priorityTiles).filter(_.nonEmpty).getOrElse(s.reachableTiles)
						.flatMap(pt => {
							val pathResult: EthanApiPlugin.PathResult = EthanApiPlugin.canPathToTile(playerPos, pt)
							Option.when(pathResult.isReachable)(pt -> pathResult.getDistance)
						}).sortBy(_._2).map(_._1)
						.headOption.foreach(safeTile => {
							MovementPackets.queueMovement(safeTile)
							log.debug(s"moving to safe tile ${safeTile} from ${playerPos}")
						})
				}
			}
		})
	}

	@Subscribe
	def onLocalRegionChanged(e: LocalRegionChanged): Unit = {
		log.info(s"Region changed from ${e.getOldRegion} to ${e.getCurRegion}")
		if(e.getCurRegion == HueyRegion) {
			state = createState
		}
		if(e.getOldRegion == HueyRegion) {
			state = Option.empty
		}
	}

	@Subscribe
	def onVarbitChanged(e: VarbitChanged): Unit = {
		if(e.getVarbitId == -1) return
		val name = ReflectionUtils.getVarbitName(e.getVarbitId)//varbitIdToName.getOrElse(e.getVarbitId, s"Unknown(${e.getVarbitId})")
		if(!name.contains("HUEY") && !name.startsWith("Unknown")) return
		log.info(s"Varbit \"${name}\" changed to ${e.getValue}")
		if(e.getVarbitId == VarbitID.HUEY_IN_AREA) {
//			if(e.getValue == 0) {}
		}
	}
	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		if (e.getNpc.templateLocation.getRegionID != HueyRegion) return
		if (!HueyNpcIds.contains(e.getNpc.getId)) return
		val name = e.getNpc.getId.pipe(n => ReflectionUtils.getNpcName(n))//npcIdToName.getOrElse(n, s"Unknown(${n})"))
		log.debug(s"Npc[${e.getNpc.getIndex}] \"${name}\" spawned at ${e.getNpc.templateLocation}")
		state = state.map(_.addNpc(e.getNpc))
//		trackedNpcs = trackedNpcs.filterNot(_ == e.getNpc).appended(e.getNpc)
	}
	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		if (e.getNpc.templateLocation.getRegionID != HueyRegion) return
		if (!HueyNpcIds.contains(e.getNpc.getId)) return
		val name = e.getNpc.getId.pipe(n => ReflectionUtils.getNpcName(n))//npcIdToName.getOrElse(n, s"Unknown(${n})"))
		log.debug(s"Npc[${e.getNpc.getIndex}] \"${name}\" despawned at ${e.getNpc.templateLocation}")
		state = state.map(_.removeNpc(e.getNpc))
//		trackedNpcs = trackedNpcs.filterNot(_ == e.getNpc)
	}

	@Subscribe
	def onProjectileMoved(e: ProjectileMoved): Unit = {
		val projectile: Projectile = e.getProjectile
		if (projectile.getTargetActor != null && projectile.getTargetActor != client.getLocalPlayer) return
		if (!HueyProjectileIds.keySet.contains(projectile.getId)) return
		val name = projectile.getId.pipe(p => ReflectionUtils.getSpotAnimationName(p))//spotAnimationIdToName.getOrElse(p, s"Unknown($p)"))
		if (projectile.templateSourceLocation.getRegionID == HueyRegion || projectile.templateTargetLocation.getRegionID == HueyRegion) {
			if(projectile.justSpawned) {
				state = state.map(_.withProjectile(projectile))
			}
		}
	}

//	@Subscribe
//	def onAnimationChanged(e: AnimationChanged): Unit = {
////		if (inRegion && inFight) {
//		if(e.getActor.templateLocation.getRegionID != HueyRegion) return
//		Option(e.getActor).collect {
//			case npc: NPC if HueyNpcIds.contains(npc.getId) => npc.getId.pipe(n => npcIdToName.getOrElse(n, s"$n").pipe(s => s"Npc(${s})")) -> npc.getAnimation
//			case player: Player if player == client.getLocalPlayer => "LocalPlayer" -> player.getAnimation
////			case player: Player => s"Player(${player.getName})" -> player.getAnimation
//		}.map{
//			case (actorName, actorAnimationId) => actorName -> actorAnimationId.pipe(a => animationIdToName.getOrElse(a, s"Unknown(${a})"))
//		}.foreach{
//			case (actorName, animationName) => log.debug(s"AnimationChanged[${client.getTickCount}]: \"$actorName\" animation changed to \"$animationName\"")
//		}
//	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		state.map(s => {
			val projectileLine = LineComponent.builder().left("Projectile").right(s"${s.projectile.fold("Null")(_.getId.pipe(p => ReflectionUtils.getSpotAnimationName(p)))}").rightColor(s.projectile.fold(Color.RED)(_ => Color.GREEN)).build
			val stageLine = LineComponent.builder().left("Stage").right(s"${s.stage}").build
			val pillarsLines = s.pillars.map {
				case (p, l) => LineComponent.builder().left(p.entryName).leftColor(p.getColor).right(s"$l").rightColor(
					Color.RED.interpolate(Color.GREEN, l/5.0)
				).build
			}.toList.prepended(TitleComponent.builder().text("Pillars").build())
			Seq(projectileLine, stageLine, pillarsLines, Seq.empty[LayoutableRenderableEntity]).flatMap{
					case e: LayoutableRenderableEntity => Seq(e)
					case le: Seq[_] => le.collect{
						case e: LayoutableRenderableEntity => e
					}
				}
		}).getOrElse(Seq.empty[LayoutableRenderableEntity])
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		def renderNpcOverlay(npc: NPC, text: String, zoffset: Int, color: Color, fillAlpha: Int, outlineWidth: Int = 4, textColor: Color = Color.WHITE ): Unit = {
			var poly = npc.getConvexHull
			if(poly!= null) OverlayUtil.renderPolygon(g, poly, Color(0, 0, 0, 0), color.withAlpha(fillAlpha))
			if(outlineWidth > 0) parent.getModelOutlineRenderer.drawOutline(npc, outlineWidth, color, 2)
//			val poly = npc.getCanvasTilePoly
//			if (poly != null) OverlayUtil.renderPolygon(g, poly, fillColor)
			if(text != null && text.nonEmpty) {
				val textLocation = npc.getCanvasTextLocation(g, text, npc.getLogicalHeight + zoffset)
				if (textLocation != null) {
					val textBounds = g.getFontMetrics.getStringBounds(text, g)
					val offset = 4
					val textArea = Rectangle(textBounds.getX.toInt - offset, textBounds.getY.toInt - offset, textBounds.getWidth.toInt + offset + offset, textBounds.getHeight.toInt + offset + offset)
	//				val textArea = new Rectangle(textLocation.getX + (textBounds.getWidth / 2.0).toInt - , textLocation.getY, textBounds.getWidth.toInt, textBounds.getHeight.toInt)
					OverlayUtil.renderPolygon(g, textArea, new Color(255 - textColor.getRed, 255 - textColor.getGreen, 255 - textColor.getBlue, fillAlpha))
					OverlayUtil.renderTextLocation(g, textLocation, text, textColor)
				}
			}
		}

		case class ProgressData(spawnTick: Int, spawnCycle: Int, maxAge: Int, pieChartColor: Color, textColor: Color){}
		def renderTile(tile: WorldPoint, color: Color, progress: ProgressData | Null = null): Unit = {
			val poly = Perspective.getCanvasTilePoly(client, tile.getLocalPoint)
			if (poly != null) OverlayUtil.renderPolygon(g, poly, color)

			Option(progress).foreach{
				case ProgressData(spawnTick, spawnCycle, maxAge, ppcColor, textColor) => {
					val ppc = new ProgressPieComponent()
					ppc.setBorderColor(Color.BLACK)
					ppc.setFill(ppcColor)
					ppc.setProgress((client.getGameCycle - spawnCycle).doubleValue / (30.0d * maxAge))
					ppc.setDiameter(28)
					val point = Perspective.localToCanvas(client, tile.getLocalPoint, client.getTopLevelWorldView.getPlane, 20)
					ppc.setPosition(point)
					ppc.render(g)

					val text = s"${maxAge - (client.getTickCount - spawnTick)}"

					val fm      = g.getFontMetrics()
					val bounds  = fm.getStringBounds(text, g)
					val xOffset = point.getX() - (bounds.getWidth() / 2).toInt
					val yOffset = point.getY() + (bounds.getHeight() / 2).toInt

					val textPoint = new Point(xOffset, yOffset)
					OverlayUtil.renderTextLocation(g, textPoint, text, textColor)
				}
			}
		}
		state.foreach(s => {
			s.npcs.foreach { n =>
				//				val id = n.getId
				//				val name = npcIdToName(id)
				val color = n.getId match {
					case NpcID.HUEY_HEAD_RESPAWN_PLACEHOLDER | NpcID.HUEY_HEAD_DEFEATED => Color.GRAY
					case NpcID.HUEY_HEAD => Color.GREEN
					case NpcID.HUEY_HEAD_INVULNERABLE => Color.BLUE

					case NpcID.HUEY_TAIL_BROKEN => Color.BLUE
					case NpcID.HUEY_TAIL => Color.GREEN

					case NpcID.HUEY_BODY_PART => Color.GREEN
					case NpcID.HUEY_BODY_PART_BROKEN => Color.GRAY
					case _ => Color.PINK
				}

				renderNpcOverlay(
					n,
					ReflectionUtils.getNpcName(n.getId),
					0,
					color.withAlpha(150),
					100,
					4
				)
			}
			s.priorityTiles.foreach(
				renderTile(_, Color.CYAN.withAlpha(150))
			)
			s.dangerousTiles.filter(_.spawnCycle <= client.getGameCycle).foreach {
				case LightingTile(location, spawnCycle, finishedCycle, spawnTick)=> {
					renderTile(location, Color.ORANGE.withAlpha(150), ProgressData(spawnTick, spawnCycle, ((finishedCycle-spawnCycle) / 30.0).floor.toInt, Color.RED.withAlpha(150), Color.WHITE))
				}
				case WaveTile(location, spawnCycle, finishedCycle, spawnTick) => {
//					val nSpawnCycle = (finishedCycle-120).max(spawnCycle)
//					val spawnTickOffset = ((nSpawnCycle - spawnCycle)/30.0).floor.toInt
//					val maxAge = ((finishedCycle - nSpawnCycle) / 30.0).floor.toInt
					renderTile(location, Color.CYAN.withAlpha(150), ProgressData(spawnTick, spawnCycle, ((finishedCycle-spawnCycle) / 30.0).floor.toInt, Color.BLUE.withAlpha(150), Color.WHITE))
				}
				case _ =>
			}
		})
		null.asInstanceOf[Dimension]
	}
}
