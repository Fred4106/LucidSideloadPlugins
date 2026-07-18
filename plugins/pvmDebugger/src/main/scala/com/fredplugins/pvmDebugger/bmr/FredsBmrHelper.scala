package com.fredplugins.pvmDebugger.bmr

import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.constants.magic.SMagicBoost.{DeathCharge, SummonThrall}
import com.fredplugins.common.constants.magic.STimedPotion.Divine_combat
import com.fredplugins.common.extensions.ActorExtensions
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.extensions.MenuExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.services.TimedBoostsService
import com.fredplugins.common.services.TimedBoostsService.{MagicBoostChanged, getCachedValue, isActive, isLocked}
import com.fredplugins.common.utils.ReflectionUtils
import com.fredplugins.pvmDebugger
import com.fredplugins.pvmDebugger.{HelperModule, PvmDebuggerPlugin, WithOverlay, WithPanel}
import com.google.inject.{Inject, Singleton}
import ethanApiPlugin.EthanApiPlugin
import ethanApiPlugin.collections.{Equipment, Inventory}
import ethanApiPlugin.interactionApi.InventoryInteraction
import ethanApiPlugin.lucidplugins.api.utils.{CombatUtils, EquipmentUtils, InteractionUtils, InventoryUtils}
import ethanApiPlugin.services.localPlayer.events.{LocalDestinationChanged, LocalPositionChanged, LocalRegionChanged}
import net.runelite.api.*
import net.runelite.api.coords.{Angle, Direction, LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.events.*
import net.runelite.api.gameval.AnimationID.{NPC_WYRD01_PUNCH_LEFT, NPC_WYRD01_PUNCH_RIGHT, NPC_WYRD01_SCREECH01, NPC_WYRD01_SCREECH02, NPC_WYRD01_SCREECH03, NPC_WYRD01_TANTRUM01, NPC_WYRD01_TANTRUM02, NPC_WYRD02_MELEE01}
import net.runelite.api.gameval.{AnimationID, InterfaceID, ItemID, NpcID}
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.{LayoutableRenderableEntity, LineComponent, ProgressPieComponent, TitleComponent}
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer
import net.runelite.client.util.ColorUtil
import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.api.WorldRegion.{given_Conversion_WorldArea_WorldRegion, *}
import com.fredplugins.common.constants.FontTypes
import net.runelite.client.events.ConfigChanged
import net.runelite.client.ui.FontManager

import java.awt.Font
import java.awt.{Color, Dimension, Graphics2D, Polygon, Shape}
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.{Failure, Success, Try}
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class FredsBmrHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: FredsBmrConfig) extends HelperModule with WithPanel with WithOverlay {
	override val moduleName: String = "FredsBmrHelper"
	private def clientThread  = parent.getClientThread
	given Client = client

	lazy val wyrdHelper: FredsWyrdHelper = new FredsWyrdHelper(parent, config)
	lazy val drakanHelper: FredsDrakanHelper = new FredsDrakanHelper(this, config)
	private var cachedFont:  Font = null //config.fontType().//FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, config.getFontSize)

	def getFont(): Font = {
		if (cachedFont == null) {
			val nFont =
				if(config.fontType != FontTypes.REGULAR)
					Font.getFont(config.fontType.getName)
				else
					FontManager.getRunescapeFont

			cachedFont = nFont.deriveFont(config.fontStyle.getFont, config.fontSize)
		}
		cachedFont
	}

	@Subscribe
	def onConfigChanged(e: ConfigChanged): Unit = {
		if (!e.getGroup.equalsIgnoreCase("FredsBmrHelper")) return

		Option(e.getKey).foreach {
			case k@("fontType" | "fontSize"| "fontStyle") => cachedFont = null
			case o =>
		}
	}

	override def init(): Unit = {
		wyrdHelper.init()
		drakanHelper.init()
		parent.getEventBus.register(wyrdHelper)
		parent.getEventBus.register(drakanHelper)
	}

	override def cleanup(): Unit = {
		parent.getEventBus.unregister(wyrdHelper)
		parent.getEventBus.unregister(drakanHelper)

		wyrdHelper.cleanup()
		drakanHelper.cleanup()
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		val wyrdElements = Option(wyrdHelper).map(_.createPanelElements()).getOrElse(Seq.empty[LayoutableRenderableEntity])
		val drakanElements = Option(drakanHelper).map(_.createPanelElements()).getOrElse(Seq.empty[LayoutableRenderableEntity])
		Seq(wyrdElements, drakanElements).flatMap{
			case e: LayoutableRenderableEntity => Seq(e)
			case le: Seq[_] => le.collect{
				case e: LayoutableRenderableEntity => e
			}
		}
	}

	override def renderOverlay(g: Graphics2D): Dimension = {
		given Graphics2D = g
		given ModelOutlineRenderer = parent.getModelOutlineRenderer
		import com.fredplugins.common.overlays

		def renderPie(diameter: Int, heightOffset: Int)(pieChartColor: Color, textColor: Color)(lp: LocalPoint, text: String, progress: Double): Unit = {
			val ppc = new ProgressPieComponent()
			ppc.setBorderColor(Color.BLACK)
			ppc.setFill(pieChartColor)
			ppc.setProgress(progress)
			ppc.setDiameter(diameter)
			val point = Perspective.localToCanvas(client, lp, client.getTopLevelWorldView.getPlane, heightOffset)
			ppc.setPosition(point)
			ppc.render(g)

			val fm = g.getFontMetrics()
			val bounds = fm.getStringBounds(text, g)
			val xOffset = point.getX() - (bounds.getWidth() / 2).toInt
			val yOffset = point.getY() + (bounds.getHeight() / 2).toInt

			val textPoint = new Point(xOffset, yOffset)
			OverlayUtil.renderTextLocation(g, textPoint, text, textColor)
		}

		def renderNpcOverlay(n:NPC, color: Color): Unit = {
			parent.getModelOutlineRenderer.drawOutline(n, 2,  color, 4)
			val poly = n.getCanvasTilePoly
			if (poly != null) OverlayUtil.renderPolygon(g, poly, color)
//			val textLocation = n.getCanvasTextLocation(g, text, n.getLogicalHeight + zoffset)
//			if (textLocation != null) OverlayUtil.renderTextLocation(g, textLocation, text, color)
		}
		def renderNpcText(n: NPC, text: String, color: Color, zoffset: Int): Unit = {
			val textLocation = n.getCanvasTextLocation(g, text, n.getLogicalHeight + zoffset)
			if (textLocation != null)
				OverlayUtil.renderTextLocation(g, textLocation, text, color)
		}
		def renderTile(tile: WorldPoint, color: Color): Unit = {
			val poly = Perspective.getCanvasTilePoly(client, tile.getLocalPoint)
			if (poly != null) OverlayUtil.renderPolygon(g, poly, color)
		}

		Option(wyrdHelper).foreach(_.renderOverlay(renderNpcOverlay, renderNpcText, renderTile))
		Option(drakanHelper).foreach(_.renderOverlay())

		null.asInstanceOf[Dimension]
	}
}
