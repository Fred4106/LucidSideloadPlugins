package com.fredplugins.mixology;

import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.ProgressBarComponent;
import net.runelite.client.ui.overlay.components.SplitComponent;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

class GoalInfoBoxOverlay extends OverlayPanel {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");
    private static final int BORDER_SIZE = 3;
    private static final int VERTICAL_GAP = 2;
    private static final int ICON_AND_GOAL_GAP = 5;
    private static final Rectangle TOP_PANEL_BORDER = new Rectangle(2, 0, 4, 4);
    private static final int COMPONENT_SPRITE_SIZE = 16;
    private static final Color PROGRESS_BAR_BACKGROUND_COLOR = new Color(61, 56, 49);

    private final FredsMixologyPlugin plugin;
    private final FredsMixologyConfig config;
    private final ItemManager itemManager;
    private final SpriteManager spriteManager;

    private final PanelComponent topPanel = new PanelComponent();

    // Caching
    private BufferedImage cachedRewardIcon;
    private RewardItem cachedRewardItem;

    private final BufferedImage[] componentSprites = new BufferedImage[PotionComponent.ENTRIES.length];

    @Inject
    GoalInfoBoxOverlay(
            FredsMixologyPlugin plugin,
            FredsMixologyConfig config,
            ItemManager itemManager,
            SpriteManager spriteManager
    ) {
        super(plugin);
        this.plugin = plugin;
        this.config = config;
        this.itemManager = itemManager;
        this.spriteManager = spriteManager;

        setPosition(OverlayPosition.TOP_RIGHT);
        setPriority(PRIORITY_MED);
        panelComponent.setPreferredSize(new Dimension(250, 0));
        panelComponent.setBorder(new Rectangle(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
        panelComponent.setGap(new Point(0, VERTICAL_GAP));

        topPanel.setBorder(TOP_PANEL_BORDER);
        topPanel.setBackgroundColor(null);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        var goal = plugin.getGoal();
        var rewardItem = goal.getRewardItem();

        if (rewardItem == RewardItem.NONE || !plugin.isInLabRegion()) {
            return null;
        }
        topPanel.getChildren().clear();
        graphics.setFont(FontManager.getRunescapeSmallFont());

        // Build the top display with the affordable amount / goal amount
        String goalAmountText = "";
        if (rewardItem.isRepeatable() && goal.getRewardQuantity() > 1) {
            goalAmountText = QuantityFormatter.quantityToStackSize(goal.getItemsAffordable())
                    + "/" + QuantityFormatter.quantityToStackSize(goal.getRewardQuantity());
        }

        var topLine = LineComponent.builder()
                                   .left(rewardItem.itemName())
                                   .leftFont(FontManager.getRunescapeFont())
                                   .right(goalAmountText)
                                   .rightFont(FontManager.getRunescapeBoldFont())
                                   .build();

        // Build the bottom line with the overall progress percentage
        var bottomLine = LineComponent.builder()
                                      .left("Progress:")
                                      .leftFont(FontManager.getRunescapeFont())
                                      .right(DECIMAL_FORMAT.format(goal.getOverallProgress() * 100) + "%")
                                      .rightFont(FontManager.getRunescapeFont())
                                      .rightColor(goal.getOverallProgress() >= 1 ? Color.GREEN : Color.WHITE)
                                      .build();

        var textSplit = SplitComponent.builder()
                                      .first(topLine)
                                      .second(bottomLine)
                                      .orientation(ComponentOrientation.VERTICAL)
                                      .build();

        var rewardImageComponent = new ImageComponent(getRewardImage(rewardItem));
        var topInfoSplit = SplitComponent.builder()
                                         .first(rewardImageComponent)
                                         .second(textSplit)
                                         .orientation(ComponentOrientation.HORIZONTAL)
                                         .gap(new Point(ICON_AND_GOAL_GAP, 0))
                                         .build();

        topPanel.getChildren().add(topInfoSplit);
        panelComponent.getChildren().add(topPanel);

        // Add a progress bar for each component
        if (config.showResinBars()) {
            for (var component : PotionComponent.ENTRIES) {
                createProgressBar(goal, component);
            }
        }

        return super.render(graphics);
    }

    private void createProgressBar(Goal goal, PotionComponent component) {
        var data = goal.getComponentData(component);
        var componentSprite = getComponentSprite(component);

        if (componentSprite == null) {
            return;
        }
        var imageComponent = new ImageComponent(componentSprite);
        var progressBarComponent = new ProgressBarComponent();

        progressBarComponent.setForegroundColor(component.color());
        progressBarComponent.setBackgroundColor(PROGRESS_BAR_BACKGROUND_COLOR);
        progressBarComponent.setValue(data.percentageToGoal * 100);
        progressBarComponent.setLeftLabel(QuantityFormatter.quantityToStackSize(data.currentAmount));
        progressBarComponent.setRightLabel(QuantityFormatter.quantityToStackSize(data.goalAmount));

        var progressBarSplit = SplitComponent.builder()
                                             .first(imageComponent)
                                             .second(progressBarComponent)
                                             .orientation(ComponentOrientation.HORIZONTAL)
                                             .gap(new Point(ICON_AND_GOAL_GAP, 0))
                                             .build();

        panelComponent.getChildren().add(progressBarSplit);
    }

    private BufferedImage getRewardImage(RewardItem rewardItem) {
        if (cachedRewardItem != rewardItem) {
            cachedRewardItem = rewardItem;
            cachedRewardIcon = itemManager.getImage(rewardItem.itemId());
        }
        return cachedRewardIcon;
    }

    private BufferedImage getComponentSprite(PotionComponent component) {
        var sprite = componentSprites[component.ordinal()];

        if (sprite == null) {
            sprite = componentSprites[component.ordinal()] = createComponentSprite(component);
        }
        return sprite;
    }

    private BufferedImage createComponentSprite(PotionComponent component) {
        var sprite = spriteManager.getSprite(component.spriteId(), 0);
        if (sprite != null) {
            // Resize and center the sprite
            var resizedImage = ImageUtil.resizeImage(sprite, COMPONENT_SPRITE_SIZE, COMPONENT_SPRITE_SIZE, true);
            return ImageUtil.resizeCanvas(resizedImage, COMPONENT_SPRITE_SIZE, COMPONENT_SPRITE_SIZE);
        }
        return null;
    }
}
