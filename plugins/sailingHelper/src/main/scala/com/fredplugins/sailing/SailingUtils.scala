package com.fredplugins.sailing

import com.google.inject.Inject
import net.runelite.api.Actor
import net.runelite.api.Client
import net.runelite.api.GameObject
import net.runelite.api.ObjectComposition
import net.runelite.api.Player
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldPoint
import net.runelite.api.gameval.VarbitID

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class SailingUtils(client: Client, config: FredsSailingConfig){
	def isSailing: Boolean = {
		Option(client.getLocalPlayer).exists(lp => !lp.getWorldView.isTopLevel)
	}

	def isUim: Boolean = client.getVarbitValue(VarbitID.IRONMAN) == 2

	def isLocalPlayer(actor: Actor): Boolean = {
		val actorId = actor match {
			case p: Player => p.getId
			case _ => -1
		}
		Option(client.getLocalPlayer).map(_.getId).contains(actorId)
	}

	def getTransformedObject(o: GameObject): ObjectComposition = {
		val `def` = client.getObjectDefinition(o.getId)
		if (`def` == null || `def`.getImpostorIds == null) return `def`
		`def`.getImpostor
	}

	def getTopLevelLocalPoint: LocalPoint = {
		val player = client.getLocalPlayer
		val wv     = player.getWorldView
		if (wv.isTopLevel) return player.getLocalLocation
		client.getTopLevelWorldView.worldEntities.byIndex(wv.getId).transformToMainWorld(client.getLocalPlayer.getLocalLocation)
	}

	def getTopLevelWorldPoint: WorldPoint = WorldPoint.fromLocal(client, getTopLevelLocalPoint)
}
