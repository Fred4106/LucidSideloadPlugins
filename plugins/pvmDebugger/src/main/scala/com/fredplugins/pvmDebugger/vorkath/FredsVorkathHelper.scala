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
import com.fredplugins.pvmDebugger.vorkath.VorkAttacks.FreezeBreath
import com.fredplugins.pvmDebugger.vorkath.VorkAttacks.Slash
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
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
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
//	var fireballCount: Int = 0
	var attacksCount: Int = 0
	var currentPhase: VorkPhase = VorkPhases.Unknown
	var attacksLeft: Int = ATTACKS_PER_SWITCH

	def handleAttack(attack: VorkAttack): Unit = {
		lastAttack = attack
		attack match {
			case VorkAttacks.BasicAttack(a) => {
				if(currentPhase == FireBall) {
					currentPhase = Spawn
					attacksLeft = ATTACKS_PER_SWITCH
					attacksCount = 0
				}
				attacksLeft = (attacksLeft - 1).max(0)
				attacksCount = attacksCount + 1
			}
			case VorkAttacks.SpecialAttack(a) => {
				a match {
					case VorkAttacks.Acid => {
						if(currentPhase == Acid) {
							currentPhase = FireBall; attacksLeft = FIRE_BALL_ATTACKS; attacksCount = 0
						}
					}
					case VorkAttacks.FireBall => {
						if(currentPhase != FireBall) {
								currentPhase = FireBall
								attacksLeft = FIRE_BALL_ATTACKS
								attacksCount = 0
						}
						attacksCount += 1
						attacksLeft = (attacksLeft - 1).max(0)

						if(attacksLeft == 0) {
							currentPhase = Spawn
							attacksLeft = ATTACKS_PER_SWITCH;
							attacksCount = 0
						}
					}
					case VorkAttacks.FreezeBreath => {
						if(currentPhase != Spawn) {
							currentPhase =  Spawn
							attacksLeft = 0
							attacksCount = 0
						}
					}
					case VorkAttacks.ZombifiedSpawn => {
						currentPhase = Acid
						attacksCount = 0
						attacksLeft = ATTACKS_PER_SWITCH
					}
				}
			}
		}
	}
}
@Singleton
class FredsVorkathHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsVorkathConfig, val localPlayerService: LocalPlayerService) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = FredsVorkathConfig.GROUP
	private def clientThread  = parent.getClientThread
	given Client = client
	given FredsVorkathConfig = config

	var vorkath: Option[Vorkath] = Option.empty[Vorkath]
	var projectiles: List[Projectile] = List.empty[Projectile]

	override def init(): Unit = {
		vorkath = Option.empty[Vorkath]
		projectiles = List.empty
//		state = clientThread.runOnClientThread(() => createState)
	}

	override def cleanup(): Unit = {
		vorkath  = Option.empty[Vorkath]
		projectiles = List.empty
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

		if(!client.getLocalPlayer.getWorldView.isInstance) {
			vorkath = Option.empty[Vorkath]
			projectiles = List.empty[Projectile]
			return
		}

		if(vorkath.isEmpty) {
			vorkath = client.getNpcs.asScala.toList.find(npc => {
				npc.templateLocation.getRegionID == VorkathRegion && npc.getId == NpcID.VORKATH
			}).map(Vorkath(_))
		}

		projectiles = projectiles.filterNot(_.hasHit)

		if(vorkath.nonEmpty) {
			log.debug(s"Tick: ${client.getTickCount}")
		}
	}

	@Subscribe
	def onLocalRegionChanged(e: LocalRegionChanged): Unit = {
		if(e.getOldRegion == VorkathRegion) {
			vorkath = Option.empty
			projectiles = List.empty
		}
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
	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		if(vorkath.exists(_.npc == e.getNpc)) {
			vorkath = None
		}
	}
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
		if (vorkath.isDefined && !projectiles.contains(projectile) && (e.getProjectile.templateSourceLocation.getRegionID == VorkathRegion || e.getProjectile.templateTargetLocation.getRegionID == VorkathRegion) && projectile.justSpawned) {
			projectiles = projectiles.appended(projectile)
			VorkAttacks.getAttackByProjectileId(projectile.getId)
				.foreach(attack => {
					val v = vorkath.get
					v.handleAttack(attack)
//					attack match {
//						case VorkAttacks.BasicAttack(a) => {
////							if( a== FireBall) {
////								if(v.currentPhase != FireBall) {
////									v.updatePhase(FireBall)
////								}
////								v.fireballCount = v.fireballCount + 1
////								v.attacksLeft = (v.attacksLeft - 1).max(0)
////							} else {
//								if(v.attacksLeft == 0) {
//									v.updatePhase(v.nextPhase)
//								}
//								v.attacksLeft = (v.attacksLeft - 1).max(0)
////							}
//						}
//						case VorkAttacks.FireBall => {
//							if(v.currentPhase != FireBall) {
//								v.updatePhase(FireBall)
//							}
//							v.fireballCount = v.fireballCount+1
//							v.attacksLeft = (v.attacksLeft-1).max(0)
//						}
//						case VorkAttacks.Acid => v.updatePhase(Acid); v.attacksLeft = 0
////						case VorkAttacks.FireBall =>
//						case VorkAttacks.FreezeBreath | VorkAttacks.ZombifiedSpawn => v.updatePhase(Spawn); v.attacksLeft = 0
////						case VorkAttacks.ZombifiedSpawn => v.updatePhase(v.nextPhase)
//					}
//					v.lastAttack = attack
//					log.debug(s"ProjectileAttack ${attack} detected by ${projectile}")
				})
		}
	}

	val vorkathIds: Seq[Int] = Seq(NpcID.VORKATH, NpcID.VORKATH_SLEEPING, NpcID.VORKATH_SPAWN)
	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
		if (!Option(e.getActor).map(_.templateLocation).map(_.getRegionID).contains(VorkathRegion)) return
