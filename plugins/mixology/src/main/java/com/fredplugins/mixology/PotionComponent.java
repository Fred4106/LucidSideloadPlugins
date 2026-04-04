package com.fredplugins.mixology;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fredplugins.common.extensions.ObjectExtensions$;
import com.google.common.collect.ImmutableMap;
import net.runelite.api.Client;
import net.runelite.api.TileObject;
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

    private static final ImmutableMap<Character, PotionComponent> lookupMap;
    static {
        var x = ImmutableMap.<Character, PotionComponent>builder();
        for (PotionComponent v : values()) {
            x.put(v.character, v);
        }
        lookupMap = x.build();
    }
    public static PotionComponent fromLetter(char c) {
        return lookupMap.get(c);
    }
    public static PotionComponent fromPedistal(Client client, TileObject to) {
        int toMorphId = ObjectExtensions$.MODULE$.morphId(to, client);
        int subVal = -1;
        if(to.getId() == 55392) {
            subVal = 54905;
        } else if(to.getId() == 55393) {
            subVal = 54908;
        } else if(to.getId() == 55394) {
            subVal = 54911;
        }
        if(toMorphId != -1 && to.getId() >= 55392 && to.getId() <= 55394 && subVal != -1) {
                switch(toMorphId - subVal) {
                    case 0:
                        return AGA;
                    case 1:
                        return LYE;
                    case 2:
                        return MOX;
                }
        }
        return null;
        //			Option.when(to.morphId != -1 && (55392 to 55394).contains(to.getId)) {
//				(to.morphId - (to.getId match {
//					case 55392 => 54905
//					case 55393 => 54908
//					case 55394 => 54911
//				}))
//			}.collect {
//				case 0 => Aga
//				case 1 => Lye
//				case 2 => Mox
//			}
    }
}