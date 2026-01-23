package com.fredplugins.dialogAssist;

import ch.qos.logback.classic.Level;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.http.api.RuneLiteAPI.GSON;

@PluginDescriptor(
	name = "Freds Dialogue Assistant",
	description = "Highlight and lock NPC dialogue options.",
	tags = "dialogue, dialog, assistant, npc, chat, options, lock, highlight"
)
public class FredsDialogueAssistantPlugin extends Plugin
{
	private final static Logger log;
	static {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FredsDialogueAssistantPlugin.class)).setLevel(Level.DEBUG);
		log = LoggerFactory.getLogger(FredsDialogueAssistantPlugin.class);
	}

	public static final String CONFIG_GROUP = "FredsDialogAssist";
	private final String MAP_KEY = "FredsDialogConfig";
	private int lastInteractionId = -1;
	private int optionParentId = -1;
	private int viewportBoxId = -1;
	private Map<Integer, DialogueConfig> dialogMap = new HashMap<>();
	private List<Widget> recentWidgets = new ArrayList<>();

	@Inject
	private Client client;
	@Inject
	private FredsDialogueAssistantConfig config;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ConfigManager configManager;

	private final static Map<Integer, String> NPC_ID_TO_NAME_MAP = Arrays.stream(NpcID.class.getDeclaredFields()).filter(x -> x.getType() == Integer.TYPE && x.getModifiers() ==(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL))
	.map(f -> {
		Pair<Integer, String> p;
		try {
			p = Pair.of(f.getInt(null), f.getName());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return p;
	}).collect(Collectors.toUnmodifiableMap(Pair::getLeft, Pair::getRight));

	@Override
	protected void startUp()
	{
		loadConfig();
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.CHATMENU)
			checkDialogOptions();
	}

	@Subscribe
	protected void onClientTick(ClientTick clientTick)
	{
		if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen())
			return;

		if (!hasTarget())
			return;

		final List<MenuEntry> entries = new ArrayList<>(Arrays.asList(client.getMenuEntries()));
		final DialogueConfig dConfig = getDConfig(lastInteractionId, true);

		for (MenuEntry entry : entries)
		{
			final Widget widget = entry.getWidget();
			if (!isDialogueOption(widget))
				continue;

			final String option = widget.getText();
			final boolean isHighlighted = dConfig != null && dConfig.isHighlighted(option);
			final boolean isLocked = dConfig != null && dConfig.isLocked(option);

			if (isHighlighted || isLocked)
			{
				client.createMenuEntry(-1)
						.setOption("Reset Option")
						.setTarget("")
						.setType(MenuAction.RUNELITE)
						.onClick((e) -> resetOption(dConfig, option, widget));
			}

			if (!isLocked)
			{
				client.createMenuEntry(-1)
						.setOption("Lock Option")
						.setTarget("")
						.setType(MenuAction.RUNELITE)
						.onClick((e) -> setAsLockedOption(lastInteractionId, option, widget));
			}

			if (!isHighlighted)
			{
				client.createMenuEntry(-1)
						.setOption("Highlight Option")
						.setTarget("")
						.setType(MenuAction.RUNELITE)
						.onClick((e) -> setAsHighlightedOption(lastInteractionId, option, widget));
			}
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
			lastInteractionId = ((NPC)event.getTarget()).getId();
			log.debug("[interact] last id: {} (\"{}\")", lastInteractionId, NPC_ID_TO_NAME_MAP.getOrDefault(lastInteractionId, "UNKNOWN"));
		}
		else if (target == null)
		{
//			lastInteractionId = -1;
//			log.debug("[interact - no set] last id: null");
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		final MenuEntry menuEntry = event.getMenuEntry();
		final Widget widget = menuEntry.getWidget();
		final String targStr = Text.standardize(menuEntry.getTarget());
		final String tOptStr = Text.standardize(menuEntry.getOption());

		log.debug("targ={}, option={}", targStr, tOptStr);
		if(widget==null) {
			return;
		}
		if (widget.getId() >>> 16 == InterfaceID.LUNAR_CONTACT_NPC || targStr.equalsIgnoreCase("NPC Contact")) {
			lastInteractionId=-1;
			switch(tOptStr) {
				case "aya":
					lastInteractionId=NpcID.SLAYER_MASTER_1_AYA;
					break;
				case "tureal":
					lastInteractionId=NpcID.SLAYER_MASTER_1_TUREAL;
					break;
				case "mazchna":
					lastInteractionId=NpcID.SLAYER_MASTER_2_MAZCHNA;
					break;
				case "duradel":
					lastInteractionId=NpcID.SLAYER_MASTER_5_DURADEL;
					break;
				case "kuradal":
					lastInteractionId=NpcID.SLAYER_MASTER_5_KURADAL;
					break;
				case "konar":
					lastInteractionId=NpcID.SLAYER_MASTER_8;
					break;
				case "steve":
					lastInteractionId=NpcID.SLAYER_MASTER_STEVE;
					break;
				case "nieve":
					lastInteractionId=NpcID.SLAYER_MASTER_NIEVE;
					break;
				default:
					break;
			}
			log.debug("[menu clicked] last id: {} (\"{}\")", lastInteractionId, NPC_ID_TO_NAME_MAP.getOrDefault(lastInteractionId, "UNKNOWN"));
			return;
		}

		final DialogueConfig dConfig = getDConfig(lastInteractionId);
		if (dConfig == null || widget == null)
			return;

		final String dialogueOption = widget.getText();
		if (dialogueOption == null || dialogueOption.equals(""))
			return;

		if (dConfig.isLocked(dialogueOption))
			event.consume();
	}

	private boolean isWorldMenuOption(MenuEntry menuEntry)
	{
		final NPC npc = menuEntry.getNpc();
		final MenuAction type = menuEntry.getType();

		return isWorldMenuActionType(type) && npc != null;
	}

	private boolean isMenuException(MenuEntry menuEntry) // Allow menu entries from NPC Contact viewport box
	{
		final int maxSearch = 5;
		Widget widget = menuEntry.getWidget();

		if (menuEntry.getTarget().contains("NPC Contact"))
			return true;

		if (viewportBoxId == -1)
		{
			final Widget viewportBox = client.getWidget(WidgetInfo.RESIZABLE_VIEWPORT_OLD_SCHOOL_BOX);
			if (viewportBox == null)
				return false;

			viewportBoxId = viewportBox.getId();
		}

		for (int i = 0; i < maxSearch; i++)
		{
			if (widget == null)
				return false;

			if (widget.getId() == viewportBoxId)
				return true;

			widget = widget.getParent();
		}

		return false;
	}

	private boolean isWorldMenuActionType(MenuAction type)
	{
		switch(type)
		{
			case GAME_OBJECT_FIRST_OPTION:
			case GAME_OBJECT_SECOND_OPTION:
			case GAME_OBJECT_THIRD_OPTION:
			case GAME_OBJECT_FOURTH_OPTION:
			case GAME_OBJECT_FIFTH_OPTION:
			case NPC_FIRST_OPTION:
			case NPC_SECOND_OPTION:
			case NPC_THIRD_OPTION:
			case NPC_FOURTH_OPTION:
			case NPC_FIFTH_OPTION:
				return true;
			default:
				return false;
		}
	}

	private boolean isDialogueOption(Widget widget)
	{
		if (optionParentId == -1)
		{
			final Widget optionGroup = client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS);
			if (optionGroup == null)
				return false;

			optionParentId = optionGroup.getId();
		}

		return widget != null && widget.getParent() != null && widget.getParent().getId() == optionParentId;
	}

	private void checkDialogOptions()
	{
		final DialogueConfig dConfig = getDConfig(lastInteractionId);
		if (dConfig == null)
			return;

		log.debug("Dialog for {} has config {}", lastInteractionId, dConfig);

		clientThread.invokeAtTickEnd(() ->
		{
			final Widget optionGroup = client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS);
			final Widget[] children = optionGroup != null ? optionGroup.getChildren() : null;

			if (children == null || children.length == 0)
				return;

			final List<Widget> options = Arrays.stream(children).filter(Widget::hasListener).collect(Collectors.toList());
			recentWidgets = options;

			for (Widget optionWidget : options)
			{
				final String option = optionWidget.getText();

				if (dConfig.isHighlighted(option))
				{
					highlightOptionWidget(optionWidget, true, false);
				}
				else if (dConfig.isLocked(option))
				{
					lockOptionWidget(optionWidget, true, false);
				}
			}
		});
	}


	public void getHotkey(Widget w, char dkc) {
		Object[] keyListener = w.getOnKeyListener();
		char kc = KeyEvent.VK_UNDEFINED;
		if (keyListener == null || keyListener.length != 11) {
			return;
		}
		kc = String.valueOf(keyListener[7]).charAt(0);
		if (kc != dkc)
		{
			keyListener[7] = String.valueOf(dkc).toLowerCase();
			w.setOnKeyListener(keyListener);
			w.revalidate();
		}
	}

	private void highlightOptionWidget(Widget optionWidget, boolean highlighted, boolean reset)
	{
		if (highlighted)
		{
			optionWidget.setTextColor(config.optionHighlightColor().getRGB());
			optionWidget.setOnMouseLeaveListener((JavaScriptCallback) ev -> optionWidget.setTextColor(config.optionHighlightColor().getRGB()));
			getHotkey(optionWidget, ' ');
		}
		else if (reset)
		{
			optionWidget.setTextColor(Color.BLACK.getRGB());
			optionWidget.setOnMouseLeaveListener((JavaScriptCallback) ev -> optionWidget.setTextColor(Color.BLACK.getRGB()));
			getHotkey(optionWidget, String.valueOf(optionWidget.getIndex()).toLowerCase().charAt(0));
		}
	}

	private void lockOptionWidget(Widget optionWidget, boolean locked, boolean reset)
	{
		if (locked)
		{
			optionWidget.setHasListener(false);
			optionWidget.setTextColor(config.optionLockedColor().getRGB());
			optionWidget.setOnMouseLeaveListener((JavaScriptCallback) ev -> optionWidget.setTextColor(config.optionLockedColor().getRGB()));
		}
		else if (reset)
		{
			optionWidget.setHasListener(true);
			optionWidget.setTextColor(Color.BLACK.getRGB());
			optionWidget.setOnMouseLeaveListener((JavaScriptCallback) ev -> optionWidget.setTextColor(Color.BLACK.getRGB()));
		}
	}

	private void setAsHighlightedOption(int targetId, String optionTarget, Widget widget)
	{
		clientThread.invokeLater(() ->
		{
			final DialogueConfig dConfig = getDConfig(targetId, true);
			dConfig.setHighlighted(optionTarget);
			refreshOptionState(dConfig, optionTarget, widget);
			saveConfig();
		});
	}

	private void setAsLockedOption(int targetId, String optionTarget, Widget widget)
	{
		clientThread.invokeLater(() ->
		{
			final DialogueConfig dConfig = getDConfig(targetId, true);
			dConfig.setLocked(optionTarget);
			refreshOptionState(dConfig, optionTarget, widget);
			saveConfig();
		});
	}

	private void resetOption(DialogueConfig dConfig, String optionTarget, Widget widget)
	{
		clientThread.invokeLater(() ->
		{
			dConfig.resetOption(optionTarget);
			refreshOptionState(dConfig, optionTarget, widget);
			saveConfig();
		});
	}

	private void refreshOptionState(DialogueConfig dConfig, String optionTarget, Widget widget)
	{
		highlightOptionWidget(widget, false, true);
		lockOptionWidget(widget, false, true);

		highlightOptionWidget(widget, dConfig.isHighlighted(optionTarget), false);
		lockOptionWidget(widget, dConfig.isLocked(optionTarget), false);
	}

	private DialogueConfig getDConfig(int id)
	{
		if (id == -1)
			return null;

		return getDConfig(id, false);
	}

	private DialogueConfig getDConfig(int id, boolean forceCreate)
	{
		if (id == -1)
			return null;

		if (forceCreate && !dialogMap.containsKey(id))
		{
			dialogMap.put(id, new DialogueConfig(id));
		}
		DialogueConfig dConfig = dialogMap.get(id);
//		if(dConfig != null)
//			log.debug("getDConfig({}, {}) = {}", id, forceCreate, dConfig);
		return dConfig;
	}

	private void resetAllRecentWidgets()
	{
		clientThread.invokeLater(() ->
		{
			for (Widget widget : recentWidgets)
			{
				if (widget == null)
					continue;

				highlightOptionWidget(widget, false, true);
				lockOptionWidget(widget, false, true);
			}

			recentWidgets.clear();
		});
	}

	private void saveConfig()
	{
		final String json = GSON.toJson(dialogMap);
		configManager.setConfiguration(CONFIG_GROUP, MAP_KEY, json);
	}

	private void loadConfig()
	{
		final String json = configManager.getConfiguration(CONFIG_GROUP, MAP_KEY);
		if (json == null || json.equals(""))
		{
			dialogMap = new HashMap<>();
		}
		else
		{
			final Type mapType = new TypeToken<Map<Integer, DialogueConfig>>(){}.getType();
			dialogMap = GSON.fromJson(json, mapType);
		}
		dialogMap.keySet().stream().sorted().collect(Collectors.toList()).forEach(x -> {
			DialogueConfig d = dialogMap.get(x);
			String dialogStr = "NULL";
			if(d!=null) dialogStr = d.toString();
			log.debug("dialogMap[{}] = {}", x, dialogStr);
		});
	}

	private boolean hasTarget()
	{
		return lastInteractionId != -1;
	}

	@Override
	protected void shutDown()
	{
		lastInteractionId = -1;
		resetAllRecentWidgets();
	}

	@Provides
	FredsDialogueAssistantConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FredsDialogueAssistantConfig.class);
	}
}
