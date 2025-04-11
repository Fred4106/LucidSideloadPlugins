package com.fredplugins.kroovy

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.events.{EventManager, KEvent}
import net.runelite.api.Client
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread

import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object KGauntlet extends EventManager.EventsOpImpl with ShimUtils.Logging() {


	given client: Client = RuneLite.getInjector.getInstance(classOf[Client])
	given clientThread: ClientThread = RuneLite.getInjector.getInstance(classOf[ClientThread])
	val stongNpcs: scala.collection.mutable.ListBuffer[net.runelite.api.NPC] = mutable.ListBuffer.empty[net.runelite.api.NPC]
	override def reaction: PartialFunction[KEvent, Unit] = {
		case me@KEvent.KNpcSpawned(wrapped) => log.debug(s"result: ${me}")
		case me@KEvent.KNpcDespawned(wrapped) => log.debug(s"result: ${me}")
		case me@KEvent.KNpcAnimationChanged(wrapped, old, cur) => log.debug(s"result: ${me}")
		case me@KEvent.KNpcChanged(wrapped, old, cur) => log.debug(s"result: ${me}")
		case me@KEvent.KNpcDied(wrapped) => log.debug(s"result: ${me}")
		case me@KEvent.KGameObjectSpawned(go) => log.debug(s"result: ${me}")
		case me@KEvent.KGameObjectDespawned(go) => log.debug(s"result: ${me}")
		case me@KEvent.KGroundObjectSpawned(go) => log.debug(s"result: ${me}")
		case me@KEvent.KGroundObjectDespawned(go) => log.debug(s"result: ${me}")
		case me@KEvent.KDecorativeObjectSpawned(go) => log.debug(s"result: ${me}")
		case me@KEvent.KDecorativeObjectDespawned(go) => log.debug(s"result: ${me}")
		case me@KEvent.KWallObjectSpawned(go) => log.debug(s"result: ${me}")
		case me@KEvent.KWallObjectDespawned(go) => log.debug(s"result: ${me}")
		case me@KEvent.KItemLayerSpawned(go) => log.debug(s"result: ${me}")
		case me@KEvent.KItemLayerDespawned(go) => log.debug(s"result: ${me}")
	}

//	override def handle(events: List[KEvent]): Unit = {
////		if(events.nonEmpty) {
////			log.debug(s"tick: ${client.getTickCount}")
////			events.filter {
////				case _ => true
////				case KEvent.KNpcSpawned(wrapped) => true
////				case KEvent.KNpcDespawned(wrapped) => true
////				case KEvent.KNpcAnimationChanged(wrapped, old, cur) => true
////				case KEvent.KNpcChanged(wrapped, old, cur) => true
////				case KEvent.KNpcDied(wrapped) => true
////				case KEvent.KGameObjectSpawned(go) => true
////				case KEvent.KGameObjectDespawned(go) => true
////				case KEvent.KGroundObjectSpawned(go) => true
////				case KEvent.KGroundObjectDespawned(go) => true
////				case KEvent.KDecorativeObjectSpawned(go) => true
////				case KEvent.KDecorativeObjectDespawned(go) => true
////				case KEvent.KWallObjectSpawned(go) => true
////				case KEvent.KWallObjectDespawned(go) => true
////				case KEvent.KItemLayerSpawned(go) => true
////				case KEvent.KItemLayerDespawned(go) => true
////				case _ => false
////			}.zipWithIndex.foreach {
////				case (evt, i) => {
////					log.info(s"event[${i}] = $evt")
////				}
////			}
////			println()
////		}
////		val currentNpcs = client.getNpcs.asScala.toList
////		stongNpcs.filterInPlace(sn => currentNpcs.contains(sn))
//	}

}
