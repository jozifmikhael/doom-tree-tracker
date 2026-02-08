package com.doomtreetracker;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@PluginDescriptor(
        name = "Doom Tree Tracker"
)
public class DoomTreeTrackerPlugin extends Plugin {
    private static final String VOLATILE_EARTH_NAME = "Volatile earth";
    private static final int VOLATILE_EARTH_ID = 14714;
    private static final int VOLATILE_ORB_ID = 14715;

    @Inject
    private Client client;

    @Inject
    private DoomTreeTrackerConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private DoomTreeTrackerOverlay overlay;

    private final List<WorldPoint> clickedOrbLocations = new ArrayList<>();

    private final Map<Integer, NPC> volatileEarthNpcs = new HashMap<>();


    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        reset();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOADING ||
                gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
            reset();
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();
        String npcName = npc.getName();
        int npcId = npc.getId();
        log.debug("Volatile earth spawned at: {}", npc.getWorldLocation());

        if (npcId == VOLATILE_EARTH_ID) {
            volatileEarthNpcs.put(npc.getIndex(), npc);
            log.debug("Volatile earth spawned at: {}", npc.getWorldLocation());
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        NPC npc = event.getNpc();
        int npcId = npc.getId();

        if (npcId == VOLATILE_EARTH_ID) {
            volatileEarthNpcs.remove(npc.getIndex());
        } else if (npcId == VOLATILE_ORB_ID) { // If the orb has despawned, then shockwave phase has ended, we can remove the tiles
            reset();
        }

    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        // Check if the player clicked on a volatile earth NPC
        if (event.getMenuOption().equals("Attack")) {
            int npcIndex = event.getId();
            NPC clickedNpc = volatileEarthNpcs.get(npcIndex);

            if (clickedNpc != null) {
                WorldPoint location = clickedNpc.getWorldLocation();

                // Only track the first two clicked orbs
                if (clickedOrbLocations.size() < 2 && !clickedOrbLocations.contains(location)) {
                    clickedOrbLocations.add(location);
                    log.debug("Volatile earth clicked at: {}, total clicked: {}", location, clickedOrbLocations.size());
                }
            }
        }
    }

    public List<WorldPoint> getClickedOrbLocations() {
        return clickedOrbLocations;
    }

    public void reset() {
        clickedOrbLocations.clear();
        volatileEarthNpcs.clear();
    }

    @Provides
    DoomTreeTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DoomTreeTrackerConfig.class);
    }
}
