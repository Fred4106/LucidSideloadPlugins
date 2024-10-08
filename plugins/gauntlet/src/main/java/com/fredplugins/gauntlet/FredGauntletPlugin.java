package com.fredplugins.gauntlet;

//import ethanApiPlugin.EthanApiPlugin;
import com.fredplugins.attacktimer.AttackTimerMetronomePlugin;
import com.fredplugins.common.ProjectileID;
import com.fredplugins.gauntlet.entity.Missile;
import com.fredplugins.gauntlet.overlay.OverlayGauntlet;
import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import com.lucidplugins.api.item.SlottedItem;
import com.lucidplugins.api.utils.*;
import com.fredplugins.gauntlet.resource.ResourceManager;
import ethanApiPlugin.EthanApiPlugin;
import interactionApi.PrayerInteraction;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "<html><font color=\"#32C8CD\">Freds</font> Gauntlet</html>",
        enabledByDefault = false,
        description = "Gauntlet Extended by lucid updated with better auto-features like pray steel skin.",
        conflicts = {"<html><font color=\"#32CD32\">Lucid </font>Gauntlet</html>"},
        tags = {"gauntlet"}
)
@PluginDependency(EthanApiPlugin.class)
@PluginDependency(AttackTimerMetronomePlugin.class)
@Singleton
@Slf4j
public class FredGauntletPlugin extends Plugin
{
    public static final int ONEHAND_SLASH_AXE_ANIMATION = 395;
    public static final int ONEHAND_CRUSH_PICKAXE_ANIMATION = 400;
    public static final int ONEHAND_CRUSH_AXE_ANIMATION = 401;
    public static final int UNARMED_PUNCH_ANIMATION = 422;
    public static final int UNARMED_KICK_ANIMATION = 423;
    public static final int BOW_ATTACK_ANIMATION = 426;
    public static final int ONEHAND_STAB_HALBERD_ANIMATION = 428;
    public static final int ONEHAND_SLASH_HALBERD_ANIMATION = 440;
    public static final int ONEHAND_SLASH_SWORD_ANIMATION = 390;
    public static final int ONEHAND_STAB_SWORD_ANIMATION = 386;
    public static final int HIGH_LEVEL_MAGIC_ATTACK = 1167;
    public static final int HUNLLEF_TORNADO = 8418;
    public static final int HUNLLEF_ATTACK_ANIM = 8419;
    public static final int HUNLLEF_STYLE_SWITCH_TO_MAGE = 8754;
    public static final int HUNLLEF_STYLE_SWITCH_TO_RANGE = 8755;

    public static final int[] MELEE_WEAPONS = {ItemID.CRYSTAL_HALBERD_PERFECTED, ItemID.CORRUPTED_HALBERD_PERFECTED, ItemID.CRYSTAL_HALBERD_ATTUNED, ItemID.CORRUPTED_HALBERD_ATTUNED, ItemID.CRYSTAL_HALBERD_BASIC, ItemID.CORRUPTED_HALBERD_BASIC, ItemID.CRYSTAL_SCEPTRE, ItemID.CORRUPTED_SCEPTRE};
    public static final int[] MELEE_WEAPONS2 = {ItemID.CRYSTAL_SCEPTRE, ItemID.CORRUPTED_SCEPTRE};
    private static final int[] RANGE_WEAPONS = {ItemID.CRYSTAL_BOW_PERFECTED, ItemID.CORRUPTED_BOW_PERFECTED, ItemID.CRYSTAL_BOW_ATTUNED, ItemID.CORRUPTED_BOW_ATTUNED, ItemID.CRYSTAL_BOW_BASIC, ItemID.CORRUPTED_BOW_BASIC};
    private static final int[] MAGE_WEAPONS = {ItemID.CRYSTAL_STAFF_PERFECTED, ItemID.CORRUPTED_STAFF_PERFECTED, ItemID.CRYSTAL_STAFF_ATTUNED, ItemID.CORRUPTED_STAFF_ATTUNED, ItemID.CRYSTAL_STAFF_BASIC, ItemID.CORRUPTED_STAFF_BASIC};

    private static final Set<Integer> MELEE_ANIM_IDS = Set.of(
            ONEHAND_STAB_SWORD_ANIMATION, ONEHAND_SLASH_SWORD_ANIMATION,
            ONEHAND_SLASH_AXE_ANIMATION, ONEHAND_CRUSH_PICKAXE_ANIMATION,
            ONEHAND_CRUSH_AXE_ANIMATION, UNARMED_PUNCH_ANIMATION,
            UNARMED_KICK_ANIMATION, ONEHAND_STAB_HALBERD_ANIMATION,
            ONEHAND_SLASH_HALBERD_ANIMATION
    );

    private static final Set<Integer> ATTACK_ANIM_IDS = new HashSet<>();

    static
    {
        ATTACK_ANIM_IDS.addAll(MELEE_ANIM_IDS);
        ATTACK_ANIM_IDS.add(BOW_ATTACK_ANIMATION);
        ATTACK_ANIM_IDS.add(HIGH_LEVEL_MAGIC_ATTACK);
    }

    private static final Set<Integer> PROJECTILE_MAGIC_IDS = Set.of(
            ProjectileID.HUNLLEF_MAGE_ATTACK, ProjectileID.HUNLLEF_CORRUPTED_MAGE_ATTACK
    );

    private static final Set<Integer> PROJECTILE_RANGE_IDS = Set.of(
            ProjectileID.HUNLLEF_RANGE_ATTACK, ProjectileID.HUNLLEF_CORRUPTED_RANGE_ATTACK
    );

    private static final Set<Integer> PROJECTILE_PRAYER_IDS = Set.of(
            ProjectileID.HUNLLEF_PRAYER_ATTACK, ProjectileID.HUNLLEF_CORRUPTED_PRAYER_ATTACK
    );

    private static final Set<Integer> PROJECTILE_IDS = new HashSet<>();

