package com.doomshieldtracker;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DoomShieldTrackerOverlay extends Overlay {
    private static final BasicStroke TILE_BORDER_STROKE = new BasicStroke(2);

    private final Client client;
    private final DoomShieldTrackerPlugin plugin;
    private final DoomShieldTrackerConfig config;

    @Inject
    public DoomShieldTrackerOverlay(Client client, DoomShieldTrackerPlugin plugin, DoomShieldTrackerConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        List<WorldPoint> clickedLocations = plugin.getClickedVolatileEarthLocations();
        WorldPoint volatileShieldLocation = plugin.getVolatileShieldLocation();

        if (clickedLocations.size() != 2) {
            return null;
        }

        WorldPoint firstVolatileEarth = clickedLocations.get(0);
        WorldPoint secondVolatileEarth = clickedLocations.get(1);

        drawTileMarker(graphics, firstVolatileEarth, config.firstTileColor());
        drawTileMarker(graphics, secondVolatileEarth, config.secondTileColor());

        if (config.showPathTiles()) {
            drawPathTiles(graphics, secondVolatileEarth, firstVolatileEarth, volatileShieldLocation);
        }

        return null;
    }

    private void drawPathTiles(
            Graphics2D graphics,
            WorldPoint start,
            WorldPoint end,
            WorldPoint volatileShieldLocation
    ) {
        List<WorldPoint> markedTiles = new ArrayList<>();
        Set<WorldPoint> visitedLocations = plugin.getVisitedShieldLocations();

        int x = start.getX();
        int y = start.getY();
        int endX = end.getX();
        int endY = end.getY();
        int z = start.getPlane();

        int dx = Integer.signum(endX - x);
        int dy = Integer.signum(endY - y);

        // Move diagonally
        while (x != endX && y != endY) {
            x += dx;
            y += dy;
            WorldPoint currentTile = new WorldPoint(x, y, z);
            if (!isShieldOrVisitedTile(currentTile, volatileShieldLocation, visitedLocations)) {
                markedTiles.add(currentTile);
            }
        }

        // Move cardinally for the remaining distance
        while (x != endX || y != endY) {
            if (x != endX) {
                x += dx;
            } else {
                y += dy;
            }
            WorldPoint currentTile = new WorldPoint(x, y, z);
            if (!isShieldOrVisitedTile(currentTile, volatileShieldLocation, visitedLocations)) {
                markedTiles.add(currentTile);
            }
        }

        // Draw remaining tiles
        for (WorldPoint tilePoint : markedTiles) {
            if (!tilePoint.equals(end)) {
                drawTileMarker(graphics, tilePoint, config.pathTileColor());
            }
        }
    }

    private boolean isShieldOrVisitedTile(WorldPoint tile, WorldPoint shieldLocation, Set<WorldPoint> visitedLocations) {
        return (shieldLocation != null && shieldLocation.equals(tile)) || visitedLocations.contains(tile);
    }

    private void drawTileMarker(Graphics2D graphics, WorldPoint worldPoint, Color color) {
        LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
        if (localPoint == null) {
            return;
        }

        Polygon polygon = Perspective.getCanvasTilePoly(client, localPoint);
        if (polygon == null) {
            return;
        }

        graphics.setColor(color);
        graphics.fillPolygon(polygon);
        graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
        graphics.setStroke(TILE_BORDER_STROKE);
        graphics.drawPolygon(polygon);
    }
}
