package com.fredplugins.layouthelper;

import ch.qos.logback.classic.Level;
import com.fredplugins.common.utils.ReflectionUtils;
import com.fredplugins.layouthelper.LootBroadcastHelper.LootBroadcastMessage;
import com.google.inject.Provides;
import ethanApiPlugin.EthanApiPlugin;
import ethanApiPlugin.collections.Widgets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayOrigin;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packets.MousePackets;
import packets.WidgetPackets;
import scala.collection.Seq$;
import scala.collection.immutable.List$;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.runelite.api.gameval.InterfaceID.Bankmain.POTIONSTORE_ITEMS;

@PluginDescriptor(
        name = "<html><font color=\"#32C8CD\">Freds</font> Layout Helper</html>",
        enabledByDefault = false,
        description = "Helps manage layout of widgets",
        tags = {"layout", "widget", "interface", "stretched", "helper", "fred4106"},
        conflicts = {"Logout Timer"}
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

    @Inject
    private LayoutHelperConfig config;

    scala.collection.immutable.Seq<OverlayWidgetHelper> overlays = List$.MODULE$.empty();

    private void sendMessage(ChatMessageType tpe, String message) {
        chatMessageManager.queue(QueuedMessage.builder().type(tpe).runeLiteFormattedMessage(message).build());
    }

    private Pair<Field, Integer> idleTimeoutField = null;

    public static Pair<Field, Integer> findIdleTimeoutField(Client client) throws IllegalAccessException {
        Field toRet = null;
        int stockTimeoutValue = client.getIdleTimeout();
        int testValue = 42069;
        client.setIdleTimeout(testValue);
        for (Field declaredField : client.getClass().getDeclaredFields()) {
            if(toRet != null) continue;
            if (declaredField.getType() == int.class && Modifier.isStatic(declaredField.getModifiers())) {
                declaredField.setAccessible(true);
                int value = declaredField.getInt(null);
                declaredField.setAccessible(false);
                if (value != testValue) {
                    continue;
                }
                log.debug("found idle ticks field: {} with value {}" , declaredField.getName(), declaredField.get(null));
                toRet = declaredField;
            }
        }
        client.setIdleTimeout(stockTimeoutValue);
        return Pair.of(toRet, stockTimeoutValue);
    }

    private static int minutesToClientTicks(int min) {
        return (int) (Duration.ofMinutes(min).toMillis() / Constants.CLIENT_TICK_LENGTH);
    }

    private static String clientTicksToDuration(int cticks) {
        long millisDur =  Constants.CLIENT_TICK_LENGTH * cticks;
        return DurationFormatUtils.formatDurationHMS(millisDur);
    }

    public void setIdleTimeoutInCTicks(int cTicks) {
        boolean didSet = false;
        if(idleTimeoutField != null) {
            Field f = idleTimeoutField.getKey();
            try {
                f.setAccessible(true);
                f.setInt(null, cTicks);
                didSet = true;
            } catch (IllegalAccessException e) {
                log. error("Problem setting idle timeout", e);
            } finally {
                f.setAccessible(false);
            }
        }
        if(!didSet) {
            client.setIdleTimeout(cTicks);
        }
    }

    public String getIdleTimeout() {
        int cTicks = -1;
        if(idleTimeoutField != null) {
            Field f = idleTimeoutField.getKey();
            try {
                f.setAccessible(true);
                cTicks = f.getInt(null);
            } catch (IllegalAccessException e) {
                log. error("Problem getting idle timeout", e);
                cTicks = client.getIdleTimeout();
            } finally {
                f.setAccessible(false);
            }
        } else {
            cTicks = client.getIdleTimeout();
        }
        return clientTicksToDuration(cTicks);
    }

    @Provides
    LayoutHelperConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(LayoutHelperConfig.class);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged)
    {
        if (configChanged.getGroup().equals(LayoutHelperConfig.GROUP) && configChanged.getKey().equals("idleTimeout"))
        {
            setIdleTimeoutInCTicks(minutesToClientTicks(config.getIdleTimeout()));
        }
    }

    @Override
    protected void startUp() throws Exception {
        super.startUp();
        idleTimeoutField = findIdleTimeoutField(client);
        setIdleTimeoutInCTicks(minutesToClientTicks(config.getIdleTimeout()));
        if (idleTimeoutField != null) {
            log.debug("idleTimeoutField was ({}, {}) and real value {}",idleTimeoutField.getKey(), clientTicksToDuration(idleTimeoutField.getRight()), getIdleTimeout());
        }

        eventBus.register(leaguesHelper);
        List.of(536, 537, 19272).stream().forEach(i -> {
            log.debug("id: {}, name: {}", i, ReflectionUtils.getItemName(i));
        });

        OverlayWidgetHelper TABS1 = OverlayWidgetHelper.getOverlay(overlayManager, InterfaceID.ToplevelPreEoc.SIDE_STATIC_LAYER).getOrElse(null);
        OverlayWidgetHelper TABS2 = OverlayWidgetHelper.getOverlay(overlayManager, InterfaceID.ToplevelPreEoc.SIDE_MOVABLE_LAYER).getOrElse(null);
        OverlayWidgetHelper INVENTORY_PARENT = OverlayWidgetHelper.getOverlay(overlayManager, InterfaceID.ToplevelPreEoc.SIDE_CONTAINER).getOrElse(null);

        overlays = (scala.collection.immutable.Seq<OverlayWidgetHelper>) overlays.appended(TABS1).appended(TABS2);
        overlays = (scala.collection.immutable.Seq<OverlayWidgetHelper>) overlays.appended(INVENTORY_PARENT);

//        overlays = OverlayWidgetHelper.getOverlays(overlayManager).filter(o ->
//            o.groupId() == 164 && o.childId() >= 94 && o.childId() <= 96
//        );
//        overlays = OverlayWidgetHelper.getOverlays(overlayManager, new int[] {InterfaceID.ToplevelPreEoc.SIDE_STATIC_LAYER, InterfaceID.ToplevelPreEoc.SIDE_MOVABLE_LAYER, InterfaceID.ToplevelPreEoc.SIDE_CONTAINER});
        overlays.foreach(o -> {
            o.snappable_$eq(false);
            if(o != INVENTORY_PARENT) {
                o.movable_$eq(false);
                o.origin_$eq(OverlayOrigin.SIDEPANEL);
                o.revalidate();
            }
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
            o.movable_$eq(true);
            o.reset(overlayManager);
            o.revalidate();
            return -1;
        });
        overlays = List$.MODULE$.empty();
        if(idleTimeoutField != null) {
            log.debug("idleTimeoutField was ({}, {}) and real value {}",idleTimeoutField.getKey(), clientTicksToDuration(idleTimeoutField.getRight()), getIdleTimeout());
            setIdleTimeoutInCTicks(idleTimeoutField.getRight());
        }
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
