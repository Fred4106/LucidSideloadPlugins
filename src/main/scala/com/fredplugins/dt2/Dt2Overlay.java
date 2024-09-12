package com.fredplugins.dt2;

import com.fredplugins.common.OldOverlayUtil;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.Optional;

import static com.fredplugins.dt2.ForsakenAssassin.*;

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
        if(plugin.getForsakenAssassin().getNpc().isDefined()) {
            renderForsaken(graphics2D, plugin.getForsakenAssassin());
        }
        if(plugin.getKetlaTheUnworthy().getTarget().isDefined()) {
            renderKetla(graphics2D, plugin.getKetlaTheUnworthy());
        }
        return null;
    }

    public Dimension renderForsaken(Graphics2D graphics2D, ForsakenAssassin forsaken) {
        for (ForsakenCloud cloud : forsaken.getClouds()) {
            LocalPoint lp = cloud.c().getLocalLocation();
            if (lp != null) {
                final Polygon polygon = Perspective.getCanvasTilePoly(client, lp);

                if (polygon == null)
                {
                    continue;
                }
                
                Color c = cloud.tpe().color();
                OldOverlayUtil.drawOutlineAndFill(graphics2D, c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 50), 2, polygon);
            }
        }

        for (ForsakenProjectile vial : forsaken.getVials()) {
            LocalPoint tlp = vial.destinationPoint();
            if(tlp != null) {
                final Polygon polygon = Perspective.getCanvasTilePoly(client, tlp);

                if (polygon == null)
                {
                    continue;
                }

                Color c = vial.tpe().color();
                OldOverlayUtil.drawOutlineAndFill(graphics2D, c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 25), 2, polygon);
            }
        }

        NPC n = forsaken.getNpc().getOrElse(() -> null);
        if(n != null) {
            Color c = Color.RED;
            if(forsaken.getClouds().stream().filter(cc -> cc.tpe().entryName().equals("White")).anyMatch(cc -> cc.c().getWorldLocation().equals(n.getWorldLocation()))) {
                c = Color.GREEN;
            }
            LocalPoint lp = n.getLocalLocation();
            if(lp != null) {
                final Polygon polygon = Perspective.getCanvasTilePoly(client, lp);

                if(polygon != null) {
                    OldOverlayUtil.drawOutlineAndFill(graphics2D, c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 50),
                        2, polygon);
                }
            }

            modelOutlineRenderer.drawOutline(n, 2, c, 0);
        }
        return null;
    }

    public Dimension renderKetla(Graphics2D graphics2D, KetlaTheUnworthy ketla) {
        if(ketla.getTarget().isDefined()) {
//            NPC n = .get();
            NPC n = ketla.getTarget().get().npc();
            String animationString = ketla.getTarget().flatMap(KetlaTheUnworthy.NpcWrapper::getLastAnimation).map(ka -> {
                    return ka.entryName() + "(" + ka.id() + ")";
                }).getOrElse(() -> "None");
            
            modelOutlineRenderer.drawOutline(n, 2, Color.ORANGE, 0);
                String npcText = "id="+n.getId()+",graphicsId=" + n.getGraphic() + ", animation=" + n.getAnimation() + "|" + animationString;
            final int tileHeight = Perspective.getTileHeight(client, n.getLocalLocation(), client.getTopLevelWorldView().getPlane());
            Point canvasLocation = Perspective.getCanvasTextLocation(client, graphics2D, n.getLocalLocation(), npcText, tileHeight + 50);
            if(canvasLocation != null) {
                net.runelite.client.ui.overlay.OverlayUtil.renderTextLocation(graphics2D, canvasLocation, npcText, Color.ORANGE);
            }
        }
        
        ketla.getMinions().forEach(m -> {
            String animationString = m.getLastAnimation().map(ka -> {
                return ka.entryName() + "(" + ka.id() + ")";
            }).getOrElse(() -> "None");
            
            modelOutlineRenderer.drawOutline(m.npc(), 2, Color.PINK, 0);
            String npcText = "id="+m.npc().getId()+", animation=" + m.npc().getAnimation() + "|" + animationString;
            final int tileHeight = Perspective.getTileHeight(client, m.npc().getLocalLocation(), client.getTopLevelWorldView().getPlane());
            Point canvasLocation = Perspective.getCanvasTextLocation(client, graphics2D, m.npc().getLocalLocation(), npcText, tileHeight + 50);
            if(canvasLocation != null) {
                net.runelite.client.ui.overlay.OverlayUtil.renderTextLocation(graphics2D, canvasLocation, npcText, Color.PINK);
            }
        });
        
        for(Projectile p : ketla.getProjectiles()) {
            LocalPoint tlp = p.getTarget();
            LocalPoint projectilePoint = new LocalPoint((int)p.getX(), (int)p.getY(), client.getTopLevelWorldView());
            if(tlp != null && projectilePoint != null) {
                final Polygon polygon = Perspective.getCanvasTilePoly(client, tlp);
                final Polygon projectilePolygon = OldOverlayUtil.getProjectilePolygon(client, p);
                String playerText = Optional.ofNullable(p.getInteracting()).filter(x -> x instanceof Player).map(x -> x.getName()).orElse("");
                String npcText = Optional.ofNullable(p.getInteracting()).filter(x -> x instanceof NPC).map(x -> x.getName() + " | " + ((NPC)x).getId()).orElse("");
                String interactingText = playerText + npcText;
                if(interactingText.isBlank()) interactingText = "null";
                if(polygon != null && projectilePolygon != null) {
                    Color c = Color.CYAN;
                    OldOverlayUtil.drawOutlineAndFill(graphics2D, c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 25), 2, polygon);
                    OldOverlayUtil.drawOutlineAndFill(graphics2D, c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 50), 2, projectilePolygon);
                    String projectileText = "id="+p.getId()+", interacting="+interactingText;
                    final int tileHeight = Perspective.getTileHeight(client, projectilePoint, client.getTopLevelWorldView().getPlane());
                    Point canvasLocation =Perspective.getCanvasTextLocation(client, graphics2D, projectilePoint, projectileText, tileHeight + (int) p.getZ());
                    if(canvasLocation != null) {
                        net.runelite.client.ui.overlay.OverlayUtil.renderTextLocation(graphics2D, canvasLocation, projectileText, Color.CYAN);
                    }
                }
            }
        }

        return null;
    }
}