package com.fredplugins.dynamicHighlights;

import com.fredplugins.dynamicHighlights.model.BufferedImageProvider;
import com.fredplugins.dynamicHighlights.model.FontType;
import com.fredplugins.dynamicHighlights.model.SoundProvider;
import com.fredplugins.dynamicHighlights.model.TextAccent;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.runelite.client.ui.FontManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DisplayConfig {
    public static final Color DEFAULT_MENU_TEXT_COLOR = Color.decode("#ff9040");

    private final Color textColor;
    private final Color backgroundColor;
    private final Color borderColor;
    private final Boolean hidden;
    private final Boolean showLootbeam;
    private final Boolean showValue;
    private final Boolean compact;
    private final Boolean showDespawn;
    private final Boolean notify;
    private final TextAccent textAccent;
    private final Color textAccentColor;
    private final Color lootbeamColor;
    private final FontType fontType;
    private final Color menuTextColor;
    private final Boolean hideOverlay;

    private final Boolean highlightTile;
    private final Color tileStrokeColor;
    private final Color tileFillColor;

    private final SoundProvider sound;
    private final Integer menuSort;
    private final BufferedImageProvider icon;

    // ideally this would be in EvalDisplayConfig which extends DisplayConfig but that's just more code tbh
    private final List<Integer> evalTrace;

    public DisplayConfig(Color textColor) {
        this.textColor = textColor;
        backgroundColor = null;
        borderColor = null;
        hidden = false;
        showLootbeam = false;
        showValue = false;
        compact = false; // compact is currently a config-only setting and not supported in rs2f
        showDespawn = false;
        notify = false;
        textAccent = null;
        textAccentColor = null;
        lootbeamColor = null;
        fontType = null;
        menuTextColor = null;
        highlightTile = null;
        tileStrokeColor = null;
        tileFillColor = null;
        hideOverlay = null;
        sound = null;
        menuSort = null;
        icon = null;
        evalTrace = new ArrayList<>();
    }

    public static DisplayConfigBuilder builder() {
        return new DisplayConfigBuilder();
    }

    public SoundProvider getSound() {
        return !isHidden() ? sound : null;
    }

    public Color getLootbeamColor() {
        return lootbeamColor != null ? lootbeamColor : textColor;
    }

    public Color getTextColor() {
        return textColor != null ? textColor : Color.WHITE;
    }

    public BufferedImageProvider getIcon() {
        return isCompact() ? new BufferedImageProvider.CurrentItem() : icon;
    }

    public Color getMenuTextColor() {
        if (isHidden()) {
            return DEFAULT_MENU_TEXT_COLOR;
        }
        if (menuTextColor != null) {
            return menuTextColor;
        }
        return textColor != null && !textColor.equals(Color.WHITE) ? textColor : DEFAULT_MENU_TEXT_COLOR;
    }

    public int getMenuSort() {
        return menuSort != null ? menuSort : 0;
    }

    public Font getFont() {
        if (fontType == null || fontType == FontType.NORMAL) {
            return FontManager.getRunescapeSmallFont();
        }
        if (fontType == FontType.LARGER) {
            return FontManager.getRunescapeFont();
        }
        return FontManager.getRunescapeBoldFont();
    }

    public Color getTileStrokeColor() {
        return tileStrokeColor != null ? tileStrokeColor : textColor;
    }

    public boolean isHidden() {
        return hidden != null && hidden;
    }

    public boolean isShowLootbeam() {
        return !isHidden() && showLootbeam != null && showLootbeam;
    }

    public boolean isShowValue() {
        return showValue != null && showValue;
    }

    public boolean isShowDespawn() {
        return showDespawn != null && showDespawn;
    }

    public boolean isNotify() {
        return !isHidden() && notify != null && notify;
    }

    public boolean isHighlightTile() {
        return !isHidden() && highlightTile != null && highlightTile;
    }

    public boolean isHideOverlay() {
        return isHidden() || (hideOverlay != null && hideOverlay);
    }

    public boolean isCompact() {
        return compact != null && compact;
    }

    public DisplayConfig merge(DisplayConfig other) {
        var b = toBuilder();
        if (other.textColor != null) {
            b.textColor(other.textColor);
        }
        if (other.backgroundColor != null) {
            b.backgroundColor(other.backgroundColor);
        }
        if (other.borderColor != null) {
            b.borderColor(other.borderColor);
        }
        if (other.hidden != null) {
            b.hidden(other.hidden);
        }
        if (other.showLootbeam != null) {
            b.showLootbeam(other.showLootbeam);
        }
        if (other.showValue != null) {
            b.showValue(other.showValue);
        }
        if (other.compact != null) {
            b.compact(other.compact);
        }
        if (other.showDespawn != null) {
            b.showDespawn(other.showDespawn);
        }
        if (other.notify != null) {
            b.notify(other.notify);
        }
        if (other.textAccent != null) {
            b.textAccent(other.textAccent);
        }
        if (other.textAccentColor != null) {
            b.textAccentColor(other.textAccentColor);
        }
        if (other.lootbeamColor != null) {
            b.lootbeamColor(other.lootbeamColor);
        }
        if (other.fontType != null) {
            b.fontType(other.fontType);
        }
        if (other.menuTextColor != null) {
            b.menuTextColor(other.menuTextColor);
        }
        if (other.highlightTile != null) {
            b.highlightTile(other.highlightTile);
        }
        if (other.tileStrokeColor != null) {
            b.tileStrokeColor(other.tileStrokeColor);
        }
        if (other.tileFillColor != null) {
            b.tileFillColor(other.tileFillColor);
        }
        if (other.hideOverlay != null) {
            b.hideOverlay(other.hideOverlay);
        }
        if (other.sound != null) {
            b.sound(other.sound);
        }
        if (other.menuSort != null) {
            b.menuSort(other.menuSort);
        }
        if (other.icon != null) {
            b.icon(other.icon);
        }
        return b.build();
    }

    public DisplayConfigBuilder toBuilder() {
        return new DisplayConfigBuilder().textColor(this.textColor).backgroundColor(this.backgroundColor).borderColor(this.borderColor).hidden(this.hidden).showLootbeam(this.showLootbeam).showValue(this.showValue).compact(this.compact).showDespawn(this.showDespawn).notify(this.notify).textAccent(this.textAccent).textAccentColor(this.textAccentColor).lootbeamColor(this.lootbeamColor).fontType(this.fontType).menuTextColor(this.menuTextColor).hideOverlay(this.hideOverlay).highlightTile(this.highlightTile).tileStrokeColor(this.tileStrokeColor).tileFillColor(this.tileFillColor).sound(this.sound).menuSort(this.menuSort).icon(this.icon).evalTrace(this.evalTrace);
    }

    public static class DisplayConfigBuilder {
        private Color textColor;
        private Color backgroundColor;
        private Color borderColor;
        private Boolean hidden;
        private Boolean showLootbeam;
        private Boolean showValue;
        private Boolean compact;
        private Boolean showDespawn;
        private Boolean notify;
        private TextAccent textAccent;
        private Color textAccentColor;
        private Color lootbeamColor;
        private FontType fontType;
        private Color menuTextColor;
        private Boolean hideOverlay;
        private Boolean highlightTile;
        private Color tileStrokeColor;
        private Color tileFillColor;
        private SoundProvider sound;
        private Integer menuSort;
        private BufferedImageProvider icon;
        private List<Integer> evalTrace;

        DisplayConfigBuilder() {
        }

        public DisplayConfigBuilder textColor(Color textColor) {
            this.textColor = textColor;
            return this;
        }

        public DisplayConfigBuilder backgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public DisplayConfigBuilder borderColor(Color borderColor) {
            this.borderColor = borderColor;
            return this;
        }

        public DisplayConfigBuilder hidden(Boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public DisplayConfigBuilder showLootbeam(Boolean showLootbeam) {
            this.showLootbeam = showLootbeam;
            return this;
        }

        public DisplayConfigBuilder showValue(Boolean showValue) {
            this.showValue = showValue;
            return this;
        }

        public DisplayConfigBuilder compact(Boolean compact) {
            this.compact = compact;
            return this;
        }

        public DisplayConfigBuilder showDespawn(Boolean showDespawn) {
            this.showDespawn = showDespawn;
            return this;
        }

        public DisplayConfigBuilder notify(Boolean notify) {
            this.notify = notify;
            return this;
        }

        public DisplayConfigBuilder textAccent(TextAccent textAccent) {
            this.textAccent = textAccent;
            return this;
        }

        public DisplayConfigBuilder textAccentColor(Color textAccentColor) {
            this.textAccentColor = textAccentColor;
            return this;
        }

        public DisplayConfigBuilder lootbeamColor(Color lootbeamColor) {
            this.lootbeamColor = lootbeamColor;
            return this;
        }

        public DisplayConfigBuilder fontType(FontType fontType) {
            this.fontType = fontType;
            return this;
        }

        public DisplayConfigBuilder menuTextColor(Color menuTextColor) {
            this.menuTextColor = menuTextColor;
            return this;
        }

        public DisplayConfigBuilder hideOverlay(Boolean hideOverlay) {
            this.hideOverlay = hideOverlay;
            return this;
        }

        public DisplayConfigBuilder highlightTile(Boolean highlightTile) {
            this.highlightTile = highlightTile;
            return this;
        }

        public DisplayConfigBuilder tileStrokeColor(Color tileStrokeColor) {
            this.tileStrokeColor = tileStrokeColor;
            return this;
        }

        public DisplayConfigBuilder tileFillColor(Color tileFillColor) {
            this.tileFillColor = tileFillColor;
            return this;
        }

        public DisplayConfigBuilder sound(SoundProvider sound) {
            this.sound = sound;
            return this;
        }

        public DisplayConfigBuilder menuSort(Integer menuSort) {
            this.menuSort = menuSort;
            return this;
        }

        public DisplayConfigBuilder icon(BufferedImageProvider icon) {
            this.icon = icon;
            return this;
        }

        public DisplayConfigBuilder evalTrace(List<Integer> evalTrace) {
            this.evalTrace = evalTrace;
            return this;
        }

        public DisplayConfig build() {
            return new DisplayConfig(this.textColor, this.backgroundColor, this.borderColor, this.hidden, this.showLootbeam, this.showValue, this.compact, this.showDespawn, this.notify, this.textAccent, this.textAccentColor, this.lootbeamColor, this.fontType, this.menuTextColor, this.hideOverlay, this.highlightTile, this.tileStrokeColor, this.tileFillColor, this.sound, this.menuSort, this.icon, this.evalTrace);
        }

        public String toString() {
            return "DisplayConfig.DisplayConfigBuilder(textColor=" + this.textColor + ", backgroundColor=" + this.backgroundColor + ", borderColor=" + this.borderColor + ", hidden=" + this.hidden + ", showLootbeam=" + this.showLootbeam + ", showValue=" + this.showValue + ", compact=" + this.compact + ", showDespawn=" + this.showDespawn + ", notify=" + this.notify + ", textAccent=" + this.textAccent + ", textAccentColor=" + this.textAccentColor + ", lootbeamColor=" + this.lootbeamColor + ", fontType=" + this.fontType + ", menuTextColor=" + this.menuTextColor + ", hideOverlay=" + this.hideOverlay + ", highlightTile=" + this.highlightTile + ", tileStrokeColor=" + this.tileStrokeColor + ", tileFillColor=" + this.tileFillColor + ", sound=" + this.sound + ", menuSort=" + this.menuSort + ", icon=" + this.icon + ", evalTrace=" + this.evalTrace + ")";
        }
    }
}
