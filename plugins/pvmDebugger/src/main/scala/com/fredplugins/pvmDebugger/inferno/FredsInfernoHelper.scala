package com.fredplugins.pvmDebugger.inferno

import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.utils.SInteractionUtils
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.fredplugins.pvmDebugger.inferno.displaymodes.InfernoWaveDisplayMode
import com.google.inject.Inject
import com.google.inject.Singleton
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.NPCs
import ethanApiPlugin.collections.query.NPCQuery
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.Actor
import net.runelite.api.ChatMessageType
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.NPC
import net.runelite.api.Perspective
import net.runelite.api.Player
import net.runelite.api.Point
import net.runelite.api.Prayer
import net.runelite.api.Projectile
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.AnimationChanged
import net.runelite.api.events.ChatMessage
import net.runelite.api.events.GameStateChanged
import net.runelite.api.events.GameTick
import net.runelite.api.events.GraphicsObjectCreated
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.PostHealthBarConfig
import net.runelite.api.events.ProjectileMoved
import net.runelite.api.events.VarbitChanged
import net.runelite.api.gameval.ItemID
import net.runelite.api.gameval.NpcID
import net.runelite.api.gameval.ObjectID1
import net.runelite.api.gameval.SpotanimID
import net.runelite.api.gameval.VarbitID
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.ConfigChanged
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.ProgressPieComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import packets.MovementPackets

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Rectangle
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import scala.annotation.unused
import scala.collection.mutable
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.jdk.OptionConverters.RichOptional
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class FredsInfernoHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsInfernoConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = FredsInfernoConfig.GROUP
	private def clientThread  = parent.getClientThread

	given Client = client
	given FredsInfernoConfig = config

	var lastLocation     : WorldPoint                               = new WorldPoint(0, 0, 0)
	var currentWaveNumber: Int                                      = -1
	val infernoNpcs                                                 = mutable.ListBuffer.empty[InfernoNpc]
	val upcomingAttacks                                             = mutable.HashMap.empty[Int, Map[InfernoNpcAttack, Int]]
	val obstacles        : mutable.ListBuffer[WorldPoint]           = mutable.ListBuffer.empty[WorldPoint]
	// 0 = total safespot
	// 1 = pray melee
	// 2 = pray range
	// 3 = pray magic
	// 4 = pray melee, range
	// 5 = pray melee, magic
	// 6 = pray range, magic
	// 7 = pray all
	val safeSpotMap      : mutable.Map[WorldPoint, Int]             = mutable.HashMap.empty
	val safeSpotAreas    : mutable.Map[Int, List[WorldPoint]]       = mutable.HashMap.empty
		val blobDeathSpots   : mutable.ListBuffer[InfernoBlobDeathSpot] = mutable.ListBuffer.empty[InfernoBlobDeathSpot]

	var closestAttack: InfernoNpcAttack = null

	var finalPhase           : Boolean    = false
	var finalPhaseTick       : Boolean    = false
	var ticksSinceFinalPhase : Int        = 0
	var zukShield            : NPC        = null
	var zuk                  : NPC        = null
	var zukShieldLastPosition: WorldPoint = null
	var zukShieldBase        : WorldPoint = null
	var zukShieldCornerTicks : Int        = -2

	var zukShieldNegativeXCoord   : Int = -1
	var zukShieldPositiveXCoord   : Int = -1
	var zukShieldLastNonZeroDelta : Int = 0
	var zukShieldLastDelta        : Int = 0
	var zukShieldTicksLeftInCorner: Int = -1

	var centralNibbler: InfernoNpc = null

	var lastTick: Long = 0

	def getPlayerRegionID: Int = WorldPoint.fromLocalInstance(client, client.getLocalPlayer.getLocalLocation).getRegionID

	def isInInferno: Boolean = {
		getPlayerRegionID == InfernoData.INFERNO_REGION
	}

	override def init(): Unit = {
		if(client.getGameState == GameState.LOGGED_IN && isInInferno) {
//			state = State()
		}
	}

	override def cleanup(): Unit = {
//		state = null
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		if(isInInferno) {}

		Seq.empty[LayoutableRenderableEntity]
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		null.asInstanceOf[Dimension]
	}
}
