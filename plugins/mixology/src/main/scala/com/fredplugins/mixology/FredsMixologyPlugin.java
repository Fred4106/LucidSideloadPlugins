package com.fredplugins.mixology;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import ethanApiPlugin.EthanApiPlugin;
import net.runelite.api.Client;
import net.runelite.api.FontID;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.Color;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fredplugins.mixology.AlchemyObject.AGA_LEVER;
import static com.fredplugins.mixology.AlchemyObject.DIGWEED_NORTH_EAST;
import static com.fredplugins.mixology.AlchemyObject.DIGWEED_NORTH_WEST;
import static com.fredplugins.mixology.AlchemyObject.DIGWEED_SOUTH_EAST;
import static com.fredplugins.mixology.AlchemyObject.DIGWEED_SOUTH_WEST;
import static com.fredplugins.mixology.AlchemyObject.LYE_LEVER;
import static com.fredplugins.mixology.AlchemyObject.MOX_LEVER;
import static com.fredplugins.mixology.PotionComponent.AGA;
import static com.fredplugins.mixology.PotionComponent.LYE;
import static com.fredplugins.mixology.PotionComponent.MOX;

@PluginDescriptor(
    name = "<html><font color=\"#32C8CD\">Freds</font> Mixology</html>",
    description = "Augments the mastering mixology interface to show recipes and other helpful addons.",
    tags = {"herblore","herb","minigame","herbtodt","herbloretodt","mastering","mixology"},
    conflicts = {"Mastering Mixology"}
)
@PluginDependency(EthanApiPlugin.class)
@Singleton
public class FredsMixologyPlugin extends Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(FredsMixologyPlugin.class);

    private static final int PROC_MASTERING_MIXOLOGY_BUILD_POTION_ORDERS = 7063;
    private static final int PROC_MASTERING_MIXOLOGY_BUILD_REAGENTS = 7064;

    private static final int VARBIT_POTION_ORDER_1 = VarbitID.MM_LAB_ORDER_1_TYPE;
    private static final int VARBIT_POTION_MODIFIER_1 = VarbitID.MM_LAB_ORDER_1_MODIFIER;
    private static final int VARBIT_POTION_ORDER_2 = VarbitID.MM_LAB_ORDER_2_TYPE;
    private static final int VARBIT_POTION_MODIFIER_2 = VarbitID.MM_LAB_ORDER_2_MODIFIER;
    private static final int VARBIT_POTION_ORDER_3 = VarbitID.MM_LAB_ORDER_3_TYPE;
    private static final int VARBIT_POTION_MODIFIER_3 = VarbitID.MM_LAB_ORDER_3_MODIFIER;
    private static final int VARBIT_ALEMBIC_PROGRESS = VarbitID.MM_ALEMBIC_PROGRESS;
    private static final int VARBIT_AGITATOR_PROGRESS = VarbitID.MM_AGITATOR_PROGRESS;

    private static final int VARBIT_AGITATOR_QUICKACTION = VarbitID.MM_LAB_HIT_SKILLSHOT_AGITATOR;
    private static final int VARBIT_ALEMBIC_QUICKACTION = VarbitID.MM_LAB_HIT_SKILLSHOT_ALEMBIC;

    private static final int VARBIT_MIXING_VESSEL_POTION = VarbitID.MM_LAB_VESSEL_READY;
    private static final int VARBIT_RETORT_POTION = VarbitID.MM_LAB_RETORT_POTION;
    
    private static final int SPOT_ANIM_AGITATOR = SpotanimID.VFX_MACHINERY_ALCHEMY01_AGITATOR01;
    private static final int SPOT_ANIM_ALEMBIC = SpotanimID.VFX_MACHINERY_ALCHEMY01_ALEMBIC01;

    private static final int COMPONENT_POTION_ORDERS_GROUP_ID = InterfaceID.MM_OVERLAY;
    private static final int COMPONENT_POTION_ORDERS = InterfaceID.MmOverlay.CONTENT;

    private static final int LABS_REGION_ID = 5521;
    private static final int LABS_REGION_PLANE = 0;

    private static final int FOUND_GEM = 2655;

    @Inject
    private Client client;

    @Inject
    private FredsMixologyConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private Notifier notifier;

    @Inject
    private ClientThread clientThread;

    @Inject
    private FredsMixologyOverlay overlay;

    @Inject
    private InventoryPotionOverlay potionOverlay;

    @Inject
    private GoalInfoBoxOverlay goalInfoBoxOverlay;

    private List<PotionOrder> __potionOrders = Collections.emptyList();
    public List<PotionOrder> potionOrders() {
        return __potionOrders.stream().collect(Collectors.toUnmodifiableList());
    }

    private final Goal goal = new Goal(RewardItem.NONE);

    public Map<AlchemyObject, HighlightedObject> highlightedObjects() {
        return Optional.ofNullable(stateData).map(MixologyStateData::highlightedObjectsJava).orElse(java.util.Map.<AlchemyObject, HighlightedObject>of());
    }

    public boolean isInLab() {
        return stateData.inLab();
    }

    /**
     * @return true if the player is in the labs region (the area where the minigame takes place)
     * the isInlab method only checks if they are inside the actual lab room where the UI is active
     */
    public boolean isInLabRegion() {
        Player player = client.getLocalPlayer();
        return player != null && player.getWorldLocation().getRegionID() == LABS_REGION_ID
                && player.getWorldLocation().getPlane() == LABS_REGION_PLANE;
    }

    @Provides
    FredsMixologyConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FredsMixologyConfig.class);
    }

    private MixologyWidgetTool widgetTool = null;
    private MixologyStateData stateData = null;

    @Override
    protected void startUp() {
        stateData = new MixologyStateData(client, clientThread, config);
        if(widgetTool == null) widgetTool = new MixologyWidgetTool(client, clientThread, config);

        overlayManager.add(overlay);
        overlayManager.add(potionOverlay);
        overlayManager.add(goalInfoBoxOverlay);

        if (client.getGameState() == GameState.LOGGED_IN) {
            clientThread.invokeLater(this::initialize);
        }
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        overlayManager.remove(potionOverlay);
        overlayManager.remove(goalInfoBoxOverlay);
//        inLab = false;
        stateData = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING) {
            stateData.writer().clearHighlightObject();
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() != COMPONENT_POTION_ORDERS_GROUP_ID) {
            return;
        }
        initialize();
    }

    @Subscribe
    public void onWidgetClosed(WidgetClosed event) {
        if (event.getGroupId() != COMPONENT_POTION_ORDERS_GROUP_ID) {
            return;
        }

        stateData.writer().clearHighlightObject();
        stateData.writer().inLab_$eq(false);
//        inLab = false;
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals(FredsMixologyConfig.CONFIG_GROUP)) {
            return;
        }

        if (event.getKey().equals("potionOrderSorting")) {
            clientThread.invokeLater(this::updatePotionOrders);
        }

        if (event.getKey().equals("highlightStations")) {
            if (!config.highlightStations()) {
                stateData.writer().unHighlightAllStations();
            } else {
                clientThread.invokeLater(this::tryHighlightNextStation);
            }
        }

        if (event.getKey().equals("displayResin")) {
            // Trigger the potion order update to refresh the resin display
            clientThread.invokeLater(this::triggerPotionOrderUpdate);
        }

        if (event.getKey().equals("highlightDigweed")) {
            if(!config.highlightDigWeed()) {
                stateData.writer().unHighlightDigweeds();
            }
        }

        if (event.getKey().equals("selectedReward") || event.getKey().equals("rewardQuantity") || event.getKey().equals("showResinBars")) {
            recalculateGoalData();
        }


        if (event.getKey().equals("highlightLevers")) {
            stateData.writer().unHighlightLevers();
            stateData.writer().highlightLevers();
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (!isInLab() || !config.highlightStations() || event.getContainerId() != InventoryID.INV) {
            return;
        }
        // Do not update the highlight if there's a potion in a station
        if (stateData.alembicPotionType().isDefined() || stateData.agitatorPotionType().isDefined() || stateData.retortPotionType().isDefined()) {
            return;
        }
        var inventory = event.getItemContainer();

        // Find the first potion item and highlight its station
        for (var item : inventory.getItems()) {
            var potionType = PotionType.fromItemId(item.getId());

            if (potionType == null || potionType.modifiedItemId() == item.getId()) {
                continue;
            }
            for (var order : potionOrders()) {
                if (order.potionType() == potionType && !order.fulfilled()) {
                    stateData.writer().unHighlightAllStations();
                    stateData.writer().highlightStation(order.potionModifier().alchemyObject());
                    return;
                }
            }
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        var varbitId = event.getVarbitId();
        var varpId = event.getVarpId();
        var value = event.getValue();

        // Whenever a potion is delivered, all the potion order related varbits are reset to 0 first then
        // set to the new values. We can use this to clear all the stations.
        if (varbitId == VarbitID.MM_LAB_ORDER_1_TYPE) {
            if (value == 0) {
                stateData.writer().unHighlightAllStations();
            } else {
                clientThread.invokeAtTickEnd(this::updatePotionOrders);
            }
        } else if (varbitId == VarbitID.MM_LAB_ALEMBIC_POTION) {
            if (value == 0) {
                // Finished crystalising
                stateData.writer().unHighlightObject(AlchemyObject.ALEMBIC);
                tryFulfillOrder(stateData.alembicPotionTypeJava(), PotionModifier.CRYSTALISED);
                tryHighlightNextStation();
                LOGGER.debug("Finished crystalising {}", stateData.alembicPotionTypeJava());
                stateData.writer().alembicPotionType_$eq(null);
            } else {
                stateData.writer().alembicPotionType_$eq(PotionType.fromIdx(value - 1));
                LOGGER.debug("Alembic potion type: {}", stateData.alembicPotionTypeJava());
            }
        } else if (varbitId == VarbitID.MM_LAB_AGITATOR_POTION) {
            if (value == 0) {
                // Finished homogenising
                stateData.writer().unHighlightObject(AlchemyObject.AGITATOR);
                tryFulfillOrder(stateData.agitatorPotionTypeJava(), PotionModifier.HOMOGENOUS);
                tryHighlightNextStation();
                LOGGER.debug("Finished homogenising {}", stateData.agitatorPotionTypeJava());
                stateData.writer().agitatorPotionType_$eq(null);
            } else {
                stateData.writer().agitatorPotionType_$eq(PotionType.fromIdx(value - 1));
                LOGGER.debug("Agitator potion type: {}", stateData.agitatorPotionTypeJava());
            }
        } else if (varbitId == VarbitID.MM_LAB_RETORT_POTION) {
            if (value == 0) {
                // Finished concentrating
                stateData.writer().unHighlightObject(AlchemyObject.RETORT);
                tryFulfillOrder(stateData.retortPotionTypeJava(), PotionModifier.CONCENTRATED);
                tryHighlightNextStation();
                LOGGER.debug("Finished concentrating {}", stateData.retortPotionTypeJava());
                stateData.writer().retortPotionType_$eq(null);
            } else {
                stateData.writer().retortPotionType_$eq(PotionType.fromIdx(value - 1));
                LOGGER.debug("Retort potion type: {}", stateData.retortPotionTypeJava());
            }
        } else if (varbitId == VarbitID.MM_AGITATOR_PROGRESS) {
            stateData.writer().handleAgitatorProgress(value);
        } else if (varbitId == VarbitID.MM_ALEMBIC_PROGRESS) {
            stateData.writer().handleAlembicProgress(value);
        } else if (varbitId == VarbitID.MM_LAB_HIT_SKILLSHOT_AGITATOR) {
            // agitator quick action was just successfully popped
            stateData.writer().highlightStation(AlchemyObject.AGITATOR);
        } else if (varbitId == VarbitID.MM_LAB_HIT_SKILLSHOT_ALEMBIC) {
            // alembic quick action was just successfully popped
            stateData.writer().highlightStation(AlchemyObject.ALEMBIC);
        } else if (varpId >= VarPlayerID.MIXOLOGY_LYE_POINTS && varpId <= VarPlayerID.MIXOLOGY_MOX_POINTS) {
            recalculateGoalData();
        } else if (varbitId >= VarbitID.MM_HERB_READY_1 && varbitId <= VarbitID.MM_HERB_READY_4) {
            AlchemyObject weed = AlchemyObject.values()[(DIGWEED_NORTH_EAST.ordinal() + (varbitId- VarbitID.MM_HERB_READY_1))];
            stateData.writer().toggleDigweed(weed,value == 1);
            if (value == 1) notifier.notify(config.notifyDigWeed(), "A digweed has spawned.");
        }
    }

    @Subscribe
    public void onSoundEffectPlayed(SoundEffectPlayed event) {
        if (isInLab() && stateData.alembicPotionType().isDefined() && event.getSoundId() == FOUND_GEM && event.getDelay() > 0 && config.soundEffectAlembic()) {
            LOGGER.debug("client found_gem sound effect detected during Alembic, blocking");
            event.consume();
        }
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        var spotAnimId = event.getGraphicsObject().getId();

        if (!config.highlightQuickActionEvents()) {
            return;
        }
        if (spotAnimId == SPOT_ANIM_ALEMBIC && stateData.alembicPotionType().isDefined()) {
            stateData.writer().highlightObject(AlchemyObject.ALEMBIC, config.stationQuickActionHighlightColor());

            if (config.soundEffectAlembic()) {
                LOGGER.debug("Playing manual found_gem sound effect");
                client.playSoundEffect(FOUND_GEM);
            }

            // start counting ticks for alembic so we know to un-highlight on the next alembic varbit update
            // note this quick action has a 1 tick window, so we use an int that goes 0 -> 1 -> unhighlight
            stateData.writer().alembicQuickActionTicks_$eq(1);
        }

        if (spotAnimId == SPOT_ANIM_AGITATOR && stateData.agitatorPotionType().isDefined()) {
            stateData.writer().highlightObject(AlchemyObject.AGITATOR, config.stationQuickActionHighlightColor());
            // start counting ticks for agitator so we know to un-highlight on the next agitator varbit update
            // note this quick action has a 2-tick window, so we use an int that goes 0 -> 1 -> 2 -> unhighlight
            stateData.writer().agitatorQuickActionTicks_$eq(1);
        }
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event) {
        var scriptId = event.getScriptId();
        if (scriptId != PROC_MASTERING_MIXOLOGY_BUILD_POTION_ORDERS && scriptId != PROC_MASTERING_MIXOLOGY_BUILD_REAGENTS) {
            return;
        }
        var baseWidget = client.getWidget(COMPONENT_POTION_ORDERS);

        if (baseWidget == null) {
            return;
        }
        if (scriptId == PROC_MASTERING_MIXOLOGY_BUILD_POTION_ORDERS) {
            widgetTool.updatePotionOrdersComponent(baseWidget, potionOrders());
        } else {
            widgetTool.appendResins(baseWidget);
        }
    }

    private void initialize() {
        var ordersLayer = client.getWidget(COMPONENT_POTION_ORDERS_GROUP_ID, 0);
        if (ordersLayer == null || ordersLayer.isSelfHidden()) {
            return;
        }

        LOGGER.debug("initialize plugin");
        stateData.writer().inLab_$eq(true);
        updatePotionOrders();
        stateData.writer().highlightLevers();
        tryHighlightNextStation();
    }

    private String stringify(List<PotionOrder> l) {
        return l.stream().map(PotionOrder::toString).collect(Collectors.joining(", ", "{", "}"));
    }
    private void updatePotionOrders() {
        var newOrders = Stream.of(0, 1, 2)
            .map(this::createPotionOrder)
            .sorted(config.potionOrderSorting().comparator())
            .collect(Collectors.toUnmodifiableList());
        if(!newOrders.stream().allMatch(u -> potionOrders().stream().anyMatch(u::equals))) {
            LOGGER.debug("Updating potion orders to from\n\t   {}\n\tto {}", stringify(potionOrders()),stringify(newOrders));
            __potionOrders = newOrders;
            triggerPotionOrderUpdate();
        }
    }

    public void triggerPotionOrderUpdate() {
        // Trigger a fake varbit update to force run the clientscript proc
        var varbitType = client.getVarbit(VARBIT_POTION_ORDER_1);
        if (varbitType != null) {
            client.queueChangedVarp(varbitType.getIndex());
        }
    }

    private void tryFulfillOrder(PotionType potionType, PotionModifier modifier) {
        for (var order : potionOrders()) {
            if (order.potionType() == potionType && order.potionModifier() == modifier && !order.fulfilled()) {
                LOGGER.debug("Order {} has been fulfilled", order);
                order.setFulfilled(true);
                break;
            }
        }
    }

    private void tryHighlightNextStation() {
        if (!config.highlightStations()) {
            return;
        }
        var inventory = client.getItemContainer(InventoryID.INV);

        if (inventory == null) {
            return;
        }

        for (var order : potionOrders()) {
            if (order.fulfilled()) {
                continue;
            }
            if (inventory.contains(order.potionType().itemId())) {
                LOGGER.debug("Highlighting station for order {}", order);
                stateData.writer().highlightStation(order.potionModifier().alchemyObject());
                break;
            }
        }
    }

    PotionOrder createPotionOrder(int idx) {
        assert(idx >= 0 && idx < 3);
        return new PotionOrder(idx, getPotionType(idx), getPotionModifier(idx));
    }

    private PotionType getPotionType(int orderIdx) {
        if (orderIdx == 0) {
            return PotionType.fromIdx(client.getVarbitValue(VARBIT_POTION_ORDER_1) - 1);
        } else if (orderIdx == 1) {
            return PotionType.fromIdx(client.getVarbitValue(VARBIT_POTION_ORDER_2) - 1);
        } else if (orderIdx == 2) {
            return PotionType.fromIdx(client.getVarbitValue(VARBIT_POTION_ORDER_3) - 1);
        } else {
            return null;
        }
    }

    private PotionModifier getPotionModifier(int orderIdx) {
        if (orderIdx == 0) {
            return PotionModifier.from(client.getVarbitValue(VARBIT_POTION_MODIFIER_1) - 1);
        } else if (orderIdx == 1) {
            return PotionModifier.from(client.getVarbitValue(VARBIT_POTION_MODIFIER_2) - 1);
        } else if (orderIdx == 2) {
            return PotionModifier.from(client.getVarbitValue(VARBIT_POTION_MODIFIER_3) - 1);
        } else {
            return null;
        }
    }

    public Goal getGoal() {
        return goal;
    }

    private void recalculateGoalData() {
        goal.recalculate(config, client);
    }

    public static class HighlightedObject {

        private final TileObject object;
        private final Color color;
        private final int outlineWidth;
        private final int feather;

        HighlightedObject(TileObject object, Color color, int outlineWidth, int feather) {
            this.object = object;
            this.color = color;
            this.outlineWidth = outlineWidth;
            this.feather = feather;
        }

        public TileObject object() {
            return object;
        }

        public Color color() {
            return color;
        }

        public int outlineWidth() {
            return outlineWidth;
        }

        public int feather() {
            return feather;
        }
    }
}