    static
    {
        PROJECTILE_IDS.addAll(PROJECTILE_MAGIC_IDS);
        PROJECTILE_IDS.addAll(PROJECTILE_RANGE_IDS);
        PROJECTILE_IDS.addAll(PROJECTILE_PRAYER_IDS);
    }

    private static final Set<Integer> HUNLLEF_IDS = Set.of(
            NpcID.CRYSTALLINE_HUNLLEF, NpcID.CRYSTALLINE_HUNLLEF_9022,
            NpcID.CRYSTALLINE_HUNLLEF_9023, NpcID.CRYSTALLINE_HUNLLEF_9024,
            NpcID.CORRUPTED_HUNLLEF, NpcID.CORRUPTED_HUNLLEF_9036,
            NpcID.CORRUPTED_HUNLLEF_9037, NpcID.CORRUPTED_HUNLLEF_9038
    );

    private static final Set<Integer> TORNADO_IDS = Set.of(NullNpcID.NULL_9025, NullNpcID.NULL_9039);

    private static final Set<Integer> DEMIBOSS_IDS = Set.of(
            NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR,
            NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST,
            NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON
    );

    private static final Set<Integer> STRONG_NPC_IDS = Set.of(
            NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION,
            NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN,
            NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF
    );

    private static final Set<Integer> WEAK_NPC_IDS = Set.of(
            NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT,
            NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT,
            NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER
    );

    private static final Set<Integer> RESOURCE_IDS = Set.of(
            ObjectID.CRYSTAL_DEPOSIT, ObjectID.CORRUPT_DEPOSIT,
            ObjectID.PHREN_ROOTS, ObjectID.CORRUPT_PHREN_ROOTS,
            ObjectID.FISHING_SPOT_36068, ObjectID.CORRUPT_FISHING_SPOT,
            ObjectID.GRYM_ROOT, ObjectID.CORRUPT_GRYM_ROOT,
            ObjectID.LINUM_TIRINUM, ObjectID.CORRUPT_LINUM_TIRINUM
    );

    private static final Set<Integer> UTILITY_IDS = Set.of(
            ObjectID.SINGING_BOWL_35966, ObjectID.SINGING_BOWL_36063,
            ObjectID.RANGE_35980, ObjectID.RANGE_36077,
            ObjectID.WATER_PUMP_35981, ObjectID.WATER_PUMP_36078
    );

    private static final Set<Integer> NODES = Set.of(NullObjectID.NULL_36000, NullObjectID.NULL_36001, NullObjectID.NULL_36103, NullObjectID.NULL_36104);

    @Inject
    private AttackTimerMetronomePlugin attackTimerMetronomePlugin;
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private FredGauntletConfig config;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private SkillIconManager skillIconManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private com.fredplugins.gauntlet.overlay.OverlayTimer overlayTimer;

    @Inject
    private OverlayGauntlet overlayGauntlet;

    @Inject
    private com.fredplugins.gauntlet.overlay.OverlayHunllef overlayHunllef;

    @Inject
    private com.fredplugins.gauntlet.overlay.OverlayPrayerWidget overlayPrayerWidget;

    @Inject
    private com.fredplugins.gauntlet.overlay.OverlayPrayerBox overlayPrayerBox;

    private Set<com.fredplugins.gauntlet.overlay.Overlay> overlays;

    @Getter
    private final Set<com.fredplugins.gauntlet.entity.Resource> resources = new HashSet<>();

    @Getter
    private final Set<GameObject> utilities = new HashSet<>();

    @Getter
    private final Set<com.fredplugins.gauntlet.entity.Tornado> tornadoes = new HashSet<>();

    @Getter
    private final Set<com.fredplugins.gauntlet.entity.Demiboss> demibosses = new HashSet<>();

    @Getter
    private final Set<NPC> strongNpcs = new HashSet<>();

    @Getter
    private final Set<NPC> weakNpcs = new HashSet<>();

    private final List<Set<?>> entitySets = Arrays.asList(resources, utilities, tornadoes, demibosses, strongNpcs, weakNpcs);

    @Getter
    private com.fredplugins.gauntlet.entity.Missile missile;

    @Getter
    private com.fredplugins.gauntlet.entity.Hunllef hunllef;

    @Getter
    @Setter
    private boolean wrongAttackStyle;

    @Getter
    @Setter
    private boolean switchWeapon;

    private boolean inGauntlet;
    private boolean inHunllef;

    private int lastSwitchTick = 0;

    private int removeWepTick = 0;

    private int lastAttackTick = 0;

    private int lastDodgeTick = -1;

    private WorldPoint lastSafeTile;

    private WorldPoint secondLastSafeTile;


    private Random rand = new Random();
    @Inject
    @Getter
    private GauntletInstanceGrid instanceGrid;

//    @AllArgsConstructor
    @Value
    private static class ScheduledAction {
        private final int ticksUntilAction;
        private final Runnable action;

        public ScheduledAction(int ticksUntilAction, Runnable action) {
            this.ticksUntilAction = ticksUntilAction;
            this.action = action;
        }
    }
    private Optional<ScheduledAction> tick(ScheduledAction action) {
        ScheduledAction toRet = null;
        if(action.getTicksUntilAction() == 0) {
            action.getAction().run();
        } else if(action.getTicksUntilAction() > 0) {
            toRet = new ScheduledAction(action.getTicksUntilAction()-1, action.getAction());
        }
        return Optional.<ScheduledAction>ofNullable(toRet);
    }

    private final List<ScheduledAction> newScheduledActions = new ArrayList<>();
    private List<ScheduledAction> scheduledActions = ImmutableList.of();

    @Provides
    FredGauntletConfig getConfig(final ConfigManager configManager)
    {
        return configManager.getConfig(FredGauntletConfig.class);
    }

