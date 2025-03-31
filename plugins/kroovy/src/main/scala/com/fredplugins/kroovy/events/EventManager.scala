package com.fredplugins.kroovy.events

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import com.google.inject.{Inject, Singleton}
import net.runelite.api.{Client, NPC}
import net.runelite.api.events.{ActorDeath, AnimationChanged, NpcChanged, NpcDespawned, NpcSpawned}
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.{EventBus, Subscribe}

import scala.compiletime.uninitialized

sealed trait EventManagerApi {

}

@Singleton
class EventManagerImpl @Inject()(val client: Client, val eventBus: EventBus, val clientThread: ClientThread) extends EventManagerApi {

}

object EventManager extends EventManagerApi {
	private val instance: EventManagerImpl = RuneLite.getInjector.getInstance(classOf[EventManagerImpl])
}
