package com.fredplugins.teleportMenu;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.PostStructComposition;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Freds Better Teleport Menu",
	description = "Customize hotkeys for the Spirit Tree/Jewelery box/Portal nexus layout/Diary/Construction cape interfaces & enlarge league menus",
	tags = {"poh","jewelery","cape","diary","tele","port","nexus","hotkey","keybind","ancient","names","disable","strikethrough","league","clue compass","fairys flight","bank heist"},
	conflicts = {"Better Teleport Menu"}
)
@Singleton
public class FredsBetterTeleportMenuPlugin extends Plugin
{
	static final int PARAMID_TELENEXUS_DESTINATION_NAME = 660;

	static final char CHAR_UNSET = '\0';

	static final Pattern KEY_PREFIX_MATCHER = Pattern.compile("^(?:(<[^>]+>)([A-Za-z0-9])(:</[^>]+> |</[^>]+> *: +))?(.*?)((?:\\([^)]+\\))?)$");
	
	private static final Map<Integer, String> ALTERNATE_NEXUS_NAMES = ImmutableMap.<Integer, String>builder()
		.put(459, "Digsite")
		.put(460, "Ape Atoll")
		.put(461, "Canifis")
		.put(466, "Demonic Ruins")
		.put(469, "Frozen Waste Plateau")
		.put(470, "Graveyard of Shadows")
		.build();

	static final Map<Integer, String> SAVE_LAST_DEST = ImmutableMap.<Integer, String>builder()
		.put(ItemID.LEAGUE_CLUE_COMPASS_TELEPORT, "clue-compass-teleports")
		.put(ItemID.LEAGUE_BANK_HEIST_TELEPORT, "bank-heist-teleports")
		.build();
	static final Set<String> LAST_DEST_NAMES = ImmutableSet.copyOf(SAVE_LAST_DEST.values());

	// cleanify(display) -> canon
	static Map<String, String> canonicalNames = new HashMap<>();

	@Inject
	@Getter
	private Client client;

	@Inject
	private KeyManager keyManager;

	@Inject
	@Getter
	private ClientThread clientThread;

	@Inject
	@Getter
	private FredsBetterTeleportMenuConfig config;

	@Inject
	@Getter
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MenuBackgroundOverlay menuBackgroundOverlay;

	@Inject
	private VarlamoreOverlay varlamoreOverlay;

	@Getter
	private List<KeyEvent> recentKeypresses = new ArrayList<>();

	List<TeleMenu> teleMenus = new ArrayList<>();
	boolean menuJustOpened = false;

	@Subscribe
	private void onGameTick(GameTick t)
	{
		List<TeleMenu> change = null;
		for (TeleMenu menu : teleMenus)
		{
			if (client.getWidget(menu.textWidget.getId() >> 16, 0) == null)
			{
				if (change == null)
				{
					change = new ArrayList<>(teleMenus);
				}
				change.remove(menu);
			}
		}
		if (change != null)
		{
			teleMenus = change;
		}
	}

	@Subscribe
	private void onWidgetLoaded(WidgetLoaded wl)
	{
		if (wl.getGroupId() == MenuBackgroundOverlay.IF_MENU)
		{
			menuBackgroundOverlay.onInterfaceLoaded();
		}
	}

	private String activeMenu = null;

	@Subscribe
	private void onScriptPreFired(ScriptPreFired ev)
	{
		switch (ev.getScriptId())
		{
			case ScriptID.MENU_SETUP:
			{
				String title = (String) client.getObjectStack()[client.getObjectStackSize() - 1];
				activeMenu = cleanify(title);
				teleMenus = new ArrayList<>();
				break;
			}
		}
	}

