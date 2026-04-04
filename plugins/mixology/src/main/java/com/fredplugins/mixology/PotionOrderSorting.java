package com.fredplugins.mixology;

import java.util.Comparator;

public enum PotionOrderSorting {
    VANILLA("Vanilla (random)"),
    BY_STATION("By station"),
    SHORTEST_PATH("Shortest Path");

    private final String name;
    PotionOrderSorting(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }
}