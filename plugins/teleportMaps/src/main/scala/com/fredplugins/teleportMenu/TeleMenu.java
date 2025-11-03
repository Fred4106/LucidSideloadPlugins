package com.fredplugins.teleportMenu;

import ch.qos.logback.classic.Level;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import static com.fredplugins.teleportMenu.FredsBetterTeleportMenuPlugin.CHAR_UNSET;
import static com.fredplugins.teleportMenu.FredsBetterTeleportMenuPlugin.KEY_PREFIX_MATCHER;
import static com.fredplugins.teleportMenu.FredsBetterTeleportMenuPlugin.LAST_DEST_NAMES;
import static com.fredplugins.teleportMenu.FredsBetterTeleportMenuPlugin.canonicalNames;
import static com.fredplugins.teleportMenu.FredsBetterTeleportMenuPlugin.cleanify;

@NoArgsConstructor
@Accessors(fluent = true, chain = true)
@Slf4j
class TeleMenu
{
	static {
		((ch.qos.logback.classic.Logger) log).setLevel(Level.DEBUG);
	}

	static final int CHANGE_BINDING_OP = 8;
	static final int CLEAR_BINDING_OP = 9;
	static final int DEFAULT_BINDING_OP = 10;

//	private static Client client = RuneLite.getInjector().getInstance(Client.class);
//	private static ClientThread clientThread = RuneLite.getInjector().getInstance(ClientThread.class);
//	private static ConfigManager configManager = RuneLite.getInjector().getInstance(ConfigManager.class);
//	private static FredsBetterTeleportMenuPlugin plugin = RuneLite.getInjector().getInstance(FredsBetterTeleportMenuPlugin.class);
//	clientThread
//	private static Client client = RuneLite.getInjector().getInstance(Client.class);

	private int timeout = 0;

	@Setter
	Widget textWidget;
	@Setter
	Widget resumeWidget;
	@Setter
	Widget opWidget;
	@Setter
	Widget keyListenerWidget;

	String identifier = "";

	@Setter
	String menuIdentifier;

	@Setter
	String highlightTag = "<shad=ffffff>";

	@Setter
	Runnable disable;

	String displayText;
	char defaultBind;
	String preText;
	String postText;

	boolean highlight;
	boolean disabled;

	@Setter
	Multikeybind bind;

	public void build(FredsBetterTeleportMenuPlugin plugin)
	{
		Matcher m = KEY_PREFIX_MATCHER.matcher(textWidget.getText());
		if (!m.find())
		{
			log.warn("bad msg \"{}\"", textWidget.getText());
			return;
		}
		preText = m.group(1);
		if (preText == null)
		{
			preText = "<col=735a28>";
		}
		if (m.group(2) == null)
		{
			defaultBind = CHAR_UNSET;
		}
		else
		{
			defaultBind = Character.toUpperCase(m.group(2).charAt(0));
		}
		postText = m.group(3);
		if (postText == null)
		{
			postText = "</col>: ";
		}
		displayText = m.group(4) + m.group(5);

		this.identifier = menuIdentifier;
		if(!this.identifier.isEmpty()){
			this.identifier += "-";
		}
		this.identifier += cleanify(m.group(4));
		if (!"spirit-tree-locations-your-house-".equals(this.identifier))
		{
			this.identifier += cleanify(m.group(5));
		}
		this.identifier = canonicalNames.getOrDefault(this.identifier, this.identifier);
		String key = FredsBetterTeleportMenuConfig.KEYBIND_PREFIX + identifier;
		if(plugin.getConfigManager().getConfiguration(FredsBetterTeleportMenuConfig.GROUP, key) == null) {
			Optional.of(defaultMultiBind().toConfig()).filter(s -> !s.isEmpty()).ifPresent(nVal -> {
				plugin.getConfigManager().setConfiguration(FredsBetterTeleportMenuConfig.GROUP, key, nVal);
			});
		}
//		String strConfigValue = Optional.ofNullable(plugin.getConfigManager().getConfiguration(FredsBetterTeleportMenuConfig.GROUP, FredsBetterTeleportMenuConfig.KEYBIND_PREFIX + identifier)).orElse(defaultMultiBind().toConfig());
//		if(strConfigValue == null) {
//			plugin.getConfigManager().setConfiguration(FredsBetterTeleportMenuConfig.GROUP, FredsBetterTeleportMenuConfig.KEYBIND_PREFIX + identifier, defaultMultiBind().toConfig());
//		}
		String strConfigValue = Optional.ofNullable(plugin.getConfigManager().getConfiguration(FredsBetterTeleportMenuConfig.GROUP, key)).orElse("");
		this.bind = Multikeybind.fromConfig(strConfigValue);

//		this.bind = Multikeybind.fromConfig(strConfigValue);
//		if (this.bind == null)
//		{
//			this.bind = defaultMultiBind();
//		}

		disabled = disable != null && plugin.getConfig().hideDisabled() && (displayText.startsWith("<str>") || displayText.startsWith("<col=5f5f5f>"));
		if (disabled)
		{
			disable.run();
		}

		clearKeyListener();
		hotkeyChanged();

		if (opWidget.getOnOpListener() == null)
		{
			// otherwise the actions don't get shown
			opWidget.setOnOpListener(net.runelite.api.ScriptID.NULL);
		}

		List<TeleMenu> change = new ArrayList<>(plugin.teleMenus);
		change.add(this);
		change.sort(Comparator.comparing((TeleMenu tm) -> tm.bind.getKeybinds().size()).reversed());
		plugin.teleMenus = change;
		plugin.menuJustOpened = true;
	}

