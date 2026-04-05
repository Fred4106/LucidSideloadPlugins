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

    static final int VARP_LYE_RESIN = VarPlayerID.MIXOLOGY_LYE_POINTS;
    static final int VARP_AGA_RESIN = VarPlayerID.MIXOLOGY_AGA_POINTS;
    static final int VARP_MOX_RESIN = VarPlayerID.MIXOLOGY_MOX_POINTS;

    private static final int VARBIT_ALEMBIC_PROGRESS = VarbitID.MM_ALEMBIC_PROGRESS;
    private static final int VARBIT_AGITATOR_PROGRESS = VarbitID.MM_AGITATOR_PROGRESS;

    private static final int VARBIT_AGITATOR_QUICKACTION = VarbitID.MM_LAB_HIT_SKILLSHOT_AGITATOR;
    private static final int VARBIT_ALEMBIC_QUICKACTION = VarbitID.MM_LAB_HIT_SKILLSHOT_ALEMBIC;

    private static final int VARBIT_MIXING_VESSEL_POTION = VarbitID.MM_LAB_VESSEL_READY;
    private static final int VARBIT_AGITATOR_POTION = VarbitID.MM_LAB_AGITATOR_POTION;
    private static final int VARBIT_RETORT_POTION = VarbitID.MM_LAB_RETORT_POTION;
    private static final int VARBIT_ALEMBIC_POTION = VarbitID.MM_LAB_ALEMBIC_POTION;

    private static final int VARBIT_DIGWEED_NORTH_EAST = VarbitID.MM_HERB_READY_1;
    private static final int VARBIT_DIGWEED_SOUTH_EAST = VarbitID.MM_HERB_READY_2;
    private static final int VARBIT_DIGWEED_SOUTH_WEST = VarbitID.MM_HERB_READY_3;
    private static final int VARBIT_DIGWEED_NORTH_WEST = VarbitID.MM_HERB_READY_4;

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

