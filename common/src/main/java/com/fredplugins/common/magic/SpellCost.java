package com.fredplugins.common.magic;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class SpellCost {
    private final int[] runes;
    private final int[] quantities;

    public SpellCost(int[] runeIds, int[] quantities)
    {
        if (runeIds.length != quantities.length)
            throw new IndexOutOfBoundsException();

        this.runes = runeIds;
        this.quantities = quantities;
    }

    public boolean matchesCost(Map<Integer, Integer> runeCount, Set<Integer> unlimitedRunes)
    {
        for (int i = 0; i < runes.length; i++)
        {
            final int targetRuneId = runes[i];
            if (!unlimitedRunes.contains(targetRuneId) && (!runeCount.containsKey(targetRuneId) || runeCount.get(targetRuneId) < quantities[i]))
                return false;
        }

        return true;
    }
    public int getRemainingCasts(Map<Integer, Integer> runeCount)
    {
        final List<Integer> castsPerRune = new ArrayList<Integer>();
        for (int i = 0; i < runes.length; i++)
        {
            final int targetRuneId = runes[i];
            if (runeCount.containsKey(targetRuneId))
            {
                final int available = runeCount.get(targetRuneId);
                final int cost = quantities[i];
                final boolean isReqStaff = available >= 1 && RuneIds.isReqStaff(targetRuneId);

                if (available == Integer.MAX_VALUE || isReqStaff)
                {
                    castsPerRune.add(Integer.MAX_VALUE);
                }
                else
                {
                    castsPerRune.add(Math.floorDiv(available, cost));
                }
            }
            else
            {
                return 0;
            }
        }

        return Collections.min(castsPerRune);
    }

	@Override
	public String toString() {
		return "SpellCost{" +
			"runes=" + Arrays.toString(runes) +
			", quantities=" + Arrays.toString(quantities) +
			'}';
	}
}