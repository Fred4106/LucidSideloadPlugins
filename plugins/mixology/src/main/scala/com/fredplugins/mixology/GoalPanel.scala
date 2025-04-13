package com.fredplugins.mixology

import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.mixology.GoalPanel.BORDER_SIZE
import com.fredplugins.mixology.GoalPanel.DECIMAL_FORMAT
import com.fredplugins.mixology.GoalPanel.PROGRESS_BAR_BACKGROUND_COLOR
import com.fredplugins.mixology.GoalPanel.TOP_PANEL_BORDER
import com.fredplugins.mixology.GoalPanel.VERTICAL_GAP
import com.google.inject.Inject
import com.google.inject.Singleton
import ethanApiPlugin.collections.TileObjects
import net.runelite.api.coords.WorldPoint
import net.runelite.api.Client
import net.runelite.api.GameObject
import net.runelite.api.TileObject
import net.runelite.client.game.ItemManager
import net.runelite.client.game.SpriteManager
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import net.runelite.client.ui.overlay.OverlayLayer
import net.runelite.client.ui.overlay.OverlayPanel
import net.runelite.client.ui.overlay.OverlayPosition
import net.runelite.client.ui.overlay.components.PanelComponent
import net.runelite.client.ui.overlay.components.ProgressBarComponent
import net.runelite.client.ui.overlay.components.SplitComponent
import net.runelite.client.util.QuantityFormatter
import org.slf4j.Logger

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.text.DecimalFormat
import javax.annotation.concurrent.Immutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.jdk.Accumulator
import scala.jdk.IntAccumulator
import scala.runtime.BoxedUnit
import scala.util.chaining.*
import scala.util.Random
import scala.util.Try
object GoalPanel {
	private val DECIMAL_FORMAT                = new DecimalFormat("0.0")
	private val BORDER_SIZE                   = 3
	private val VERTICAL_GAP                  = 2
	private val ICON_AND_GOAL_GAP             = 5
	private val TOP_PANEL_BORDER              = new Rectangle(2, 0, 4, 4)
	private val COMPONENT_SPRITE_SIZE         = 16
	private val PROGRESS_BAR_BACKGROUND_COLOR = new Color(61, 56, 49)
}
@Singleton
class GoalPanel @Inject()(plugin: FredsMixologyPlugin, config : FredsMixologyConfig, itemManager: ItemManager, spriteManager: SpriteManager) extends OverlayPanel(plugin) {
	val log: Logger = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	setPosition(OverlayPosition.TOP_RIGHT)
	setPriority(Overlay.PRIORITY_MED)
	panelComponent.setPreferredSize(new Dimension(250, 0))
	panelComponent.setBorder(new Rectangle(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE))
	panelComponent.setGap(new Point(0, VERTICAL_GAP))

	private val topPanel = new PanelComponent()

	topPanel.setBorder(TOP_PANEL_BORDER)
	topPanel.setBackgroundColor(null)
	override def render(graphics: Graphics2D): Dimension = {
		topPanel.getChildren.clear()
		graphics.setFont(FontManager.getRunescapeSmallFont)

		// Build the bottom line with the overall progress percentage
		val bottomLine = LineComponent.builder()
			.left("Progress:")
			.leftFont(FontManager.getRunescapeSmallFont)
			.right(DECIMAL_FORMAT.format(0.10d * 100) + "%")
			.rightFont(FontManager.getRunescapeFont)
			.rightColor(if (0.10d >= 1) Color.GREEN else Color.WHITE)
			.build()

//		val textSplit = SplitComponent().builder.first(topLine).second(bottomLine).orientation(ComponentOrientation
//			.VERTICAL).build

//		val rewardImageComponent = new Nothing(getRewardImage(rewardItem))
//		val topInfoSplit         = SplitComponent.builder.first(rewardImageComponent).second(textSplit)
//			.orientation(ComponentOrientation.HORIZONTAL).gap(new Dimension(ICON_AND_GOAL_GAP, 0)).build

		topPanel.getChildren.add(bottomLine)
		panelComponent.getChildren.add(topPanel)

		// Add a progress bar for each component
		if (config.showResinBars()) {
//			import scala.collection.JavaConversions._
			Seq(
				SMixType.Mox -> config.goalMox(),
				SMixType.Aga -> config.goalAga(),
				SMixType.Lye -> config.goalLye()
			).foreach {
				case (tpe, goal) => {
					val points               = plugin.getResin(tpe)
					val progressBarComponent = new ProgressBarComponent()
//					progressBarComponent.setMaximum(goal)
					progressBarComponent.setForegroundColor(tpe.color)
					progressBarComponent.setBackgroundColor(PROGRESS_BAR_BACKGROUND_COLOR)
					progressBarComponent.setValue((points.doubleValue/goal.doubleValue) * 100)
					progressBarComponent.setLeftLabel(QuantityFormatter.quantityToStackSize(points))
					progressBarComponent.setRightLabel(QuantityFormatter.quantityToStackSize(goal))
					panelComponent.getChildren.add(progressBarComponent)
				}
			}
//			for (component <- PotionComponent.ENTRIES) {
//				createProgressBar(goal, component)
//			}
		}

		super.render(graphics)
	}

