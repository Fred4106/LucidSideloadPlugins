package com.fredplugins.gauntlet.overlay;

import com.fredplugins.common.OldOverlayUtil;
import com.fredplugins.gauntlet.FredGauntletConfig;
import com.fredplugins.gauntlet.FredGauntletPlugin;
import com.fredplugins.gauntlet.entity.Hunllef;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Point;

import java.awt.*;

@Singleton
public class OverlayPrayerWidget extends Overlay
{
    private final Client client;
    private final FredGauntletPlugin plugin;
    private final FredGauntletConfig config;

    @Inject
    OverlayPrayerWidget(final Client client, final FredGauntletPlugin plugin, final FredGauntletConfig config)
    {
        super(plugin);

        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        determineLayer();
    }

    @Override
    public Dimension render(final Graphics2D graphics2D)
    {
        final FredGauntletConfig.PrayerHighlightMode prayerHighlightMode = config.prayerOverlay();

        if (prayerHighlightMode == FredGauntletConfig.PrayerHighlightMode.NONE || prayerHighlightMode == FredGauntletConfig.PrayerHighlightMode.BOX)
        {
            return null;
        }

        final Hunllef hunllef = plugin.getHunllef();

        if (hunllef == null)
        {
            return null;
        }

        final NPC npc = hunllef.getNpc();

        if (npc == null || npc.isDead())
        {
            return null;
        }

        // Overlay outline on the prayer widget

        final Hunllef.AttackPhase phase = hunllef.getAttackPhase();

        final Prayer prayer = phase.getPrayer();

        final Color phaseColor = phase.getColor();

        final Rectangle rectangle = OldOverlayUtil.renderPrayerOverlay(graphics2D, client, prayer, phaseColor);

        if (rectangle == null)
        {
            return null;
        }

        // Overlay tick count on the prayer widget

        final int ticksUntilAttack = hunllef.getTicksUntilNextAttack();

        final String text = String.valueOf(ticksUntilAttack);

        final int fontSize = 16;
        final int fontStyle = Font.BOLD;
        final Color fontColor = ticksUntilAttack == 1 ? Color.WHITE : phaseColor;

        final int x = (int) (rectangle.getX() + rectangle.getWidth() / 2);
        final int y = (int) (rectangle.getY() + rectangle.getHeight() / 2);

        final Point point = new Point(x, y);

        final Point canvasPoint = new Point((int) (point.getX() - 3), (int) (point.getY() + 6));

        OldOverlayUtil.renderTextLocation(graphics2D, text, fontSize, fontStyle, fontColor, canvasPoint, true, 0);

        return null;
    }

    public void determineLayer()
    {
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }
}