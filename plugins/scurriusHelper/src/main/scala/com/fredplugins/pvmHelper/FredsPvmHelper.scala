package com.fredplugins.pvmHelper

import com.fredplugins.common.utils.{SInteractionUtils, ShimUtils}
import com.fredplugins.pvmHelper
import com.fredplugins.pvmHelper.PvmGui.SEvent.{SNpcAnimationChanged, SNpcCompositionChanged, SNpcDespawned, SNpcSpawned}
import com.fredplugins.pvmHelper.PvmGui.{SEvent}
import com.fredplugins.pvmHelper.config.NamedAnimationEntry
import com.fredplugins.pvmHelper.config.NamedAnimationEntry.decode
import com.fredplugins.pvmHelper.scurrius.ScurriusLogic
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
	name = "<html><font color=\"#A1004B\">Freds</font> PVM Helper</html>",
	description = "Provides some auto movement and prayer help for limited set of bosses",
	tags = Array("pvm", "scurrius", "prayer", "helper", "maps")
)
@PluginDependency(classOf[EthanApiPlugin])
@Singleton
class FredsPvmHelper() extends Plugin with BossToolTrait with Publisher {
	@Inject val client: Client = null
	@Inject val clientThread: ClientThread = null
	@Inject val config: FredsPvmHelperConfig = null
	@Inject val notifier: Notifier = null
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	@Inject private val eventBus: EventBus = null
	@Inject private val overlayManager: OverlayManager = null
	@Inject private val configManager: ConfigManager = null
	private val panel: FredsPvmHelperPanel[FredsPvmHelper] = new FredsPvmHelperPanel(this) {}

	case class State(regionId: Int, projectiles: List[Projectile], npcAnimations: Map[NPC, Int], playerAnimation: Int) {
		def withRegionId(id: Int): State = if (id == regionId) this else copy(regionId = id)
		def withProjectile(p: Projectile): State = if (projectiles.contains(p)) this else copy(projectiles = projectiles :+ p)
		def withoutNpc(n: NPC): State = if (!npcAnimations.contains(n)) this else copy(npcAnimations = npcAnimations.removed(n))
		def withNpc(n: NPC): State = if (npcAnimations.contains(n)) this else copy(npcAnimations = npcAnimations.updated(n, n.getAnimation))
		def withNpcAnimation(n: NPC, aid: Int): State = if (npcAnimations.get(n).contains(aid)) this else copy(npcAnimations = npcAnimations.updated(n, aid))
		def withPlayerAnimation(aid: Int): State = if (playerAnimation == aid) this else copy(playerAnimation = aid)
	}

	object State {
		def empty(): State = {
			State(-1, List.empty[Projectile], Map.empty[NPC, Int], -1)
		}

		def cleanup(s: State): State = {
			s.copy(projectiles = s.projectiles.flatMap(p => {
				Option.when(p.getRemainingCycles > 0)(p)
			}))
		}

