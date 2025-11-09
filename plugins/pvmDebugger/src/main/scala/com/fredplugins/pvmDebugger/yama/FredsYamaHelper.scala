package com.fredplugins.pvmDebugger.yama

import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.google.inject.Inject
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.Client
import net.runelite.api.NPC
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.gameval.NpcID
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.TitleComponent

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class FredsYamaHelper @Inject()(override val parent: PvmDebuggerPlugin, override val config: FredsYamaConfig)
	extends HelperModule with WithPanel with WithOverlay {
	override val client    : Client = parent.getClient
	override val moduleName: String = FredsYamaConfig.GroupName

	var state: Option[State] = Option.empty[State]

	override def init(): Unit = {}

	override def cleanup(): Unit = {}

	private def getRegion(): Int = {
		Option(client.getLocalPlayer)
			.map(_.getLocalLocation)
			.map(WorldPoint.fromLocalInstance(client, _))
			.map(_.getRegionID)
			.getOrElse(-1)
	}

	private def inYamasDomain: Boolean = getRegion() == YAMAS_DOMAIN_REGION_ID



	@Subscribe
	def onRegionChanged(event: LocalRegionChanged): Unit = {
		if(event.getCurRegion != YAMAS_DOMAIN_REGION_ID && state.isDefined) {
			state = Option.empty[State]
		}
		if (event.getCurRegion == YAMAS_DOMAIN_REGION_ID && state.isEmpty) {
			state = Option(State())
		}
	}

	@Subscribe
	private def onNpcSpawned(event: NpcSpawned): Unit = {
		if(state.isEmpty) return;
		val s = state.get
		val npcId = event.getNpc.getId
		if (npcId == NpcID.YAMA) {
			s.npc = event.getNpc
			s.phase = s.phase match {
				case null => YamaPhase.P1
				case YamaPhase.Judge1 => YamaPhase.P2
				case YamaPhase.Judge2 =>YamaPhase.P3
			}
		}

		if (npcId == NpcID.YAMA_JUDGE_OF_YAMA) {
			s.npc = event.getNpc
			s.phase = s.phase match {
				case YamaPhase.P1 => YamaPhase.Judge1
				case YamaPhase.P2 => YamaPhase.Judge2
			}
		}
	}

	@Subscribe
	private def onNpcDespawned(event: NpcDespawned): Unit = {
		if (state.isEmpty) return;
		val s     = state.get
		val npcId = event.getNpc.getId
		if (npcId == NpcID.YAMA && s.phase == YamaPhase.P3) {
			s.npc = null
			s.phase = null
		}
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		state.filter(s =>  s.npc !=null && s.phase != null).map(s => {
			val l1 = LineComponent.builder().left("npc").right(s"${s.npc.getId} | ${s.npc.getName}").build()
			val l2 = LineComponent.builder().left("phase").right(s.phase.name).build()
			Seq(l1, l2)
		}).getOrElse(Seq.empty[LayoutableRenderableEntity])
	}
	override def renderOverlay(g: Graphics2D): Dimension = {
		def renderNpcOverlay(n: NPC, text: String, color: Color, zoffset: Int): Unit = {
			parent.getModelOutlineRenderer.drawOutline(n, 2, color, 4)
			val poly = n.getCanvasTilePoly
			if (poly != null) OverlayUtil.renderPolygon(g, poly, color)
			val textLocation = n.getCanvasTextLocation(g, text, n.getLogicalHeight + zoffset)
			if (textLocation != null) OverlayUtil.renderTextLocation(g, textLocation, text, color)
		}

		state.foreach(s => {
			if(s.npc!=null && s.phase != null) {
				renderNpcOverlay(s.npc, s.phase.name, Color.GREEN, 0)
			}
		})
		null.asInstanceOf[Dimension]
	}
}
