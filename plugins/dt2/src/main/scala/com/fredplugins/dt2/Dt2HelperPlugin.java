package com.fredplugins.dt2;

//import ethanApiPlugin.collections.NPCs;
//import ethanApiPlugin.collections.TileObjects;
//import ethanApiPlugin.EthanApiPlugin;
//import ethanApiPlugin.NpcUtils;
import ethanApiPlugin.EthanApiPlugin;
    import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import javax.inject.Singleton;

@PluginDescriptor(
        name = "<html><font color=\"#32C8CD\">Freds</font> DT2 Helper</html>",
        enabledByDefault = false,
        description = "Helps during the final boss fight of dt2.",
        tags = {"dt2", "helper", "fred4106"}
)
@PluginDependency(EthanApiPlugin.class)
@Singleton
@Slf4j
public class Dt2HelperPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private Dt2Overlay overlay;

    @Inject
    private ClientThread clientThread;

    @Inject
    private EventBus eventBus;

    @Getter(AccessLevel.PACKAGE)
    @Inject
    private ForsakenAssassin forsakenAssassin;
    
    @Getter(AccessLevel.PACKAGE)
    @Inject
    private KetlaTheUnworthy ketlaTheUnworthy;
    
    @Override
    protected void startUp() throws Exception {
        super.startUp();
        forsakenAssassin.init();
        ketlaTheUnworthy.init();
        eventBus.register(forsakenAssassin);
        eventBus.register(ketlaTheUnworthy);
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
        forsakenAssassin.reset();
        ketlaTheUnworthy.reset();
        eventBus.unregister(forsakenAssassin);
        eventBus.unregister(ketlaTheUnworthy);
        overlayManager.remove(overlay);
    }
//
    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        if(getForsakenAssassin().getNpc().nonEmpty() || getKetlaTheUnworthy().getTarget().nonEmpty()) return;
        Projectile projectile = event.getProjectile();
        log.info("projectile={} with id={} and remainingCycles={} at target={}", projectile, projectile.getId(), projectile.getRemainingCycles(), WorldPoint.fromLocal(client,projectile.getTarget()));
    }
//
    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if(getForsakenAssassin().getNpc().nonEmpty() || getKetlaTheUnworthy().getTarget().nonEmpty()) return;
        log.info("Npc {}[{}] spawned at {}", event.getNpc(),event.getNpc().getId(), event.getNpc().getWorldLocation());
    }
//
    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        if(getForsakenAssassin().getNpc().nonEmpty() || getKetlaTheUnworthy().getTarget().nonEmpty()) return;
        log.info("Npc {}[{}] despawned at {}", event.getNpc(), event.getNpc().getId(), event.getNpc().getWorldLocation());
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if(getForsakenAssassin().getNpc().nonEmpty() || getKetlaTheUnworthy().getTarget().nonEmpty()) return;
        if(event.getActor() instanceof  NPC) {
            NPC n = ((NPC)event.getActor());
            log.info("Npc {}[{}] at {} changed animation to {}", n, n.getId(), n.getWorldLocation(), n.getAnimation());
        }
    }
//
//    @Subscribe
//    public void onGameObjectSpawned(GameObjectSpawned event) {
//        if(!CLOUD_OBJECT_IDS.contains(event.getGameObject().getId())) return;
//        clouds.add(event.getGameObject());
//    }
//
//    @Subscribe
//    public void onGameObjectDespawned(GameObjectDespawned event) {
//        if(!CLOUD_OBJECT_IDS.contains(event.getGameObject().getId())) return;
//        clouds.remove(event.getGameObject());
//    }
}
