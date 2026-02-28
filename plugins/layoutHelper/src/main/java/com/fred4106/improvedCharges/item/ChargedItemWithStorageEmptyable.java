package com.fred4106.improvedCharges.item;

import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.store.Provider;

public class ChargedItemWithStorageEmptyable extends ChargedItemWithStorage {
	public ChargedItemWithStorageEmptyable(final String configKey, final int itemId, final Provider provider) {
		super(configKey, itemId, provider);

		this.triggers.add(
			new OnChatMessage("You empty all of your containers into the bank").emptyStorage()
		);
	}
}