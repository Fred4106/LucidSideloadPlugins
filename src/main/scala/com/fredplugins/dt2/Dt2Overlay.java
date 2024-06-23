package com.fredplugins.dt2;

import com.example.EthanApiPlugin.Collections.TileObjects;
import com.fredplugins.demonicgorilla.ChaosDemonicGorilla;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class Dt2Overlay extends Overlay {

    private static final Color COLOR_ICON_BACKGROUND = new Color(0, 0, 0, 128);
    private static final Color COLOR_ICON_BORDER = new Color(0, 0, 0, 255);
    private static final Color COLOR_ICON_BORDER_FILL = new Color(219, 175, 0, 255);
    private static final int OVERLAY_ICON_DISTANCE = 50;
    private static final int OVERLAY_ICON_MARGIN = 8;

    private final Client client;
    private final Dt2HelperPlugin plugin;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    public Dt2Overlay(final Client client, final Dt2HelperPlugin plugin, final ModelOutlineRenderer modelOutlineRenderer) {
        this.client = client;
        this.plugin = plugin;
        this.modelOutlineRenderer = modelOutlineRenderer;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(PRIORITY_DEFAULT);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics2D) {
        for (TileObject cloud : plugin.getClouds()) {
            LocalPoint lp = cloud.getLocalLocation();
            if (lp != null) {
                final Polygon polygon = Perspective.getCanvasTilePoly(client, lp);

                if (polygon == null)
                {
                    continue;
                }
                
                Color c;
                if(cloud.getId() == Dt2HelperPlugin.WHITE_CLOUD_ID) c = Color.WHITE;
                else if(cloud.getId() == Dt2HelperPlugin.PINK_CLOUD_ID) c = Color.PINK;
                else c = Color.RED;
                drawOutlineAndFill(graphics2D, c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 50), 2, polygon);
            }
        }
        NPC n = plugin.getForsakenAssassin();
        if(n != null) {
            Color c = Color.MAGENTA;
            LocalPoint lp = n.getLocalLocation();
            if (lp != null) {
                final Polygon polygon = Perspective.getCanvasTilePoly(client, lp);
                
                if (polygon != null) {
                    drawOutlineAndFill(graphics2D, c, new Color( c.getRed(), c.getGreen(), c.getBlue(), 50),
                            2, polygon);
                }
            }

            modelOutlineRenderer.drawOutline(n, 2, c, 0);
        }

        return null;
    }

    static void drawOutlineAndFill(final Graphics2D graphics2D, final Color outlineColor, final Color fillColor, final float strokeWidth, final Shape shape)
    {
        final Color originalColor = graphics2D.getColor();
        final Stroke originalStroke = graphics2D.getStroke();

        graphics2D.setStroke(new BasicStroke(strokeWidth));
        graphics2D.setColor(outlineColor);
        graphics2D.draw(shape);

        graphics2D.setColor(fillColor);
        graphics2D.fill(shape);

        graphics2D.setColor(originalColor);
        graphics2D.setStroke(originalStroke);
    }
}