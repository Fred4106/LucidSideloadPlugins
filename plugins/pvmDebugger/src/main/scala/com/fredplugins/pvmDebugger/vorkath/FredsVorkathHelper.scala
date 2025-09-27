package com.fredplugins.pvmDebugger.vorkath

import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.fredplugins.pvmDebugger.vorkath.VorkPhases.Acid
import com.fredplugins.pvmDebugger.vorkath.VorkPhases.FireBall
import com.fredplugins.pvmDebugger.vorkath.VorkPhases.Spawn
import com.fredplugins.pvmDebugger.vorkath.Vorkath.ATTACKS_PER_SWITCH
import com.fredplugins.pvmDebugger.vorkath.Vorkath.FIRE_BALL_ATTACKS
import com.google.inject.Inject
import com.google.inject.Singleton
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.NPCs
import ethanApiPlugin.collections.query.NPCQuery
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.services.localPlayer.LocalPlayerService
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
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps
object Vorkath {
	val ATTACKS_PER_SWITCH = 6
	val FIRE_BALL_ATTACKS  = 25
}
class Vorkath(val npc: NPC) extends ShimUtils.Logging{
	assert(npc != null && npc.getId == NpcID.VORKATH)
	var lastAttack: VorkAttack = null
	var currentPhase: VorkPhase = VorkPhases.Unknown
	var nextPhase: VorkPhase = VorkPhases.Unknown
	var lastPhase: VorkPhase = VorkPhases.Unknown
	var attacksLeft: Int = ATTACKS_PER_SWITCH

	def updatePhase(newPhase: VorkPhase): Unit = {
		val oldLastPhase    = this.lastPhase
		val oldCurrentPhase = this.currentPhase
		val oldNextPhase    = this.nextPhase
		val oldAttacksLeft  = this.attacksLeft
		this.lastPhase = this.currentPhase
		this.currentPhase = newPhase
		this.nextPhase = newPhase match {
			case VorkPhases.Acid => VorkPhases.FireBall
			case VorkPhases.FireBall => VorkPhases.Spawn
			case VorkPhases.Spawn => VorkPhases.Acid
			case _ => VorkPhases.Unknown
		}
		if (this.currentPhase == FireBall) this.attacksLeft = FIRE_BALL_ATTACKS
		else if (this.currentPhase == Acid || this.currentPhase == Spawn) this.attacksLeft = 0
		else this.attacksLeft = ATTACKS_PER_SWITCH
		log.debug("[Vorkath] Update! Last Phase: {}->{}, Current Phase: {}->{}, Next Phase: {}->{}, Attacks: {}->{}", oldLastPhase, this.lastPhase, oldCurrentPhase, this.currentPhase, oldNextPhase, this.nextPhase, oldAttacksLeft, this.attacksLeft)
	}

}
@Singleton
class FredsVorkathHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsVorkathConfig, val localPlayerService: LocalPlayerService) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = FredsVorkathConfig.GROUP
	private def clientThread  = parent.getClientThread
	given Client = client
	given FredsVorkathConfig = config

	var vorkath: Option[Vorkath] = Option.empty[Vorkath]


	override def init(): Unit = {
		vorkath = Option.empty[Vorkath]
//		state = clientThread.runOnClientThread(() => createState)
	}

	override def cleanup(): Unit = {
		vorkath  = Option.empty[Vorkath]
//		state = Option.empty
	}

//	@Subscribe
//	def onGraphicsObjectCreated(e: GraphicsObjectCreated): Unit = {
////		val graphicsObject = e.getGraphicsObject
////		val name           = spotAnimationIdToName.getOrElse(graphicsObject.getId, s"Unknown(${graphicsObject.getId})")
////		if (!name.startsWith("VFX_HUEY")) return
////		if(graphicsObject.templateLocation.getRegionID == HueyRegion) {
////			//ticksSinceDangerousTiles = 5
////			Option(
////				if(graphicsObject.getId == SpotanimID.VFX_HUEYCOATL_PRAYER_02) {
////					LightingTile(graphicsObject.templateLocation, client.getGameCycle, graphicsObject.getStartCycle, client.getTickCount)
////				} else if (HueyShockwaveIds.contains(graphicsObject.getId)) {
////					val goDuration = (graphicsObject.getStartCycle- client.getGameCycle)
////					val fakeDuration = Math.min(120, goDuration)
////					val fakeSpawnCycle = graphicsObject.getStartCycle - fakeDuration
////					val fakeSpawnTick = ((goDuration - fakeDuration) / 30.0).floor.toInt + client.getTickCount
////					WaveTile(graphicsObject.templateLocation, fakeSpawnCycle, graphicsObject.getStartCycle, fakeSpawnTick)
////				} else null
////			).foreach(dt=> {
////				state = state.map(_.withDangerousTile(dt))
////			})
////		}
////		log.info(s"GraphicsObject \"${name}\" created on tick ${client.getTickCount} at ${graphicsObject.templateLocation} with start cycle ${graphicsObject.getStartCycle} on cycle ${client.getGameCycle} animation ${Option(graphicsObject.getAnimation).map(a => a.getId -> a.getDuration).getOrElse(-1 -> 0)}")
//	}

	@Subscribe
	def onGameTick(gameTick: GameTick): Unit = {
		if(localPlayerService.getCurrentRegionID != VorkathRegion || client.getGameState != GameState.LOGGED_IN) {
			return
		}

		if(vorkath.isEmpty) {
			vorkath = client.getNpcs.asScala.toList.find(npc => {
				npc.templateLocation.getRegionID == VorkathRegion && npc.getId == NpcID.VORKATH
			}).map(Vorkath(_))
		}

		if(vorkath.nonEmpty) {
			log.debug(s"Tick: ${client.getTickCount}")
		}
	}

	@Subscribe
	def onLocalRegionChanged(e: LocalRegionChanged): Unit = {
		if(e.getOldRegion == VorkathRegion || e.getCurRegion == VorkathRegion) {
			log.info(s"Region changed from ${e.getOldRegion} to ${e.getCurRegion}")
		}
	}

	private lazy val varbitIdToName: Map[Int, String] = classOf[net.runelite.api.gameval.VarbitID].getDeclaredFields.toList
		.filter(_.getType == Integer.TYPE)
		.filter(_.getModifiers == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL))
		.map(f => {
			f.getInt(null) -> f.getName
		}).toMap

