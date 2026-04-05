package com.fredplugins.mixology;

import java.awt.Color;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarPlayerID;

public enum PotionComponent {
    MOX('M', "03a9f4", SpriteID.IconAlchemyChemicals01_20x20._0, VarPlayerID.MIXOLOGY_MOX_POINTS),
    AGA('A', "00e676", SpriteID.IconAlchemyChemicals01_20x20._1, VarPlayerID.MIXOLOGY_AGA_POINTS),
    LYE('L', "e91e63", SpriteID.IconAlchemyChemicals01_20x20._2, VarPlayerID.MIXOLOGY_LYE_POINTS);

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
