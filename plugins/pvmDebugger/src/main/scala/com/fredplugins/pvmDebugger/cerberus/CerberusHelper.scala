package com.fredplugins.pvmDebugger.cerberus

import com.fredplugins.common.extensions.MenuExtensions.*
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.extensions.LocatableExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.pvmDebugger.HelperModule
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.WithOverlay
import com.fredplugins.pvmDebugger.WithPanel
import com.google.inject.Inject
import ethanApiPlugin.collections.Inventory
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.lucidplugins.api.utils.EquipmentUtils
import ethanApiPlugin.lucidplugins.api.utils.InteractionUtils
import ethanApiPlugin.lucidplugins.api.utils.InventoryUtils
import ethanApiPlugin.lucidplugins.api.utils.NpcUtils
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.Client
import net.runelite.api.Menu
import net.runelite.api.MenuAction
import net.runelite.api.MenuEntry
import net.runelite.api.NPC
import net.runelite.api.Prayer
import net.runelite.api.Skill
import net.runelite.api.events.AnimationChanged
import net.runelite.api.events.GameTick
import net.runelite.api.events.MenuEntryAdded
import net.runelite.api.events.NpcChanged
import net.runelite.api.events.NpcDespawned
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.PostMenuSort
import net.runelite.api.events.ProjectileMoved
import net.runelite.api.gameval.ItemID
import net.runelite.api.gameval.NpcID
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import net.runelite.client.util.ColorUtil
import org.slf4j.Logger

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Rectangle
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.chaining.scalaUtilChainingOps
import scala.util.Random

class CerberusHelper @Inject()(override val parent: PvmDebuggerPlugin, override val client: Client, override val config: CerberusConfig) extends HelperModule  with WithPanel with WithOverlay{
	private val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	override val moduleName: String = "CerberusHelper"
	given Client = client

	/**
		* Cerberus
	*/
	private val CERBERUS_ATTACKING: Int = 5862
	private val CERBERUS_SITTING: Int = 5863
	private val CERBERUS_RESETTING: Int = 5866

	/**
	 * Summoned Soul
	 */
	private val SPECTRE_RANGED: Int = 5867
	private val SPECTRE_MAGIC: Int = 5868
	private val SPECTRE_MELEE: Int = 5869

	private val REGION_IDS: Seq[Int] = List(4883, 5140, 5395)
	private val CERBERUS_IDS: Seq[Int] = List(CERBERUS_ATTACKING, CERBERUS_SITTING, CERBERUS_RESETTING)
	private val SUMMONED_SOUL_IDS: Seq[Int] = List(SPECTRE_RANGED, SPECTRE_MAGIC, SPECTRE_MELEE)

	var enabled: Boolean = false

	override def init(): Unit = {
		enabled = Option(client.getLocalPlayer).map(_.templateLocation.getRegionID).exists(x => REGION_IDS.contains(x))
	}

	override def cleanup(): Unit = {
		enabled = false
	}

	@Subscribe
	def onRegionChanged(e: LocalRegionChanged): Unit = {
		enabled = Option(client.getLocalPlayer).map(_.templateLocation.getRegionID).exists(x => REGION_IDS.contains(x))
	}