//    private final Map<AlchemyObject, HighlightedObject> highlightedObjects = new LinkedHashMap<>();

    private List<PotionOrder> potionOrders = Collections.emptyList();
    private boolean inLab = false;

    private PotionType alembicPotionType;
    private PotionType agitatorPotionType;
    private PotionType retortPotionType;

    private int previousAgitatorProgess;
    private int previousAlembicProgress;

    private int agitatorQuickActionTicks = 0;
    private int alembicQuickActionTicks = 0;

    private final Goal goal = new Goal(RewardItem.NONE);

    public Map<AlchemyObject, HighlightedObject> highlightedObjects() {
        return Optional.ofNullable(stateData).map(u -> u.highlightedObjectsJava()).orElse(java.util.Map.<AlchemyObject, HighlightedObject>of());
//        return highlightedObjects;
    }

    public boolean isInLab() {
        return inLab;
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
        inLab = false;
        stateData = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING) {
            stateData.getWriter().clearHighlightObject();
//            highlightedObjects.clear();
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

        stateData.getWriter().clearHighlightObject();
        //highlightedObjects.clear();
        inLab = false;
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
                unHighlightAllStations();
            } else {
                clientThread.invokeLater(this::tryHighlightNextStation);
            }
        }

        if (event.getKey().equals("displayResin")) {
            // Trigger the potion order update to refresh the resin display
            clientThread.invokeLater(this::triggerPotionOrderUpdate);
        }

        if (!config.highlightDigWeed()) {
            unHighlightObject(AlchemyObject.DIGWEED_NORTH_EAST);
            unHighlightObject(AlchemyObject.DIGWEED_SOUTH_EAST);
            unHighlightObject(AlchemyObject.DIGWEED_SOUTH_WEST);
            unHighlightObject(AlchemyObject.DIGWEED_NORTH_WEST);
        }

        if (event.getKey().equals("selectedReward") || event.getKey().equals("rewardQuantity") || event.getKey().equals("showResinBars")) {
            recalculateGoalData();
        }

        if (config.highlightLevers()) {
            highlightLevers();
        } else {
            unHighlightLevers();
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (!inLab || !config.highlightStations() || event.getContainerId() != InventoryID.INV) {
            return;
        }
        // Do not update the highlight if there's a potion in a station
        if (alembicPotionType != null || agitatorPotionType != null || retortPotionType != null) {
            return;
        }
        var inventory = event.getItemContainer();

        // Find the first potion item and highlight its station
        for (var item : inventory.getItems()) {
            var potionType = PotionType.fromItemId(item.getId());

            if (potionType == null || potionType.modifiedItemId() == item.getId()) {
                continue;
            }
            for (var order : potionOrders) {
                if (order.potionType() == potionType && !order.fulfilled()) {
                    unHighlightAllStations();
                    highlightObject(order.potionModifier().alchemyObject(), config.stationHighlightColor());
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
        if (varbitId == VARBIT_POTION_ORDER_1) {
            if (value == 0) {
                unHighlightAllStations();
            } else {
                clientThread.invokeAtTickEnd(this::updatePotionOrders);
            }
        } else if (varbitId == VARBIT_ALEMBIC_POTION) {
            if (value == 0) {
                // Finished crystalising
                unHighlightObject(AlchemyObject.ALEMBIC);
                tryFulfillOrder(alembicPotionType, PotionModifier.CRYSTALISED);
                tryHighlightNextStation();
                LOGGER.debug("Finished crystalising {}", alembicPotionType);
                alembicPotionType = null;
            } else {
                alembicPotionType = PotionType.fromIdx(value - 1);
                LOGGER.debug("Alembic potion type: {}", alembicPotionType);
            }
        } else if (varbitId == VARBIT_AGITATOR_POTION) {
            if (value == 0) {
                // Finished homogenising
                unHighlightObject(AlchemyObject.AGITATOR);
                tryFulfillOrder(agitatorPotionType, PotionModifier.HOMOGENOUS);
                tryHighlightNextStation();
                LOGGER.debug("Finished homogenising {}", agitatorPotionType);
                agitatorPotionType = null;
            } else {
                agitatorPotionType = PotionType.fromIdx(value - 1);
                LOGGER.debug("Agitator potion type: {}", agitatorPotionType);
            }
        } else if (varbitId == VARBIT_RETORT_POTION) {
            if (value == 0) {
                // Finished concentrating
                unHighlightObject(AlchemyObject.RETORT);
                tryFulfillOrder(retortPotionType, PotionModifier.CONCENTRATED);
                tryHighlightNextStation();
                LOGGER.debug("Finished concentrating {}", retortPotionType);
                retortPotionType = null;
            } else {
                retortPotionType = PotionType.fromIdx(value - 1);
                LOGGER.debug("Retort potion type: {}", retortPotionType);
            }
        } else if (varbitId == VARBIT_DIGWEED_NORTH_EAST) {
            if (value == 1) {
                if (config.highlightDigWeed()) {
                    highlightObject(AlchemyObject.DIGWEED_NORTH_EAST, config.digweedHighlightColor());
                }
                notifier.notify(config.notifyDigWeed(), "A digweed has spawned north east.");
            } else {
                unHighlightObject(AlchemyObject.DIGWEED_NORTH_EAST);
            }
        } else if (varbitId == VARBIT_DIGWEED_SOUTH_EAST) {
            if (value == 1) {
                if (config.highlightDigWeed()) {
                    highlightObject(AlchemyObject.DIGWEED_SOUTH_EAST, config.digweedHighlightColor());
                }
                notifier.notify(config.notifyDigWeed(), "A digweed has spawned south east.");
            } else {
                unHighlightObject(AlchemyObject.DIGWEED_SOUTH_EAST);
            }
        } else if (varbitId == VARBIT_DIGWEED_SOUTH_WEST) {
            if (value == 1) {
                if (config.highlightDigWeed()) {
                    highlightObject(AlchemyObject.DIGWEED_SOUTH_WEST, config.digweedHighlightColor());
                }
                notifier.notify(config.notifyDigWeed(), "A digweed has spawned south west.");
            } else {
                unHighlightObject(AlchemyObject.DIGWEED_SOUTH_WEST);
            }
        } else if (varbitId == VARBIT_DIGWEED_NORTH_WEST) {
            if (value == 1) {
                if (config.highlightDigWeed()) {
                    highlightObject(AlchemyObject.DIGWEED_NORTH_WEST, config.digweedHighlightColor());
                }
                notifier.notify(config.notifyDigWeed(), "A digweed has spawned north west.");
            } else {
                unHighlightObject(AlchemyObject.DIGWEED_NORTH_WEST);
            }
        } else if (varbitId == VARBIT_AGITATOR_PROGRESS) {
            if (agitatorQuickActionTicks == 2) {
                // quick action was triggered two ticks ago, so it's now too late
                resetStationHighlight(AlchemyObject.AGITATOR);
                agitatorQuickActionTicks = 0;
            }
            if (agitatorQuickActionTicks == 1) {
                agitatorQuickActionTicks = 2;
            }
            if (value < previousAgitatorProgess) {
                // progress was set back due to a quick action failure
                resetStationHighlight(AlchemyObject.AGITATOR);
            }
            previousAgitatorProgess = value;
        } else if (varbitId == VARBIT_ALEMBIC_PROGRESS) {
            if (alembicQuickActionTicks == 1) {
                // quick action was triggered last tick, so it's now too late
                resetStationHighlight(AlchemyObject.ALEMBIC);
                alembicQuickActionTicks = 0;
            }
            if (value < previousAlembicProgress) {
                // progress was set back due to a quick action failure
                resetStationHighlight(AlchemyObject.ALEMBIC);
            }
            previousAlembicProgress = value;
        } else if (varbitId == VARBIT_AGITATOR_QUICKACTION) {
            // agitator quick action was just successfully popped
            resetStationHighlight(AlchemyObject.AGITATOR);
        } else if (varbitId == VARBIT_ALEMBIC_QUICKACTION) {
            // alembic quick action was just successfully popped
            resetStationHighlight(AlchemyObject.ALEMBIC);
        } else if (varpId == VARP_MOX_RESIN || varpId == VARP_AGA_RESIN || varpId == VARP_LYE_RESIN) {
            recalculateGoalData();
        }
    }

    @Subscribe
    public void onSoundEffectPlayed(SoundEffectPlayed event) {
        if (inLab && alembicPotionType != null && event.getSoundId() == FOUND_GEM && event.getDelay() > 0 && config.soundEffectAlembic()) {
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
        if (spotAnimId == SPOT_ANIM_ALEMBIC && alembicPotionType != null) {
            highlightObject(AlchemyObject.ALEMBIC, config.stationQuickActionHighlightColor());

            if (config.soundEffectAlembic()) {
                LOGGER.debug("Playing manual found_gem sound effect");
                client.playSoundEffect(FOUND_GEM);
            }

            // start counting ticks for alembic so we know to un-highlight on the next alembic varbit update
            // note this quick action has a 1 tick window, so we use an int that goes 0 -> 1 -> unhighlight
            alembicQuickActionTicks = 1;
        }

        if (spotAnimId == SPOT_ANIM_AGITATOR && agitatorPotionType != null) {
            highlightObject(AlchemyObject.AGITATOR, config.stationQuickActionHighlightColor());
            // start counting ticks for agitator so we know to un-highlight on the next agitator varbit update
            // note this quick action has a 2-tick window, so we use an int that goes 0 -> 1 -> 2 -> unhighlight
            agitatorQuickActionTicks = 1;
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
            widgetTool.updatePotionOrdersComponent(baseWidget, potionOrders);
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
        inLab = true;
        updatePotionOrders();
        highlightLevers();
        tryHighlightNextStation();
    }

/*    public void highlightObject(AlchemyObject alchemyObject, Color color) {
        var worldView = client.getTopLevelWorldView();

        if (worldView == null) {
            return;
        }
        var localPoint = LocalPoint.fromWorld(worldView, alchemyObject.coordinate());

        if (localPoint == null) {
            return;
        }
        var tiles = worldView.getScene().getTiles();
        var tile = tiles[worldView.getPlane()][localPoint.getSceneX()][localPoint.getSceneY()];

        for (var gameObject : tile.getGameObjects()) {
            if (gameObject == null) {
                continue;
            }

            if (gameObject.getId() == alchemyObject.objectId()) {
                highlightedObjects.put(alchemyObject, new HighlightedObject(gameObject, color, config.highlightBorderWidth(), config.highlightFeather()));
                return;
            }
        }
        // The aga lever is actually a wall decoration, not a scenery object
        var decorativeObject = tile.getDecorativeObject();

        if (decorativeObject != null && decorativeObject.getId() == alchemyObject.objectId()) {
            highlightedObjects.put(alchemyObject, new HighlightedObject(decorativeObject, color, config.highlightBorderWidth(), config.highlightFeather()));
        }
    }*/

    public void highlightObject(AlchemyObject alchemyObject, Color color) {
        stateData.getWriter().highlightObject(alchemyObject, color);
    }

    public void resetStationHighlight(AlchemyObject alchemyObject) {
        if (config.highlightStations()) {
            highlightObject(alchemyObject, config.stationHighlightColor());
        }
    }

/*    public void unHighlightObject(AlchemyObject alchemyObject) {
        highlightedObjects.remove(alchemyObject);
    }*/
    public void unHighlightObject(AlchemyObject alchemyObject) {
        stateData.getWriter().unHighlightObject(alchemyObject);
    }
    private void unHighlightAllStations() {
        unHighlightObject(AlchemyObject.RETORT);
        unHighlightObject(AlchemyObject.ALEMBIC);
        unHighlightObject(AlchemyObject.AGITATOR);
    }

    private void highlightLevers() {
        if (!config.highlightLevers()) {
            return;
        }

        highlightObject(LYE_LEVER, LYE.color());
        highlightObject(AGA_LEVER, AGA.color());
        highlightObject(MOX_LEVER, MOX.color());
    }

    private void unHighlightLevers() {
        unHighlightObject(LYE_LEVER);
        unHighlightObject(AGA_LEVER);
        unHighlightObject(MOX_LEVER);
    }

    private String stringify(List<PotionOrder> l) {
        return l.stream().map(PotionOrder::toString).collect(Collectors.joining(", ", "{", "}"));
    }
    private void updatePotionOrders() {
        var newOrders = Stream.of(0, 1, 2)
            .map(this::createPotionOrder)
            .sorted(config.potionOrderSorting().comparator())
            .collect(Collectors.toUnmodifiableList());
        if(!newOrders.stream().allMatch(u -> potionOrders.stream().anyMatch(u::equals))) {
            LOGGER.debug("Updating potion orders to from\n\t   {}\n\tto {}", stringify(potionOrders),stringify(newOrders));
            potionOrders = newOrders;
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
        for (var order : potionOrders) {
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

        for (var order : potionOrders) {
            if (order.fulfilled()) {
                continue;
            }
            if (inventory.contains(order.potionType().itemId())) {
                LOGGER.debug("Highlighting station for order {}", order);
                highlightObject(order.potionModifier().alchemyObject(), config.stationHighlightColor());
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
