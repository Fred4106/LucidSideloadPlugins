package com.fredplugins.common.magic;

import com.fredplugins.common.constants.Rune;
import com.google.common.primitives.Ints;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.client.RuneLite;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class RuneChanges {
	static Client client = RuneLite.getInjector().getInstance(Client.class);
    private Map<Integer, Integer> changes;
    private Map<Integer, Integer> currentRunes;
    private Set<Integer> unlimitedRunes;

    public RuneChanges(Map<Integer, Integer> changes, Map<Integer, Integer> currentRunes, Set<Integer> unlimitedRunes)
    {
        this.changes = changes;//Map.<Rune, Integer>ofEntries(Arrays.stream(((Map.Entry<Integer, Integer>[])changes.entrySet().toArray(new Entry[0]))).map(e -> Map.entry(Rune.getRuneFromItemId(e.getKey()).get(), e.getValue())).collect(Collectors.toList()).toArray(new Map.Entry[0]));
        this.currentRunes = currentRunes;//Map.<Rune, Integer>ofEntries(Arrays.stream(((Map.Entry<Integer, Integer>[])currentRunes.entrySet().toArray(new Entry[0]))).map(e -> Map.entry(Rune.getRuneFromItemId(e.getKey()).get(), e.getValue())).collect(Collectors.toList()).toArray(new Map.Entry[0]));
        this.unlimitedRunes = unlimitedRunes;//unlimitedRunes.stream().flatMap(i -> Rune.getRuneFromItemId(i).stream()).collect(Collectors.toSet());
    }

    public Map<Integer, Integer> getInvertedChanges()
    {
        Map<Integer, Integer> costMap = new HashMap<>(changes);
        costMap.replaceAll((k,v) -> v * -1);
        return costMap;
    }

	public Map<Rune, Integer> getRuneChanges() {
		return Map.<Rune, Integer>ofEntries(changes.entrySet().stream().flatMap(e -> Rune.getRuneFromItemId(e.getKey()).stream().map(r -> Map.entry(r, e.getValue()))).collect(Collectors.toList()).toArray(new Map.Entry[0]));
	}
	public Map<Rune, Integer> getNonRuneChanges() {
		return Map.<String, Integer>ofEntries(changes.entrySet().stream().filter(e -> Rune.getRuneFromItemId(e.getKey()).isEmpty()).map(e -> Map.entry( client.getItemDefinition(e.getKey()).getName(), e.getValue())).collect(Collectors.toList()).toArray(new Map.Entry[0]));
	}

	@Override
	public String toString() {
		return "RuneChanges{" +
			"\n\truneChanges=" + getRuneChanges() +
			"\n\tnonRunChanges=" + getNonRuneChanges() +
			"\n\tchanges=" + changes +
			",\n\tcurrentRunes=" + currentRunes +
			",\n\tunlimitedRunes=" + unlimitedRunes +
			"\n}";
	}
}