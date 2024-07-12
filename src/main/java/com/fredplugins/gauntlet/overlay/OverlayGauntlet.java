package com.fredplugins.gauntlet.overlay;

import com.fredplugins.gauntlet.FredGauntletConfig;
import com.fredplugins.gauntlet.FredGauntletPlugin;
import com.fredplugins.gauntlet.GauntletRoom;
import com.fredplugins.gauntlet.entity.Demiboss;
import com.fredplugins.gauntlet.entity.Resource;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class OverlayGauntlet extends Overlay
{

    private final Client client;
    private final FredGauntletPlugin plugin;
    private final FredGauntletConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    private Player player;

    @Inject
    private OverlayGauntlet(final Client client, final FredGauntletPlugin plugin, final FredGauntletConfig config, final ModelOutlineRenderer modelOutlineRenderer)
    {
        super(plugin);

        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        determineLayer();
    }

    @Override
    public void determineLayer()
    {
        setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    @Override
    public Dimension render(final Graphics2D graphics2D)
    {
        player = client.getLocalPlayer();

        if (player == null)
        {
            return null;
        }

        renderResources(graphics2D);
        renderNextRoom(graphics2D);
        renderUtilities();
        renderDemibosses();
        renderStrongNpcs();
        renderWeakNpcs();

        return null;
    }

    private void renderResources(final Graphics2D graphics2D)
    {
        if ((!config.resourceOverlay() && !config.resourceOutline()) || plugin.getResources().isEmpty())
        {
            return;
        }

        final LocalPoint localPointPlayer = player.getLocalLocation();

        for (final Resource resource : plugin.getResources())
        {
            final GameObject gameObject = resource.getGameObject();

            final LocalPoint localPointGameObject = gameObject.getLocalLocation();

            if (isOutsideRenderDistance(localPointGameObject, localPointPlayer))
            {
                continue;
            }

            if (config.resourceOverlay())
            {
                final Polygon polygon = Perspective.getCanvasTilePoly(client, localPointGameObject);

                if (polygon == null)
                {
                    continue;
                }

                drawOutlineAndFill(graphics2D, config.resourceTileOutlineColor(), config.resourceTileFillColor(),
                        config.resourceTileOutlineWidth(), polygon);

                OverlayUtil.renderImageLocation(client, graphics2D, localPointGameObject, resource.getIcon(), 0);
            }

            if (config.resourceOutline())
            {
                final Shape shape = gameObject.getConvexHull();

                if (shape == null)
                {
                    continue;
                }

                modelOutlineRenderer.drawOutline(gameObject, config.resourceOutlineWidth(),
                        config.resourceOutlineColor(), 0);
            }
        }
    }
    private void renderGraphicsObjects(Graphics2D graphics, LocalPoint localPointPlayer, Color color, GameObject object, String text)
    {
        if(object == null) {
            return;
        }
        LocalPoint lp = object.getLocalLocation();
//        color new Color(color.getRed(), color.getGreen(), color.getBlue(), 80)
        if (!isOutsideRenderDistance(lp, localPointPlayer)) {
            final Shape shape = object.getConvexHull();
            if (shape != null) {
                modelOutlineRenderer.drawOutline(object, 2,
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 80)
                        , 0);
            }

            Polygon poly = Perspective.getCanvasTilePoly(client, lp);
            if (poly != null)
            {
                OverlayUtil.renderPolygon(graphics, poly, Color.MAGENTA);
            }
            ObjectComposition comp = client.getObjectDefinition(object.getId());
            final ObjectComposition impostor = (comp != null && comp.getImpostorIds() != null) ? comp.getImpostor() : null;
            String infoString1 = (comp != null) ? "("  + comp.getId() + "|" + comp.getName() + ")" : "null";
            String infoString2 =  (impostor != null) ? "["  + impostor.getId() + "|" + impostor.getName() + "]" : "null";
            String infoString = text + ": " + "comp: " + infoString1 + ", imposter: " + infoString2;
            Point textLocation = Perspective.getCanvasTextLocation(
                    client, graphics, lp,
                    infoString, 0);
            if (textLocation != null)
            {
                OverlayUtil.renderTextLocation(graphics, textLocation, infoString, color);
            }
        }
    }

    private static final Font FONT = FontManager.getRunescapeFont().deriveFont(Font.BOLD, 14);

    private void renderNextRoom(final Graphics2D graphics2D)
    {
        Font lastFont = graphics2D.getFont();
        graphics2D.setFont(FONT);
        final LocalPoint localPointPlayer = player.getLocalLocation();
//        GameObject x1 = plugin.findNodeForUnopenedRoom(plugin.getInstanceGrid().getNextUnlitRoomFirstPass());
//        GameObject x2 = plugin.findNodeForUnopenedRoom(plugin.getInstanceGrid().getNextUnlitRoomSecondPass());
//        GameObject x3 = ;
        GauntletRoom r1 = plugin.getInstanceGrid().getNextUnlitRoomFirstPass();
        GauntletRoom r2 = plugin.getInstanceGrid().getNextUnlitRoomSecondPass();
        GauntletRoom r3 = plugin.getInstanceGrid().getNextUnlitRoomLastPass();
        if(r1 != null) renderGraphicsObjects(graphics2D, localPointPlayer, Color.WHITE, plugin.findNodeForUnopenedRoom(r1), "first");
        if(r2 != null && r2 != r1) renderGraphicsObjects(graphics2D, localPointPlayer, Color.GREEN, plugin.findNodeForUnopenedRoom(r2), "second");
        if(r3 != null && r3 != r1 && r3 != r2) renderGraphicsObjects(graphics2D, localPointPlayer, Color.BLUE, plugin.findNodeForUnopenedRoom(r3), "last");
        graphics2D.setFont(lastFont);
    }
    private void renderUtilities()
    {
        if (!config.utilitiesOutline() || plugin.getUtilities().isEmpty())
        {
            return;
        }

        final LocalPoint localPointPlayer = player.getLocalLocation();

        for (final GameObject gameObject : plugin.getUtilities())
        {
            if (isOutsideRenderDistance(gameObject.getLocalLocation(), localPointPlayer))
            {
                continue;
            }

            final Shape shape = gameObject.getConvexHull();

            if (shape == null)
            {
                continue;
            }

            modelOutlineRenderer.drawOutline(gameObject, config.utilitiesOutlineWidth(),
                    config.utilitiesOutlineColor(), 0);
        }
    }

    private void renderDemibosses()
    {
        if (!config.demibossOutline() || plugin.getDemibosses().isEmpty())
        {
            return;
        }

        final LocalPoint localPointPlayer = player.getLocalLocation();

        for (final Demiboss demiboss : plugin.getDemibosses())
        {
            final NPC npc = demiboss.getNpc();

            final LocalPoint localPointNpc = npc.getLocalLocation();

            if (localPointNpc == null || npc.isDead() || isOutsideRenderDistance(localPointNpc, localPointPlayer))
            {
                continue;
            }

            modelOutlineRenderer.drawOutline(npc, config.demibossOutlineWidth(),
                    demiboss.getType().getOutlineColor(), 0);
        }
    }

    private void renderStrongNpcs()
    {
        if (!config.strongNpcOutline() || plugin.getStrongNpcs().isEmpty())
        {
            return;
        }

        final LocalPoint localPointPLayer = player.getLocalLocation();

        for (final NPC npc : plugin.getStrongNpcs())
        {
            final LocalPoint localPointNpc = npc.getLocalLocation();

            if (localPointNpc == null || npc.isDead() || isOutsideRenderDistance(localPointNpc, localPointPLayer))
            {
                continue;
            }

            modelOutlineRenderer.drawOutline(npc, config.strongNpcOutlineWidth(), config.strongNpcOutlineColor(),
                    0);
        }
    }

    private void renderWeakNpcs()
    {
        if (!config.weakNpcOutline() || plugin.getWeakNpcs().isEmpty())
        {
            return;
        }

        final LocalPoint localPointPlayer = player.getLocalLocation();

        for (final NPC npc : plugin.getWeakNpcs())
        {
            final LocalPoint localPointNpc = npc.getLocalLocation();

            if (localPointNpc == null || npc.isDead() || isOutsideRenderDistance(localPointNpc, localPointPlayer))
            {
                continue;
            }

            modelOutlineRenderer.drawOutline(npc, config.weakNpcOutlineWidth(), config.weakNpcOutlineColor(),
                    0);
        }
    }

    private boolean isOutsideRenderDistance(final LocalPoint localPoint, final LocalPoint playerLocation)
    {
        final int maxDistance = config.resourceRenderDistance().getDistance();

        if (maxDistance == 0)
        {
            return false;
        }

        return localPoint.distanceTo(playerLocation) >= maxDistance;
    }
}