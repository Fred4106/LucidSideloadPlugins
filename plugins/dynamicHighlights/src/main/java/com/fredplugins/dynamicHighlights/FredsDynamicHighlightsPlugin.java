package com.fredplugins.dynamicHighlights;

import ch.qos.logback.classic.Level;

import com.google.inject.Inject;

import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Dynamic Highlighter</html>",
	description = "Allows writing a config to dynamically switch ground item visibility/highlighting as well as npc/object highlighting",
	tags = {"loot", "filters", "ground", "items", "script", "scriptable", "npc", "object", "tile"}
)
public class FredsDynamicHighlightsPlugin extends Plugin
{
	private final static Logger log;
	static {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FredsDynamicHighlightsPlugin.class)).setLevel(Level.DEBUG);
		log = LoggerFactory.getLogger(FredsDynamicHighlightsPlugin.class);
	}

	@Inject
	@Getter
	private Client client;
	@Inject
	@Getter
	private ClientThread clientThread;

	@Inject
	@Getter
	private ConfigManager configManager;

	public static final String CONFIG_GROUP = "FredsDynamicHighlights";

//	private final String MAP_KEY = "groundItemsConfig";
//	private final static Type MAP_TYPE = new TypeToken<Map<Integer, String>>(){}.getType();


	public final ConfigAdapter<Map<Integer, String>> ca1 = new ConfigAdapter<>(this, "Int2StrMap", new TypeToken<Map<Integer, String>>(){}, HashMap::new);
	private Map<Integer, String> ca1Data = null;

	@Override
	protected void startUp() {
		ca1Data = ca1.load();
		log.debug("Loading from {} resulted in {}", ca1, ca1Data);
	}

	@Override
	protected void shutDown() {
		log.debug("Saving {} with {} resulted in {}", ca1Data, ca1, ca1.save(ca1Data));
		ca1Data = null;
	}

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned event) {
		Player p = event.getPlayer();
		if(!Objects.equals(client.getLocalPlayer(), p)) {
			log.debug("Player({}, {}) spawned", p.getId(), p.getName());
			ca1Data.put(p.getId(), p.getName());
		}
	}

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned event) {
		Player p = event.getPlayer();
		log.debug("Player({}, {}) despawned", p.getId(), p.getName());
		ca1Data.remove(p.getId());
	}

	int oldHashCode = -1;

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		GameState gameState  = event.getGameState();
		if (gameState == GameState.CONNECTION_LOST || gameState == GameState.LOGIN_SCREEN || gameState == GameState.HOPPING) {
			log.debug("Cleared data since gamestate was {}", gameState);
			ca1Data.clear();
		}
	}

	@Subscribe
	protected void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		int newHash = ca1Data.hashCode();
		if(newHash != oldHashCode) {
			log.debug("{} changed to {} on tick {}", ca1, ca1Data, client.getTickCount());
			oldHashCode = newHash;
		}
	}

	@Subscribe
	protected void onClientTick(ClientTick clientTick)
	{
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
	}

	@Subscribe
	private void onInteractingChanged(InteractingChanged event)
	{
		if (event.getSource() != client.getLocalPlayer())
			return;

		final Actor target = event.getTarget();

		if (target instanceof NPC)
		{
		}
		else if (target == null)
		{
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		final MenuEntry menuEntry = event.getMenuEntry();
		final String targStr = Text.standardize(menuEntry.getTarget());
		final String tOptStr = Text.standardize(menuEntry.getOption());

		log.debug("targ={}, option={}", targStr, tOptStr);
	}
}
