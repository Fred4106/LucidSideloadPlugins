package com.fredplugins.zulrahhelper;

import com.fredplugins.zulrahhelper.options.Prayer;
import com.fredplugins.zulrahhelper.options.StandLocation;
import com.google.inject.Provides;
import com.fredplugins.zulrahhelper.tree.Node;
import com.fredplugins.zulrahhelper.tree.PatternTree;
import com.fredplugins.zulrahhelper.ui.FredsZulrahHelperPanel;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Zulrah Helper</html>",
	description = "Panel to show Zulrah rotations",
	tags = {"zulrah", "pvm"}
)
public class FredsZulrahHelperPlugin extends Plugin
{
	public static BufferedImage ICON_16 = ImageUtil.loadImageResource(com.fredplugins.zulrahhelper.FredsZulrahHelperPlugin.class, "icon.png");
	public static BufferedImage FLOOR_IMG =  ImageUtil.loadImageResource(FredsZulrahHelperPlugin.class, "floor.png");
	public static BufferedImage SNAKELINGS = ImageUtil.loadImageResource(FredsZulrahHelperPlugin.class, "options/snakeling2.png");
	public static BufferedImage HITSPLAT =   ImageUtil.loadImageResource(FredsZulrahHelperPlugin.class, "options/hitsplat.png");
	public static BufferedImage VENOM =      ImageUtil.loadImageResource(FredsZulrahHelperPlugin.class, "options/venom.png");
	public static BufferedImage RESET_IMG = ImageUtil.loadImageResource(FredsZulrahHelperPlugin.class, "ui/reset.png");

	static final String CONFIG_GROUP = "fredszulrahhelper";
	static final String SECTION_IMAGE_OPTIONS = "Image Options";
	static final String SECTION_HOTKEYS = "Hotkeys";
	static final String SECTION_MISC = "Miscellaneous";

	static final String DARK_MODE_KEY = "darkMode";
	static final String DISPLAY_PRAYER_KEY = "displayPrayer";
	static final String DISPLAY_ATTACK_KEY = "displayAttack";
	static final String DISPLAY_VENOM_KEY = "displayVenom";
	static final String DISPLAY_SNAKELINGS_KEY = "displaySnakelings";
	static final String IMAGE_ORIENTATION_KEY = "imageOrientation";
	static final String AUTO_HIDE_KEY = "autoHide";
	static final String RESET_ON_LEAVE_KEY = "resetOnLeave";
	static final String MAGE_COLOR_KEY = "mageColor";
	static final String RANGE_COLOR_KEY = "rangeColor";
	static final String MELEE_COLOR_KEY = "meleeColor";


	private static final int ZULANDRA_REGION_ID = 8751;
	private static final int ZULRAH_SPAWN_REGION_ID = 9007;
	private static final int ZULRAH_REGION_ID = 9008;

	private static final List<String> OPTION_KEYS = Arrays.asList(
		DARK_MODE_KEY,
		DISPLAY_PRAYER_KEY,
		DISPLAY_ATTACK_KEY,
		IMAGE_ORIENTATION_KEY,
		RESET_ON_LEAVE_KEY,
		DISPLAY_VENOM_KEY,
		DISPLAY_SNAKELINGS_KEY,
		MAGE_COLOR_KEY,
		MELEE_COLOR_KEY,
		RANGE_COLOR_KEY
	);

	@Inject
	private KeyManager keyManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;

	@Getter
	private PatternTree tree = new PatternTree();

	@Inject
	private FredsZulrahHelperOverlay fredsZulrahHelperOverlay;

	@Inject
	private FredsZulrahHelperSceneOverlay fredsZulrahHelperSceneOverlay;

	@Inject
	@Getter
	private FredsZulrahHelperConfig config;

	private FredsZulrahHelperPanel panel;
	private NavigationButton navButton;

	private final List<HotkeyListener> hotkeys = new ArrayList<>();
	private boolean hotkeysEnabled = false;
	private boolean panelEnabled = false;
	private boolean wasInInstance = false;

	WorldPoint startPosition = null;
	NPC npcZulrah = null;

	@Override
	protected void startUp() throws Exception
	{
		npcZulrah = null;
		overlayManager.add(fredsZulrahHelperOverlay);
		overlayManager.add(fredsZulrahHelperSceneOverlay);

		panel = new FredsZulrahHelperPanel(this);
		navButton = NavigationButton.builder()
			.tooltip("Freds Zulrah Helper")
			.icon(ICON_16)
			.priority(70)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		initHotkeys();
		togglePanel(!config.autoHide(), false);
		reset();
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		overlayManager.remove(fredsZulrahHelperOverlay);
		overlayManager.remove(fredsZulrahHelperSceneOverlay);
		hotkeys.forEach(keyManager::unregisterKeyListener);
		hotkeys.clear();
		npcZulrah = null;
		startPosition = null;
	}

	public Node getCurrentNode()
	{
		return tree.getState();
	}
	
	public List<Node> getBuildPath()
	{
		return tree.buildPath();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}

		if (OPTION_KEYS.contains(event.getKey()))
		{
			rebuildPanel();
		}

