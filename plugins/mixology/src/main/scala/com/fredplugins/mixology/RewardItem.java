package com.fredplugins.mixology;

import net.runelite.api.gameval.ItemID;
import static com.fredplugins.mixology.PotionComponent.AGA;
import static com.fredplugins.mixology.PotionComponent.LYE;
import static com.fredplugins.mixology.PotionComponent.MOX;

public enum RewardItem {
    NONE("None", 0, false, 0, 0, 0),
    APPRENTICE_POTION_PACK("Apprentice Potion Pack", ItemID.MM_POTION_PACK_LOW, true, 420, 70, 30),
    ADEPT_POTION_PACK("Adept Potion Pack", ItemID.MM_POTION_PACK_MED, true, 180, 440, 70),
    EXPERT_POTION_PACK("Expert Potion Pack", ItemID.MM_POTION_PACK_HIGH, true, 410, 320, 480),
    PRESCRIPTION_GOGGLES("Prescription Goggles", ItemID.MM_ALCHEMIST_HAT, false, 8600, 7000, 9350),
    ALCHEMIST_LABCOAT("Alchemist Labcoat", ItemID.MM_ALCHEMIST_BODY, false, 2250, 2800, 3700),
    ALCHEMIST_PANTS("Alchemist Pants", ItemID.MM_ALCHEMIST_LEGS, false, 2250, 2800, 3700),
    ALCHEMIST_GLOVES("Alchemist Gloves", ItemID.MM_ALCHEMIST_GLOVES, false, 2250, 2800, 3700),
    REAGENT_POUCH("Reagent Pouch", ItemID.MM_SECONDARY_POUCH, false, 13800, 11200, 15100),
    POTION_STORAGE("Potion Storage", ItemID.MM_POTION_STORAGE_UNLOCK_DUMMY, false, 7750, 6300, 8950),
    CHUGGING_BARREL("Chugging Barrel", ItemID.MM_PREPOT_DEVICE, false, 17250, 14000, 18600),
    ALCHEMISTS_AMULET("Alchemist's Amulet", ItemID.AMULET_OF_CHEMISTRY_IMBUED_UNCHARGED, false, 6900, 5650, 7400),
    ALDARIUM("Aldarium", ItemID.ALDARIUM, true, 80, 60, 90);

    private final String itemName;
    private final int itemId;
    private final boolean repeatable;
    private final int[] componentCost = new int[PotionComponent.ENTRIES.length];

    RewardItem(String itemName, int itemId, boolean repeatable, int moxResinCost, int agaResinCost, int lyeResinCost) {
        this.itemName = itemName;
        this.itemId = itemId;
        this.repeatable = repeatable;
        this.componentCost[MOX.ordinal()] = moxResinCost;
        this.componentCost[AGA.ordinal()] = agaResinCost;
        this.componentCost[LYE.ordinal()] = lyeResinCost;
    }

    public String itemName() {
        return itemName;
    }

    public int itemId() {
        return itemId;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public int componentCost(PotionComponent component) {
        return componentCost[component.ordinal()];
    }
}