	//	type LineData = (String, String) | String
//	private def lines: Seq[LineData] = {
//		val mixCodeLine: LineData = "Mix" -> TileObjects.search().withId(55392, 55393, 55394).result().asScala.toList.sortBy(_.getId).flatMap {
//			to => SMixType.fromPedestal(to)
//		}.foldLeft("")((j, j2) => s"${j}${j2.letter}")
//
////		val benchesLines: Seq[LineData] = plugin.state.toolBenches.flatMap((pt, tpl) => tpl._2.map(pt -> _)).map[LineData] {
////			case (processesType, brew) => s"$processesType" -> s"$brew"
////		}.prepended("Toolbenches")
//		val benchesLines: Seq[LineData] = List(SProcessType.Alembic -> plugin.alembicPotionType, SProcessType.Agitator -> plugin.agitatorPotionType, SProcessType.Retort -> plugin.retortPotionType).map {
//			case (z, zz) => (s"${z}", zz.map(zzz => s"${zzz}").getOrElse("Empty"))
//		}.map[LineData] {
//			case (processesTypeStr, brewStr) => processesTypeStr -> brewStr
//		}.prepended("Toolbenches")
//		val ordersLines: Seq[LineData] = plugin.potionOrders.pipe(x => List(x._1, x._2, x._3).zipWithIndex).map[LineData] {
//			case ((pt, br), idx) => s"Order ${idx + 1}" -> s"${pt} ${br}"
////			case (processType, brew) =>
////			case (, idx) => s"Order ${idx + 1}" -> s"${pt} ${br}"
//		}.prepended("Orders")
//		val inventoryLines: Seq[LineData] = plugin.inventorySnapshot//.filter(j => SBrew.values.exists(b => b.unprocessedId == j._2 || b.processedId == j._2))
//			.map[LineData] {
////				case (idx, id, qty) if SBrew.values.exists(b => b.unprocessedId == id) => s"${idx}" -> s"Unprocessed(${SBrew.values.find(b => b.unprocessedId == id).get})"
////				case (idx, id, qty) if SBrew.values.exists(b => b.processedId == id) => s"${idx}" -> s"Processed(${SBrew.values.find(b => b.processedId == id).get}, ${"Unknown"})"
//				case (idx, id, qty) => s"${idx}" -> s"(${id}, ${qty})"
//			}.prepended("Inventory")
//
//		val debugLines: Seq[LineData] = List(
//			SProcessType.Retort -> (plugin.previousRetortProgess, if(plugin.previousRetortProgess < 17 && plugin.previousRetortProgess > 0) 1 else 0),
//			SProcessType.Agitator -> (plugin.previousAgitatorProgess, plugin.agitatorQuickActionTicks),
//			SProcessType.Alembic -> (plugin.previousAlembicProgress, plugin.alembicQuickActionTicks),
//		)
//			.map[LineData] {
//				case (processType, (prog, ticks)) => s"${processType}" -> s"${prog} | ${ticks}"
//			}.prepended("debug")
////		log.info("benchesLines: {}", benchesLines)
//		List[LineData | Seq[LineData]](mixCodeLine, benchesLines, ordersLines, inventoryLines, debugLines).flatMap {
//			case a: LineData => Seq(a)
//			case b: Seq[LineData] => b
//		}
//	}
//	override def render(graphics: Graphics2D): Dimension = {
////		if (plugin.inLab) {
//		List[LayoutableRenderableEntity|Seq[LayoutableRenderableEntity]](
//			TitleComponent.builder.text("Mixology").color(Color.GREEN).build,
//			lines.map {
//					case (left, right) => LineComponent.builder.left(left).right(right).build
//					case line: String => TitleComponent.builder.text(line).build
//				}
//		).flatMap {
//			case x: LayoutableRenderableEntity => List(x)
//			case x: Seq[LayoutableRenderableEntity] => x
//		}.foreach(u => panelComponent.getChildren.add(u))
////		}
//		super.render(graphics)
//	}
}