		if (event.getKey().equals(AUTO_HIDE_KEY) && !config.autoHide())
		{
			togglePanel(true, false);
		}
	}

	@Provides
	FredsZulrahHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FredsZulrahHelperConfig.class);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		if (isNpcZulrah(npc.getId()))
		{
			npcZulrah = npc;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		if (isNpcZulrah(npc.getId()))
		{
			npcZulrah = null;
		}
	}

	public static boolean isNpcZulrah(int npcId)
	{
		return npcId == NpcID.ZULRAH ||
				npcId == NpcID.ZULRAH_2043 ||
				npcId == NpcID.ZULRAH_2044;
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		checkRegion();
		if(startPosition != null && npcZulrah == null) {
			startPosition = null;
		}
		if(startPosition == null && npcZulrah != null) {
			startPosition = npcZulrah.getWorldLocation();
		}
	}

	private void checkRegion()
	{
		if (inZulrahRegion())
		{
			if (!hotkeysEnabled)
			{
				toggleHotkeys();
			}

			if (config.autoHide() && !panelEnabled)
			{
				togglePanel(true, true);
			}

			if (wasInInstance && !client.getTopLevelWorldView().isInstance())
			{
				reset();
			}

			wasInInstance = client.getTopLevelWorldView().isInstance();
		}
		else
		{
			if (hotkeysEnabled)
			{
				toggleHotkeys();
			}

			if (panelEnabled && config.autoHide())
			{
				togglePanel(false, false);
			}

			if (wasInInstance)
			{
				if (config.resetOnLeave())
				{
					reset();
				}

				wasInInstance = false;
			}
		}
	}

	private boolean inZulrahRegion()
	{
		final int regionId = getRegionId();
		return regionId == ZULANDRA_REGION_ID ||
			((regionId == ZULRAH_SPAWN_REGION_ID || regionId == ZULRAH_REGION_ID) && client.getTopLevelWorldView().isInstance());
	}

	private int getRegionId()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return -1;
		}

		return WorldPoint.fromLocalInstance(client, player.getLocalLocation()).getRegionID();
	}

	public boolean isPrayerEnabled(Prayer p) {
		return client.isPrayerActive(p.getRlPrayer());
	}

	public void reset()
	{
		tree.reset();
		SwingUtilities.invokeLater(() -> panel.rebuildPanel());
	}

	public void setState(Node node)
	{
		var s = tree.find(node.getValue());
		if (s == null)
		{
			log.debug("state not found {}", node);
			return;
		}

		if (s.getValue().isReset())
		{
			reset();
			return;
		}

		tree.setState(s);
		rebuildPanel();
	}

	private void rebuildPanel()
	{
		SwingUtilities.invokeLater(() -> panel.rebuildPanel());
	}

	private void selectOption(int choice)
	{
		var node = tree.getState();
		var choices = node.getChildren();
		if (choice >= choices.size())
		{
			log.error("trying to select nonexistent phase: {} {}", choice, choices.size());
			return;
		}

		setState(choices.get(choice));
	}

	private void initHotkeys()
	{
		hotkeys.forEach(keyManager::unregisterKeyListener);
		hotkeys.clear();

		hotkeys.add(new HotkeyListener(() -> config.phaseSelection1Hotkey())
		{
			@Override
			public void hotkeyPressed()
			{
				selectOption(0);
			}
		});

		hotkeys.add(new HotkeyListener(() -> config.phaseSelection2Hotkey())
		{
			@Override
			public void hotkeyPressed()
			{
				selectOption(1);
			}
		});

		hotkeys.add(new HotkeyListener(() -> config.phaseSelection3Hotkey())
		{
			@Override
			public void hotkeyPressed()
			{
				selectOption(2);
			}
		});

		hotkeys.add(new HotkeyListener(() -> config.nextPhaseHotkey())
		{
			@Override
			public void hotkeyPressed()
			{
				selectOption(0);
			}
		});

		hotkeys.add(new HotkeyListener(() -> config.resetPhasesHotkey())
		{
			@Override
			public void hotkeyPressed()
			{
				reset();
			}
		});
	}

	private void toggleHotkeys()
	{
		hotkeys.forEach(hotkeysEnabled ? keyManager::unregisterKeyListener : keyManager::registerKeyListener);
		hotkeysEnabled = !hotkeysEnabled;
	}

	private void togglePanel(boolean enable, boolean show)
	{
		panelEnabled = enable;
		if (enable)
		{
			clientToolbar.addNavigation(navButton);
			if (show)
			{
				SwingUtilities.invokeLater(() -> {
					clientToolbar.openPanel(navButton);
				});
			}
		}
		else
		{
			clientToolbar.removeNavigation(navButton);
		}
	}

	public int distanceToTile(StandLocation p) {
		return Optional.ofNullable(startPosition).map(sp -> StandLocation.getWorldPoint(sp, p)).map(swp -> {
			log.debug("standLocation {} has worldpoint {}", p, swp);
			return client.getLocalPlayer().getWorldLocation().distanceTo(swp);
		}).orElse(100);
	}
}
