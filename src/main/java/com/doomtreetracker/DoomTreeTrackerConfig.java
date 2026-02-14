package com.doomtreetracker;

import net.runelite.client.config.*;
import java.awt.Color;

@ConfigGroup("doomtreetracker")
public interface DoomTreeTrackerConfig extends Config
{
	@ConfigSection(
		name = "Tile Markers",
		description = "Settings for starting and ending tile markers",
		position = 0
	)
	String tileMarkersSection = "tileMarkers";

	@ConfigSection(
		name = "Path Tiles",
		description = "Settings for tiles along the path",
		position = 1
	)
	String pathTilesSection = "pathTiles";

	@ConfigItem(
		keyName = "firstTileColor",
		name = "First Tile Color",
		description = "Color of the starting tile of the shield path",
		section = tileMarkersSection,
		position = 0
	)
	default Color firstTileColor()
	{
		return new Color(0, 255, 0, 80);
	}

	@ConfigItem(
		keyName = "secondTileColor",
		name = "Second Tile Color",
		description = "Color of the end tile of the shield path",
		section = tileMarkersSection,
		position = 1
	)
	default Color secondTileColor()
	{
		return new Color(0, 255, 255, 80);
	}

	@ConfigItem(
		keyName = "showPathTiles",
		name = "Show Path Tiles",
		description = "Highlight tiles along the path between the shield",
		section = pathTilesSection,
		position = 0
	)
	default boolean showPathTiles()
	{
		return true;
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
