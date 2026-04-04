package com.fredplugins.mixology;

import java.awt.Color;
import net.runelite.api.gameval.SpriteID;

public enum PotionComponent {
    MOX('M', "03a9f4", SpriteID.IconAlchemyChemicals01_20x20._0, FredsMixologyPlugin.VARP_MOX_RESIN),
    AGA('A', "00e676", SpriteID.IconAlchemyChemicals01_20x20._1, FredsMixologyPlugin.VARP_AGA_RESIN),
    LYE('L', "e91e63", SpriteID.IconAlchemyChemicals01_20x20._2, FredsMixologyPlugin.VARP_LYE_RESIN);

    public static final PotionComponent[] ENTRIES = values();

    private final char character;
    private final String colorCode;
    private final Color color;
    private final int spriteId;
    private final int resinVarpId;

    PotionComponent(char character, String colorCode, int spriteId, int resinVarpId) {
        this.character = character;
        this.colorCode = colorCode;
        this.color = Color.decode("#" + colorCode);
        this.spriteId = spriteId;
        this.resinVarpId = resinVarpId;
    }

    public char character() {
        return character;
    }

    public String colorCode() {
        return colorCode;
    }

    public Color color() {
        return color;
    }

    public int spriteId() {
        return spriteId;
    }

    public int resinVarpId() {
        return resinVarpId;
    }
}
