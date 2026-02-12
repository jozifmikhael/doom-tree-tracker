package com.doomtreetracker;

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

public class DoomTreeTrackerOverlay extends Overlay {
    private final Client client;
    private final DoomTreeTrackerPlugin plugin;
    private final DoomTreeTrackerConfig config;
    private final ArrayList<WorldPoint> markedTiles = new ArrayList<>();

    @Inject
    public DoomTreeTrackerOverlay(Client client, DoomTreeTrackerPlugin plugin, DoomTreeTrackerConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        List<WorldPoint> clickedLocations = plugin.getClickedOrbLocations();
        WorldPoint volatileOrbLocation = plugin.getVolatileOrbLocation();

        // Only draw if we have exactly 2 clicked orbs
        if (clickedLocations.size() != 2) {
            return null;
        }

        WorldPoint firstOrb = clickedLocations.get(0);
        WorldPoint secondOrb = clickedLocations.get(1);

        // Draw tile markers at both orb locations
        drawTileMarker(graphics, firstOrb, config.firstOrbColor());
        drawTileMarker(graphics, secondOrb, config.secondOrbColor());

        // Draw line connecting the two orbs
        if (config.showLine()) {
            drawLineBetweenOrbs(graphics, firstOrb, secondOrb);
        }

        // Draw the earthen shield's path from second orb to first orb
        if (config.showPathTiles()) {
            drawPathTiles(graphics, secondOrb, firstOrb, volatileOrbLocation);
        }

        return null;
    }

    private void drawPathTiles(Graphics2D graphics, WorldPoint start, WorldPoint end, WorldPoint volatileOrbLocation) {
        markedTiles.clear();
        List<WorldPoint> visitedLocations = plugin.getVisitedOrbLocations();

        int x = start.getX();
        int y = start.getY();
        int endX = end.getX();
        int endY = end.getY();
        int z = start.getPlane();

        int dx = Integer.signum(endX - x);
        int dy = Integer.signum(endY - y);

        // Phase 1: Move diagonally
        while (x != endX && y != endY) {
            x += dx;
            y += dy;
            WorldPoint currentTile = new WorldPoint(x, y, z);
            if (!volatileOrbLocation.equals(currentTile) && !visitedLocations.contains(currentTile)) {
                markedTiles.add(currentTile);
            }
        }

        // Phase 2: Move cardinally for the remaining distance
        while (x != endX || y != endY) {
            if (x != endX) {
                x += dx;
            } else {
                y += dy;
            }
            WorldPoint currentTile = new WorldPoint(x, y, z);
            // if orb is on the current tile, and current tile has been visited, do not add to markedTiles
            if (!volatileOrbLocation.equals(currentTile) && !visitedLocations.contains(currentTile)) {
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

    private void drawLineBetweenOrbs(Graphics2D graphics, WorldPoint start, WorldPoint end) {
        LocalPoint startLocal = LocalPoint.fromWorld(client, start);
        LocalPoint endLocal = LocalPoint.fromWorld(client, end);

        if (startLocal == null || endLocal == null) {
            return;
        }

        Polygon startPoly = Perspective.getCanvasTilePoly(client, startLocal);
        Polygon endPoly = Perspective.getCanvasTilePoly(client, endLocal);

        if (startPoly == null || endPoly == null) {
            return;
        }

        // Draw line from center of first tile to center of second tile
        Point startCenter = getPolygonCenter(startPoly);
        Point endCenter = getPolygonCenter(endPoly);

        graphics.setColor(config.lineColor());
        graphics.setStroke(new BasicStroke(config.lineWidth()));
        graphics.drawLine(startCenter.x, startCenter.y, endCenter.x, endCenter.y);
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

        graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
        graphics.fillPolygon(polygon);
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(2));
        graphics.drawPolygon(polygon);
    }

    private Point getPolygonCenter(Polygon polygon) {
        int sumX = 0;
        int sumY = 0;
        for (int i = 0; i < polygon.npoints; i++) {
            sumX += polygon.xpoints[i];
            sumY += polygon.ypoints[i];
        }
        return new Point(sumX / polygon.npoints, sumY / polygon.npoints);
    }
}
