package ethanApiPlugin.collections;

import ethanApiPlugin.collections.query.ItemQuery;
import ethanApiPlugin.EthanApiPlugin;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import packets.MousePackets;
import packets.WidgetPackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Bank {
	private static final int WITHDRAW_MODE = 3958;
	private static final int WITHDRAW_ITEM_MODE_WIDGET = 0x000c_0018;
	private static final int WITHDRAW_NOTE_MODE_WIDGET = 0x000c_001a;
	static Client client = RuneLite.getInjector().getInstance(Client.class);
	static List<Widget> bankItems = new ArrayList<>();
	boolean bankUpdate = true;
	static int lastUpdateTick = 0;

	public static ItemQuery search() {
		if (lastUpdateTick < client.getTickCount()) {
			Bank.bankItems.clear();
			int i = 0;
			if (client.getItemContainer(InventoryID.BANK) == null) {
				return new ItemQuery(new ArrayList<>());
			}
			for (Item item : client.getItemContainer(InventoryID.BANK).getItems()) {
				try {
					if (item == null) {
						i++;
						continue;
					}
					if (EthanApiPlugin.itemDefs.get(item.getId()).getPlaceholderTemplateId() == 14401) {
						i++;
						continue;
					}
					Bank.bankItems.add(new BankItemWidget(EthanApiPlugin.itemDefs.get(item.getId()).getName(), item.getId(), item.getQuantity(), i));
				} catch (NullPointerException | ExecutionException ex) {
					//todo fix this
				}
				i++;
			}
			lastUpdateTick = client.getTickCount();
		}
		return new ItemQuery(bankItems.stream().filter(Objects::nonNull).collect(Collectors.toList()));
	}

	public static boolean isOpen() {
		return client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER) != null && !client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER).isHidden();
	}


	public static void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.HOPPING || gameStateChanged.getGameState() == GameState.LOGIN_SCREEN || gameStateChanged.getGameState() == GameState.CONNECTION_LOST) {
			Bank.bankItems.clear();
		}
	}

	public static boolean isNotedMode() {
		return client.getVarbitValue(WITHDRAW_MODE) == 1;
	}

	public static void setWithdrawMode(boolean noted) {
		Widget w = null;
		String action = "";
		if (noted && !isNotedMode()) {
			w = client.getWidget(WITHDRAW_NOTE_MODE_WIDGET);
			action = "Note";
//            MousePackets.queueClickPacket();
//            WidgetPackets.queueWidgetActionPacket(1, 786458, -1, -1);
		} else if (!noted && isNotedMode()) {
			w = client.getWidget(WITHDRAW_ITEM_MODE_WIDGET);
			action = "Item";
//            MousePackets.queueClickPacket();
//            WidgetPackets.queueWidgetActionPacket(1, 786456, -1, -1);
		}
		if (w != null && w.getBounds() != null && w.getActions() != null) {
			MousePackets.queueClickPacket(w.getBounds());
			WidgetPackets.queueWidgetAction(w, action);
		}
	}
}