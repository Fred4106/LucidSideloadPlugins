package com.fredplugins.mixology;

import net.runelite.api.Client;

import java.util.EnumMap;
import java.util.Map;

public class Goal {
    private RewardItem rewardItem;
    private double overallProgress = 0.0;
    private int itemsAffordable = 0;
    private int rewardQuantity = 1;

    /*
     * Whenever we recalculate the goal, we will create a new ComponentData object for each component for caching
     */
    private final Map<PotionComponent, ComponentData> componentDataMap = new EnumMap<>(PotionComponent.class);

    public Goal(RewardItem rewardItem) {
        this.rewardItem = rewardItem;
    }

    public RewardItem getRewardItem() {
        return rewardItem;
    }

    public double getOverallProgress() {
        return overallProgress;
    }

    public int getItemsAffordable() {
        return itemsAffordable;
    }

    public int getRewardQuantity() {
        return rewardQuantity;
    }

    public ComponentData getComponentData(PotionComponent component) {
        return componentDataMap.get(component);
    }

    public void recalculate(FredsMixologyConfig config, Client client) {
        rewardItem = config.selectedReward();
        rewardQuantity = rewardItem.isRepeatable() ? config.rewardQuantity() : 1;

        // Create the component data for each component
        for (var component : PotionComponent.ENTRIES) {
            int currentAmount = client.getVarpValue(component.resinVarpId());
            int baseGoalAmount = rewardItem.componentCost(component);
            componentDataMap.put(component, new ComponentData(currentAmount, baseGoalAmount, rewardQuantity));
        }

        // Calculate the amount of items affordable based on the component with the lowest affordable amount
        int minAffordable = componentDataMap.values().stream()
                .mapToInt(data -> data.affordableAmount)
                .min()
                .orElse(0);
        itemsAffordable = Math.min(minAffordable, rewardQuantity);

        // Overall progress is the average of all component progress
        overallProgress = componentDataMap.values().stream()
                .mapToDouble(data -> data.percentageToGoal)
                .average()
                .orElse(0.0);
    }

    public static class ComponentData {
        final int currentAmount;
        final int goalAmount;
        final double percentageToGoal;
        final int affordableAmount;

        ComponentData(int currentAmount, int baseGoalAmount, int rewardQuantity) {
            this.currentAmount = currentAmount;
            this.goalAmount = baseGoalAmount * rewardQuantity;

            if (goalAmount == 0) {
                this.percentageToGoal = 1.0;
                this.affordableAmount = rewardQuantity;
            } else {
                this.percentageToGoal = Math.min((double) currentAmount / goalAmount, 1.0);
                this.affordableAmount = currentAmount / baseGoalAmount;
            }
        }
    }
}
