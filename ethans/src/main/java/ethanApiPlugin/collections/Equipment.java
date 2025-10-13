package ethanApiPlugin.collections;

import ethanApiPlugin.collections.query.EquipmentItemQuery;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.ItemContainer;
import net.runelite.api.annotations.Interface;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InterfaceID.Wornitems;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class Equipment {
	static Client client = RuneLite.getInjector().getInstance(Client.class);
	static ClientThread clientThread = RuneLite.getInjector().getInstance(ClientThread.class);
	static List<EquipmentItemWidget> equipment = new ArrayList<>();
	static HashMap<Integer, Integer> equipmentSlotWidgetMapping = new HashMap<>();
	static HashMap<Integer, EquipmentInventorySlot> widgetToEquipmentSlotMapping = new HashMap<>();
//	static HashMap<Integer, Integer> mappingToEquipmentSlot = new HashMap<>();
	static int lastUpdateTick = 0;

	static HashMap<EquipmentInventorySlot, Integer> equipmentSlotToWidget= new HashMap<>();

	static {
		equipmentSlotWidgetMapping.put(0, 15);
		equipmentSlotWidgetMapping.put(1, 16);
		equipmentSlotWidgetMapping.put(2, 17);
		equipmentSlotWidgetMapping.put(3, 18);
		equipmentSlotWidgetMapping.put(4, 19);
		equipmentSlotWidgetMapping.put(5, 20);
		equipmentSlotWidgetMapping.put(7, 21);
		equipmentSlotWidgetMapping.put(9, 22);
		equipmentSlotWidgetMapping.put(10, 23);
		equipmentSlotWidgetMapping.put(12, 24);
		equipmentSlotWidgetMapping.put(13, 25);

		widgetToEquipmentSlotMapping.put(15, EquipmentInventorySlot.HEAD);
		widgetToEquipmentSlotMapping.put(16, EquipmentInventorySlot.CAPE);
		widgetToEquipmentSlotMapping.put(17, EquipmentInventorySlot.AMULET);
		widgetToEquipmentSlotMapping.put(18, EquipmentInventorySlot.WEAPON);
		widgetToEquipmentSlotMapping.put(19, EquipmentInventorySlot.BODY);
		widgetToEquipmentSlotMapping.put(20, EquipmentInventorySlot.SHIELD);
		widgetToEquipmentSlotMapping.put(21, EquipmentInventorySlot.LEGS);
		widgetToEquipmentSlotMapping.put(22, EquipmentInventorySlot.GLOVES);
		widgetToEquipmentSlotMapping.put(23, EquipmentInventorySlot.BOOTS);
		widgetToEquipmentSlotMapping.put(24, EquipmentInventorySlot.RING);
		widgetToEquipmentSlotMapping.put(25, EquipmentInventorySlot.AMMO);

		equipmentSlotToWidget.put(EquipmentInventorySlot.HEAD, Wornitems.SLOT0);
		equipmentSlotToWidget.put(EquipmentInventorySlot.CAPE, Wornitems.SLOT1);
		equipmentSlotToWidget.put(EquipmentInventorySlot.AMULET, Wornitems.SLOT2);
		equipmentSlotToWidget.put(EquipmentInventorySlot.WEAPON, Wornitems.SLOT3);
		equipmentSlotToWidget.put(EquipmentInventorySlot.BODY, Wornitems.SLOT4);
		equipmentSlotToWidget.put(EquipmentInventorySlot.SHIELD, Wornitems.SLOT5);
		equipmentSlotToWidget.put(EquipmentInventorySlot.LEGS, Wornitems.SLOT7);
		equipmentSlotToWidget.put(EquipmentInventorySlot.GLOVES, Wornitems.SLOT9);
		equipmentSlotToWidget.put(EquipmentInventorySlot.BOOTS, Wornitems.SLOT10);
		equipmentSlotToWidget.put(EquipmentInventorySlot.RING, Wornitems.SLOT12);
		equipmentSlotToWidget.put(EquipmentInventorySlot.AMMO, Wornitems.SLOT13);
//		mappingToIterableIntegers.put(1, 1);
//		mappingToIterableIntegers.put(2, 2);
//		mappingToIterableIntegers.put(3, 3);
//		mappingToIterableIntegers.put(4, 4);
//		mappingToIterableIntegers.put(5, 5);
//		mappingToIterableIntegers.put(6, 7);
//		mappingToIterableIntegers.put(7, 9);
//		mappingToIterableIntegers.put(8, 10);
//		mappingToIterableIntegers.put(9, 12);
//		mappingToIterableIntegers.put(10, 13);
	}
	public static EquipmentItemQuery search() {
		if (lastUpdateTick < client.getTickCount()) {
			clientThread.invoke(() -> {
					equipmentSlotToWidget.entrySet().forEach(slot -> {
						client.runScript(545, slot.getValue(), slot.getKey().getSlotIdx(), 1, 1, 2);
					});
				}
			);
			equipment.clear();
			ItemContainer ic = client.getItemContainer(InventoryID.WORN);
			if (ic != null) {
				int idx = -1;
				for (Item item : ic.getItems()) {
					idx++;
					if (item == null) {
						continue;
					}
					if (item.getId() == 6512 || item.getId() == -1) {
						continue;
					}
					Widget w = client.getWidget(WidgetInfo.EQUIPMENT.getGroupId(), equipmentSlotWidgetMapping.get(idx));
					if (w == null || w.getActions() == null) {
						continue;
					}
					EquipmentInventorySlot slot = widgetToEquipmentSlotMapping.get(WidgetInfo.TO_CHILD(w.getId()));
					equipment.add(new EquipmentItemWidget(item, w, slot, idx));
				}
//					return equipmentSlotToWidget.entrySet().stream().map(e -> {
//						Item item = ic.getItem(e.getKey().getSlotIdx());
//						if(item == null || item.getId()== -1 || item.getId() == 6512) {
//							return null;
//						}
//						Widget w=client.getWidget(e.getValue());
//						if(w == null || w.getActions() == null) {
//							return null;
//						}
//						return new EquipmentItemWidget(item, w, e.getKey());
//					}).collect(Collectors.toList());
			}
			lastUpdateTick = client.getTickCount();
		}
		return new EquipmentItemQuery(equipment);
	}

//    @SneakyThrows
//    @Subscribe
//    public void onItemContainerChanged(ItemContainerChanged e) {
//        if (e.getContainerId() == InventoryID.EQUIPMENT.getId()) {
//            int x = 25362447;
//            for (int i = 0; i < 11; i++) {
//                client.runScript(545, (x + i), mappingToIterableIntegers.get(i), 1, 1, 2);
//            }
//            equipment.clear();
//            int i = -1;
//            for (Item item : e.getItemContainer().getItems()) {
//                i++;
//                if (item == null) {
//                    continue;
//                }
//                if (item.getId() == 6512 || item.getId() == -1) {
//                    continue;
//                }
//                Widget w = client.getWidget(WidgetInfo.EQUIPMENT.getGroupId(), equipmentSlotWidgetMapping.get(i));
//                if (w == null || w.getActions() == null) {
//                    continue;
//                }
//                equipment.add(new EquipmentItemWidget(w.getName(), item.getId(), w.getId(), i, w.getActions()));
//            }
//        }
//    }
}