    @Override
    protected void startUp()
    {
        if (overlays == null)
        {
            overlays = Set.of(overlayTimer, overlayGauntlet, overlayHunllef, overlayPrayerWidget, overlayPrayerBox);
        }

        if (client.getGameState() == GameState.LOGGED_IN)
        {
            clientThread.invoke(this::pluginEnabled);
        }

    }

    @Override
    protected void shutDown()
    {
        overlays.forEach(o -> overlayManager.remove(o));

        inGauntlet = false;
        inHunllef = false;

        hunllef = null;
        missile = null;
        wrongAttackStyle = false;
        switchWeapon = false;

        overlayTimer.reset();
        resourceManager.reset();
        instanceGrid.reset();
        entitySets.forEach(Set::clear);
    }

    @Subscribe
    private void onConfigChanged(final ConfigChanged event)
    {
        if (!event.getGroup().equals(FredGauntletConfig.GROUP_NAME))
        {
            return;
        }

        switch (event.getKey())
        {
            case "resourceIconSize":
                if (!resources.isEmpty())
                {
                    resources.forEach(r -> r.setIconSize(config.resourceIconSize()));
                }
                break;
            case "resourceTracker":
                if (inGauntlet && !inHunllef)
                {
                    resourceManager.reset();
                    resourceManager.init();
                }
                break;
            case "projectileIconSize":
                if (missile != null)
                {
                    missile.setIconSize(config.projectileIconSize());
                }
                break;
            case "hunllefAttackStyleIconSize":
                if (hunllef != null)
                {
                    hunllef.setIconSize(config.hunllefAttackStyleIconSize());
                }
                break;
            case "mirrorMode":
                overlays.forEach(overlay -> {
                    overlay.determineLayer();
                    if (overlayManager.anyMatch(o -> o == overlay))
                    {
                        overlayManager.remove(overlay);
                        overlayManager.add(overlay);
                    }
                });
                break;
            default:
                break;
        }
    }

    @Subscribe
    private void onVarbitChanged(final VarbitChanged event)
    {
        if (isHunllefVarbitSet())
        {
            if (!inHunllef)
            {
                initHunllef();
            }
        }
        else if (isGauntletVarbitSet())
        {
            if (!inGauntlet)
            {
                initGauntlet();
            }
        }
        else
        {
            if (inGauntlet || inHunllef)
            {
                shutDown();
            }
        }
    }

    private void onGameTick1(final GameTick event)
    {
//        log.info("GameTick1");
        NPC hun = NpcUtils.getNearestNpc(npc -> npc.getName() != null && npc.getName().contains("Hunllef"));
        if (hun != null && !instanceGrid.isInitialized())
        {
            instanceGrid.initialize();
        }
        if (hunllef == null)
        {
            return;
        }

        if (!inHunllef)
        {
//            Actor interactingTarget = client.getLocalPlayer().getInteracting();
            if (attackTimerMetronomePlugin.getTicksUntilNextAttack() == 1 && attackTimerMetronomePlugin.attackState != AttackTimerMetronomePlugin.AttackState.NOT_ATTACKING) {
                Optional.ofNullable(getPrayerBasedOnWeapon()).filter(x -> !client.isPrayerActive(x)).ifPresent(x -> {
                    int createdTickCount = client.getTickCount();
                    log.info("Enabling prayer {} on tick {}", x, createdTickCount);
                    PrayerInteraction.setPrayerState(x, true);
                    newScheduledActions.add(new ScheduledAction(rand.nextInt(2) + 1, () -> {
                        log.info("Disabling prayer {} after {} ticks on tick {}", x, client.getTickCount() - createdTickCount, client.getTickCount());
                        PrayerInteraction.setPrayerState(x, false);
                    }));
                });
            }
            return;
        }

        hunllef.decrementTicksUntilNextAttack();

        if (missile != null && missile.getProjectile().getRemainingCycles() <= 0)
        {
            missile = null;
        }

        if (!tornadoes.isEmpty())
        {
            tornadoes.forEach(com.fredplugins.gauntlet.entity.Tornado::updateTimeLeft);
        }

        if (client.getTickCount() - lastAttackTick == 1)
        {
            if (hunllef.getPlayerAttackCount() == 6 && config.weaponSwitchMode() == FredGauntletConfig.WeaponSwitchStyle.NORMAL)
            {
                swapWeaponNormal();
            }

            if ((hunllef.getPlayerAttackCount() == 6 || hunllef.getPlayerAttackCount() == 1) && (config.weaponSwitchMode() == FredGauntletConfig.WeaponSwitchStyle.RANGED_5_1 || config.weaponSwitchMode() == FredGauntletConfig.WeaponSwitchStyle.MAGE_5_1))
            {
                swapWeapon51(hunllef.getPlayerAttackCount(), hunllef.getHeadIcon().orElse(null));
            }
        }

        boolean attacked = false;

        if (client.getTickCount() - lastSwitchTick == 1)
        {
            final Item wep = EquipmentUtils.getWepSlotItem();

            if (wep != null)
            {
                ItemComposition composition = client.getItemDefinition(wep.getId());
                if (composition.getName() != null && (composition.getName().contains("bow") || composition.getName().contains("staff")))
                {
                    if (config.autoAttack())
                    {
                        attackHunllef();
                        attacked = true;
                    }
                }
                else
                {
                    if (config.autoAttackMelee())
                    {
                        attackHunllef();
                        attacked = true;
                    }
                }
            }
            else
            {
                if (config.autoAttackMelee())
                {
                    attackHunllef();
                    attacked = true;
                }
            }
        }

        if (config.autoDodge())
        {
            WorldPoint safeTile = getToSafeTile();
        }
    }

