package com.fredplugins.kroovy.events

import com.fredplugins.kroovy.events.KActor.KPlayer
import com.fredplugins.kroovy.events.KActor.KNpc
import com.fredplugins.kroovy.events.KTileObject.KDecorativeObject
import com.fredplugins.kroovy.events.KTileObject.KGameObject
import com.fredplugins.kroovy.events.KTileObject.KGroundObject
import com.fredplugins.kroovy.events.KTileObject.KItemLayer
import com.fredplugins.kroovy.events.KTileObject.KWallObject
import net.runelite.api.coords.WorldPoint
import net.runelite.api.{GameObject as RlGameObject, NPC as RlNpc, Player as RlPlayer}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

//sealed trait KEvent {
//}

enum KEvent {
	case KNpcSpawned(wrapped: KNpc) extends KEvent
	case KNpcDespawned(wrapped: KNpc) extends KEvent
	case KNpcAnimationChanged(wrapped: KNpc, old: Int, cur: Int) extends KEvent
	case KNpcChanged(wrapped: KNpc, old: Int, cur: Int) extends KEvent
	case KNpcDied(wrapped: KNpc) extends KEvent
	case KNpcMoved(wrapped: KNpc, delta: (Int, Int), cur: WorldPoint) extends KEvent
	case KPlayerSpawned(wrapped: KPlayer) extends KEvent
	case KPlayerDespawned(wrapped: KPlayer) extends KEvent
	case KPlayerAnimationChanged(wrapped: KPlayer, old: Int, cur: Int) extends KEvent
	case KPlayerDied(wrapped: KPlayer) extends KEvent
	case KPlayerMoved(wrapped: KPlayer, delta: (Int, Int), cur: WorldPoint) extends KEvent
	case KLocalPlayerAnimationChanged(old: Int, cur: Int) extends KEvent
	case KLocalPlayerDied extends KEvent
	case KLocalPlayerMoved(delta: (Int, Int), cur: WorldPoint) extends KEvent
	case KGameObjectSpawned(go: KGameObject) extends KEvent
	case KGameObjectDespawned(go: KGameObject) extends KEvent
	case KGroundObjectSpawned(go: KGroundObject) extends KEvent
	case KGroundObjectDespawned(go: KGroundObject) extends KEvent
	case KDecorativeObjectSpawned(go: KDecorativeObject) extends KEvent
	case KDecorativeObjectDespawned(go: KDecorativeObject) extends KEvent
	case KWallObjectSpawned(go: KWallObject) extends KEvent
	case KWallObjectDespawned(go: KWallObject) extends KEvent
	case KItemLayerSpawned(go: KItemLayer) extends KEvent
	case KItemLayerDespawned(go: KItemLayer) extends KEvent

}