	Multikeybind defaultMultiBind()
	{
		if (defaultBind == CHAR_UNSET)
		{
			return new Multikeybind();
		}
		return new Multikeybind(new Keybind(defaultBind, 0));
	}

	void hotkeyChanged()
	{
		opWidget.setAction(CHANGE_BINDING_OP, "Set Hotkey (" + this.bind + ")");
		opWidget.setAction(CLEAR_BINDING_OP, "Clear Hotkey");
		opWidget.setAction(DEFAULT_BINDING_OP, "Default Hotkey");
		if (this.bind.isUnset())
		{
			textWidget.setText(displayText);
		}
		else
		{
			String preHighlight = this.highlight ? this.highlightTag : "";
			textWidget.setText(this.preText + this.bind + this.postText + preHighlight + displayText);
		}
	}

	void openSetDialog(FredsBetterTeleportMenuPlugin plugin)
	{
		SwingUtilities.invokeLater(() ->
		{
			Window window = null;
			for (Component c = (Applet) plugin.getClient(); c != null; c = c.getParent())
			{
				if (c instanceof Window)
				{
					window = (Window) c;
					break;
				}
			}
			new HotkeyDialog(window, this.displayText, defaultMultiBind(), bind, bind ->
			{
				this.bind = bind;
				plugin.getConfigManager().setConfiguration(FredsBetterTeleportMenuConfig.GROUP, FredsBetterTeleportMenuConfig.KEYBIND_PREFIX + identifier, bind.toConfig());
				plugin.getClientThread().invokeLater(() -> hotkeyChanged());
			});
		});
	}

	void clearKeyListener()
	{
		keyListenerWidget.setOnKeyListener((Object[]) null);
	}

	void onTrigger(FredsBetterTeleportMenuPlugin plugin)
	{
		if (disabled || timeout >= plugin.getClient().getGameCycle())
		{
			return;
		}

		saveLastDest(plugin);
		plugin.resume(resumeWidget);
		textWidget.setText("Please wait...");
		timeout = plugin.getClient().getGameCycle() + 20;
	}

	void saveLastDest(FredsBetterTeleportMenuPlugin plugin)
	{
		if (LAST_DEST_NAMES.contains(menuIdentifier))
		{
			plugin.getConfigManager().setRSProfileConfiguration(FredsBetterTeleportMenuConfig.GROUP, "lastdest." + menuIdentifier, displayText);
		}
	}

	Multikeybind.MatchState matches(FredsBetterTeleportMenuPlugin plugin, List<KeyEvent> keyEvent)
	{
		if (bind == null)
		{
			return Multikeybind.MatchState.NO;
		}

		return bind.matches(keyEvent, plugin.getConfig().aliasNumpad());
	}
}