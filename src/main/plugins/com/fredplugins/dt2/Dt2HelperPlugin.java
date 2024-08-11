package com.fredplugins.dt2;

import ethanApiPlugin.collections.NPCs;
import ethanApiPlugin.collections.TileObjects;
import ethanApiPlugin.EthanApiPlugin;
import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Extension
@PluginDescriptor(
        name = "<html><font color=\"#32C8CD\">Freds</font> DT2 Helper</html>",
        enabledByDefault = false,
        description = "Helps during the final boss fight of dt2.",
        tags = {"dt2", "helper", "fred4106"}
)
@PluginDependency(EthanApiPlugin.class)
@Singleton
public class Dt2HelperPlugin extends Plugin {
    static final int FORSAKEN_ASSASSIN_ID = 12328;

    static final int THROW_ACID_ANIM_ID = 385;
    static final int BASIC_RANGE_ATTACK_ANIM_ID = 426;
    static final int BASIC_MELEE_ATTACK_ANIM_ID = 406;
    static final int THROW_3_ANIM_ID = 7617;

    static final int ACID_JAR_PROJECTILE_ID = 2383;
    static final int WHITE_CLOUD_PROJECTILE_ID = 2295;
    static final int PINK_JAR_PROJECTILE_ID = 2300;

    static final int WHITE_CLOUD_ID = 46622;
    static final int PINK_CLOUD_ID = 47505;

    static final List<Integer> PROJECTILE_IDS = ImmutableList.of(ACID_JAR_PROJECTILE_ID, PINK_JAR_PROJECTILE_ID, WHITE_CLOUD_PROJECTILE_ID);
    static final List<Integer> CLOUD_OBJECT_IDS = ImmutableList.of(WHITE_CLOUD_ID, PINK_CLOUD_ID);
    static final List<Integer> ANIMATION_IDS = ImmutableList.of(BASIC_MELEE_ATTACK_ANIM_ID, BASIC_RANGE_ATTACK_ANIM_ID, THROW_ACID_ANIM_ID, THROW_3_ANIM_ID);

    @Getter(AccessLevel.PACKAGE)
    private NPC forsakenAssassin;

    @Getter(AccessLevel.PACKAGE)
    private List<TileObject> clouds = new ArrayList<TileObject>();;


    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private Dt2Overlay overlay;

    @Inject
    private ClientThread clientThread;


    @Override
    protected void startUp() throws Exception {
        super.startUp();
        clouds = TileObjects.search().idInList(CLOUD_OBJECT_IDS).result();
        forsakenAssassin = NPCs.search().withId(FORSAKEN_ASSASSIN_ID).nearestToPlayer().orElse(null);
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
        overlayManager.remove(overlay);
        clouds.clear();
        forsakenAssassin = null;

    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        Projectile projectile = event.getProjectile();
        if(!PROJECTILE_IDS.contains(projectile.getId())) return;
        if(projectile.getRemainingCycles() != (projectile.getEndCycle() - projectile.getStartCycle())) return;
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if(event.getNpc().getId() != FORSAKEN_ASSASSIN_ID) return;
        forsakenAssassin = event.getNpc();
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        if(event.getNpc().getId() != FORSAKEN_ASSASSIN_ID) return;
        forsakenAssassin = null;
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event) {
        if(event.getNpc().getId() != FORSAKEN_ASSASSIN_ID) return;
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if(!(event.getActor() instanceof NPC)) return;
        NPC animNpc = ((NPC)event.getActor());
        if(animNpc != forsakenAssassin) return;
        if(!ANIMATION_IDS.contains(animNpc.getAnimation())) return;

        int animID = animNpc.getAnimation();
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        if(!CLOUD_OBJECT_IDS.contains(event.getGameObject().getId())) return;
        clouds.add(event.getGameObject());
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        if(!CLOUD_OBJECT_IDS.contains(event.getGameObject().getId())) return;
        clouds.remove(event.getGameObject());
    }
}