//	@Subscribe
//	def onVarbitChanged(e: VarbitChanged): Unit = {
//		if(e.getVarbitId == -1) return
//		val name = varbitIdToName.getOrElse(e.getVarbitId, s"Unknown(${e.getVarbitId})")
//		log.info(s"Varbit \"${name}\" changed to ${e.getValue}")
//	}

//	@Subscribe
//	def onNpcSpawned(e: NpcSpawned): Unit = {
////		if (e.getNpc.templateLocation.getRegionID != VorkathRegion) return
//		Option(e.getNpc).map(n => n.getId -> n).filter(u => u._2.templateLocation.getRegionID == VorkathRegion)
//			.collect {
//				case (NpcID.VORKATH, loc) => "Vorkath" -> loc.templateLocation
//				case (NpcID.VORKATH_SPAWN, loc) => "Spawn" -> loc.templateLocation
//			}
//			.foreach {
//				case (name, loc) => log.debug(s"$name spawned @ $loc")
//			}
//	}
//	@Subscribe
//	def onNpcDespawned(e: NpcDespawned): Unit = {
//		Option(e.getNpc).map(n => n.getId -> n.templateLocation).filter(_._2.getRegionID == VorkathRegion)
//			.collect {
//				case (NpcID.VORKATH, loc) => "Vorkath" -> loc
//				case (NpcID.VORKATH_SPAWN, loc) => "Spawn" -> loc
//			}
//			.foreach {
//				case (name, loc) => log.debug(s"$name despawned @ $loc")
//			}
//		//		trackedNpcs = trackedNpcs.filterNot(_ == e.getNpc)
//	}

	@Subscribe
	def onProjectileMoved(e: ProjectileMoved): Unit = {
		val projectile: Projectile = e.getProjectile
		if (vorkath.isDefined && (e.getProjectile.templateSourceLocation.getRegionID == VorkathRegion || e.getProjectile.templateTargetLocation.getRegionID == VorkathRegion) && projectile.justSpawned) {
			VorkAttacks.getAttackByProjectileId(projectile.getId)
				.foreach(attack => {
					val v = vorkath.get
					attack match {
						case VorkAttacks.Acid => v.updatePhase(Acid)
						case VorkAttacks.FireBall => v.updatePhase(FireBall)
						case VorkAttacks.FreezeBreath => v.updatePhase(Spawn)
						case VorkAttacks.ZombifiedSpawn => v.updatePhase(Spawn)
						case a if v.attacksLeft > 0 => v.attacksLeft = v.attacksLeft - 1
						case a => {
							v.updatePhase(v.nextPhase)
							v.attacksLeft = v.attacksLeft - 1
						}
					}
					log.debug(s"ProjectileAttack ${attack} detected by ${projectile}")
				})
		}
	}

	val vorkathIds: Seq[Int] = Seq(NpcID.VORKATH, NpcID.VORKATH_SLEEPING, NpcID.VORKATH_SPAWN)
	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
		if (!Option(e.getActor).map(_.templateLocation).map(_.getRegionID).contains(VorkathRegion)) return
