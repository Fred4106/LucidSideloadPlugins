package com.fredplugins.layouthelper;

//import ethanApiPlugin.EthanApiPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;
import scala.collection.immutable.List$;


import javax.inject.Inject;
import javax.inject.Singleton;

@Extension
@PluginDescriptor(
        name = "<html><font color=\"#32C8CD\">Freds</font> Layout Helper</html>",
        enabledByDefault = false,
        description = "Helps manage layout of widgets",
        tags = {"layout", "widget", "interface", "stretched", "helper", "fred4106"}
)
//@PluginDependency(EthanApiPlugin.class)
@Singleton
@Slf4j
public class LayoutHelper extends Plugin {

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ChatMessageManager chatMessageManager;
    
    scala.collection.immutable.List<OverlayWidgetHelper> overlays = List$.MODULE$.empty();

    private void sendMessage(ChatMessageType tpe, String message) {
        chatMessageManager.queue(QueuedMessage.builder().type(tpe).runeLiteFormattedMessage(message).build());
    }

    @Override
    protected void startUp() throws Exception {
        super.startUp();

        overlays = OverlayWidgetHelper.getOverlays(overlayManager).filter(o ->
            o.groupId() == 164 && o.childId() >= 94 && o.childId() <= 96
        );
        overlays.foreach(o -> {
            o.snappable_$eq(false);
            log.info("{}", o);
            return -1;
        });
    }

    @Subscribe
    private void onClientTick(ClientTick gt) {
        OverlayWidgetHelper.tick(overlays);
    }
    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
        overlays.foreach(o -> {
            o.snappable_$eq(true);
            return -1;
        });
        overlays = List$.MODULE$.empty();
    }
}
