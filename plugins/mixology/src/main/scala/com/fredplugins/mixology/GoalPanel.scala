package com.fredplugins.mixology

import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.mixology.GoalPanel.BORDER_SIZE
import com.fredplugins.mixology.GoalPanel.COMPONENT_SPRITE_SIZE
import com.fredplugins.mixology.GoalPanel.DECIMAL_FORMAT
import com.fredplugins.mixology.GoalPanel.ICON_AND_GOAL_GAP
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
import net.runelite.client.ui.overlay.components.ComponentOrientation
import net.runelite.client.ui.overlay.components.ImageComponent
import net.runelite.client.ui.overlay.components.PanelComponent
import net.runelite.client.ui.overlay.components.ProgressBarComponent
import net.runelite.client.ui.overlay.components.SplitComponent
import net.runelite.client.util.ImageUtil
import net.runelite.client.util.QuantityFormatter
import org.slf4j.Logger

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.image.BufferedImage
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

	// Caching
	private var cachedRewardIcon: BufferedImage = null
	private var cachedRewardItem: RewardItem = null
	private val componentSprites = new Array[BufferedImage](PotionComponent.ENTRIES.length)

	topPanel.setBorder(TOP_PANEL_BORDER)
	topPanel.setBackgroundColor(null)
	override def render(graphics: Graphics2D): Dimension = {
		topPanel.getChildren.clear()

		plugin.getGoal.foreach(goal => {
			graphics.setFont(FontManager.getRunescapeSmallFont)

			val topLine = LineComponent.builder
				.left(goal.rewardItem.itemName)
				.leftFont(FontManager.getRunescapeFont)
				.right(s"${QuantityFormatter.quantityToStackSize(goal.itemsAffordable)}/${QuantityFormatter.quantityToStackSize(goal.rewardQty)}")
				.rightFont(FontManager.getRunescapeBoldFont)
				.build

			val bottomLine = LineComponent.builder
				.left("Progress: ")
				.leftFont(FontManager.getRunescapeFont)
				.right(s"${DECIMAL_FORMAT.format(goal.overallProgress * 100)}%")
				.rightFont(FontManager.getRunescapeFont)
				.rightColor( if(goal.overallProgress >= 1.0d) Color.GREEN else Color.WHITE )
				.build

			var textSplit = SplitComponent.builder
				.first(topLine)
				.second(bottomLine)
				.orientation(ComponentOrientation.VERTICAL)
				.build

			var rewardImageComponent = new ImageComponent(getRewardImage(goal.rewardItem))
			var topInfoSplit = SplitComponent.builder
				.first(rewardImageComponent)
				.second(textSplit)
				.orientation(ComponentOrientation.HORIZONTAL)
				.gap(new Point(ICON_AND_GOAL_GAP, 0))
				.build

			topPanel.getChildren().add(topInfoSplit)
			panelComponent.getChildren().add(topPanel)

			if(config.showResinBars()) {
				for(component <- PotionComponent.values()) {
					createProgressBar(goal, component)
				}
			}
		})

		super.render(graphics)
	}

	private def createProgressBar(goal: Goal, component: PotionComponent): Unit = {
		val data            = goal.componentMap(component)
		val componentSprite = getComponentSprite(component)
		if (componentSprite == null) return
		val imageComponent       = new ImageComponent(componentSprite)
		val progressBarComponent = new ProgressBarComponent
		progressBarComponent.setForegroundColor(component.color)
		progressBarComponent.setBackgroundColor(PROGRESS_BAR_BACKGROUND_COLOR)
		progressBarComponent.setValue(data.percentageToGoal * 100)
		progressBarComponent.setLeftLabel(QuantityFormatter.quantityToStackSize(data.curAmount))
		progressBarComponent.setRightLabel(QuantityFormatter.quantityToStackSize(data.goalAmount))
		val progressBarSplit = SplitComponent.builder.first(imageComponent).second(progressBarComponent).orientation(ComponentOrientation.HORIZONTAL).gap(new Point(ICON_AND_GOAL_GAP, 0)).build
		panelComponent.getChildren.add(progressBarSplit)
	}

	private def getRewardImage(rewardItem: RewardItem) = {
		if (cachedRewardItem != rewardItem || cachedRewardIcon == null) {
			cachedRewardItem = rewardItem
			cachedRewardIcon = itemManager.getImage(rewardItem.itemId)
		}
		cachedRewardIcon
	}

	private def getComponentSprite(component: PotionComponent): BufferedImage = {
		if (componentSprites(component.ordinal) == null) {
			componentSprites(component.ordinal) = createComponentSprite(component)
		}
		componentSprites(component.ordinal)
	}

	private def createComponentSprite(component: PotionComponent): BufferedImage = {
		val sprite = spriteManager.getSprite(component.spriteId, 0)
		if (sprite != null) {
			// Resize and center the sprite
			val resizedImage = ImageUtil.resizeImage(sprite, COMPONENT_SPRITE_SIZE, COMPONENT_SPRITE_SIZE, true)
			return ImageUtil.resizeCanvas(resizedImage, COMPONENT_SPRITE_SIZE, COMPONENT_SPRITE_SIZE)
		}
		null
	}
}