//		if (inRegion && inFight) {
//			if(e.getActor.templateLocation.getRegionID != HueyRegion) return
		if(!vorkath.exists(_.npc == e.getActor)) return

		val model = vorkath.get
		Option(e.getActor).foreach {
			case vork: NPC if vork.getId == NpcID.VORKATH && vorkath.exists(_.npc == vork) => {
				val possibleAttacks = VorkAttacks.getPossibleAttacks(vork.getAnimation)
				possibleAttacks.foreach{
					case a: VorkMeleeAttack => model.handleAttack(a)
					case b: VorkRangedAttack => //model.handlePossibleAttack(a)
				}
//				if(possibleAttacks.size == 1) {
//
//				} else if(possibleAttacks.size > 1) {
//					log.debug(s"Vorkath ${vork} has these possible attacks ${possibleAttacks}")
//				} else {
//					log.debug(s"Vorkath ${vork} animation changed to ${vork.getAnimation}")
//				}
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
			val lastAttack = LineComponent.builder().left("Last Attack").right(s"${v.lastAttack}").rightColor(
				v.lastAttack match {
					case null => Color.RED
					case VorkAttacks.Slash => Color.BLACK
					case VorkAttacks.FireBreath => new Color(255, 162, 32);
					case VorkAttacks.PrayerBreath => Color.magenta
					case VorkAttacks.VenomBreath => new Color(0x63, 0x99, 0x34)//Color.GREEN.darker().darker()
					case VorkAttacks.Spike => new Color(0x00, 0xe8, 0x6D)
					case VorkAttacks.Ice => new Color(0, 201, 255)
					case VorkAttacks.FireBomb => new Color(255, 80, 0);
					case VorkAttacks.Acid => new Color(0, 255, 0)
					case VorkAttacks.FireBall => new Color(255, 120, 0);
					case VorkAttacks.FreezeBreath => new Color(40, 255, 255)
					case VorkAttacks.ZombifiedSpawn => Color.gray
				}
			).build

//			val stageLine = LineComponent.builder().left("Stage").right(s"${s.stage}").build
//			val pillarsLines = s.pillars.map {
//				case (p, l) => LineComponent.builder().left(p.entryName).leftColor(p.getColor).right(s"$l").rightColor(
//					Color.RED.interpolate(Color.GREEN, l/5.0)
//				).build
//			}.toList.prepended(TitleComponent.builder().text("Pillars").build())
			Seq(vorkathLine, phase, atkleft, lastAttack, Seq.empty[LayoutableRenderableEntity]).flatMap{
				case e: LayoutableRenderableEntity => Seq(e)
				case le: Seq[_] => le.collect{
					case e: LayoutableRenderableEntity => e
				}
			}
		}.getOrElse(Seq.empty[LayoutableRenderableEntity])
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		given Graphics2D = g
		given ModelOutlineRenderer = parent.getModelOutlineRenderer
		vorkath.foreach(v => {
			OverlayUtil.renderActorOverlay(g, v.npc, s"${v.currentPhase} + ${v.attacksLeft}",
				v.currentPhase match {
					case VorkPhases.Unknown => Color.YELLOW
					case VorkPhases.Acid => Color.GREEN
					case VorkPhases.FireBall => Color.RED
					case VorkPhases.Spawn => Color.BLUE
				})
		})
		projectiles.flatMap(p => Option(p).zip(VorkAttacks.getAttackByProjectileId(p.getId)))
			.foreach((a, b) => {
				val (projectileColor) = b match {
					case VorkAttacks.FireBreath => new Color(255, 162, 32)
					case VorkAttacks.PrayerBreath => Color.magenta
					case VorkAttacks.VenomBreath => new Color(0x63, 0x99, 0x34)//Color.GREEN.darker().darker()
					case VorkAttacks.Spike => new Color(0x00, 0xe8, 0x6D)
					case VorkAttacks.Ice => new Color(0, 201, 255)
					case VorkAttacks.FireBomb => new Color(255, 80, 0);
					case VorkAttacks.Acid => new Color(0, 255, 0)
					case VorkAttacks.FireBall => new Color(255, 120, 0);
					case VorkAttacks.FreezeBreath => new Color(40, 255, 255)
					case VorkAttacks.ZombifiedSpawn => Color.gray
				}
				import com.fredplugins.common.overlays
				overlays.renderProjectileOverlay(a, b.entryName)(2, 2, projectileColor)
				overlays.renderTileOverlay(a.getTargetPoint, s"${a.ticksRemaining}", projectileColor, false)
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