//		if (inRegion && inFight) {
//			if(e.getActor.templateLocation.getRegionID != HueyRegion) return
		Option(e.getActor).foreach {
			case vork: NPC if vork.getId == NpcID.VORKATH => {
				val possibleAttacks = VorkAttacks.getPossibleAttacks(vork.getAnimation)
				if(possibleAttacks.nonEmpty) {
					log.debug(s"Vorkath ${vork} has these possible attacks ${possibleAttacks}")
				} else {
					log.debug(s"Vorkath ${vork} animation changed to ${vork.getAnimation}")
				}
			}
			case vork: NPC if vork.getId == NpcID.VORKATH_SLEEPING => {
				log.debug(s"Sleeping Vorkath ${vork} animation changed to ${vork.getAnimation}")
			}
			case spawn: NPC if spawn.getId == NpcID.VORKATH_SPAWN =>{
				log.debug(s"Spawn ${spawn} animation changed to ${spawn.getAnimation}")
			}
			case _ =>
		}
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
//		state.map(s => {
		vorkath.map{v =>
			val vorkathLine = LineComponent.builder().left("Vorkath").right(s"${v.npc}").build
			val phase = LineComponent.builder().left("Phase").right(s"${v.currentPhase}").rightColor(
				v.currentPhase match {
					case VorkPhases.Unknown => Color.YELLOW
					case VorkPhases.Acid => Color.GREEN
					case VorkPhases.FireBall => Color.RED
					case VorkPhases.Spawn => Color.BLUE
				}
			).build
			val atkleft = LineComponent.builder().left("AttacksLeft").right(s"${v.attacksLeft}").rightColor(
				if(v.attacksLeft > 0) {
					Color.GREEN
				} else {
					Color.RED
				}
			).build
//			val stageLine = LineComponent.builder().left("Stage").right(s"${s.stage}").build
//			val pillarsLines = s.pillars.map {
//				case (p, l) => LineComponent.builder().left(p.entryName).leftColor(p.getColor).right(s"$l").rightColor(
//					Color.RED.interpolate(Color.GREEN, l/5.0)
//				).build
//			}.toList.prepended(TitleComponent.builder().text("Pillars").build())
			Seq(vorkathLine, phase, atkleft, Seq.empty[LayoutableRenderableEntity]).flatMap{
				case e: LayoutableRenderableEntity => Seq(e)
				case le: Seq[_] => le.collect{
					case e: LayoutableRenderableEntity => e
				}
			}
		}.getOrElse(Seq.empty[LayoutableRenderableEntity])
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		vorkath.foreach(v => {
			OverlayUtil.renderActorOverlay(g, v.npc, s"${v.currentPhase} + ${v.attacksLeft}",
				v.currentPhase match {
					case VorkPhases.Unknown => Color.YELLOW
					case VorkPhases.Acid => Color.GREEN
					case VorkPhases.FireBall => Color.RED
					case VorkPhases.Spawn => Color.BLUE
				})
		})
//		state.foreach(s => {
//			s.npcs.foreach { n =>
//				//				val id = n.getId
//				//				val name = npcIdToName(id)
//				val color = n.getId match {
//					case NpcID.HUEY_HEAD_RESPAWN_PLACEHOLDER | NpcID.HUEY_HEAD_DEFEATED => Color.GRAY
//					case NpcID.HUEY_HEAD => Color.GREEN
//					case NpcID.HUEY_HEAD_INVULNERABLE => Color.BLUE
//
//					case NpcID.HUEY_TAIL_BROKEN => Color.BLUE
//					case NpcID.HUEY_TAIL => Color.GREEN
//
//					case NpcID.HUEY_BODY_PART => Color.GREEN
//					case NpcID.HUEY_BODY_PART_BROKEN => Color.GRAY
//					case _ => Color.PINK
//				}
//
//				renderNpcOverlay(
//					n,
//					npcIdToName(n.getId),
//					0,
//					color.withAlpha(150),
//					100,
//					4
//				)
//			}
//			s.priorityTiles.foreach(
//				renderTile(_, Color.CYAN.withAlpha(150))
//			)
//			s.dangerousTiles.filter(_.spawnCycle <= client.getGameCycle).foreach {
//				case LightingTile(location, spawnCycle, finishedCycle, spawnTick)=> {
//					renderTile(location, Color.ORANGE.withAlpha(150), ProgressData(spawnTick, spawnCycle, ((finishedCycle-spawnCycle) / 30.0).floor.toInt, Color.RED.withAlpha(150), Color.WHITE))
//				}
//				case WaveTile(location, spawnCycle, finishedCycle, spawnTick) => {
//					//					val nSpawnCycle = (finishedCycle-120).max(spawnCycle)
//					//					val spawnTickOffset = ((nSpawnCycle - spawnCycle)/30.0).floor.toInt
//					//					val maxAge = ((finishedCycle - nSpawnCycle) / 30.0).floor.toInt
//					renderTile(location, Color.CYAN.withAlpha(150), ProgressData(spawnTick, spawnCycle, ((finishedCycle-spawnCycle) / 30.0).floor.toInt, Color.BLUE.withAlpha(150), Color.WHITE))
//				}
//				case _ =>
//			}
//		})
		null.asInstanceOf[Dimension]
	}
}