    private void onGameTick2(final GameTick event)
    {
//        log.info("GameTick2");
        List<ScheduledAction> tempActions = ImmutableList.<ScheduledAction>builder().addAll(newScheduledActions).addAll(scheduledActions).build();
        newScheduledActions.clear();
        scheduledActions = tempActions.stream().flatMap(a -> tick(a).stream()).collect(Collectors.toUnmodifiableList());
    }
    @Subscribe
    private void onGameTick(final GameTick event) {
        onGameTick1(event);
        onGameTick2(event);
    }
    private WorldPoint getToSafeTile()
    {
        WorldPoint safeTile = getClosestSafeTile();

        if (tileUnderUsUnsafe(false))
        {
            if (ticksSinceLastDodge() > 1 && safeTile != null && !safeTile.equals(client.getLocalPlayer().getWorldLocation()))
            {
                InteractionUtils.walk(safeTile);
                secondLastSafeTile = lastSafeTile;
                lastSafeTile = safeTile;
                lastDodgeTick = client.getTickCount();
                return safeTile;
            }
        }
        else if (tileUnderUsUnsafe(true))
        {
            if (ticksSinceLastDodge() > 1 || tooCloseToTornado(client.getLocalPlayer().getWorldLocation(), 3))
            {
                if (safeTile != null && !safeTile.equals(client.getLocalPlayer().getWorldLocation()))
                {
                    InteractionUtils.walk(safeTile);
                    secondLastSafeTile = lastSafeTile;
                    lastSafeTile = safeTile;
                    lastDodgeTick = client.getTickCount();
                    return safeTile;
                }
            }
        }
        return null;
    }

    private boolean tileUnderUsUnsafe(boolean checkTornados)
    {
        Predicate<TileObject> unsafeTileFilter = (groundObject) -> (groundObject.getId() == 36150 || groundObject.getId() == 36151 ||
                groundObject.getId() == 36047 || groundObject.getId() == 36048) &&
                groundObject.getLocalLocation().equals(client.getLocalPlayer().getLocalLocation());

        boolean isTileSafe = GameObjectUtils.nearest(unsafeTileFilter) == null;

        boolean underHunllef = hunllef.getNpc().getWorldArea().contains(client.getLocalPlayer().getWorldLocation());

        boolean tooCloseToTornados = tooCloseToTornado(client.getLocalPlayer().getWorldLocation(), 3);

        if (!isTileSafe)
        {
            log.info("Need to move from unsafe tile!");
        }

        if (underHunllef)
        {
            log.info("Need to get out from under the beast!");
        }

        if (tooCloseToTornados)
        {
            log.info("There's a tornado about to fuck us up");
        }

        return !isTileSafe || underHunllef || (checkTornados && tooCloseToTornados);
    }

    public WorldArea fromSwToNe(WorldPoint swLocation, WorldPoint neLocation)
    {
        return new WorldArea(swLocation.getX(), swLocation.getY(), neLocation.getX() - swLocation.getX() + 1, neLocation.getY() - swLocation.getY() + 1, swLocation.getPlane());
    }

