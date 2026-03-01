package com.fredplugins.dynamicHighlights;

import com.fredplugins.dynamicHighlights.model.BufferedImageProvider;
import com.fredplugins.dynamicHighlights.model.FontType;
import com.fredplugins.dynamicHighlights.model.SoundProvider;
import com.fredplugins.dynamicHighlights.model.TextAccent;
import net.runelite.client.ui.FontManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DisplayConfigOld {
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

    public DisplayConfigOld(Color textColor) {
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

    public DisplayConfigOld(Color textColor, Color backgroundColor, Color borderColor, Boolean hidden, Boolean showLootbeam, Boolean showValue, Boolean compact, Boolean showDespawn, Boolean notify, TextAccent textAccent, Color textAccentColor, Color lootbeamColor, FontType fontType, Color menuTextColor, Boolean hideOverlay, Boolean highlightTile, Color tileStrokeColor, Color tileFillColor, SoundProvider sound, Integer menuSort, BufferedImageProvider icon, List<Integer> evalTrace) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.hidden = hidden;
        this.showLootbeam = showLootbeam;
        this.showValue = showValue;
        this.compact = compact;
        this.showDespawn = showDespawn;
        this.notify = notify;
        this.textAccent = textAccent;
        this.textAccentColor = textAccentColor;
        this.lootbeamColor = lootbeamColor;
        this.fontType = fontType;
        this.menuTextColor = menuTextColor;
        this.hideOverlay = hideOverlay;
        this.highlightTile = highlightTile;
        this.tileStrokeColor = tileStrokeColor;
        this.tileFillColor = tileFillColor;
        this.sound = sound;
        this.menuSort = menuSort;
        this.icon = icon;
        this.evalTrace = evalTrace;
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

    public DisplayConfigOld merge(DisplayConfigOld other) {
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

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public Color getBorderColor() {
        return this.borderColor;
    }

    public Boolean getHidden() {
        return this.hidden;
    }

    public Boolean getShowLootbeam() {
        return this.showLootbeam;
    }

    public Boolean getShowValue() {
        return this.showValue;
    }

    public Boolean getCompact() {
        return this.compact;
    }

    public Boolean getShowDespawn() {
        return this.showDespawn;
    }

    public Boolean getNotify() {
        return this.notify;
    }

    public TextAccent getTextAccent() {
        return this.textAccent;
    }

    public Color getTextAccentColor() {
        return this.textAccentColor;
    }

    public FontType getFontType() {
        return this.fontType;
    }

    public Boolean getHideOverlay() {
        return this.hideOverlay;
    }

    public Boolean getHighlightTile() {
        return this.highlightTile;
    }

    public Color getTileFillColor() {
        return this.tileFillColor;
    }

    public List<Integer> getEvalTrace() {
        return this.evalTrace;
    }

    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DisplayConfigOld))
            return false;
        final DisplayConfigOld other = (DisplayConfigOld) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$textColor = this.getTextColor();
        final Object other$textColor = other.getTextColor();
        if (this$textColor == null ? other$textColor != null : !this$textColor.equals(other$textColor))
            return false;
        final Object this$backgroundColor = this.getBackgroundColor();
        final Object other$backgroundColor = other.getBackgroundColor();
        if (this$backgroundColor == null ? other$backgroundColor != null : !this$backgroundColor.equals(other$backgroundColor))
            return false;
        final Object this$borderColor = this.getBorderColor();
        final Object other$borderColor = other.getBorderColor();
        if (this$borderColor == null ? other$borderColor != null : !this$borderColor.equals(other$borderColor))
            return false;
        final Object this$hidden = this.getHidden();
        final Object other$hidden = other.getHidden();
        if (this$hidden == null ? other$hidden != null : !this$hidden.equals(other$hidden))
            return false;
        final Object this$showLootbeam = this.getShowLootbeam();
        final Object other$showLootbeam = other.getShowLootbeam();
        if (this$showLootbeam == null ? other$showLootbeam != null : !this$showLootbeam.equals(other$showLootbeam))
            return false;
        final Object this$showValue = this.getShowValue();
        final Object other$showValue = other.getShowValue();
        if (this$showValue == null ? other$showValue != null : !this$showValue.equals(other$showValue))
            return false;
        final Object this$compact = this.getCompact();
        final Object other$compact = other.getCompact();
        if (this$compact == null ? other$compact != null : !this$compact.equals(other$compact))
            return false;
        final Object this$showDespawn = this.getShowDespawn();
        final Object other$showDespawn = other.getShowDespawn();
        if (this$showDespawn == null ? other$showDespawn != null : !this$showDespawn.equals(other$showDespawn))
            return false;
        final Object this$notify = this.getNotify();
        final Object other$notify = other.getNotify();
        if (this$notify == null ? other$notify != null : !this$notify.equals(other$notify))
            return false;
        final Object this$textAccent = this.getTextAccent();
        final Object other$textAccent = other.getTextAccent();
        if (this$textAccent == null ? other$textAccent != null : !this$textAccent.equals(other$textAccent))
            return false;
        final Object this$textAccentColor = this.getTextAccentColor();
        final Object other$textAccentColor = other.getTextAccentColor();
        if (this$textAccentColor == null ? other$textAccentColor != null : !this$textAccentColor.equals(other$textAccentColor))
            return false;
        final Object this$lootbeamColor = this.getLootbeamColor();
        final Object other$lootbeamColor = other.getLootbeamColor();
        if (this$lootbeamColor == null ? other$lootbeamColor != null : !this$lootbeamColor.equals(other$lootbeamColor))
            return false;
        final Object this$fontType = this.getFontType();
        final Object other$fontType = other.getFontType();
        if (this$fontType == null ? other$fontType != null : !this$fontType.equals(other$fontType))
            return false;
        final Object this$menuTextColor = this.getMenuTextColor();
        final Object other$menuTextColor = other.getMenuTextColor();
        if (this$menuTextColor == null ? other$menuTextColor != null : !this$menuTextColor.equals(other$menuTextColor))
            return false;
        final Object this$hideOverlay = this.getHideOverlay();
        final Object other$hideOverlay = other.getHideOverlay();
        if (this$hideOverlay == null ? other$hideOverlay != null : !this$hideOverlay.equals(other$hideOverlay))
            return false;
        final Object this$highlightTile = this.getHighlightTile();
        final Object other$highlightTile = other.getHighlightTile();
        if (this$highlightTile == null ? other$highlightTile != null : !this$highlightTile.equals(other$highlightTile))
            return false;
        final Object this$tileStrokeColor = this.getTileStrokeColor();
        final Object other$tileStrokeColor = other.getTileStrokeColor();
        if (this$tileStrokeColor == null ? other$tileStrokeColor != null : !this$tileStrokeColor.equals(other$tileStrokeColor))
            return false;
        final Object this$tileFillColor = this.getTileFillColor();
        final Object other$tileFillColor = other.getTileFillColor();
        if (this$tileFillColor == null ? other$tileFillColor != null : !this$tileFillColor.equals(other$tileFillColor))
            return false;
        final Object this$sound = this.getSound();
        final Object other$sound = other.getSound();
        if (this$sound == null ? other$sound != null : !this$sound.equals(other$sound))
            return false;
        final Object this$menuSort = this.getMenuSort();
        final Object other$menuSort = other.getMenuSort();
        if (this$menuSort == null ? other$menuSort != null : !this$menuSort.equals(other$menuSort))
            return false;
        final Object this$icon = this.getIcon();
        final Object other$icon = other.getIcon();
        if (this$icon == null ? other$icon != null : !this$icon.equals(other$icon))
            return false;
        final Object this$evalTrace = this.getEvalTrace();
        final Object other$evalTrace = other.getEvalTrace();
        if (this$evalTrace == null ? other$evalTrace != null : !this$evalTrace.equals(other$evalTrace))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DisplayConfigOld;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $textColor = this.getTextColor();
        result = result * PRIME + ($textColor == null ? 43 : $textColor.hashCode());
        final Object $backgroundColor = this.getBackgroundColor();
        result = result * PRIME + ($backgroundColor == null ? 43 : $backgroundColor.hashCode());
        final Object $borderColor = this.getBorderColor();
        result = result * PRIME + ($borderColor == null ? 43 : $borderColor.hashCode());
        final Object $hidden = this.getHidden();
        result = result * PRIME + ($hidden == null ? 43 : $hidden.hashCode());
        final Object $showLootbeam = this.getShowLootbeam();
        result = result * PRIME + ($showLootbeam == null ? 43 : $showLootbeam.hashCode());
        final Object $showValue = this.getShowValue();
        result = result * PRIME + ($showValue == null ? 43 : $showValue.hashCode());
        final Object $compact = this.getCompact();
        result = result * PRIME + ($compact == null ? 43 : $compact.hashCode());
        final Object $showDespawn = this.getShowDespawn();
        result = result * PRIME + ($showDespawn == null ? 43 : $showDespawn.hashCode());
        final Object $notify = this.getNotify();
        result = result * PRIME + ($notify == null ? 43 : $notify.hashCode());
        final Object $textAccent = this.getTextAccent();
        result = result * PRIME + ($textAccent == null ? 43 : $textAccent.hashCode());
        final Object $textAccentColor = this.getTextAccentColor();
        result = result * PRIME + ($textAccentColor == null ? 43 : $textAccentColor.hashCode());
        final Object $lootbeamColor = this.getLootbeamColor();
        result = result * PRIME + ($lootbeamColor == null ? 43 : $lootbeamColor.hashCode());
        final Object $fontType = this.getFontType();
        result = result * PRIME + ($fontType == null ? 43 : $fontType.hashCode());
        final Object $menuTextColor = this.getMenuTextColor();
        result = result * PRIME + ($menuTextColor == null ? 43 : $menuTextColor.hashCode());
        final Object $hideOverlay = this.getHideOverlay();
        result = result * PRIME + ($hideOverlay == null ? 43 : $hideOverlay.hashCode());
        final Object $highlightTile = this.getHighlightTile();
        result = result * PRIME + ($highlightTile == null ? 43 : $highlightTile.hashCode());
        final Object $tileStrokeColor = this.getTileStrokeColor();
        result = result * PRIME + ($tileStrokeColor == null ? 43 : $tileStrokeColor.hashCode());
        final Object $tileFillColor = this.getTileFillColor();
        result = result * PRIME + ($tileFillColor == null ? 43 : $tileFillColor.hashCode());
        final Object $sound = this.getSound();
        result = result * PRIME + ($sound == null ? 43 : $sound.hashCode());
        final Object $menuSort = this.getMenuSort();
        result = result * PRIME + ($menuSort == null ? 43 : $menuSort.hashCode());
        final Object $icon = this.getIcon();
        result = result * PRIME + ($icon == null ? 43 : $icon.hashCode());
        final Object $evalTrace = this.getEvalTrace();
        result = result * PRIME + ($evalTrace == null ? 43 : $evalTrace.hashCode());
        return result;
    }

    public String toString() {
        return "DisplayConfig(textColor=" + this.getTextColor() + ", backgroundColor=" + this.getBackgroundColor() + ", borderColor=" + this.getBorderColor() + ", hidden=" + this.getHidden() + ", showLootbeam=" + this.getShowLootbeam() + ", showValue=" + this.getShowValue() + ", compact=" + this.getCompact() + ", showDespawn=" + this.getShowDespawn() + ", notify=" + this.getNotify() + ", textAccent=" + this.getTextAccent() + ", textAccentColor=" + this.getTextAccentColor() + ", lootbeamColor=" + this.getLootbeamColor() + ", fontType=" + this.getFontType() + ", menuTextColor=" + this.getMenuTextColor() + ", hideOverlay=" + this.getHideOverlay() + ", highlightTile=" + this.getHighlightTile() + ", tileStrokeColor=" + this.getTileStrokeColor() + ", tileFillColor=" + this.getTileFillColor() + ", sound=" + this.getSound() + ", menuSort=" + this.getMenuSort() + ", icon=" + this.getIcon() + ", evalTrace=" + this.getEvalTrace() + ")";
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

        public DisplayConfigOld build() {
            return new DisplayConfigOld(this.textColor, this.backgroundColor, this.borderColor, this.hidden, this.showLootbeam, this.showValue, this.compact, this.showDespawn, this.notify, this.textAccent, this.textAccentColor, this.lootbeamColor, this.fontType, this.menuTextColor, this.hideOverlay, this.highlightTile, this.tileStrokeColor, this.tileFillColor, this.sound, this.menuSort, this.icon, this.evalTrace);
        }

        public String toString() {
            return "DisplayConfig.DisplayConfigBuilder(textColor=" + this.textColor + ", backgroundColor=" + this.backgroundColor + ", borderColor=" + this.borderColor + ", hidden=" + this.hidden + ", showLootbeam=" + this.showLootbeam + ", showValue=" + this.showValue + ", compact=" + this.compact + ", showDespawn=" + this.showDespawn + ", notify=" + this.notify + ", textAccent=" + this.textAccent + ", textAccentColor=" + this.textAccentColor + ", lootbeamColor=" + this.lootbeamColor + ", fontType=" + this.fontType + ", menuTextColor=" + this.menuTextColor + ", hideOverlay=" + this.hideOverlay + ", highlightTile=" + this.highlightTile + ", tileStrokeColor=" + this.tileStrokeColor + ", tileFillColor=" + this.tileFillColor + ", sound=" + this.sound + ", menuSort=" + this.menuSort + ", icon=" + this.icon + ", evalTrace=" + this.evalTrace + ")";
        }
    }
}
