package com.doomtreetracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DoomTreeTrackerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DoomTreeTrackerPlugin.class);
		RuneLite.main(args);
	}
}