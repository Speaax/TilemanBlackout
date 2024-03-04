package com.example;

import com.google.common.base.Strings;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class UnlockableTilesOverlay extends Overlay
{
    private static final int MAX_DRAW_DISTANCE = 32;

    private final Client client;
    private final ExampleConfig config;
    private final ExamplePlugin plugin;

    @Inject
    private UnlockableTilesOverlay(Client client, ExampleConfig config, ExamplePlugin plugin)
    {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(PRIORITY_LOW);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        final Collection<WorldPoint> unlockableTiles = plugin.getViewableUnlockableTiles();
        if (unlockableTiles.isEmpty())
        {
            return null;
        }

        Stroke stroke = new BasicStroke((float) 0); // Assuming you have a config for border width
        for (final WorldPoint point : unlockableTiles)
        {
            if (point.getPlane() != client.getPlane())
            {
                continue;
            }

            // Assuming you have a desired color for unlockable tiles
            Color baseColor = config.highlightTileColor();
            Color highlightColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), config.alphaValue());

            drawTile(graphics, point, highlightColor, stroke); // Assuming no labels for unlockable tiles
        }

        return null;
    }

    private void drawTile(Graphics2D graphics, WorldPoint point, Color color, Stroke borderStroke)
    {
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

        if (point.distanceTo(playerLocation) >= config.drawDistance())
        {
            return;
        }

        LocalPoint lp = LocalPoint.fromWorld(client, point);
        if (lp == null)
        {
            return;
        }

        Polygon poly = Perspective.getCanvasTilePoly(client, lp);
        if (poly != null)
        {
            Color baseColor = config.highlightTileColor();
            Color highlightColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), config.alphaValue());
            OverlayUtil.renderPolygon(graphics, poly, color, highlightColor, borderStroke);
        }
    }
}