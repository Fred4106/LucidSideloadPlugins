package com.fredplugins.mixology;

import net.runelite.api.TileObject;

import java.awt.*;

public class HighlightedObject {

    private final TileObject object;
    private final Color color;
    private final int outlineWidth;
    private final int feather;

    HighlightedObject(TileObject object, Color color, int outlineWidth, int feather) {
        this.object = object;
        this.color = color;
        this.outlineWidth = outlineWidth;
        this.feather = feather;
    }

    public TileObject object() {
        return object;
    }

    public Color color() {
        return color;
    }

    public int outlineWidth() {
        return outlineWidth;
    }

    public int feather() {
        return feather;
    }
}