package com.fredplugins.dynamicHighlights;

import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

public class Icons {
    public static final BufferedImage FOLDER;
    public static final BufferedImage OVERLAY_DISABLED;
    public static final BufferedImage PANEL_ICON;
    public static final BufferedImage CLIPBOARD_PASTE;
    public static final BufferedImage RELOAD;

    static {
        FOLDER = ImageUtil.loadImageResource(Icons.class, "/com/fredplugins/dynamicHighlights/icons/folder_icon.png");
        OVERLAY_DISABLED = ImageUtil.loadImageResource(Icons.class, "/com/fredplugins/dynamicHighlights/icons/overlay_disabled.png");
        PANEL_ICON = ImageUtil.loadImageResource(Icons.class, "/com/fredplugins/dynamicHighlights/icons/panel.png");
        CLIPBOARD_PASTE = ImageUtil.loadImageResource(Icons.class, "/com/fredplugins/dynamicHighlights/icons/paste_icon.png");
        RELOAD = ImageUtil.loadImageResource(Icons.class, "/com/fredplugins/dynamicHighlights/icons/reload_icon.png");
    }

    private Icons() {
    }
}
