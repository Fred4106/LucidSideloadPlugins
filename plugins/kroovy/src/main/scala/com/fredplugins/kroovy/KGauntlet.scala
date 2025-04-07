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

//	case class GauntletNpcMoved(knpc: KEvent.KNpcMoved, tag: GauntletTag)

	val stongNpcs: scala.collection.mutable.ListBuffer[net.runelite.api.NPC] = mutable.ListBuffer.empty[net.runelite.api.NPC]
	override def handle(events: List[KEvent]): Unit = {
//		GauntletTags.Strong.values.foreach(report(_))
		val reactedEvents = mutable.ListBuffer.empty[KEvent]

		def report(e: KEvent): Unit = {
			reactedEvents.addOne(e)
		}

		if(events.nonEmpty) {
			log.debug(s"tick: ${client.getTickCount}")

//		events.zipWithIndex.foreach{
//			case (event, i) => log.debug(s"Event[${i}] = $event")
//		}
			events.collect {
//				case me@KEvent.KNpcSpawned(wrapped) if wrapped.id == 7371 || wrapped.id == 7372 => report(me)
//				case me@KEvent.KNpcDespawned(wrapped) if wrapped.id == 7371 || wrapped.id == 7372 => report(me)
//				case me@KEvent.KNpcMoved(wrapped, delta, cur) if wrapped.id == 7371 || wrapped.id == 7372 => report(me)
//				case me@KEvent.KNpcAnimationChanged(wrapped, old, cur)if wrapped.id == 7371 || wrapped.id == 7372  => report(me)
//				case me@KEvent.KNpcChanged(wrapped, old, cur) if wrapped.id == 7371 || wrapped.id == 7372 => report(me)
//
					case me@KEvent.KNpcDied(wrapped) if(GauntletTags.values.exists(_.ids.contains(wrapped.id))) => {
						report(me)
//					stongNpcs.addOne(wrapped.wrapped)
				}
//				case me@KEvent.KNpcDespawned(wrapped) if(stongNpcs.contains(wrapped.wrapped)) => {
//					report(me)
//					stongNpcs.subtractOne(wrapped.wrapped)
//				}
//				case me@KEvent.KNpcMoved(wrapped, delta, cur) if(stongNpcs.contains(wrapped.wrapped)) => report(me)//(_.refString.equals(wrapped.refString))) => report(me -> cur.distanceTo(client.getLocalPlayer.getWorldLocation))
//				case me@KEvent.KNpcAnimationChanged(wrapped, old, cur) if(stongNpcs.contains(wrapped.wrapped)) => report(me)//_.refString.equals(wrapped.refString))) => report(me)
//				case me@KEvent.KLocalPlayerMoved(delta, cur) => report(me)
//				case me@KEvent.KLocalPlayerAnimationChanged(old, cur) => report(me)
//				case me => report(me)
		}
			var finalReacted = reactedEvents.toList.zipWithIndex
//		finalReacted.foreach{
//			case (evt, i) => log.warn(s"Reacted[${i.toString.padTo(3, ' ')}] = ${evt}")
//		}
			events.filter {
				case KEvent.KNpcSpawned(wrapped) => true
				case KEvent.KNpcDespawned(wrapped) => true
				case KEvent.KNpcAnimationChanged(wrapped, old, cur) => true
				case KEvent.KNpcChanged(wrapped, old, cur) => true
				case KEvent.KNpcDied(wrapped) => true
				case KEvent.KGameObjectSpawned(go) => true
				case KEvent.KGameObjectDespawned(go) => true
				case KEvent.KGroundObjectSpawned(go) => true
				case KEvent.KGroundObjectDespawned(go) => true
				case KEvent.KDecorativeObjectSpawned(go) => true
				case KEvent.KDecorativeObjectDespawned(go) => true
				case KEvent.KWallObjectSpawned(go) => true
				case KEvent.KWallObjectDespawned(go) => true
				case KEvent.KItemLayerSpawned(go) => true
				case KEvent.KItemLayerDespawned(go) => true
				case _ => false
			}.zipWithIndex.foreach {
				case (evt, i) => {
						val reactedHead = finalReacted.headOption
						(if(reactedHead.exists(_._1 == evt)){
								finalReacted = finalReacted.drop(1)
								(s: String) => {
		//							finalReacted = finalReacted.take(1)
									log.debug(s"Reacted[${reactedHead.get._2.toString.padTo(5, ' ')}] ${s}")
								}
							} else {
								(s: String) => {
									log.info(s"Skipped        ${s}")
								}
							}
						)
						.apply(s"[${i.toString.padTo(5, ' ')}] = ${evt.toString}")
				}
			}
			println()
		}
//		val currentNpcs = client.getNpcs.asScala.toList
//		stongNpcs.filterInPlace(sn => currentNpcs.contains(sn))
	}
}
