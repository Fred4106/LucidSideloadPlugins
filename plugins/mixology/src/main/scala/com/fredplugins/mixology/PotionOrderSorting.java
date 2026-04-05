package com.fredplugins.mixology;

import java.util.Comparator;

public enum PotionOrderSorting {
    VANILLA("Vanilla (random)"),

    // Sort by modifier, in the order CRYSTALISED > HOMOGENOUS > CONCENTRATED
    // And then by PotionType name alphabetically
    BY_STATION("By station", Comparator
        .comparingInt((PotionOrder order) -> {
            switch (order.potionModifier()) {
                case CRYSTALISED:
                    return 0;
                case HOMOGENOUS:
                    return 1;
                case CONCENTRATED:
                    return 2;
                default:
                    throw new IllegalStateException("Unexpected value: " + order.potionModifier().toString());
            }
        })
        .thenComparing(order -> order.potionType().name())
    ),

    // Sort by modifier, in the order CRYSTALISED > CONCENTRATED > HOMOGENOUS
    // And then by PotionType name alphabetically
    SHORTEST_PATH("Shortest Path", Comparator
        .comparingInt((PotionOrder order) -> {
            switch (order.potionModifier()) {
                case CRYSTALISED:
                    return 1;
                case CONCENTRATED:
                    return 2;
                case HOMOGENOUS:
                    return 3;
                default:
                    throw new IllegalStateException("Unexpected value: " + order.potionModifier().toString());
            }
        })
        .thenComparing(order -> order.potionType().name())
    );

    private final String name;
    private final Comparator<PotionOrder> comparator;


    PotionOrderSorting(String name) {
        this(name, Comparator.comparing(PotionOrder::idx));
    }
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
