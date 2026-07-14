package com.drakanhelper;

import ch.qos.logback.classic.Level;
import com.fredplugins.common.constants.FontTypes;
import com.fredplugins.common.utils.ReflectionUtils$;
import com.fredplugins.common.utils.TWorldPoint;
import com.google.inject.Provides;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Inject;

import com.google.inject.Singleton;
import ethanApiPlugin.EthanApiPlugin;
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils;
import net.runelite.api.ActorSpotAnim;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for Lowerniel Drakan (Untethered, The Blood Moon Rises finale).
 * Danger tiles are read from the live blood-mark graphics (ground truth) rather than predicted,
 * because Drakan re-faces to track the player. Overlay handles the tile rendering; this class
 * tracks the boss, the attack state (spear combo / radial AoE), the incoming Pray-Magic
 * projectiles, and the current phase.
 */
@PluginDescriptor(
	name = "Drakan Helper",
	description = "Dodge tiles, AoE safe-spots and prayer warnings for Lowerniel Drakan",
	tags = {"drakan", "blood", "moon", "vampyre", "boss", "morytania"},
	enabledByDefault = false
)
@PluginDependency(EthanApiPlugin.class)
@Singleton
public class DrakanHelperPlugin extends Plugin
{
	private final static Logger log;
	static {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(DrakanHelperPlugin.class)).setLevel(Level.DEBUG);
		log = LoggerFactory.getLogger(DrakanHelperPlugin.class);
	}

	static final int DRAKAN_ID = 16204;

	@Inject
	private Client client;
	@Inject
	private DrakanHelperConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private DrakanHelperOverlay overlay;

	@Inject
	private EthanApiPlugin ethans;
	

	@Provides
	DrakanHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DrakanHelperConfig.class);
	}

//	@Override
//	protected void startUp()
//	{
//		overlayManager.add(overlay);
//	}
//
//	@Override
//	protected void shutDown()
//	
//		overlayManager.remove(overlay);reset();
//	}

	/**
	 * Maintains the dodge plan in two phases. FLUID (before the first strike): the plan tracks the
	 * player — rebuilt from their current tile whenever they reposition — until they commit to the
	 * path (step within 1 tile of the first click tile). LOCKED (dodging has begun): the boss
	 * anchor, frame, and tiles are frozen — Drakan lunges 2-3 tiles during the strike phase, so
	 * his live position must never re-enter the math. A mid-combo re-anchor happens only if the
	 * player stays off the path for 2 consecutive ticks (a slightly late roll lands within 1).
	 */

}