	@Subscribe
	private void onPostStructComposition(PostStructComposition ev)
	{
		String newName = ALTERNATE_NEXUS_NAMES.get(ev.getStructComposition().getId());
		if (newName != null)
		{
			if (config.alternateNames())
			{
				String oldName = ev.getStructComposition().getStringValue(PARAMID_TELENEXUS_DESTINATION_NAME);
				// the old name in parentheses is stripped before being passed to cleanify
				String cleanOld = "telenexus-" + cleanify(oldName);
				String cleanNew = "telenexus-" + cleanify(newName);
				canonicalNames.put(cleanNew + "-", cleanOld);
				ev.getStructComposition().setValue(PARAMID_TELENEXUS_DESTINATION_NAME, newName + " (" + oldName + ")");
			}
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged ev)
	{
		if (!FredsBetterTeleportMenuConfig.GROUP.equals(ev.getGroup()))
		{
			return;
		}

		if ("alternateNames".equals(ev.getKey()))
		{
			clientThread.invoke(() ->
				client.getStructCompositionCache().reset());
		}
	}

	@Subscribe(priority = 1.f)
	private void onScriptPostFired(ScriptPostFired ev)
	{
		switch (ev.getScriptId())
		{
			case ScriptID.MENU:
			case ScriptID.TOPLEVEL_RESIZE:
				menuBackgroundOverlay.resize();
				break;
			case ScriptID.MENU_CREATEENTRY:
				if (activeMenu != null)
				{
					new TeleMenu()
						.textWidget(client.getScriptActiveWidget())
						.resumeWidget(client.getScriptActiveWidget())
						.opWidget(client.getScriptActiveWidget())
						.keyListenerWidget(client.getScriptDotWidget())
						.menuIdentifier(activeMenu)
						.disable(() ->
						{
							Widget textWidget = client.getScriptActiveWidget();
							textWidget.setHidden(true);
							client.getIntStack()[client.getIntStackSize() - 1] -= textWidget.getOriginalHeight();
						})
						.build(this);
				}
				break;
			case ScriptID.TELENEXUS_CREATE_TELELINE:
				new TeleMenu()
					.textWidget(client.getScriptActiveWidget())
					.resumeWidget(client.getScriptDotWidget())
					.opWidget(client.getWidget(client.getScriptActiveWidget().getId() + 1)
						.getChild(client.getScriptActiveWidget().getIndex()))
					.keyListenerWidget(client.getScriptDotWidget())
					.menuIdentifier("telenexus")
					.build(this);
				break;
			case ScriptID.POH_JEWELLERY_BOX_ADDBUTTON:
				new TeleMenu()
					.textWidget(client.getScriptActiveWidget())
					.resumeWidget(client.getScriptDotWidget())
					.opWidget(client.getScriptActiveWidget())
					.keyListenerWidget(client.getScriptDotWidget())
					.menuIdentifier("jewelbox")
					.build(this);
				break;
		}
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded ev)
	{
		if (ev.getMenuEntry().isItemOp() && "Last-destination".equals(ev.getOption()) && config.showLastDestination())
		{
			String name = SAVE_LAST_DEST.get(ev.getMenuEntry().getItemId());
			if (name != null)
			{
				String last = configManager.getRSProfileConfiguration(FredsBetterTeleportMenuConfig.GROUP, "lastdest." + name);
				if (last != null)
				{
					ev.getMenuEntry().setOption(ev.getMenuEntry().getOption() + " (" + last + ")");
				}
			}
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked ev)
	{
		log.debug("option=\"{}\", target=\"{}\", action=\"{}\", id=\"{}\", params({}, {})", ev.getMenuOption(), ev.getMenuTarget(), ev.getMenuAction(), ev.getId(), ev.getParam0(), ev.getParam1());
		if (ev.getMenuAction() == MenuAction.CC_OP_LOW_PRIORITY && (ev.getId() - 1) == TeleMenu.CLEAR_BINDING_OP)
		{
			for (TeleMenu menu : teleMenus)
			{
				if (menu.opWidget.getId() == ev.getParam1() && menu.opWidget.getIndex() == ev.getParam0())
				{
					menu.bind = new Multikeybind();
					configManager.setConfiguration(FredsBetterTeleportMenuConfig.GROUP, FredsBetterTeleportMenuConfig.KEYBIND_PREFIX + menu.identifier, menu.bind.toConfig());
					clientThread.invokeLater(menu::hotkeyChanged);
					ev.consume();
					//menu.hotkeyChanged();
//					if(ev.getMenuOption().equals("Reset Hotkey")) {
//						newBinding = new Multikeybind();
//					} else if(ev.getMenuOption().equals("Clear Hotkey")){
//						newBinding = menu.defaultMultiBind();
//					} else {
//						log.debug("Cant process {} from menu {}", ev.getMenuOption(), menu.identifier);
//					}
					return;
				}
			}
		}

		if (ev.getMenuAction() == MenuAction.CC_OP_LOW_PRIORITY && (ev.getId() - 1) == TeleMenu.DEFAULT_BINDING_OP)
		{
			for (TeleMenu menu : teleMenus)
			{
				if (menu.opWidget.getId() == ev.getParam1() && menu.opWidget.getIndex() == ev.getParam0())
				{
					menu.bind = menu.defaultMultiBind();
					configManager.setConfiguration(FredsBetterTeleportMenuConfig.GROUP, FredsBetterTeleportMenuConfig.KEYBIND_PREFIX + menu.identifier, menu.bind.toConfig());
					clientThread.invokeLater(menu::hotkeyChanged);
					ev.consume();
					//menu.hotkeyChanged();
//					if(ev.getMenuOption().equals("Reset Hotkey")) {
//						newBinding = new Multikeybind();
//					} else if(ev.getMenuOption().equals("Clear Hotkey")){
//						newBinding = menu.defaultMultiBind();
//					} else {
//						log.debug("Cant process {} from menu {}", ev.getMenuOption(), menu.identifier);
//					}
					return;
				}
			}
		}

		if (ev.getMenuAction() == MenuAction.CC_OP_LOW_PRIORITY && (ev.getId() - 1) == TeleMenu.CHANGE_BINDING_OP)
		{
			for (TeleMenu menu : teleMenus)
			{
				if (menu.opWidget.getId() == ev.getParam1() && menu.opWidget.getIndex() == ev.getParam0())
				{
					menu.openSetDialog(this);
					ev.consume();
					return;
				}
			}
		}

		if (ev.getMenuAction() == MenuAction.WIDGET_CONTINUE)
		{
			for (TeleMenu menu : teleMenus)
			{
				if (menu.opWidget.getId() == ev.getParam1() && menu.opWidget.getIndex() == ev.getParam0())
				{
					menu.saveLastDest(this);
				}
			}
		}
	}

	@Override
	public void resetConfiguration()
	{
		for (String key : configManager.getConfigurationKeys(FredsBetterTeleportMenuConfig.GROUP + ".keybind."))
		{
			int firstDot = key.indexOf('.');
			if (firstDot != -1)
			{
				configManager.unsetConfiguration(FredsBetterTeleportMenuConfig.GROUP, key.substring(firstDot + 1));
			}
		}
	}

	void resume(Widget w)
	{
		assert w.getId() == w.getParentId();
		// we are abusing this cs2 to just do a cc_find + cc_resume_pausebutton for us
		client.runScript(ScriptID.SOMETHING_THAT_CC_RESUME_PAUSEBUTTON, w.getId(), w.getIndex());
	}

	static String cleanify(String in)
	{
		in = Text.removeTags(in);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < in.length(); i++)
		{
			char c = in.charAt(i);
			c = Character.toLowerCase(c);
			if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))
			{
				sb.append(c);
			}
			else if (c == '-' || c == ' ')
			{
				sb.append('-');
			}
		}
		return sb.toString();
	}

	private final KeyListener keyAdapter = new net.runelite.client.input.KeyListener() {
		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyReleased(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			if (teleMenus.isEmpty())
			{
				return;
			}

			if (menuJustOpened)
			{
				menuJustOpened = false;
				recentKeypresses.clear();
			}

			recentKeypresses.add(e);
			for (; recentKeypresses.size() > 8; recentKeypresses.remove(0)) ;
			for (TeleMenu menu : teleMenus)
			{
				if (menu.disabled)
				{
					continue;
				}

				Multikeybind.MatchState match = menu.matches(FredsBetterTeleportMenuPlugin.this, recentKeypresses);
				if (match != Multikeybind.MatchState.NO)
				{
					e.consume();

					if (match == Multikeybind.MatchState.YES)
					{
						clientThread.invokeLater(() -> menu.onTrigger(FredsBetterTeleportMenuPlugin.this));
						return;
					}
				}
				if (menu.highlight != (match == Multikeybind.MatchState.PARTIAL))
				{
					menu.highlight = match == Multikeybind.MatchState.PARTIAL;
					menu.hotkeyChanged();
				}
			}
		}
	};

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(keyAdapter);
		// bleh idc to make this work

		overlayManager.add(menuBackgroundOverlay);
		overlayManager.add(varlamoreOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(keyAdapter);
		clientThread.invokeLater(() ->
		{
			client.getStructCompositionCache().reset();
		});
		// less bleh to make work but idc still

		overlayManager.remove(menuBackgroundOverlay);
		overlayManager.remove(varlamoreOverlay);
	}

	@Provides
	FredsBetterTeleportMenuConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FredsBetterTeleportMenuConfig.class);
	}
}
