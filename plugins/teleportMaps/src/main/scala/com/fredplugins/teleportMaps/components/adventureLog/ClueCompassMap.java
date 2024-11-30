package com.fredplugins.teleportMaps.components.adventureLog;

import com.fredplugins.teleportMaps.FredsTeleportMapsPlugin;
import com.fredplugins.teleportMaps.HotkeyDialog;
import com.fredplugins.teleportMaps.Multikeybind;
import com.fredplugins.teleportMaps.TeleportMapsConfig;
import com.fredplugins.teleportMaps.components.BaseMap;
import com.fredplugins.teleportMaps.definition.ClueCompassDefinition;
import com.fredplugins.teleportMaps.definition.HotKeyDefinition;
import com.fredplugins.teleportMaps.ui.AdventureLogEntry;
import com.fredplugins.teleportMaps.ui.UIHotkey;
import com.fredplugins.teleportMaps.ui.UITeleport;
import com.fredplugins.teleportMaps.ui.ClueCompass;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ClueCompassMap extends BaseMap implements IAdventureMap
{
	/* Definition JSON files */
	private static final String DEF_FILE_CLUECOMPASS = "/ClueCompass/ClueCompassDefinitions.json";

	/* Sprite IDs, dimensions and positions */
	private static final int MAP_SPRITE_ID = -19700;
	private static final int MAP_SPRITE_WIDTH = 507;
	private static final int MAP_SPRITE_HEIGHT = 319;
	private static final int CLUECOMPASS_SPRITE_ID = -19701;
	private static final int CLUECOMPASS_HIGHLIGHTED_SPRITE_ID = -19702;
	private static final int CLUECOMPASS_DISABLED_SPRITE_ID = -19703;

	private static final int SCRIPT_TRIGGER_KEY = 1437;
	private static final String CLUE_COMPASS_LABEL_NAME_PATTERN = "<col=735a28>(.+)</col>: (<col=5f5f5f>)?(.+)";
	private static final String TRAVEL_ACTION = "Teleport";
	private static final String REBIND_ACTION = "Rebind";
	private static final String EXAMINE_ACTION = "Examine";
	private static final int ADVENTURE_LOG_CONTAINER_BACKGROUND = 0;
	private static final int ADVENTURE_LOG_CONTAINER_TITLE = 1;
	private static final String MENU_TITLE = "Clue Compass teleports";

	private ClueCompassDefinition[] clueCompassDefinitions;
	private HashMap<Integer, ClueCompassDefinition> clueCompassDefinitionsLookup;
	private HashMap<String, ClueCompass> availableLocations;

	@Inject
	public ClueCompassMap(FredsTeleportMapsPlugin plugin, TeleportMapsConfig config, Client client, ClientThread clientThread)
	{
		super(plugin, config, client, clientThread);
		this.loadDefinitions();
		this.buildClueCompassDefinitionLookup();
	}

	@Override
	public boolean matchesTitle(String title)
	{
		return title.matches(MENU_TITLE);
	}

	private void loadDefinitions()
	{
		this.clueCompassDefinitions = this.plugin.loadDefinitionResource(ClueCompassDefinition[].class, DEF_FILE_CLUECOMPASS);
	}

	private void buildClueCompassDefinitionLookup()
	{
		this.clueCompassDefinitionsLookup = new HashMap<>();
		for (ClueCompassDefinition clueCompassDefinition: this.clueCompassDefinitions)
		{
			// Place the ClueCompass definition in the lookup table indexed by its name
			this.clueCompassDefinitionsLookup.put(clueCompassDefinition.getIndex(), clueCompassDefinition);
		}
	}

	public void buildInterface(Widget adventureLogContainer)
	{
		this.hideAdventureLogContainerChildren(adventureLogContainer);
		this.buildAvailableTeleportList();

		this.createMapWidget(adventureLogContainer);
		this.createTeleportWidgets(adventureLogContainer);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		switch (e.getKey())
		{
			case TeleportMapsConfig.KEY_DISPLAY_HOTKEYS:
				this.updateTeleports((teleport) -> {
					teleport.setHotKeyVisibility(config.displayHotkeys());
				});
                break;
		}
        super.onConfigChanged(e);
	}
    private void hideAdventureLogContainerChildren(Widget adventureLogContainer)
	{
		Widget title = adventureLogContainer.getChild(ADVENTURE_LOG_CONTAINER_TITLE);
		if (title != null)
			title.setHidden(true);
	}

	/**
	 * Constructs the list of Clue compass teleports available for the player to use
	 */
	private void buildAvailableTeleportList()
	{
		this.availableLocations = new HashMap<>();

		// Compile the pattern that will match the teleport label
		// and place the hotkey and teleport name into groups
		Pattern labelPattern = Pattern.compile(CLUE_COMPASS_LABEL_NAME_PATTERN);

		// Get the parent widgets containing the teleport locations list
		Widget teleportList = this.plugin.getClient().getWidget(InterfaceID.ADVENTURE_LOG, 3);

		// Fetch all teleport label widgets
		Widget[] labelWidgets = teleportList.getDynamicChildren();

		for (Widget child : labelWidgets)
		{
			String shortcutKey;
			String disabledColor;
			String teleportName;

			// Create a pattern matcher with the widgets text content
			Matcher matcher = labelPattern.matcher(child.getText());

			// If the text doesn't match the pattern, skip onto the next
			if (!matcher.matches())
				continue;

			// Extract the pertinent information
			shortcutKey = matcher.group(1);
			disabledColor = matcher.group(2);
			teleportName = matcher.group(3);

			// Don't include unavailable teleports in available collection..
			if (disabledColor != null)
				continue;

			ClueCompassDefinition clueCompassDefinition = this.clueCompassDefinitionsLookup.get(child.getIndex());

			if (clueCompassDefinition == null)
				continue;

			this.availableLocations.put(clueCompassDefinition.getName(), new ClueCompass(clueCompassDefinition, child, shortcutKey, teleportName));
		}
	}

	private void createMapWidget(Widget container)
	{
		this.createSpriteWidget(container,
			MAP_SPRITE_WIDTH,
			MAP_SPRITE_HEIGHT,
			0,
			0,
			MAP_SPRITE_ID);
	}

	private void createTeleportWidgets(Widget container)
	{
		this.clearTeleports();

		for (ClueCompassDefinition clueCompassDefinition : this.clueCompassDefinitions)
		{
			Widget widgetContainer = container.createChild(-1, WidgetType.GRAPHIC);
			Widget teleportWidget = container.createChild(-1, WidgetType.GRAPHIC);

			UITeleport teleport = new UITeleport(widgetContainer, teleportWidget);

			teleport.setPosition(clueCompassDefinition.getX(), clueCompassDefinition.getY());
			teleport.setTeleportSprites(CLUECOMPASS_SPRITE_ID, CLUECOMPASS_HIGHLIGHTED_SPRITE_ID, CLUECOMPASS_DISABLED_SPRITE_ID);

			if (isLocationUnlocked(clueCompassDefinition.getName()))
			{
				ClueCompass clueCompassEntry = this.availableLocations.get(clueCompassDefinition.getName());

				teleport.setName(clueCompassEntry.getDisplayedName());
				teleport.addAction(TRAVEL_ACTION, () -> this.triggerTeleport(clueCompassEntry));

				HotKeyDefinition tempHotKeyDef = clueCompassDefinition.getHotkey();
				tempHotKeyDef.setX((tempHotKeyDef.getX()==-1) ? clueCompassDefinition.getX() : tempHotKeyDef.getX());
				tempHotKeyDef.setY((tempHotKeyDef.getY()==-1) ? clueCompassDefinition.getY() : tempHotKeyDef.getY());
				UIHotkey hotkey = this.createHotKey(container, clueCompassDefinition.getHotkey(), clueCompassEntry.getKeyShortcut());
				teleport.addAction(REBIND_ACTION, () -> this.triggerRebind(clueCompassEntry, hotkey));

				teleport.attachHotkey(hotkey);
				teleport.setSize(hotkey.getHotkeyFont().getTextWidth(hotkey.getLabel().getWidget().getText()), hotkey.getHotkeyFont().getBaseline());
				teleport.setHotKeyVisibility(config.displayHotkeys());
			}
			else
			{
				teleport.setSize(23,23);
				teleport.setName(clueCompassDefinition.getName());
				teleport.setLocked(true);
				teleport.addAction(EXAMINE_ACTION, () -> this.triggerLockedMessage(clueCompassDefinition));
			}

			this.addTeleport(teleport);
		}
	}

	private void triggerRebind(ClueCompass clueCompassEntry, UIHotkey hotkey) {
	}

	private boolean isLocationUnlocked(String teleportName)
	{
		return this.availableLocations.containsKey(teleportName);
	}

	private void triggerTeleport(AdventureLogEntry<ClueCompassDefinition> adventureLogEntry)
	{
		this.clientThread.invokeLater(() -> this.client.runScript(SCRIPT_TRIGGER_KEY, this.plugin.getClient().getWidget(0xBB0003).getId(), adventureLogEntry.getWidget().getIndex()));
	}
	private void triggerRebind(AdventureLogEntry<ClueCompassDefinition> adventureLogEntry)
	{
SwingUtilities.invokeLater(() ->
			{
				Window window = null;
				for (Component c = (Applet) client; c != null; c = c.getParent())
				{
					if (c instanceof Window)
					{
						window = (Window) c;
						break;
					}
				}
//				adventureLogEntry.getDefinition().get
//				new HotkeyDialog(window, adventureLogEntry.getDefinition().getName(), , adventureLogEntry.getKeyShortcut(), bind ->
//				{
//					adventureLogEntry. = bind;
//					configManager.setConfiguration(BetterTeleportMenuConfig.GROUP, BetterTeleportMenuConfig.KEYBIND_PREFIX + identifier, bind.toConfig());
//					clientThread.invokeLater(() -> hotkeyChanged());
//				});
			});
	}

	private void triggerLockedMessage(ClueCompassDefinition clueCompassDefinition)
	{
		this.clientThread.invokeLater(() -> this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", String.format("The talisman does not have the power to take you to %s yet.", clueCompassDefinition.getName()), null));
	}
}