    private boolean tooCloseToTornado(WorldPoint point, int range)
    {
        if (tornadoes.size() == 0)
        {
            return false;
        }

        for (com.fredplugins.gauntlet.entity.Tornado t : tornadoes)
        {
            NPC tornado = t.getNpc();
            if (tornado.getWorldLocation().distanceTo2D(point) < range)
            {
                return true;
            }
        }
        return false;
    }
    @Nullable
    private ObjectComposition getObjectComposition(int id)
    {
        ObjectComposition objectComposition = client.getObjectDefinition(id);
        return objectComposition.getImpostorIds() == null ? objectComposition : objectComposition.getImpostor();
    }
    public List<GameObject> findNodeForUnopenedRoom(GauntletRoom room)
    {
        if(room == null) {
            return null;
        }
        return new GameObjectQuery().getGameObjectQuery(client).stream()
                .filter(gameObject ->
                {
                    final boolean isAboveBy1 = gameObject.getWorldLocation().getY() == room.getBaseY() + 1;
                    final boolean isBelowBy1 = gameObject.getWorldLocation().getY() == room.getBaseY() - GauntletInstanceGrid.ROOM_SIZE - 1;
                    final boolean isRight = gameObject.getWorldLocation().getX() == room.getBaseX() + GauntletInstanceGrid.ROOM_SIZE;
                    final boolean isLeftBy2 = gameObject.getWorldLocation().getX() == room.getBaseX() - 2;
                    final boolean isInsideRoomX = gameObject.getWorldLocation().getX() >= room.getBaseX() && gameObject.getWorldLocation().getX() <= room.getBaseX() + GauntletInstanceGrid.ROOM_SIZE;
                    final boolean isInsideRoomY = gameObject.getWorldLocation().getY() <= room.getBaseY() && gameObject.getWorldLocation().getY() >= room.getBaseY() - GauntletInstanceGrid.ROOM_SIZE;
                    return ((isAboveBy1 && isInsideRoomX) ||
                            (isBelowBy1 && isInsideRoomX) ||
                            (isLeftBy2 && isInsideRoomY) ||
                            (isRight && isInsideRoomY)) &&
                            getObjectComposition(gameObject.getId()).getName().equals("Node");
                }).sorted((x, y) -> {
                    return client.getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation()) + client.getLocalPlayer().getWorldLocation().distanceTo(y.getWorldLocation());
                }).collect(Collectors.toList());
    }

    private WorldPoint getClosestSafeTile()
    {
        //WorldArea biggerArea = fromSwToNe(hunllef.getNpc().getWorldLocation().dx(-2).dy(-2), hunllef.getNpc().getWorldLocation().dx(6).dy(6));
        if (lastSafeTile == null)
        {
            lastSafeTile = client.getLocalPlayer().getWorldLocation();
        }

        if (secondLastSafeTile == null)
        {
            secondLastSafeTile = client.getLocalPlayer().getWorldLocation();
        }

        WorldArea biggerArea = hunllef.getNpc().getWorldArea();
        Predicate<TileObject> filter1 = groundObject ->
                (groundObject.getId() == 36149 || groundObject.getId() == 36046) &&
                !biggerArea.contains(groundObject.getWorldLocation()) &&
                !tooCloseToTornado(groundObject.getWorldLocation(), 5) &&
                groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) >= 1 &&
                (groundObject.getWorldLocation().getX() == client.getLocalPlayer().getWorldLocation().getX() || groundObject.getWorldLocation().getY() == client.getLocalPlayer().getWorldLocation().getY()) &&
                isInsideArena(groundObject.getWorldLocation()) &&
                (lastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1 || secondLastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1) &&
                groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) < 5;

        Predicate<TileObject> filter2 = groundObject ->
                (groundObject.getId() == 36149 || groundObject.getId() == 36046) &&
                !biggerArea.contains(groundObject.getWorldLocation()) &&
                !tooCloseToTornado(groundObject.getWorldLocation(), 4) &&
                groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) >= 1 &&
                (groundObject.getWorldLocation().getX() == client.getLocalPlayer().getWorldLocation().getX() || groundObject.getWorldLocation().getY() == client.getLocalPlayer().getWorldLocation().getY()) &&
                isInsideArena(groundObject.getWorldLocation()) &&
                (lastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1 || secondLastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1) &&
                groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) < 5;


        Predicate<TileObject> filter3 = groundObject ->
                (groundObject.getId() == 36149 || groundObject.getId() == 36046) &&
                !biggerArea.contains(groundObject.getWorldLocation()) &&
                !tooCloseToTornado(groundObject.getWorldLocation(), 3) &&
                groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) >= 1 &&
                (groundObject.getWorldLocation().getX() == client.getLocalPlayer().getWorldLocation().getX() || groundObject.getWorldLocation().getY() == client.getLocalPlayer().getWorldLocation().getY()) &&
                isInsideArena(groundObject.getWorldLocation()) &&
                (lastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1 || secondLastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1) &&
                groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) < 5;

        Predicate<TileObject> filter4 = groundObject ->
                (groundObject.getId() == 36149 || groundObject.getId() == 36046) &&
                !biggerArea.contains(groundObject.getWorldLocation())
                && !tooCloseToTornado(groundObject.getWorldLocation(), 2)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) >= 1
                && (groundObject.getWorldLocation().getX() == client.getLocalPlayer().getWorldLocation().getX() || groundObject.getWorldLocation().getY() == client.getLocalPlayer().getWorldLocation().getY())
                && isInsideArena(groundObject.getWorldLocation())
                && (lastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1
                || secondLastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) < 5;


        Predicate<TileObject> filter5 = groundObject ->
                (groundObject.getId() == 36149 || groundObject.getId() == 36046) &&
                !biggerArea.contains(groundObject.getWorldLocation())
                && !tooCloseToTornado(groundObject.getWorldLocation(), 5)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) >= 1
                && isInsideArena(groundObject.getWorldLocation())
                && (lastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1
                || secondLastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) < 5;

        Predicate<TileObject> filter6 = groundObject ->
                (groundObject.getId() == 36149 || groundObject.getId() == 36046) &&
                !biggerArea.contains(groundObject.getWorldLocation())
                && !tooCloseToTornado(groundObject.getWorldLocation(), 4)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) >= 1
                && isInsideArena(groundObject.getWorldLocation())
                && (lastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1
                || secondLastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) < 5;

        Predicate<TileObject> filter7 = groundObject ->
                (groundObject.getId() == 36149 || groundObject.getId() == 36046) &&
                !biggerArea.contains(groundObject.getWorldLocation())
                && !tooCloseToTornado(groundObject.getWorldLocation(), 3)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) >= 1
                && isInsideArena(groundObject.getWorldLocation())
                && (lastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1
                || secondLastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) < 5;

        Predicate<TileObject> filter8 = groundObject ->
                (groundObject.getId() == 36149 || groundObject.getId() == 36046) &&
                !biggerArea.contains(groundObject.getWorldLocation())
                && !tooCloseToTornado(groundObject.getWorldLocation(), 2)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) >= 1
                && isInsideArena(groundObject.getWorldLocation())
                && (lastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1
                || secondLastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) < 5;

        Predicate<TileObject> filter9 = groundObject ->
                (groundObject.getId() == 36149 || groundObject.getId() == 36046) &&
                !hunllef.getNpc().getWorldArea().contains(groundObject.getWorldLocation())
                && !tooCloseToTornado(groundObject.getWorldLocation(), 5)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) >= 1
                && isInsideArena(groundObject.getWorldLocation())
                && (lastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1
                || secondLastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1);

        Predicate<TileObject> filter10 = groundObject ->
                (groundObject.getId() == 36149 || groundObject.getId() == 36046) &&
                !hunllef.getNpc().getWorldArea().contains(groundObject.getWorldLocation())
                && !tooCloseToTornado(groundObject.getWorldLocation(), 4)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) >= 1
                && isInsideArena(groundObject.getWorldLocation())
                && (lastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1
                || secondLastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1);

        Predicate<TileObject> filter11 = groundObject ->
                (groundObject.getId() == 36149 || groundObject.getId() == 36046) &&
                !hunllef.getNpc().getWorldArea().contains(groundObject.getWorldLocation())
                && !tooCloseToTornado(groundObject.getWorldLocation(), 3)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) >= 1
                && isInsideArena(groundObject.getWorldLocation())
                && (lastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1
                || secondLastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1);

        Predicate<TileObject> filter12 = groundObject ->
                (groundObject.getId() == 36149 || groundObject.getId() == 36046) &&
                !hunllef.getNpc().getWorldArea().contains(groundObject.getWorldLocation())
                && !tooCloseToTornado(groundObject.getWorldLocation(), 2)
                && groundObject.getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) >= 1
                && isInsideArena(groundObject.getWorldLocation())
                && (lastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1
                || secondLastSafeTile.distanceTo2D(groundObject.getWorldLocation()) > 1);

        List<Predicate<TileObject>> filters = List.of(filter1, filter2, filter3, filter4, filter5, filter6, filter7, filter8, filter9, filter10, filter11, filter12);

        for (Predicate<TileObject> filter : filters)
        {
            TileObject nearest = GameObjectUtils.nearest(filter);

            if (nearest != null)
            {
                return nearest.getWorldLocation();
            }
        }

        return null;
    }

    private int ticksSinceLastDodge()
    {
        return client.getTickCount() - lastDodgeTick;
    }

    private boolean isInsideArena(WorldPoint point)
    {
        final int hunllefBaseX = instanceGrid.getRoom(3, 3).getBaseX();
        final int hunllefBaseY = instanceGrid.getRoom(3, 3).getBaseY();
        final WorldPoint arenaSouthWest = new WorldPoint(hunllefBaseX + 2, hunllefBaseY - 13, client.getLocalPlayer().getWorldLocation().getPlane());
        final WorldPoint arenaNorthEast = new WorldPoint(hunllefBaseX + 13, hunllefBaseY - 2, client.getLocalPlayer().getWorldLocation().getPlane());

        return fromSwToNe(arenaSouthWest, arenaNorthEast).contains(point);
    }

