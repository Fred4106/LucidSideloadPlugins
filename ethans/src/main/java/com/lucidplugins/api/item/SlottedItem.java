package com.lucidplugins.api.item;

import lombok.Getter;
import net.runelite.api.Item;

import java.util.Objects;

public class SlottedItem
{
    @Getter
    private int slot = -1;

    @Getter
    private Item item;

    public SlottedItem(int id, int quantity, int slotP)
    {
        assert(id > -1 && quantity > -1 && slotP > -1);
        this.item = new Item(id, quantity);
        this.slot = slotP;
    }
    public SlottedItem(Item itemP, int slotP)
    {
        assert(itemP != null && itemP.getId() > -1 && itemP.getQuantity() > -1 && slotP > -1);
        this.item = itemP;
        this.slot = slotP;
    }

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		SlottedItem that = (SlottedItem) o;
		return this.slot == that.slot && this.item.getId() == that.item.getId() && this.item.getQuantity() == that.getItem().getQuantity();
	}

	@Override
	public int hashCode() {
		return Objects.hash(slot, item);
	}

	//    public static SlottedItem fromItem(Item item, int slot)
//    {
//        assert(item != null && item.getId() > -1 && item.getQuantity() > -1 && slot > -1);
//        return new SlottedItem(item.getId(), item.getQuantity(), slot);
//    }
}