		def fromClient()(using client: Client): State = {
			State(
				Try(WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation).getRegionID).getOrElse(-1),
				Try(client.getProjectiles.asScala.toList).getOrElse(List.empty[Projectile]),
				Try(client.getNpcs.asScala.toList.map(n => {
					n -> Try(n.getAnimation).getOrElse(-1)
				}).toMap).getOrElse(Map.empty[NPC, Int]),
				Try(client.getLocalPlayer.getAnimation).getOrElse(-1)
			)
		}
	}

	private var state: State = State.empty()
	private var oldState: State = State.empty()

	override def resetState(): Unit = {
		state = State.fromClient()
		oldState = State.empty()
	}

	//	@Inject private val overlay: FredsPvmHelperOverlay = null
	given Client = client

	@Provides
	def getConfig(configManager: ConfigManager): FredsPvmHelperConfig = {
		configManager.getConfig[FredsPvmHelperConfig](classOf[FredsPvmHelperConfig])
	}
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
	object Config {
		def getNamedAnimationEntries: List[NamedAnimationEntry] = {
			config.namedAnimationEntries().lines().	iterator().asScala.toList.flatMap(NamedAnimationEntry.encode)
		}
		def getNamedAnimationEntry(i: Int): NamedAnimationEntry = {
			getNamedAnimationEntries.apply(i)
		}
	}

	@Subscribe
	private def onConfigChanged(e: ConfigChanged): Unit = {
		//		def withEntry(configItem: ConfigItem)
		if (e.getGroup == FredsPvmHelperConfig.GroupName) {
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

	override def layoutPanel(): Seq[LayoutableRenderableEntity] = {
		Seq(
			LineComponent.builder
				.left("RegionId")
				.right(s"${state.regionId}")
				.build,
			LineComponent.builder
				.left("AnimationId")
				.right(s"${state.playerAnimation}")
				.build
		) ++
			state.npcAnimations.map(n => {
				val npc = n._1
				val anim = n._2
				LineComponent.builder
					.left(s"Npc{idx=${npc.getIndex}, id=${npc.getId}}")
					.right(s"${anim}")
					.build
			}).toList.prepended(TitleComponent.builder.text("Npc Animations").build()) ++
			state.projectiles.zipWithIndex.map(p => {
				val id = p._1.getId
				val interacting = if (p._1.getInteracting != null) {
					if (client.getLocalPlayer == p._1.getInteracting) "local" else p._1.getInteracting
				} else {
					"null"
				}
				val idx = p._2
				LineComponent.builder
					.left(s"${idx}")
					.right(s"${id} -> ${interacting}")
					.build
			}).prepended(TitleComponent.builder.text("Projectiles").build())
	}

	override def inArea(): Boolean = true

	private val gui: PvmGui = new pvmHelper.PvmGui().tap(_.visible = false)
	override protected def startUp(): Unit = {
		resetState()
		overlayManager.add(panel)
		gui.visible = true
		gui.listenTo(this)
		Config.getNamedAnimationEntries.zipWithIndex.map(_.swap).foreach(u => log.debug(s"testing[${u._1}] = ${u._2}"))
	}


	override protected def shutDown(): Unit = {
		gui.deafTo(this)
		gui.visible = false
		overlayManager.remove(panel)
		resetState()
	}

	@Subscribe
	private def onNpcChanged(event: NpcChanged): Unit = {
		//		event.getNpc.getComposition
		Option(event.getNpc).map(npc => SNpcCompositionChanged(npc.getIndex, npc.getId, npc.getComposition, event.getOld))
			.foreach(publish(_))
	}

	@Subscribe
	private def onGameTick(event: GameTick): Unit = {
		state = state
			.withRegionId(
				Try(WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation).getRegionID).getOrElse(-1)
			)
		if (oldState != state) {
			log.debug("state changed from\n{}\nto\n{}", oldState, state)
		}
		oldState = state
	}

	@Subscribe
	private def onGameStateChanged(event: GameStateChanged): Unit = {
		log.debug("GameState changed to {}", event.getGameState)
		if (event.getGameState != GameState.LOGGED_IN) resetState()
	}

	//	@Subscribe
	//	private def onVarbitChanged(event: VarbitChanged): Unit = {
	//		if (!inArea()) return
	////		if(ConfigCache.debugVarps.contains(event.getVarpId) || ConfigCache.debugVarbits.contains(event.getVarbitId)) {
	////			log.debug(s"Varbit {}[{}]={}", event.getVarpId, event.getVarbitId, event.getValue)
	////		}
	//	}

	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
		if (!inArea() || event.getNpc == null) return
		log.debug(s"NpcSpawned: id={}, name={}", event.getNpc.getId, event.getNpc.getName)
		state = state.withNpc(event.getNpc)
		Option(event.getNpc).map(npc => SNpcSpawned(npc.getIndex, npc.getId, npc.getAnimation, npc.getComposition))
			.foreach(publish(_))
		//		eventSource.push(SNpcSpawned(event.getNpc))
	}

	@Subscribe
	private def onNpcDespawned(event: NpcDespawned): Unit = {
		log.debug(s"NpcDespawned: id={}, name={}", event.getNpc.getId, event.getNpc.getName)
		state = state.withoutNpc(event.getNpc)
		Option(event.getNpc).map(npc => SNpcDespawned(npc.getIndex, npc.getId, npc.getComposition))
			.foreach(publish(_))
	}

	@Subscribe
	private def onAnimationChanged(event: AnimationChanged): Unit = {
		event.getActor match {
			case npc: NPC if state.npcAnimations.contains(npc) => {
				log.debug(s"onAnimationChanged: source=npc[{}], animationId={}", npc.getId, npc.getAnimation)
				val old = state.npcAnimations(npc)
				state = state.withNpcAnimation(npc, npc.getAnimation)
				Option(SNpcAnimationChanged(npc.getIndex, npc.getId, state.npcAnimations(npc), old))
					.foreach(publish(_))
				//				eventSource.push(SNpcAnimationChanged(npc, old))
			}
			case player: Player if (client.getLocalPlayer == player) => {
				log.debug(s"onAnimationChanged: source={}, animationId={}", "local", player.getAnimation)
				state = state.withPlayerAnimation(player.getAnimation)
			}
			case _ => {}
		}
	}

	@Subscribe
	private def onProjectileMoved(event: ProjectileMoved): Unit = {
		if (event.getProjectile.getRemainingCycles == event.getProjectile.getEndCycle - event.getProjectile.getStartCycle) {
			log.debug(s"Projectile Spawned: id={}, event={}, clazz={}", event.getProjectile.getId, event, event.getProjectile.getClass.getSimpleName)
			state = state.withProjectile(event.getProjectile)
		}
	}
}