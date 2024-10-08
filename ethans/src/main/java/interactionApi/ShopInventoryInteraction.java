package interactionApi;

import ethanApiPlugin.collections.ShopInventory;
import packets.MousePackets;
import packets.WidgetPackets;
import net.runelite.api.widgets.Widget;

import java.util.Optional;
import java.util.function.Predicate;

public class ShopInventoryInteraction {

    public static boolean useItem(String name, String... actions) {
        return ShopInventory.search().withName(name).first().flatMap(item ->
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(item, actions);
            return Optional.of(true);
        }).orElse(false);
    }

    public static boolean useItem(int id, String... actions) {
        return ShopInventory.search().withId(id).first().flatMap(item ->
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(item, actions);
            return Optional.of(true);
        }).orElse(false);
    }

    public static boolean useItem(Predicate<? super Widget> predicate, String... actions) {
        return ShopInventory.search().filter(predicate).first().flatMap(item ->
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(item, actions);
            return Optional.of(true);
        }).orElse(false);
    }

    public static boolean useItemIndex(int index, String... actions) {
        return ShopInventory.search().indexIs(index).first().flatMap(item ->
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(item, actions);
            return Optional.of(true);
        }).orElse(false);
    }

    public static boolean useItem(Widget item, String... actions) {
        if (item == null) {
            return false;
        }
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetAction(item, actions);
        return true;
    }
}
