package com.fredplugins.kroovy

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.events.{EventManager, KEvent, KNpc}
import net.runelite.api.Client
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object KGauntlet extends EventManager.EventsOpImpl with ShimUtils.Logging {
	given client: Client = RuneLite.getInjector.getInstance(classOf[Client])
	given clientThread: ClientThread = RuneLite.getInjector.getInstance(classOf[ClientThread])

//	case class GauntletNpcMoved(knpc: KEvent.KNpcMoved, tag: GauntletTag)
	def report(e: Any): Unit = {
		log.debug("Reporting KGauntlet: {}", e)
	}

	val stongNpcs: scala.collection.mutable.Set[KNpc] = scala.collection.mutable.HashSet.empty[KNpc]
	override def handle(events: List[KEvent]): Unit = {
		events.foreach {
				case me@KEvent.KNpcSpawned(wrapped) if(GauntletTags.Strong.values.exists(_.ids.contains(wrapped.id))) => report(me); stongNpcs.addOne(wrapped)
				case me@KEvent.KNpcDespawned(wrapped) if(GauntletTags.Strong.values.exists(_.ids.contains(wrapped.id))) => report(me); stongNpcs.remove(wrapped)
				case me@KEvent.KNpcMoved(wrapped, delta, cur) if(stongNpcs.contains(wrapped)) => report(me -> cur.distanceTo(client.getLocalPlayer.getWorldLocation))
				case me@KEvent.KNpcAnimationChanged(wrapped, old, cur) if(stongNpcs.contains(wrapped)) => report(me)
				case me@KEvent.KLocalPlayerMoved(delta, cur) => report(me)
				case me@KEvent.KLocalPlayerAnimationChanged(old, cur) => report(me)
				case _ =>
		}
	}
}
