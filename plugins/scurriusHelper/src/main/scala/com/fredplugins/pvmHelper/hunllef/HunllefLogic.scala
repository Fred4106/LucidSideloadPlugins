package com.fredplugins.pvmHelper.hunllef

import com.fredplugins.common.Locatable
import com.fredplugins.common.utils.{SInteractionUtils, ShimUtils}
import com.fredplugins.pvmHelper.hunllef.Tornado.isTornado
import com.fredplugins.pvmHelper.{BossToolTrait, FredsPvmHelperOverlay, FredsPvmHelperPanel}
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.{CombatUtils, InteractionUtils, InventoryUtils, MessageUtils}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.NPCs
import ethanApiPlugin.collections.query.NPCQuery
import net.runelite.api.*
import net.runelite.api.coords.{LocalPoint, WorldPoint}
import net.runelite.api.events.*
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.game.SkillIconManager
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, TitleComponent}
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import org.slf4j.Logger

import java.awt.Color
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.util.chaining.scalaUtilChainingOps
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
	@Inject val modelOutlineRenderer: ModelOutlineRenderer = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject private val eventBus: EventBus = null
	@Inject private val overlayManager: OverlayManager = null

	given Client = client
	given SkillIconManager = skillIconManager
	given ModelOutlineRenderer = modelOutlineRenderer

	private lazy val panel: FredsPvmHelperPanel[HunllefLogic] = new FredsPvmHelperPanel[HunllefLogic](this) {}
	private lazy val overlay: FredsPvmHelperOverlay[HunllefLogic] = new FredsPvmHelperOverlay[HunllefLogic] (this) {}

	@Provides
	def getConfig(configManager: ConfigManager): HunleffConfig = {
		configManager.getConfig[HunleffConfig](classOf[HunleffConfig])
	}

	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if (e.getGroup == HunleffConfig.GroupName) {
			e.getKey match {
				case u => log.debug("Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e.getNewValue)
			}
		}
	}


	inline def isGauntletVarbitSet: Boolean = client.getVarbitValue(9178) == 1
	inline def isHunllefVarbitSet: Boolean = client.getVarbitValue(9177) == 1

	override def inArea(): Boolean = {
		State.inGauntlet || State.inHunllef
	}



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

		projectilesSpawnedThisTick = Set.empty[Int]
		HunllefState.reset()

		tornadoes = Set.empty[Tornado]
		lastSafeTile = null
		secondLastSafeTile = null

		wrongAttackStyle = false
		switchWeapon = false
	}

	override def layoutPanel(): Seq[LayoutableRenderableEntity] = {

		val spacerElement = "" -> ""
		val block1 =
			Seq("inGauntlet" -> State.inGauntlet,
				"inHunllef" -> State.inHunllef,
				"nextCycle" -> State.HunllefState.getNextCycle,
				"ticksTillAttack" -> State.HunllefState.getTicksUntilNextAttack
			).map(d => LineComponent.builder.left(d._1).right(Option(d._2).map(_.toString).getOrElse("None")).build())
		val block2 = {
			(State.tornadoes.toList.partition(_.isInstanceOf[Tornado.ChaseTornado]) match {
				case (tl1, tl2) => {
					Seq(
					tl1.map(t => LineComponent.builder().left("Chase").right(s"age=${client.getTickCount - t.spawnTick}, spawn=${t.spawnLoc}").rightColor(Color.MAGENTA).build()),
					tl2.map(t => LineComponent.builder().left("Roaming").right(s"age=${client.getTickCount - t.spawnTick}, spawn=${t.spawnLoc}").rightColor(Color.BLUE).build()),
					).flatten
				}
			}).prepended(TitleComponent.builder().text("Tornadoes").build())
		}
		block1 ++ block2
	}

	override def tilesToPaint(): Seq[(Actor, Color, String)] = {
		State.tornadoes.toList.map {
			case ct: Tornado.ChaseTornado => (ct.wrapped, Color.RED, s"${ct.wrapped.toString}, ${client.getTickCount - ct.spawnTick}")
			case rt: Tornado.RoamingTornado => (rt.wrapped, Color.BLUE, s"${rt.wrapped.toString}, ${client.getTickCount - rt.spawnTick}")
		}
	}

	override protected def startUp(): Unit = {
		resetState()
		overlayManager.add(panel)
		overlayManager.add(overlay)
	}


	override protected def shutDown(): Unit = {
		overlayManager.remove(panel)
		overlayManager.remove(overlay)

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

	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
		if (!inArea() || event.getNpc == null) return
		var shouldLog = true

		if (isHunllef(event.getNpc)) {
			State.HunllefState.reset()
		} else if (isTornado(event.getNpc)) {
			Tornado.apply(event.getNpc).foreach(toAdd => {
				State.tornadoes = State.tornadoes + toAdd
			})
		} else {
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
		} else if (State.tornadoes.exists(_.wrapped == event.getNpc)) {
			State.tornadoes = State.tornadoes.filter(t => t.wrapped != event.getNpc)
		} else {shouldLog = false}

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
	@Subscribe
	private def onChatMessage(event: ChatMessage): Unit = {
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

	var lastHeadIcon: HeadIcon = null

	@Subscribe
	private def onGameTick(event: GameTick): Unit = {
		if (!inArea()) {
		} else {
			import State.projectilesSpawnedThisTick
			if(State.inHunllef) {
				val currentCycle = State.HunllefState.getNextCycle
//				val oldHunleffCycle = State.HunllefState.getLastCycle
				import scala.jdk.OptionConverters.*
				val hunleffNpc: Option[NPC] = NPCs.search().idInList(HunllefIds.map(Integer.valueOf).asJava).first.toScala

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
				hunleffNpc.map(EthanApiPlugin.getHeadIcon(_)).tap(hi => logTick(s"Head icon changed to ${hi}")) match {
//					case HeadIcon.RANGE_MAGE_MELEE => InventoryUtils.wieldItem(30340)
//					case null => InventoryUtils.wieldItem(23857)
					case _ => //InventoryUtils.wieldItem(23851)
				}

				val wepItem = client.getItemContainer(InventoryID.EQUIPMENT).getItem(EquipmentInventorySlot.WEAPON.getSlotIdx)
				if(wepItem != null && wepItem.getId == 30340) {
					if(!CombatUtils.isSpecEnabled) CombatUtils.toggleSpec()
				} else if(wepItem != null && wepItem.getId == 23851) {
					CombatUtils.activateQuickPrayers()
				}
				State.HunllefState.onTick()
				projectilesSpawnedThisTick = Set.empty
			}
		}

		if(didLogThisTick) {
			logTick ("GameTick\n")
			didLogThisTick = false
		}
	}

}
