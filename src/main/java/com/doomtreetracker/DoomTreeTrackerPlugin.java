package com.doomtreetracker;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@PluginDescriptor(
        name = "Doom Tree Tracker"
)
public class DoomTreeTrackerPlugin extends Plugin {
    private static final int VOLATILE_EARTH_ID = NpcID.DOM_SHOCKWAVE_PATH_NODE;
    private static final int SHIELD_ID = NpcID.DOM_SHOCKWAVE_SHIELD;
    private static final List<Integer> DOOM_BOSS_ID = List.of(
            NpcID.DOM_BOSS,
            NpcID.DOM_BOSS_SHIELDED,
            NpcID.DOM_BOSS_BURROWED
    );

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private DoomTreeTrackerOverlay overlay;

    private final List<WorldPoint> clickedVolatileEarthLocations = new ArrayList<>();
    private final Set<WorldPoint> visitedShieldLocations = new HashSet<>();
    private final Map<Integer, NPC> volatileEarthNpcs = new HashMap<>();

    private NPC volatileShield;
    private NPC doomBoss;
    private WorldPoint volatileShieldLocation;

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        reset();
    }

    @Provides
    DoomTreeTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DoomTreeTrackerConfig.class);
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
        int npcId = npc.getId();

        if (npcId == VOLATILE_EARTH_ID) {
            volatileEarthNpcs.put(npc.getIndex(), npc);
        } else if (npcId == SHIELD_ID) {
            volatileShield = npc;
        } else if (DOOM_BOSS_ID.contains(npcId)) {
            doomBoss = npc;
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        NPC npc = event.getNpc();
        int npcId = npc.getId();

        if (npcId == VOLATILE_EARTH_ID) {
            volatileEarthNpcs.remove(npc.getIndex());
        } else if (npcId == SHIELD_ID || DOOM_BOSS_ID.contains(npcId)) {
            reset();
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("Attack")) {
            int npcIndex = event.getId();
            NPC clickedNpc = volatileEarthNpcs.get(npcIndex);

            if (clickedNpc != null) {
                WorldPoint location = clickedNpc.getWorldLocation();

                if (clickedVolatileEarthLocations.size() < 2 && !clickedVolatileEarthLocations.contains(location)) {
                    clickedVolatileEarthLocations.add(location);
                }
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (volatileShield != null) {
            WorldPoint currentShieldLocation = getCenterLocation(volatileShield);

            // track volatile shield locations as it moves each game tick, use the visited shields to remove marked
            // tiles as shield moves
            if (volatileShieldLocation != null && !volatileShieldLocation.equals(currentShieldLocation)) {
                visitedShieldLocations.add(volatileShieldLocation);
            }
            volatileShieldLocation = currentShieldLocation;
        }

        if (volatileShield == null && doomBoss == null && !clickedVolatileEarthLocations.isEmpty()) {
            reset();
        }
    }

    public List<WorldPoint> getClickedVolatileEarthLocations() {
        return clickedVolatileEarthLocations;
    }

    public WorldPoint getVolatileShieldLocation() {
        return volatileShieldLocation;
    }

    public Set<WorldPoint> getVisitedShieldLocations() {
        return visitedShieldLocations;
    }

    public void reset() {
        clickedVolatileEarthLocations.clear();
        volatileEarthNpcs.clear();
        volatileShieldLocation = null;
        visitedShieldLocations.clear();
        volatileShield = null;
        doomBoss = null;
    }

    private WorldPoint getCenterLocation(NPC npc) {
        WorldPoint southWest = npc.getWorldLocation();
        NPCComposition composition = npc.getTransformedComposition();
        int size = composition != null ? composition.getSize() : 1;
        int offset = size / 2;
        return new WorldPoint(southWest.getX() + offset, southWest.getY() + offset, southWest.getPlane());
    }
}
