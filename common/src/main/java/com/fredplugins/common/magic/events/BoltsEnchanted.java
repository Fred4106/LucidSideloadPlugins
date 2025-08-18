package com.fredplugins.common.magic.events;
import com.fredplugins.common.magic.RuneChanges;
import com.fredplugins.common.magic.SpellInfo;
import lombok.Getter;

@Getter
public class BoltsEnchanted {
    private SpellInfo enchantSpell;

    private RuneChanges changes;

    public BoltsEnchanted(SpellInfo enchantSpell, RuneChanges changes)
    {
        this.enchantSpell = enchantSpell;
        this.changes = changes;
    }

	@Override
	public String toString() {
		return "BoltsEnchanted{" +
			"enchantSpell=" + enchantSpell +
			", changes=" + changes +
			'}';
	}
}