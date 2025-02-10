package com.fredplugins.pvmHelper2

import com.fredplugins.common.utils.{SInteractionUtils, ShimUtils}
import com.fredplugins.pvmHelper2.gauntlet.GauntletSolver
import com.google.inject.{Inject, Provides, Singleton}
import com.lucidplugins.api.utils.{CombatUtils, InteractionUtils, NpcUtils}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{TileObjects, Widgets}
import net.runelite.api.coords.WorldPoint
import net.runelite.api.*
import net.runelite.api.events.{AnimationChanged, GameStateChanged, GameTick, NpcChanged, NpcDespawned, NpcSpawned, ProjectileMoved, VarbitChanged}
import net.runelite.client.Notifier
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.{ConfigItem, ConfigManager}
import net.runelite.client.eventbus.{EventBus, Subscribe}
import net.runelite.client.events.ConfigChanged
import net.runelite.client.plugins.{Plugin, PluginDependency, PluginDescriptor}
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.OverlayManager
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, TitleComponent}
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
import scala.swing.Publisher
import scala.swing.event.Event
import scala.util.chaining.*
import scala.util.{Random, Try}

@PluginDescriptor(
	name = "<html><font color=\"#A1004B\">Freds</font> PVM Helper 2</html>",
	description = "Provides some auto movement and prayer help for limited set of bosses",
	tags = Array("pvm", "scurrius", "jad", "prayer", "helper", "maps")
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsPvmHelper2() extends Plugin {
	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: FredsPvmHelperConfig2 = null
	@Inject val notifier: Notifier = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject val eventBus: EventBus = null
	@Inject private val overlayManager: OverlayManager = null
	@Inject private val configManager: ConfigManager = null
	@Inject private val panel: FredsPvmHelper2Panel = null
//	@Inject private val overlay: FredsPvmHelper2Overlay = null
	given Client = client
	given EventBus = eventBus

	@Provides
	def getConfig(configManager: ConfigManager): FredsPvmHelperConfig2 = {
		configManager.getConfig[FredsPvmHelperConfig2](classOf[FredsPvmHelperConfig2])
	}

	@Inject val gauntletRoom: GauntletSolver = null

//	lazy val animationConfigEntries: Map[Int, () => NamedAnimationEntry] = {
//		configManager.getConfigDescriptor(config).getItems.asScala.toList.filter(cid => {
//			cid.getItem.section() == FredsPvmHelperConfig.ANIMATION_NAMES_SECTION
//		}).map(cid => {
//			cid.name.toInt -> (() => {
//				configManager.getConfiguration(FredsPvmHelperConfig.GroupName, cid.key).pipe(NamedAnimationEntry.encode(_)).get
//			})
//		}).toMap
//	}
//	object ConfigObj {
//		def animation(i: Int): Option[NamedAnimationEntry] = {
//			animationConfigEntries.get(i).map(_.apply)
//		}
//	}

	@Subscribe
	private def onConfigChanged(e: ConfigChanged): Unit = {
		//		def withEntry(configItem: ConfigItem)
		if (e.getGroup == FredsPvmHelperConfig2.GroupName) {
			log.debug("config[{}] changed from {} to {}", e.getKey, e.getOldValue, e.getNewValue)
			//			if(e.getKey.startsWith("animation")) {
//			Option(e.getKey).filter(_.startsWith("animation")).flatMap(_.drop(9).toIntOption.filter(animationConfigEntries.contains)).foreach(animationKey => {
//				NamedAnimationEntry.roundTrip(e.getNewValue) match {
//					case Some(fae) => 	{
//						val os = Try(NamedAnimationEntry.encode(e.getOldValue).get).fold(_ => s"\"${e.getOldValue}\"", u => u.toString)
//						log.debug(s"changing from {} => {}", os, fae)
//					}
//					case None => {
//						configManager.setConfiguration(FredsPvmHelperConfig.GroupName, e.getKey, decode(NamedAnimationEntry.encode(e.getOldValue).getOrElse(NamedAnimationEntry.empty)))
//					}
//				}
//			})
		}
	}
//				.filter(key => )}
//					.getKey.stripPrefix("animation")).flatMap(_.toIntOption).find(animationConfigEntries.contains(_)).map(iii =>iii
//					case i if animationConfigEntries.contains(i) =>  {
//						NamedAnimationEntry.roundTrip(e.getNewValue) match {
//							case Some(canRoundTrip) => {
//		//						val niceNewVal = NamedAnimationEntry.encode().get
//		//						val niceOldVal = NamedAnimationEntry.encode(e.getOldValue).get
//								log.debug(s"changing from \"${e.getOldValue}\": {} => \"${NamedAnimationEntry.decode(canRoundTrip)}\": {}", NamedAnimationEntry.encode(e.getOldValue).getOrElse(NamedAnimationEntry.empty), canRoundTrip)
//							}
//							case None => {
//								decode(NamedAnimationEntry.encode(e.getOldValue).getOrElse(NamedAnimationEntry.empty)).pipe(revertValue => configManager.setConfiguration(e.getGroup, e.getKey, revertValue))
//		//						log.warn(s"cant change from \"${e.getOldValue}\": {} => \"${e.getNewValue}\": {}", NamedAnimationEntry.encode(e.getOldValue).getOrElse(NamedAnimationEntry.empty), canRoundTrip)
//							}
//						}
//					}
//					//configManager.getConfiguration(e.getGroup, e.getKey).pipe(NamedAnimationEntry.encode(_))
//				}
//			}
//			configManager.getConfigDescriptor(config).getItems.asScala.toList.find(cid => cid.key().equals(e.getKey)).foreach(c => {
//				//c is animation section
//			})
//
//			e.getKey match {
//				case u => log.debug("Key {} changed from {} to {}, but had no associated action", u, e.getOldValue, e.getNewValue)
//			}
//		}
//	}
//	var gauntletSolver: GauntletRoom = GauntletRoom(eventBus, client, clientThread)
	private val eventSubs = mutable.ListBuffer.empty[EventBus.Subscriber]
	override protected def startUp(): Unit = {
		gauntletRoom.startup()
		log.debug("Staring up plugin")

		overlayManager.add(panel)
//		eventBus.register(gauntletSolver)
//		gauntletSolver.startup();

//		println(s"\n${sEventBus.getKeys.map(_.toString).map(s => s"\t${s}").mkString("\n")}")
//		overlayManager.add(overlay)
	}


	override protected def shutDown(): Unit = {
		gauntletRoom.shutdown()
		log.debug("Shutting down up plugin")
		overlayManager.remove(panel)
//		overlayManager.remove(overlay)
	}

//	@Subsqcribe
	private def onNpcChanged(event: NpcChanged): Unit = {
//		Option(event.getNpc).map(npc => SNpcCompositionChanged(npc.getIndex, npc.getId, npc.getComposition, event.getOld))
//			.foreach(publish(_))
	}

//	@Subscribe
	private def onGameTick(event: GameTick): Unit = {
//		state = state
//			.withRegionId(
//				Try(WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation).getRegionID).getOrElse(-1)
//			)
//		if (oldState != state) {
//			log.debug("state changed from\n{}\nto\n{}", oldState, state)
//		}
//		oldState = state
	}

//	@Subscribe
	private def onGameStateChanged(event: GameStateChanged): Unit = {
		log.debug("GameState changed to {}", event.getGameState)
//		if (event.getGameState != GameState.LOGGED_IN) resetState()
	}

//	@Subscribe
	private def onVarbitChanged(event: VarbitChanged): Unit = {
//		if(event.getVarbitId == 9178) {
//			if(event.getValue == 0) {
//				eventBus.unregister(PvmSolver.Gauntlet)
//			} else if(event.getValue == 1) {
//				PvmSolver.Gauntlet.resetState()
//				eventBus.register(PvmSolver.Gauntlet)
//			}
//		}

//		if(ConfigCache.debugVarps.contains(event.getVarpId) || ConfigCache.debugVarbits.contains(event.getVarbitId)) {
//			log.debug(s"Varbit {}[{}]={}", event.getVarpId, event.getVarbitId, event.getValue)
//		}
	}

//	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
/*		if (!inArea() || event.getNpc == null) return
		log.debug(s"NpcSpawned: id={}, name={}", event.getNpc.getId, event.getNpc.getName)
		state = state.withNpc(event.getNpc)
		Option(event.getNpc).map(npc => SNpcSpawned(npc.getIndex, npc.getId, npc.getAnimation, npc.getComposition))
			.foreach(publish(_))
		//		eventSource.push(SNpcSpawned(event.getNpc))*/
	}

//	@Subscribe
	private def onNpcDespawned(event: NpcDespawned): Unit = {
/*		log.debug(s"NpcDespawned: id={}, name={}", event.getNpc.getId, event.getNpc.getName)
		state = state.withoutNpc(event.getNpc)
		Option(event.getNpc).map(npc => SNpcDespawned(npc.getIndex, npc.getId, npc.getComposition))
			.foreach(publish(_))*/
	}

//	@Subscribe
	private def onAnimationChanged(event: AnimationChanged): Unit = {
//		event.getActor match {
//			case npc: NPC if state.npcAnimations.contains(npc) => {
//				log.debug(s"onAnimationChanged: source=npc[{}], animationId={}", npc.getId, npc.getAnimation)
//				val old = state.npcAnimations(npc)
//				state = state.withNpcAnimation(npc, npc.getAnimation)
//				Option(SNpcAnimationChanged(npc.getIndex, npc.getId, state.npcAnimations(npc), old))
//					.foreach(publish(_))
//				//				eventSource.push(SNpcAnimationChanged(npc, old))
//			}
//			case player: Player if (client.getLocalPlayer == player) => {
//				log.debug(s"onAnimationChanged: source={}, animationId={}", "local", player.getAnimation)
//				state = state.withPlayerAnimation(player.getAnimation)
//			}
//			case _ => {}
//		}
	}
//
//	@Subscribe
//	private def onProjectileMoved(event: ProjectileMoved): Unit = {
//		if (event.getProjectile.getRemainingCycles == event.getProjectile.getEndCycle - event.getProjectile.getStartCycle) {
//			log.debug(s"Projectile Spawned: id={}, event={}", event.getProjectile.getId, event)
////			state = state.withProjectile(event.getProjectile)
//		}
//	}
}