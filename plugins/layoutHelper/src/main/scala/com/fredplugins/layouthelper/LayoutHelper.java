package com.fredplugins.layouthelper;

import ch.qos.logback.classic.Level;
import com.fredplugins.common.utils.ReflectionUtils;
import com.fredplugins.layouthelper.LootBroadcastHelper.LootBroadcastMessage;
import ethanApiPlugin.EthanApiPlugin;
import ethanApiPlugin.collections.Widgets;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.MousePackets;
import packets.WidgetPackets;
import scala.collection.Seq$;
import scala.collection.immutable.List$;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.runelite.api.gameval.InterfaceID.Bankmain.POTIONSTORE_ITEMS;

@PluginDescriptor(
        name = "<html><font color=\"#32C8CD\">Freds</font> Layout Helper</html>",
        enabledByDefault = false,
        description = "Helps manage layout of widgets",
        tags = {"layout", "widget", "interface", "stretched", "helper", "fred4106"}
)
@Singleton
public class LayoutHelper extends Plugin {
    private final static Logger log;
    static {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LayoutHelper.class)).setLevel(Level.DEBUG);
        log = LoggerFactory.getLogger(LayoutHelper.class);
    }
    @Inject
    private Client client;

    @Inject
    private EventBus eventBus;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private LeaguesToggleHelper leaguesHelper;

    scala.collection.immutable.List<OverlayWidgetHelper> overlays = List$.MODULE$.empty();

    private void sendMessage(ChatMessageType tpe, String message) {
        chatMessageManager.queue(QueuedMessage.builder().type(tpe).runeLiteFormattedMessage(message).build());
    }

    @Override
    protected void startUp() throws Exception {
        super.startUp();
        eventBus.register(leaguesHelper);

        List.of(536, 537, 19272).stream().forEach(i -> {
            log.debug("id: {}, name: {}", i, ReflectionUtils.getItemName(i));
        });

        overlays = OverlayWidgetHelper.getOverlays(overlayManager).filter(o ->
            o.groupId() == 164 && o.childId() >= 94 && o.childId() <= 96
        );
        overlays.foreach(o -> {
            o.snappable_$eq(false);
            log.debug("{}", o);
            return -1;
        });

//        eventBus.register(LeaguesToggleHelper$.MODULE$);
    }

//    private boolean skillMultiOpened = false;
//    @Subscribe
//    private void onGameTick(GameTick e) {
//            Widget sw = client.getWidget(InterfaceID.Skillmulti.BOTTOM);
//            if(sw != null) {
////                skillMultiOpened = false;
//                List<Widget> multiOtions = IntStream.of(
//                    InterfaceID.Skillmulti.A,
//                    InterfaceID.Skillmulti.B,
//                    InterfaceID.Skillmulti.C,
//                    InterfaceID.Skillmulti.D,
//                    InterfaceID.Skillmulti.E,
//                    InterfaceID.Skillmulti.F,
//                    InterfaceID.Skillmulti.G,
//                    InterfaceID.Skillmulti.H,
//                    InterfaceID.Skillmulti.I,
//                    InterfaceID.Skillmulti.J,
//                    InterfaceID.Skillmulti.K,
//                    InterfaceID.Skillmulti.L,
//                    InterfaceID.Skillmulti.M,
//                    InterfaceID.Skillmulti.N,
//                    InterfaceID.Skillmulti.O,
//                    InterfaceID.Skillmulti.P,
//                    InterfaceID.Skillmulti.Q,
//                    InterfaceID.Skillmulti.R
//                ).mapToObj(i -> client.getWidget(i)).filter(w -> {
//                    return w.getName() != null && !w.getName().isEmpty();
//                }).collect(Collectors.toList());
//                
//                Helper
////                if(multiOtions.size() == 1) {
////                    Widget toClick = multiOtions.get(0);
//////                    MousePackets.queueClickPacket(toClick);
//////                    WidgetPackets.queueWidgetActionPacket(1, toClick.getId(), toClick.getItemId(), toClick.getIndex());
//////                    WidgetPackets.queueWidgetAction(toClick, "Item");
////                }
//            }
//    }
    
    @Subscribe
    private void onClientTick(ClientTick gt) {
        OverlayWidgetHelper.tick(overlays);
    }

    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
        eventBus.unregister(leaguesHelper);
        overlays.foreach(o -> {
            o.snappable_$eq(true);
            return -1;
        });
        overlays = List$.MODULE$.empty();
    }

    @Subscribe
    public void onMenuEntryAdded(final MenuEntryAdded event) {
        if(event.getMenuEntry().getType() == MenuAction.CC_OP_LOW_PRIORITY && event.getActionParam1() == POTIONSTORE_ITEMS && event.getOption().endsWith("Dose")) {
            event.getMenuEntry().setType(MenuAction.CC_OP);
        }

//		if (event.getMenuEntry().getItemId() != -1) {
//			System.out.println("MENU ENTRY ADDED | " +
//				"item id: " + event.getMenuEntry().getItemId() +
//				", option: " + event.getOption() +
//				", target: " + event.getTarget()
//			);
//		}
    }

    @Subscribe
    public void onScriptCallbackEvent(ScriptCallbackEvent event)
    {
        if (!"chatFilterCheck".equals(event.getEventName()))
        {
            return;
        }

        int[] intStack = client.getIntStack();
        int intStackSize = client.getIntStackSize();
        Object[] objectStack = client.getObjectStack();
        int objectStackSize = client.getObjectStackSize();

        final int messageType = intStack[intStackSize - 2];
        final int messageId = intStack[intStackSize - 1];
        String message = (String) objectStack[objectStackSize - 1];

        ChatMessageType chatMessageType = ChatMessageType.of(messageType);
        final MessageNode messageNode = client.getMessages().get(messageId);
        final String name = messageNode.getName();

        if((chatMessageType == ChatMessageType.GAMEMESSAGE || chatMessageType == ChatMessageType.SPAM) && name.isEmpty() && messageNode.getSender() == null) {
            LootBroadcastMessage lootMsg = LootBroadcastHelper$.MODULE$.parse(message);
            if(lootMsg != null) {
//                log.debug("Loot message {}", lootMsg);
//                intStack[intStackSize - 3] = 0;
            }
        }
    }

    @Subscribe(priority = -2) // run after ChatMessageManager
    public void onChatMessage(final ChatMessage event) {
        if(event.getType() == ChatMessageType.GAMEMESSAGE || event.getType() == ChatMessageType.SPAM && event.getSender() == null && event.getName().isEmpty()) {
            //log.debug("[onChatMessage] {}", event);
        }
    }
}