//    @Subscribe
//    private void onInteractingChanged(final InteractingChanged event)
//    {
//        if (event.getTarget() instanceof Player && event.getSource() instanceof NPC)
//        {
//            NPC source = (NPC)event.getSource();
//            Player target = (Player) event.getTarget();
//            Demiboss.Type tpe = Demiboss.Type.fromId(source.getId());
//            if (target == client.getLocalPlayer() && tpe != null)
//            {
//                if(tpe == Demiboss.Type.BEAR)
//                {
//                    CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MELEE);
//                }
//                else if(tpe == Demiboss.Type.DARK_BEAST)
//                {
//                    CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MISSILES);
//                }
//                else if(tpe == Demiboss.Type.DRAGON)
//                {
//                    CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MAGIC);
//                }
//            }
//        }
//    }
    @Subscribe
    private void onGameStateChanged(final GameStateChanged event)
    {
        switch (event.getGameState())
        {
            case LOADING:
                resources.clear();
                utilities.clear();
                break;
            case LOGIN_SCREEN:
            case HOPPING:
                shutDown();
                break;
        }
    }

    @Subscribe
    private void onWidgetLoaded(final WidgetLoaded event)
    {
        if (event.getGroupId() == WidgetID.GAUNTLET_TIMER_GROUP_ID)
        {
            overlayTimer.setGauntletStart();
            resourceManager.init();
        }
    }

    @Subscribe
    private void onGameObjectSpawned(final GameObjectSpawned event)
    {
        final GameObject gameObject = event.getGameObject();

        final int id = gameObject.getId();

        if (RESOURCE_IDS.contains(id))
        {
            resources.add(new com.fredplugins.gauntlet.entity.Resource(gameObject, skillIconManager, config.resourceIconSize()));
        }
        else if (UTILITY_IDS.contains(id))
        {
            utilities.add(gameObject);
        }

        if (NODES.contains(id))
        {
            Point nodeGridLocation = instanceGrid.getGridLocationByWorldPoint(event.getTile().getWorldLocation());
            GauntletRoom litRoom = instanceGrid.getRoom(nodeGridLocation.getX(), nodeGridLocation.getY());
            if (!litRoom.isLit())
            {
                litRoom.setLit(true);
            }
        }
    }

    @Subscribe
    private void onGameObjectDespawned(final GameObjectDespawned event)
    {
        final GameObject gameObject = event.getGameObject();

        final int id = gameObject.getId();

        if (RESOURCE_IDS.contains(gameObject.getId()))
        {
            resources.removeIf(o -> o.getGameObject() == gameObject);
        }
        else if (UTILITY_IDS.contains(id))
        {
            utilities.remove(gameObject);
        }
    }

    @Subscribe
    private void onNpcSpawned(final NpcSpawned event)
    {
        final NPC npc = event.getNpc();

        final int id = npc.getId();

        if (HUNLLEF_IDS.contains(id))
        {
            hunllef = new com.fredplugins.gauntlet.entity.Hunllef(npc, skillIconManager, config.hunllefAttackStyleIconSize());
        }
        else if (TORNADO_IDS.contains(id))
        {
            tornadoes.add(new com.fredplugins.gauntlet.entity.Tornado(npc));
        }
        else if (DEMIBOSS_IDS.contains(id))
        {
            demibosses.add(new com.fredplugins.gauntlet.entity.Demiboss(npc));
        }
        else if (STRONG_NPC_IDS.contains(id))
        {
            strongNpcs.add(npc);
        }
        else if (WEAK_NPC_IDS.contains(id))
        {
            weakNpcs.add(npc);
        }
    }

    @Subscribe
    private void onItemContainerChanged(ItemContainerChanged event)
    {
        if (!inHunllef)
        {
            return;
        }
        if (event.getContainerId() == InventoryID.EQUIPMENT.getId())
        {
            String x = Arrays.stream(Prayer.values()).flatMap(p -> Optional.of(p.name()).filter(o -> client.isPrayerActive(p)).stream()).collect(Collectors.joining(", ", "[", "]"));
            MessageUtils.addMessage("Equipment changed: [" + Optional.ofNullable(getPrayerBasedOnWeapon()).map(Prayer::name).orElse("null") + "] " + x, Color.BLUE);
            if (config.autoOffense() || config.autoDefense())
            {
                Prayer wepPrayer = getPrayerBasedOnWeapon();
                Prayer defPrayer = getDefensePrayer();
                if(wepPrayer != Prayer.CHIVALRY && wepPrayer != Prayer.PIETY && defPrayer != null) {
                    log.info("Activating {} and {}", wepPrayer.name(), defPrayer.name());
                    CombatUtils.activatePrayer(defPrayer);
                    CombatUtils.activatePrayer(wepPrayer);
                } else {
                    log.info("Activating {}", wepPrayer.name());
                    CombatUtils.activatePrayer(wepPrayer);
                }
            }
        }
    }

    @Subscribe
    private void onNpcDespawned(final NpcDespawned event)
    {
        final NPC npc = event.getNpc();

        final int id = npc.getId();

        if (HUNLLEF_IDS.contains(id))
        {
            hunllef = null;
        }
        else if (TORNADO_IDS.contains(id))
        {
            tornadoes.removeIf(t -> t.getNpc() == npc);
        }
        else if (DEMIBOSS_IDS.contains(id))
        {
            demibosses.removeIf(d -> d.getNpc() == npc);
        }
        else if (STRONG_NPC_IDS.contains(id))
        {
            strongNpcs.remove(npc);
        }
        else if (WEAK_NPC_IDS.contains(id))
        {
            weakNpcs.remove(npc);
        }
    }

    @Subscribe
    private void onProjectileMoved(final ProjectileMoved event)
    {
        if (hunllef == null)
        {
            return;
        }

        final Projectile projectile = event.getProjectile();

        if (projectile.getRemainingCycles() != (projectile.getEndCycle() - projectile.getStartCycle()))
        {
            return;
        }

        final int id = projectile.getId();

        if (!PROJECTILE_IDS.contains(id))
        {
            return;
        }

        missile = new Missile(projectile, skillIconManager, config.projectileIconSize());

        if (PROJECTILE_PRAYER_IDS.contains(id) && config.hunllefPrayerAudio())
        {
            client.playSoundEffect(SoundEffectID.MAGIC_SPLASH_BOING);
        }
    }

    @Subscribe
    private void onChatMessage(final ChatMessage event)
    {
        final ChatMessageType type = event.getType();

        if (type == ChatMessageType.SPAM || type == ChatMessageType.GAMEMESSAGE)
        {
            resourceManager.parseChatMessage(event.getMessage());
        }

        if (event.getMessage().contains("prayers have been disabled"))
        {
            String x = Arrays.stream(Prayer.values()).flatMap(p -> Optional.of(p.name()).filter(o -> client.isPrayerActive(p)).stream()).collect(Collectors.joining(", ", "[", "]"));
            MessageUtils.addMessage("Chat message: " + "[" + hunllef.getAttackPhase().getPrayer().name() + "] [" + Optional.ofNullable(getPrayerBasedOnWeapon()).map(Prayer::name).orElse("null") + "] " + x, Color.BLUE);
            if (config.autoPrayer())
            {
                CombatUtils.togglePrayer(hunllef.getAttackPhase().getPrayer());
            }
            if (config.autoOffense() || config.autoDefense())
            {
                Prayer wepPrayer = getPrayerBasedOnWeapon();
                Prayer defPrayer = getDefensePrayer();
                if(wepPrayer != Prayer.CHIVALRY && wepPrayer != Prayer.PIETY && defPrayer != null) {
                    log.info("Reactivating {} and {}", wepPrayer.name(), defPrayer.name());
                    CombatUtils.togglePrayer(defPrayer);
                    CombatUtils.togglePrayer(wepPrayer);
                } else {
                    log.info("Reactivating {}", wepPrayer.name());
                    CombatUtils.togglePrayer(wepPrayer);
                }
            }
        }
    }

    @Subscribe
    private void onActorDeath(final ActorDeath event)
    {
        if (event.getActor() != client.getLocalPlayer())
        {
            return;
        }

        overlayTimer.onPlayerDeath();
    }

    @Subscribe
    private void onAnimationChanged(final AnimationChanged event)
    {
        if (!isHunllefVarbitSet() || hunllef == null)
        {
            return;
        }

        final Actor actor = event.getActor();

        if (actor == null)
        {
            return;
        }

        final int animationId = actor.getAnimation();

        if (actor instanceof Player)
        {
            if (!ATTACK_ANIM_IDS.contains(animationId))
            {
                return;
            }

            final boolean validAttack = isAttackAnimationValid(animationId);

            if (validAttack)
            {

                wrongAttackStyle = false;

                hunllef.updatePlayerAttackCount();

                if (hunllef.getPlayerAttackCount() == 1)
                {
                    switchWeapon = true;
                }

                lastAttackTick = client.getTickCount();
            }
            else
            {
                wrongAttackStyle = true;
            }
        }
        else if (actor instanceof NPC)
        {
            if (animationId == HUNLLEF_ATTACK_ANIM || animationId == HUNLLEF_TORNADO)
            {
                hunllef.updateAttackCount();
            }

            if (animationId == HUNLLEF_STYLE_SWITCH_TO_MAGE || animationId == HUNLLEF_STYLE_SWITCH_TO_RANGE)
            {
                hunllef.toggleAttackHunllefAttackStyle();

                if (config.autoPrayer())
                {
                    String x = Arrays.stream(Prayer.values()).flatMap(p -> Optional.of(p.name()).filter(o -> client.isPrayerActive(p)).stream()).collect(Collectors.joining(", ", "[", "]"));
                    MessageUtils.addMessage("Animation changed: " + "[" + hunllef.getAttackPhase().getPrayer().name() + "] " + x, Color.BLUE);
                    CombatUtils.togglePrayer(hunllef.getAttackPhase().getPrayer());
                }
            }
        }
    }

    private boolean isAttackAnimationValid(final int animationId)
    {
        final HeadIcon headIcon = EthanApiPlugin.getHeadIcon(hunllef.getNpc());

        if (headIcon == null)
        {
            return true;
        }

        switch (headIcon)
        {
            case MELEE:
                if (MELEE_ANIM_IDS.contains(animationId))
                {
                    return false;
                }
                break;
            case RANGED:
                if (animationId == BOW_ATTACK_ANIMATION)
                {
                    return false;
                }
                break;
            case MAGIC:
                if (animationId == HIGH_LEVEL_MAGIC_ATTACK)
                {
                    return false;
                }
                break;
        }

        return true;
    }

    private void pluginEnabled()
    {
        if (isGauntletVarbitSet())
        {
            overlayTimer.setGauntletStart();
            resourceManager.init();
            addSpawnedEntities();
            initGauntlet();
        }

        if (isHunllefVarbitSet())
        {
            initHunllef();
        }
    }

    private void addSpawnedEntities()
    {
        for (final GameObject gameObject : new GameObjectQuery().getGameObjectQuery(client))
        {
            GameObjectSpawned gameObjectSpawned = new GameObjectSpawned();
            gameObjectSpawned.setTile(null);
            gameObjectSpawned.setGameObject(gameObject);
            onGameObjectSpawned(gameObjectSpawned);
        }

        for (final NPC npc : client.getTopLevelWorldView().npcs())
        {
            onNpcSpawned(new NpcSpawned(npc));
        }
    }

    private void initGauntlet()
    {
        inGauntlet = true;

        overlayManager.add(overlayTimer);
        overlayManager.add(overlayGauntlet);
    }

    private void initHunllef()
    {
        inHunllef = true;

        overlayTimer.setHunllefStart();
        resourceManager.reset();
        overlayManager.remove(overlayGauntlet);
        overlayManager.add(overlayHunllef);
        overlayManager.add(overlayPrayerWidget);
        overlayManager.add(overlayPrayerBox);
    }

    private boolean isGauntletVarbitSet()
    {
        return client.getVarbitValue(9178) == 1;
    }

    private boolean isHunllefVarbitSet()
    {
        return client.getVarbitValue(9177) == 1;
    }

    private Prayer getPrayerBasedOnWeapon()
    {
        if(config.autoOffense()) {
            if(EquipmentUtils.contains(RANGE_WEAPONS)) {
                return config.offenseRangePrayer().getPrayer();
            }
            if(EquipmentUtils.contains(MAGE_WEAPONS)) {
                return config.offenseMagicPrayer().getPrayer();
            }
            if(EquipmentUtils.contains(MELEE_WEAPONS) || EquipmentUtils.contains(MELEE_WEAPONS2)) {
                return config.offenseMeleePrayer().getPrayer();
            }
        }
        return null;
    }

    private Prayer getDefensePrayer()
    {
        if (config.autoDefense()) {
            return config.defensePrayer().getPrayer();
        }
        return null;
    }
    private void swapWeaponNormal()
    {
        if (InventoryUtils.contains(MAGE_WEAPONS))
        {
            SlottedItem wep = InventoryUtils.getFirstItemSlotted(MAGE_WEAPONS);
            if (wep != null)
            {
                InventoryUtils.wieldItem(wep.getItem().getId());
            }
        }
        else if (InventoryUtils.contains(RANGE_WEAPONS))
        {
            SlottedItem wep = InventoryUtils.getFirstItemSlotted(RANGE_WEAPONS);

            if (wep != null)
            {
                InventoryUtils.wieldItem(wep.getItem().getId());
            }
        }
        else if (InventoryUtils.contains(MELEE_WEAPONS))
        {
            SlottedItem wep = InventoryUtils.getFirstItemSlotted(MELEE_WEAPONS);
            if (wep != null)
            {
                InventoryUtils.wieldItem(wep.getItem().getId());
            }
        }
        else
        {
            EquipmentUtils.removeWepSlotItem();
        }

        lastSwitchTick = client.getTickCount();
    }

    private void swapWeapon51(int attackCount, HeadIcon current)
    {
        assert(current != null);
        if (attackCount == 1)
        {
            if (current == HeadIcon.RANGED || current == HeadIcon.MAGIC)
            {
                if (InventoryUtils.contains(MELEE_WEAPONS))
                {
                    SlottedItem wep = InventoryUtils.getFirstItemSlotted(MELEE_WEAPONS);
                    if (wep != null)
                    {
                        InventoryUtils.wieldItem(wep.getItem().getId());
                    }
                }
                else
                {
                    EquipmentUtils.removeWepSlotItem();
                }
                lastSwitchTick = client.getTickCount();
            }

            if (current == HeadIcon.MELEE)
            {
                if (config.weaponSwitchMode() == FredGauntletConfig.WeaponSwitchStyle.MAGE_5_1)
                {
                    if (InventoryUtils.contains(RANGE_WEAPONS))
                    {
                        SlottedItem wep = InventoryUtils.getFirstItemSlotted(RANGE_WEAPONS);
                        if (wep != null)
                        {
                            InventoryUtils.wieldItem(wep.getItem().getId());
                        }

                        lastSwitchTick = client.getTickCount();
                    }
                }
                else
                {
                    if (InventoryUtils.contains(MAGE_WEAPONS))
                    {
                        SlottedItem wep = InventoryUtils.getFirstItemSlotted(MAGE_WEAPONS);
                        if (wep != null)
                        {
                            InventoryUtils.wieldItem(wep.getItem().getId());
                        }

                        lastSwitchTick = client.getTickCount();
                    }
                }
            }
        }

        if (attackCount == 6)
        {
            if (config.weaponSwitchMode() == FredGauntletConfig.WeaponSwitchStyle.MAGE_5_1)
            {
                if (InventoryUtils.contains(MAGE_WEAPONS))
                {
                    SlottedItem wep = InventoryUtils.getFirstItemSlotted(MAGE_WEAPONS);
                    if (wep != null)
                    {
                        InventoryUtils.wieldItem(wep.getItem().getId());
                    }

                    lastSwitchTick = client.getTickCount();
                }

            }
            else
            {
                if (InventoryUtils.contains(RANGE_WEAPONS))
                {
                    SlottedItem wep = InventoryUtils.getFirstItemSlotted(RANGE_WEAPONS);
                    if (wep != null)
                    {
                        InventoryUtils.wieldItem(wep.getItem().getId());
                    }

                    lastSwitchTick = client.getTickCount();
                }

            }
        }
    }

    private void attackHunllef()
    {
        final NPC hunllef = NpcUtils.getNearestNpc(npc -> npc.getName() != null && npc.getName().contains("Hunllef"));
        if (hunllef == null)
        {
            return;
        }

        NpcUtils.attackNpc(hunllef);
    }

}