	@Subscribe
	def onProjectileMoved(e: ProjectileMoved): Unit = {
		if(enabled && e.getProjectile.justSpawned) {
			log.debug("[ProjectileSpawned] id={}, ticksRemaining={}, srcLoc={}, targLoc={}", e.getProjectile.getId, e.getProjectile.ticksRemaining, e.getProjectile.templateSourceLocation, e.getProjectile.templateTargetLocation)
		}
	}

	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		val npc = e.getNpc
		if(enabled && npc != null) {
			log.debug("[NpcSpawned] id={}, loc={}", npc.getId, npc.templateLocation)
		}
	}

	@Subscribe
	def onNpcChanged(e: NpcChanged): Unit = {
		val npc = e.getNpc
		if(enabled && npc != null) {
			log.debug("[NpcChanged] from={}, to={}, loc={}", e.getOld.getId, npc.getId, npc.templateLocation)
		}
	}

	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		val npc = e.getNpc
		if(enabled && npc != null) {
			log.debug("[NpcDespawned] id={}, loc={}", npc.getId, npc.templateLocation)
		}
	}

	@Subscribe
	def onGameTick(e: GameTick): Unit = {
	}

	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
		if(enabled) {
			val (npcId, animId) = Option(e.getActor).collect {
				case npc: NPC => (npc.getId, npc.getAnimation)
			}.getOrElse((-1, -1))
			log.debug("[AnimationChanged] npcId={}, npcAnim={}", npcId, animId)
		}
	}

	override protected def createPanelElements(): Seq[LayoutableRenderableEntity] = {
		if(enabled) {
			val cerbLines = NpcUtils.getNearestNpc(n => CERBERUS_IDS.contains(n.getId))
				.pipe(Option.apply)
				.map(cerb => {
					val titleComp = TitleComponent.builder()
						.text("Cerberus")
						.color(Color.RED)
						.build()
					val idComp = LineComponent.builder()
						.left("id")
						.leftColor(Color.CYAN)
						.right(s"${cerb.getId}")
						.rightColor(Color.GREEN)
						.build()

					val animComp = LineComponent.builder()
						.left("animation")
						.leftColor(Color.CYAN)
						.right(s"${cerb.getAnimation}")
						.rightColor(Color.GREEN)
						.build()

					val locComp = LineComponent.builder()
						.left("loc")
						.leftColor(Color.CYAN)
						.right(s"${cerb.templateLocation}")
						.rightColor(Color.GREEN)
						.build()
					Seq(titleComp, idComp, animComp, locComp)
				}).getOrElse(Seq.empty[LayoutableRenderableEntity])

			val soulsLines = NpcUtils.getAll(n => SUMMONED_SOUL_IDS.contains(n.getId)).asScala.toList
				.sortBy(_.templateLocation.getX)
				.map(soul => {
					(soul.getId match {
						case SPECTRE_MELEE => ("Melee", Color.RED)
						case SPECTRE_MAGIC => ("Magic", Color.BLUE)
						case SPECTRE_RANGED => ("Range", Color.GREEN)
					}).pipe((txt, color) => {
						TitleComponent.builder()
							.text(txt)
							.color(color)
							.build()
					})
				}).prepended(
					TitleComponent.builder()
						.text("Souls")
						.color(Color.MAGENTA)
						.build()
				)
			cerbLines :++ soulsLines
		} else {
			Seq.empty[LayoutableRenderableEntity]
		}
	}

	override def renderOverlay(g: Graphics2D): Dimension = {
		def renderNpcOverlay(npc: NPC, text: String, zoffset: Int, color: Color, fillAlpha: Int, outlineWidth: Int = 4, textColor: Color = Color.WHITE): Unit = {
			var poly = npc.getConvexHull
			if (poly != null) OverlayUtil.renderPolygon(g, poly, Color(0, 0, 0, 0), color.withAlpha(fillAlpha))
			if (outlineWidth > 0) parent.getModelOutlineRenderer.drawOutline(npc, outlineWidth, color, 2)
			//			val poly = npc.getCanvasTilePoly
			//			if (poly != null) OverlayUtil.renderPolygon(g, poly, fillColor)
			if (text != null && text.nonEmpty) {
				val textLocation = npc.getCanvasTextLocation(g, text, npc.getLogicalHeight + zoffset)
				if (textLocation != null) {
					val textBounds = g.getFontMetrics.getStringBounds(text, g)
					val offset     = 4
					val textArea   = Rectangle(
						textBounds.getX.toInt - offset, textBounds.getY.toInt - offset, textBounds.getWidth.toInt + offset + offset,
						textBounds.getHeight.toInt + offset + offset)
					//				val textArea = new Rectangle(textLocation.getX + (textBounds.getWidth / 2.0).toInt - , textLocation.getY, textBounds.getWidth.toInt, textBounds.getHeight.toInt)
					OverlayUtil.renderPolygon(
						g, textArea, new Color(
							255 - textColor.getRed, 255 - textColor.getGreen, 255 - textColor.getBlue, fillAlpha))
					OverlayUtil.renderTextLocation(g, textLocation, text, textColor)
				}
			}
		}
		NpcUtils.getAllNpcs(
				CERBERUS_ATTACKING,
				CERBERUS_SITTING,
				CERBERUS_RESETTING,
				SPECTRE_RANGED,
				SPECTRE_MAGIC,
				SPECTRE_MELEE
			).asScala.toList
			.map(n => {
				(n.getId match {
					case CERBERUS_ATTACKING => ("Attacking", Color.ORANGE)
					case CERBERUS_SITTING => ("Sitting", Color.CYAN)
					case CERBERUS_RESETTING => ("Resetting", Color.YELLOW)
					case SPECTRE_RANGED => ("Ranged", Color.GREEN)
					case SPECTRE_MAGIC => ("Magic", Color.BLUE)
					case SPECTRE_MELEE => ("Melee", Color.RED)
				}).pipe((txt, color) => (n, txt, color))
			})
			.foreach((npc, txt, color) => {
				renderNpcOverlay(npc, txt, 0, color, 150)
			})

		null.asInstanceOf[Dimension]
	}
}
