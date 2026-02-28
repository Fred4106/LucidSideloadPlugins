package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.FredsItemChargesConfig;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.OnMenuOptionClicked;
import com.fred4106.improvedCharges.item.triggers.OnWidgetLoaded;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.ItemId;

import java.util.List;

public class J_SailorsAmulet extends ChargedItem {
	public J_SailorsAmulet(final Provider provider) {
		super(Constants.SAILORS_AMULET, ItemId.SAILORS_AMULET, provider);

		this.items = new TriggerItem[]{
			new TriggerItem(ItemId.SAILORS_AMULET_UNCHARGED).fixedCharges(0),
			new TriggerItem(ItemId.SAILORS_AMULET)
		};

		this.triggers.addAll(List.of(
			// Check
			new OnChatMessage("(The|Your) amulet has (?<charges>.+) charges.")
				.setDynamicallyCharges()
				.onItemClick(),
			// Charge
			new OnChatMessage("You add \\d+ charges? to your amulet\\. It now has (?<charges>\\d+) charges?\\.")
				.setDynamicallyCharges(),
			// Teleport
			new OnMenuOptionClicked("The Pandemonium", "Port Roberts", "Deepfin Point")
				.onMenuOptionId(65540, 131076, 327684)
				.decreaseCharges(1)
		));
	}
}