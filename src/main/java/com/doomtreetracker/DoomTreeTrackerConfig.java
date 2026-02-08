package com.doomtreetracker;

import net.runelite.client.config.*;

import java.awt.Color;

@ConfigGroup("doomtreetracker")
public interface DoomTreeTrackerConfig extends Config
{
	@ConfigSection(
		name = "Orb Markers",
		description = "Settings for orb tile markers",
		position = 0
	)
	String orbMarkersSection = "orbMarkers";

	@ConfigSection(
		name = "Line Settings",
		description = "Settings for the line between orbs",
		position = 1
	)
	String lineSettingsSection = "lineSettings";

	@ConfigSection(
		name = "Path Tiles",
		description = "Settings for tiles along the path",
		position = 2
	)
	String pathTilesSection = "pathTiles";

	@ConfigItem(
		keyName = "firstOrbColor",
		name = "First Orb Color",
		description = "Color of the first clicked orb marker",
		section = orbMarkersSection,
		position = 0
	)
	default Color firstOrbColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
		keyName = "secondOrbColor",
		name = "Second Orb Color",
		description = "Color of the second clicked orb marker",
		section = orbMarkersSection,
		position = 1
	)
	default Color secondOrbColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
		keyName = "showLine",
		name = "Show Line",
		description = "Draw a line between the two clicked orbs",
		section = lineSettingsSection,
		position = 0
	)
	default boolean showLine()
	{
		return true;
	}

	@ConfigItem(
		keyName = "lineColor",
		name = "Line Color",
		description = "Color of the line between orbs",
		section = lineSettingsSection,
		position = 1
	)
	default Color lineColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = "lineWidth",
		name = "Line Width",
		description = "Width of the line between orbs",
		section = lineSettingsSection,
		position = 2
	)
	default int lineWidth()
	{
		return 3;
	}

	@ConfigItem(
		keyName = "showPathTiles",
		name = "Show Path Tiles",
		description = "Highlight tiles along the path between orbs",
		section = pathTilesSection,
		position = 0
	)
	default boolean showPathTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "pathTileColor",
		name = "Path Tile Color",
		description = "Color of tiles along the path",
		section = pathTilesSection,
		position = 1
	)
	default Color pathTileColor()
	{
		return new Color(255, 255, 0, 80);
	}
}
