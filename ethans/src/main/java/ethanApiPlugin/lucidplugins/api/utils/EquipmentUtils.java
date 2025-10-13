package ethanApiPlugin.lucidplugins.api.utils;

import ethanApiPlugin.collections.Equipment;
import ethanApiPlugin.collections.EquipmentItemWidget;
import ethanApiPlugin.lucidplugins.api.item.SlottedItem;
import net.runelite.api.EquipmentInventorySlot;
import packets.MousePackets;
import packets.WidgetPackets;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.List;
import java.util.stream.Collectors;

public class EquipmentUtils
{

    static Client client = RuneLite.getInjector().getInstance(Client.class);

    public static List<SlottedItem> getAll()
    {
        return Equipment.search().result().stream().map(equipmentItemWidget -> new SlottedItem(equipmentItemWidget.getEquipmentItemId(), equipmentItemWidget.getItemQuantity(), equipmentItemWidget.getEquipmentIndex())).collect(Collectors.toList());
    }

    public static List<SlottedItem> getAll(Predicate<SlottedItem> filter)
    {
        return Equipment.search().result().stream().map(equipmentItemWidget -> new SlottedItem(equipmentItemWidget.getEquipmentItemId(), equipmentItemWidget.getEquipmentItemQty(), equipmentItemWidget.getEquipmentIndex())).filter(filter).collect(Collectors.toList());
    }

    public static Item getItemInSlot(EquipmentInventorySlot slot) {
        return Equipment.search().slotIs(slot).first().map(EquipmentItemWidget::getEquipmentItem).orElse(new Item(-1, 0));
    }

    public static Item getWepSlotItem()
    {
		return getItemInSlot(EquipmentInventorySlot.WEAPON);
    }

    public static Item getShieldSlotItem()
    {
        return getItemInSlot(EquipmentInventorySlot.SHIELD);
    }

    public static boolean contains(int id)
    {
        return !Equipment.search().withId(id).result().isEmpty();
    }

    public static boolean contains(int[] ids)
    {
        List<Integer> intIdList = Arrays.stream(ids).boxed().collect(Collectors.toList());

        return !Equipment.search().idInList(intIdList).result().isEmpty();
    }

    public static boolean contains(String name)
    {
        return !Equipment.search().nameContains(name).result().isEmpty();
    }

    public static void removeWepSlotItem()
    {
        EquipmentItemWidget itemWidget = Equipment.search().slotIs(EquipmentInventorySlot.WEAPON).first().orElse(null);

        if (itemWidget != null)
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(itemWidget, "Remove");
        }
    }
}
