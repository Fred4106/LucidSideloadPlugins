package com.fredplugins.common.magic;

import lombok.Getter;

@Getter
public class SpellProduct {
    public int itemId;
    public int quantity;

    public SpellProduct(int id, int quantity)
    {
        this.itemId = id;
        this.quantity = quantity;
    }

    @Override
    public int hashCode()
    {
        return itemId * quantity * 31;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
            return true;

        if (!(o instanceof SpellProduct))
            return false;

        return this.hashCode() == o.hashCode();
    }

	@Override
	public String toString() {
		return "SpellProduct{" +
			"itemId=" + itemId +
			", quantity=" + quantity +
			'}';
	}
}