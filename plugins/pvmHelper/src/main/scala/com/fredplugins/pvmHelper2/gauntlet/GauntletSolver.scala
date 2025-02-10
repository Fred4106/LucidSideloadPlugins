package com.fredplugins.pvmHelper2.gauntlet

import com.fredplugins.common.utils.ShimUtils
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Client, NPC}
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.{EventBus, Subscribe}
import org.slf4j.Logger

import scala.util.chaining.*
import net.runelite.api.events.{ActorDeath, AnimationChanged, InteractingChanged, NpcDespawned, NpcSpawned, VarbitChanged}

@Singleton
class GauntletSolver @Inject()(val eventBus: EventBus, val client: Client, val clientThread: ClientThread) {
	private val log: Logger = ShimUtils.getLogger(getClass.getName, "DEBUG")

	def onVarbitChanged(v: VarbitChanged): Unit = {
		Option(v.getVarbitId -> v.getValue).collect {
			case (9178, 0) => {
				log.debug("Closing gauntlet")
				eventBus.unregister(this)
			}
			case (9178, 1) => {
				log.debug("Initializing gauntlet")
				eventBus.register(this)
			}//eventBus.register(Instance)
		}
	}

	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		val tpeOpt = GauntletNpcType.values.find(_.ids.contains(e.getNpc.getId))
		tpeOpt.foreach(tpe => {
			log.debug("Gauntlet - Spawned {} {} @ {}", tpe, e.getNpc.getName, e.getNpc.getWorldLocation)
		})
	}

	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
		val tpeAndAnimationOpt = Option(e.getActor).collect {
			case n: NPC => GauntletNpcType.values.find(_.ids.contains(n.getId)).map(j => j -> n)
		}.flatten

		tpeAndAnimationOpt.foreach(
			(tpe, npc) =>  {
				log.debug("Gauntlet - Animation {} {} @ {} changed to {}", tpe, npc.getName, npc.getWorldLocation, npc.getAnimation)
			}
		)
	}

	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		val tpeOpt = GauntletNpcType.values.find(_.ids.contains(e.getNpc.getId))
		tpeOpt.foreach(tpe => {
			log.debug("Gauntlet - Despawned {} {} @ {}", tpe, e.getNpc.getName, e.getNpc.getWorldLocation)
		})
	}

	@Subscribe
	def onInteractingChanged(e: InteractingChanged): Unit = {
		val sourceTpeAndNpc = Option(e.getSource).collect {
			case n: NPC => GauntletNpcType.values.find(_.ids.contains(n.getId)).map(j => j -> n)
		}.flatten

		val targetTpeAndNpc = Option(e.getTarget).collect {
			case n: NPC => GauntletNpcType.values.find(_.ids.contains(n.getId)).map(j => j -> n)
		}.flatten

		(sourceTpeAndNpc, targetTpeAndNpc) match {
			case (Some((sourceTpe, sourceNpc)), Some((targetTpe, targetNpc))) => log.debug("Gauntlet - InteractingChanged ({} {} @ {}) -> ({} {} @ {})", sourceTpe, sourceNpc.getName, sourceNpc.getWorldLocation,targetTpe, targetNpc.getName, targetNpc.getWorldLocation)
			case (None, Some((targetTpe, targetNpc))) =>log.debug("Gauntlet - InteractingChanged (???) -> ({} {} @ {})", targetTpe, targetNpc.getName, targetNpc.getWorldLocation)
			case (Some((sourceTpe, sourceNpc)), None) =>log.debug("Gauntlet - InteractingChanged ({} {} @ {}) -> (???)", sourceTpe, sourceNpc.getName, sourceNpc.getWorldLocation)
			case (_, _) =>
		}
	}

	@Subscribe
	def onActorDeath(e: ActorDeath): Unit = {
		val deadTpeAndNpc = Option(e.getActor).collect {
			case n: NPC => GauntletNpcType.values.find(_.ids.contains(n.getId)).map(j => j -> n)
		}.flatten

		deadTpeAndNpc.foreach{
			case (tpe, npc) => log.debug("Gauntlet - ActorDeath {} {} @ {}", tpe, npc.getName, npc.getWorldLocation)
		}
	}

	private var monitorSub: EventBus.Subscriber = _
	def startup(): Unit = {
		monitorSub = eventBus.register[VarbitChanged](classOf[VarbitChanged], (e: VarbitChanged) => onVarbitChanged(e), 0.0f)

		log.debug("Staring gauntlet room")
		val v1 = clientThread.runOnClientThread(() => client.getVarbitValue(9178))
		val v2 = clientThread.runOnClientThread(() =>client.getVarbitValue(9177))
		val vbc1 = new VarbitChanged()
		vbc1.setVarbitId(9178)
		vbc1.setValue(v1)
		val vbc2 = new VarbitChanged()
		vbc2.setVarbitId(9177)
		vbc2.setValue(v2)
		clientThread.runOnClientThread(() =>{
			eventBus.post(vbc1)
			eventBus.post(vbc2)
		})
	}

	def shutdown(): Unit = {
		eventBus.unregister(monitorSub)
		monitorSub = null
		eventBus.unregister(this)
		log.debug("Stopping Gauntlet room")
	}
}
