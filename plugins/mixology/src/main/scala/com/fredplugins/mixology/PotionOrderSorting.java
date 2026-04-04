package com.fredplugins.mixology;

import java.util.Comparator;

public enum PotionOrderSorting {
    VANILLA("Vanilla (random)", null),
    BY_STATION("By station", PotionComparators.byStation()),
    SHORTEST_PATH("Shortest Path", PotionComparators.shortestPath());

    private final String name;
    private final Comparator<PotionOrder> comparator;

    PotionOrderSorting(String name, Comparator<PotionOrder> comparator) {
        this.name = name;
        this.comparator = comparator;
    }

    public Comparator<PotionOrder> comparator() {
        return comparator;
    }

    @Override
    public String toString() {
        return name;
    }
}
