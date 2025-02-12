package com.fredplugins.pvmHelper2.gauntlet

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmHelper2.{PvmHelperOverlay, TypeName}
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Actor, Client, NPC, Perspective, Player}
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.{EventBus, Subscribe}
import org.slf4j.Logger

import scala.util.chaining.*
import net.runelite.api.events.{ActorDeath, AnimationChanged, InteractingChanged, NpcDespawned, NpcSpawned, VarbitChanged}
import net.runelite.client.ui.overlay.{OverlayManager, OverlayUtil}
import net.runelite.client.util.GameEventManager

import java.awt.{BasicStroke, Color}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.swing.{Dimension, Graphics2D}

@Singleton
class GauntletSolver @Inject()(val eventBus: EventBus, val client: Client, val clientThread: ClientThread, val gameEventManager: GameEventManager, val overlayManager: OverlayManager) {
	private val log: Logger = ShimUtils.getLogger(getClass.getName, "DEBUG")
	private  var isRunning	= false

	val overlay: PvmHelperOverlay = PvmHelperOverlay.create("GauntletSolver")(g => {
		client.getNpcs.asScala.toList.flatMap(GauntletNpcType.unapply).foreach(n =>{
			val poly = Perspective.getCanvasTilePoly(client, n.wrapped.getLocalLocation, 30)
			OverlayUtil.renderPolygon(g, poly, GauntletNpcType.color(n.tpe), Color.BLACK, new BasicStroke(4))
		})
//		g.drawRoundRect(10, 20, 40, 70, 12, 12)

		null.asInstanceOf[Dimension]
	})


	def onVarbitChanged(v: VarbitChanged): Unit = {
		Option(v.getVarbitId -> v.getValue).collect {
			case (9178, 0) if isRunning => {
				log.debug("Stopping Gauntlet")
				overlayManager.remove(overlay)
				eventBus.unregister(this)
				isRunning = false
			}
			case (9178, 1) if !isRunning => {
				log.debug("Starting gauntlet")
				isRunning = true
				overlayManager.add(overlay)
				gameEventManager.simulateGameEvents(this)
				eventBus.register(this)
			}//eventBus.register(Instance)
		}
	}

	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		Option(e.getNpc).flatMap(GauntletNpcType.unapply).foreach((n: GauntletNpcInstance) => {
			log.debug("Gauntlet - Spawned {}", n)
		})
//		val tpeOpt = GauntletNpcType.values.find(_.ids.contains(e.getNpc.getId))
//		tpeOpt.foreach(tpe => {
//			log.debug("Gauntlet - Spawned {} {} @ {}", tpe, e.getNpc.getName, e.getNpc.getWorldLocation)
//		})
	}

	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
//		val tpeAndAnimationOpt = Option(e.getActor).collect {
//			case n: NPC => GauntletNpcType.values.find(_.ids.contains(n.getId)).map(j => j -> n)
//		}.flatten
//
//		tpeAndAnimationOpt.foreach(
//			(tpe, npc) =>  {
//				log.debug("Gauntlet - Animation {} {} @ {} changed to {}", tpe, npc.getName, npc.getWorldLocation, npc.getAnimation)
//			}
//		)
		Option(e.getActor).flatMap(GauntletNpcType.unapply).foreach{
			n => log.debug("Gauntlet - Animation of {} changed to {}", n, n.animation)
		}
	}

	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
//		val tpeOpt = GauntletNpcType.values.find(_.ids.contains(e.getNpc.getId))
//		tpeOpt.foreach(tpe => {
//			log.debug("Gauntlet - Despawned {} {} @ {}", tpe, e.getNpc.getName, e.getNpc.getWorldLocation)
//		})
		Option(e.getActor).flatMap(GauntletNpcType.unapply).foreach {
			n => log.debug("Gauntlet - Despawned {}", n)
		}
	}

	@Subscribe
	def onInteractingChanged(e: InteractingChanged): Unit = {
//		val sourceTpeAndNpc = Option(e.getSource).collect {
//			case n: NPC => GauntletNpcType.values.find(_.ids.contains(n.getId)).map(j => j -> n)
//		}.flatten
//
//		val targetTpeAndNpc = Option(e.getTarget).collect {
//			case n: NPC => GauntletNpcType.values.find(_.ids.contains(n.getId)).map(j => j -> n)
//		}.flatten
//
//		(sourceTpeAndNpc, targetTpeAndNpc) match {
//			case (Some((sourceTpe, sourceNpc)), Some((targetTpe, targetNpc))) => log.debug("Gauntlet - InteractingChanged ({} {} @ {}) -> ({} {} @ {})", sourceTpe, sourceNpc.getName, sourceNpc.getWorldLocation,targetTpe, targetNpc.getName, targetNpc.getWorldLocation)
//			case (None, Some((targetTpe, targetNpc))) =>log.debug("Gauntlet - InteractingChanged (???) -> ({} {} @ {})", targetTpe, targetNpc.getName, targetNpc.getWorldLocation)
//			case (Some((sourceTpe, sourceNpc)), None) =>log.debug("Gauntlet - InteractingChanged ({} {} @ {}) -> (???)", sourceTpe, sourceNpc.getName, sourceNpc.getWorldLocation)
//			case (_, _) =>
//		}

//		inline type unwrapped = GauntletNpcInstance |  NPC | Player
		type unwrapped = (GauntletNpcType#Instance |  NPC | Player | "None")
		inline def unwrap(a: Actor): unwrapped = a match{
			case GauntletNpcType(n) => n
			case npc: NPC => npc
			case player: Player => player
			case null => "None"
		}
		val src =unwrap(e.getSource)
		val target = unwrap(e.getTarget)
		if(src.isInstanceOf[GauntletNpcInstance] || target.isInstanceOf[GauntletNpcInstance]) {
			log.debug("Gauntlet - InteractingChanged {} -> {}", src, target)
		}

//		(, Option(e.getTarget).flatMap(GauntletNpcType.unapply)) match {
//			case (Some(source), Some(target)) =>
//		}
//		e.getSource
	}

	@Subscribe
	def onActorDeath(e: ActorDeath): Unit = {
//		val deadTpeAndNpc = Option(e.getActor).collect {
//			case n: NPC => GauntletNpcType.values.find(_.ids.contains(n.getId)).map(j => j -> n)
//		}.flatten
//
//		deadTpeAndNpc.foreach{
//			case (tpe, npc) => log.debug("Gauntlet - ActorDeath {} {} @ {}", tpe, npc.getName, npc.getWorldLocation)
//		}
		Option(e.getActor).collect {
			case GauntletNpcType(n) => n
		}.foreach(n => {
			log.debug("Gauntlet - ActorDeath {}", n)
		})
	}

	private var monitorSub: EventBus.Subscriber = _

	def startup(): Unit = {
		log.debug("Initializing Gauntlet")
		monitorSub = eventBus.register[VarbitChanged](classOf[VarbitChanged], (e: VarbitChanged) => onVarbitChanged(e), 0.0f)

		clientThread.runOnClientThread(() => {
			monitorSub.invoke(9178.pipe(id => {
				new VarbitChanged().tap(_.setVarbitId(id)).tap(_.setValue(client.getVarbitValue(id)))
			}))
		})
	}

	def shutdown(): Unit = {
		log.debug("Tearing down Gauntlet")
		eventBus.unregister(monitorSub)
		monitorSub.invoke(new VarbitChanged().tap(_.setVarbitId(9178)).tap(_.setValue(0)))
		monitorSub = null
	}
}
