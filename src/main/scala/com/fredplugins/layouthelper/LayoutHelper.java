package com.fredplugins.layouthelper;

import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.fredplugins.dt2.Dt2Overlay;
import com.google.common.collect.ImmutableList;
import com.lucidplugins.api.utils.MessageUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Projectile;
import net.runelite.api.TileObject;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.WidgetOverlay;
import org.pf4j.Extension;
import scala.Tuple2;
import scala.Tuple2$;
import scala.collection.immutable.List$;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Extension
@PluginDescriptor(
        name = "<html><font color=\"#32C8CD\">Freds</font> Layout Helper</html>",
        enabledByDefault = false,
        description = "Helps manage layout of widgets",
        tags = {"layout", "widget", "interface", "stretched", "helper", "fred4106"}
)
@PluginDependency(EthanApiPlugin.class)
@Singleton
public class LayoutHelper extends Plugin {

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ClientThread clientThread;

    private OverlayWidgetHelper tab1 = null;
    private OverlayWidgetHelper tab2 = null;
    private OverlayWidgetHelper inventory = null;
    scala.collection.immutable.List<OverlayWidgetHelper> overlays = List$.MODULE$.empty();

    @Override
    protected void startUp() throws Exception {
        super.startUp();

        scala.collection.immutable.List<OverlayWidgetHelper> overlays = OverlayWidgetHelper.getOverlays(overlayManager).filter(o ->
            o.groupId() == 164 && o.childId() >= 94 && o.childId() <= 96
        );
        overlays.foreach(o -> {
            o.snappable_$eq(false);
            MessageUtils.addMessage(
                    "[" + o.groupId() + ":" + o.childId() + "] [" + o.name() + "]\n\t[" + o.preferredPosition() + "]\n\t[" + o.preferredLocation() + "]\n\t[snap: " + o.snappable() + ", moveable: " + o.movable() + ", resizeable: " + o.resizable() + "]",
                    Color.RED
            );
            return -1;
        });
//        for (OverlayWidgetHelper o : overlays) {
//            if(!o.movable() || !o.resettable()) {
//                String message = "[" + o.groupId() + ":" + o.childId() + "] [" + o.name() + "] [" + o.preferredPosition() + "] [" + o.preferredLocation() + "]";
//                MessageUtils.addMessage(message, Color.RED);
//            }
//        }
        tab1 = overlays.find(o ->
                o.groupId() == 164 && o.childId() == 94
        ).get();

        tab2 = overlays.find(o ->
                o.groupId() == 164 && o.childId() == 95
        ).get();

        inventory = overlays.find(o ->
                o.groupId() == 164 && o.childId() == 96
        ).get();
    }

    @Subscribe
    private void onClientTick(ClientTick gt) {
        boolean noMove = false;
        if(tab1.moved()) {
            if(tab1.preferredLocation() == null) {
                tab2.reset();
                inventory.reset();
            }else {
                tab2.preferredLocation_$eq(Tuple2.apply(tab1.preferredLocation().x, tab1.preferredLocation().y - 36));
                inventory.preferredLocation_$eq(Tuple2.apply(tab1.preferredLocation().x + 14, tab1.preferredLocation().y - 273 - 36));
            }
        } else if(tab2.moved()) {
            if(tab2.preferredLocation() == null) {
                tab1.reset();
                inventory.reset();
            } else {
                tab1.preferredLocation_$eq(Tuple2.apply(tab2.preferredLocation().x, tab2.preferredLocation().y + 36));
                inventory.preferredLocation_$eq(Tuple2.apply(tab2.preferredLocation().x + 14, tab2.preferredLocation().y - 273));
            }
        } else if(inventory.moved()) {
            if(inventory.preferredLocation() == null) {
                tab1.reset();
                tab2.reset();
            } else {
                tab1.preferredLocation_$eq(Tuple2.apply(inventory.preferredLocation().x - 14, inventory.preferredLocation().y + 273 + 36));
                tab2.preferredLocation_$eq(Tuple2.apply(inventory.preferredLocation().x - 14, inventory.preferredLocation().y + 273));
            }
        } else {
            noMove = true;
        }
        if(!noMove) {
            MessageUtils.addMessage(" tab1: " + tab1.preferredLocation(), Color.WHITE);
            MessageUtils.addMessage(" tab2: " + tab2.preferredLocation(), Color.GREEN);
            MessageUtils.addMessage("panel: " + inventory.preferredLocation(), Color.BLUE);
            tab2.cache();
            tab1.cache();
            inventory.cache();
        }
    }
    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
        tab1 = null;
        tab2 = null;
        inventory = null;
        overlays = List$.MODULE$.empty();
    }
}
