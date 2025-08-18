package com.fredplugins.common.magic;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class SpellInfo {
    private String name;
    private int spriteId;
    private SpellCost spellCost;
    private SpellProduct[] spellProducts;

    public SpellInfo(String name, int spriteId, SpellCost spellCost, SpellProduct... products)
    {
        this(name, spriteId, spellCost);
        this.spellProducts = products;
    }

    public SpellInfo(String name, int spriteId, SpellCost spellCost)
    {
        this.name = name;
        this.spriteId = spriteId;
        this.spellCost = spellCost;
    }

	@Override
	public String toString() {
		return "SpellInfo{" +
			"name='" + name + '\'' +
			", spriteId=" + spriteId +
			", spellCost=" + spellCost +
			", spellProducts=" + Arrays.toString(spellProducts) +
			'}';
	}
}