package com.fredplugins.customprayers;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;
import com.lucidplugins.api.item.SlottedItem;
import com.lucidplugins.api.utils.CombatUtils;
import com.lucidplugins.api.utils.EquipmentUtils;
import com.lucidplugins.api.utils.InteractionUtils;
import com.lucidplugins.api.utils.MessageUtils;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Extension
@PluginDescriptor(
		name = "<html><font color=\"#32C8CD\">Freds</font> Custom Prayers</html>",
		description = "Set up auto prayers based on various event IDs",
		enabledByDefault = false,
		tags = {"prayer", "swap"},
		conflicts = {"<html><font color=\"#32CD32\">Lucid </font>Custom Prayers</html>"}
)
public class FredsCustomPrayersPlugin extends Plugin implements KeyListener {

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private FredsCustomPrayersConfig config;

	@Inject
	ConfigManager configManager;

	@Inject
	private KeyManager keyManager;

	private Map<EventType, List<CustomPrayer>> eventMap = new HashMap<>();

	private List<Projectile> validProjectiles = new ArrayList<>();

	private List<ScheduledPrayer> scheduledPrayers = new ArrayList<>();

	private List<Integer> animationsThisTick = new ArrayList<>();

	private List<Integer> npcsSpawnedThisTick = new ArrayList<>();

	private List<Integer> npcsDespawnedThisTick = new ArrayList<>();

	private List<Integer> npcsChangedThisTick = new ArrayList<>();

	private List<Integer> projectilesSpawnedThisTick = new ArrayList<>();

	private List<Integer> graphicsCreatedThisTick = new ArrayList<>();

	private List<Integer> gameObjectsSpawnedThisTick = new ArrayList<>();

	private List<Integer> npcsInteractingWithYouThisTick = new ArrayList<>();

	private List<Integer> npcsYouInteractedWithThisTick = new ArrayList<>();

	private List<String> lastEquipmentList = new ArrayList<>();

	public static final File PRESET_DIR = new File(RUNELITE_DIR, "freds-custom-prayers");

	public static final String FILENAME_SPECIAL_CHAR_REGEX = "[^a-zA-Z\\d:]";

	public final GsonBuilder builder = new GsonBuilder()
			.setPrettyPrinting();
	public final Gson gson = builder.create();

	public Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private static boolean oneTickFlicking = false;

	private static boolean disableQuickPrayers = false;

	private final NPC DUMMY_NPC = new DummyNPC();

	@Provides
	FredsCustomPrayersConfig getConfig(final ConfigManager configManager) {
		return configManager.getConfig(FredsCustomPrayersConfig.class);
	}

	@Override
	protected void startUp() {

		keyManager.registerKeyListener(this);
		eventMap = ConfigUtils$.MODULE$.parsePrayers(config);
	}

	@Override
	protected void shutDown() {
		keyManager.unregisterKeyListener(this);
	}

	@Subscribe(priority = 20)
	private void onAnimationChanged(final AnimationChanged event) {
		if(event.getActor() == null) {
			return;
		}

		int animId = event.getActor().getAnimation();

		if(!animationsThisTick.contains(animId) || config.allowDuplicateAnimationEvents()) {
			if(event.getActor() instanceof NPC) {
				final NPC npc = (NPC) event.getActor();
				eventFired(EventType.ANIMATION_CHANGED, animId, npc.getInteracting() == client.getLocalPlayer(), npc);
			} else {
				eventFired(EventType.ANIMATION_CHANGED, animId, event.getActor() == client.getLocalPlayer());
			}

			animationsThisTick.add(animId);
		}
	}

	@Subscribe(priority = 20)
	private void onNpcSpawned(final NpcSpawned event) {
		if(event.getNpc() == null) {
			return;
		}

		int npcId = event.getNpc().getId();

		if(!npcsSpawnedThisTick.contains(npcId)) {
			eventFired(EventType.NPC_SPAWNED, npcId, false, event.getNpc());
			npcsSpawnedThisTick.add(npcId);
		}
	}

	@Subscribe(priority = 20)
	private void onNpcDespawned(final NpcDespawned event) {
		if(event.getNpc() == null) {
			return;
		}

		int npcId = event.getNpc().getId();

		if(!npcsDespawnedThisTick.contains(npcId)) {
			eventFired(EventType.NPC_DESPAWNED, npcId, false);
			npcsDespawnedThisTick.add(npcId);
		}
	}

	@Subscribe(priority = 20)
	private void onNpcChanged(final NpcChanged event) {
		if(event.getNpc() == null) {
			return;
		}

		int npcId = event.getNpc().getId();

		if(!npcsChangedThisTick.contains(npcId)) {
			eventFired(EventType.NPC_CHANGED, npcId, event.getNpc().getInteracting() == client.getLocalPlayer(), event.getNpc());
			npcsChangedThisTick.add(npcId);
		}
	}

	@Subscribe(priority = 20)
	private void onProjectileMoved(final ProjectileMoved event) {
		if(validProjectiles.contains(event.getProjectile())) {
			return;
		}

		validProjectiles.add(event.getProjectile());

		int projectileId = event.getProjectile().getId();
		if(!projectilesSpawnedThisTick.contains(projectileId) || config.allowDuplicateProjectileEvents()) {
			eventFired(EventType.PROJECTILE_SPAWNED, projectileId, event.getProjectile().getTarget().equals(client.getLocalPlayer().getLocalLocation()) || event.getProjectile().getInteracting() == client.getLocalPlayer());
			projectilesSpawnedThisTick.add(projectileId);
		}
	}

	@Subscribe(priority = 20)
	private void onGraphicsObjectCreated(final GraphicsObjectCreated event) {
		int graphicsId = event.getGraphicsObject().getId();

		if(!graphicsCreatedThisTick.contains(graphicsId) || config.allowDuplicateGraphicsEvents()) {
			eventFired(EventType.GRAPHICS_CREATED, graphicsId, event.getGraphicsObject().getLocation().equals(client.getLocalPlayer().getLocalLocation()));
			graphicsCreatedThisTick.add(graphicsId);
		}
	}

	@Subscribe(priority = 20)
	private void onGameObjectSpawned(final GameObjectSpawned event) {
		int objectId = event.getGameObject().getId();

		if(!gameObjectsSpawnedThisTick.contains(objectId)) {
			eventFired(EventType.GAME_OBJECT_SPAWNED, objectId, false);
			gameObjectsSpawnedThisTick.add(objectId);
		}
	}

	@Subscribe(priority = 20)
	private void onInteractingChanged(final InteractingChanged event) {
		Actor source = event.getSource();
		Actor interacting = event.getSource().getInteracting();

		if(interacting == null) {
			return;
		}

		if(interacting == client.getLocalPlayer() && !(source instanceof Player)) {
			final NPC npc = (NPC) source;
			if(!npcsInteractingWithYouThisTick.contains(npc.getId())) {
				eventFired(EventType.OTHER_INTERACT_YOU, npc.getId(), true, npc);
				npcsInteractingWithYouThisTick.add(npc.getId());
			}
		}

		if(source == client.getLocalPlayer() && !(interacting instanceof Player)) {
			final NPC interactingNpc = (NPC) interacting;
			if(!npcsYouInteractedWithThisTick.contains(interactingNpc.getId())) {
				eventFired(EventType.YOU_INTERACT_OTHER, interactingNpc.getId(), interacting.getInteracting() == client.getLocalPlayer(), interactingNpc);
				npcsYouInteractedWithThisTick.add(interactingNpc.getId());
			}
		}
	}

	@Subscribe(priority = 20)
	private void onConfigChanged(final ConfigChanged event) {
		if(!event.getGroup().equals("freds-custom-prayers")) {
			return;
		}

		eventMap = ConfigUtils$.MODULE$.parsePrayers(config);
	}

	@Subscribe(priority = 20)
	private void onGameTick(final GameTick event) {
		getEquipmentChanges();

		if(oneTickFlicking) {
			if(CombatUtils.isQuickPrayersEnabled()) {
				CombatUtils.toggleQuickPrayers();
				CombatUtils.toggleQuickPrayers();
			} else {
				CombatUtils.toggleQuickPrayers();
			}
		} else {
			if(disableQuickPrayers && CombatUtils.isQuickPrayersEnabled()) {
				CombatUtils.toggleQuickPrayers();
				disableQuickPrayers = false;
			} else if(config.flickOnActivate()) {
				boolean usedQP = CombatUtils.isQuickPrayersEnabled();
				Prayer offense = CombatUtils.getActiveOffense();
				Prayer overhead = CombatUtils.getActiveOverhead();
				if(usedQP) {
					CombatUtils.toggleQuickPrayers();
					CombatUtils.toggleQuickPrayers();
				} else if(overhead != null) {
					CombatUtils.deactivatePrayer(overhead);
					CombatUtils.activatePrayer(overhead);
				}

				if(config.flickOffensives() && offense != null) {
					CombatUtils.deactivatePrayer(offense);
					CombatUtils.activatePrayer(offense);
				}
			}
		}

		for(ScheduledPrayer prayer : scheduledPrayers) {
			boolean ignore = config.ignoreDeadNpcEvents() && (prayer.getAttached() == null || prayer.getAttached().isDead());
			if(client.getTickCount() == prayer.getActivationTick() && !ignore) {
				activatePrayer(client, prayer.getPrayer(), prayer.isToggle());
			}
		}

		scheduledPrayers.removeIf(prayer -> prayer.getActivationTick() <= client.getTickCount() - 1);

		animationsThisTick.clear();
		npcsSpawnedThisTick.clear();
		npcsDespawnedThisTick.clear();
		npcsChangedThisTick.clear();
		projectilesSpawnedThisTick.clear();
		graphicsCreatedThisTick.clear();
		gameObjectsSpawnedThisTick.clear();
		npcsInteractingWithYouThisTick.clear();
		npcsYouInteractedWithThisTick.clear();

		validProjectiles.removeIf(proj -> proj.getRemainingCycles() < 1);
	}

	private void getEquipmentChanges() {
		Widget bankWidget = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if(bankWidget != null && !bankWidget.isSelfHidden()) {
			return;
		}

		final List<SlottedItem> equippedItems = EquipmentUtils.getAll();
		final List<String> itemsMapped = equippedItems.stream().map(item -> client.getItemDefinition(item.getItem().getId()).getName()).collect(Collectors.toList());

		if(!listsMatch(itemsMapped, lastEquipmentList)) {
			for(SlottedItem slottedItem : equippedItems) {
				String name = client.getItemDefinition(slottedItem.getItem().getId()).getName();
				if(!lastEquipmentList.contains(name)) {
					eventFired(EventType.ITEM_EQUIPPED, slottedItem.getItem().getId(), false);
				}
			}
			lastEquipmentList.clear();
			lastEquipmentList.addAll(itemsMapped);
		}
	}

	private static void activatePrayer(Client client, Prayer prayer, boolean toggle) {
		boolean useQuickPrayers = false;

		if(prayer == Prayer.THICK_SKIN) {
			useQuickPrayers = true;
		}

		if(prayer == Prayer.BURST_OF_STRENGTH) {
			if(toggle) {
				oneTickFlicking = !oneTickFlicking;
				if(!oneTickFlicking) {
					disableQuickPrayers = true;
				}
			} else {
				oneTickFlicking = true;
			}
			return;
		}

		if(prayer == Prayer.CLARITY_OF_THOUGHT) {
			oneTickFlicking = false;
			disableQuickPrayers = true;
			return;
		}

		if(toggle) {
			if(useQuickPrayers) {
				CombatUtils.toggleQuickPrayers();
			} else {
				CombatUtils.togglePrayer(prayer);
			}
		} else {
			if(useQuickPrayers) {
				CombatUtils.activateQuickPrayers();
			} else {
				CombatUtils.activatePrayer(prayer);
			}
		}
	}

	private void eventFired(EventType type, int id, boolean isTargetingPlayer, NPC attached) {
		if(config.debugMode() && isEventDebugged(type)) {
			if((config.hideNonTargetEventsDebug() && isTargetingPlayer) || !config.hideNonTargetEventsDebug()) {
				MessageUtils.addMessage("Event Type: " + type.name() + ",  ID: " + id + ", Tick: " + client.getTickCount() + ", Targeting player: " + isTargetingPlayer, Color.RED);
			}
		}

		List<CustomPrayer> prayers = eventMap.get(type);
		if(prayers == null || prayers.isEmpty()) {
			return;
		}

		for(final CustomPrayer prayer : prayers) {
			if(prayer.getActivationId() == id) {
				if(prayer.isIgnoreNonTargetEvent()) {
					if(!isTargetingPlayer) {
						continue;
					}
				}

				scheduledPrayers.add(new ScheduledPrayer(prayer.getPrayerToActivate(), client.getTickCount() + prayer.getTickDelay(), prayer.isToggle(), attached));
			}
		}
	}

	private void eventFired(EventType type, int id, boolean isTargetingPlayer) {
		eventFired(type, id, isTargetingPlayer, DUMMY_NPC);
	}

	public boolean listsMatch(List<String> list1, List<String> list2) {
		if(list1.size() != list2.size()) {
			return false;
		}

		List<String> list2Copy = Lists.newArrayList(list2);
		for(String element : list1) {
			if(!list2Copy.remove(element)) {
				return false;
			}
		}

		return list2Copy.isEmpty();
	}

	private boolean isEventDebugged(EventType type) {
		switch(type) {
			case ANIMATION_CHANGED:
				return config.debugAnimationChanged();
			case NPC_SPAWNED:
				return config.debugNpcSpawned();
			case NPC_DESPAWNED:
				return config.debugNpcDespawned();
			case NPC_CHANGED:
				return config.debugNpcChanged();
			case PROJECTILE_SPAWNED:
				return config.debugProjectileSpawned();
			case GRAPHICS_CREATED:
				return config.debugGraphicsCreated();
			case GAME_OBJECT_SPAWNED:
				return config.debugGameObjectSpawned();
			case OTHER_INTERACT_YOU:
				return config.debugOtherInteractYou();
			case YOU_INTERACT_OTHER:
				return config.debugYouInteractOther();
			case ITEM_EQUIPPED:
				return config.debugItemEquipped();
			default:
				return false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(config.toggle1tickQuickPrayersHotkey().matches(e)) {
			oneTickFlicking = !oneTickFlicking;
			if(!oneTickFlicking) {
				disableQuickPrayers = true;
			}
		}

		if(config.dumpPresetHotkey().matches(e)) {
			clientThread.invoke(this::dumpPreset);
		}

		if(config.loadPresetHotkey().matches(e)) {
			clientThread.invoke(this::loadPreset);
		}

		if(config.savePresetHotkey().matches(e)) {
			clientThread.invoke(this::savePreset);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	private void dumpPreset() {

	}

	private void savePreset() {
		String presetName = config.presetName();
		String presetNameFormatted = presetName.replaceAll(FILENAME_SPECIAL_CHAR_REGEX, "").replaceAll(" ", "_").toLowerCase();

		if(presetNameFormatted.isEmpty()) {
			return;
		}

		ExportableConfig exportableConfig = new ExportableConfig();

		exportableConfig.setPrayer(0, config.activated1(), config.pray1Ids(), config.pray1delays(), config.pray1choice(), config.eventType1(), config.toggle1(), config.ignoreNonTargetEvents1());
		exportableConfig.setPrayer(1, config.activated2(), config.pray2Ids(), config.pray2delays(), config.pray2choice(), config.eventType2(), config.toggle2(), config.ignoreNonTargetEvents2());
		exportableConfig.setPrayer(2, config.activated3(), config.pray3Ids(), config.pray3delays(), config.pray3choice(), config.eventType3(), config.toggle3(), config.ignoreNonTargetEvents3());
		exportableConfig.setPrayer(3, config.activated4(), config.pray4Ids(), config.pray4delays(), config.pray4choice(), config.eventType4(), config.toggle4(), config.ignoreNonTargetEvents4());
		exportableConfig.setPrayer(4, config.activated5(), config.pray5Ids(), config.pray5delays(), config.pray5choice(), config.eventType5(), config.toggle5(), config.ignoreNonTargetEvents5());
		exportableConfig.setPrayer(5, config.activated6(), config.pray6Ids(), config.pray6delays(), config.pray6choice(), config.eventType6(), config.toggle6(), config.ignoreNonTargetEvents6());
		exportableConfig.setPrayer(6, config.activated7(), config.pray7Ids(), config.pray7delays(), config.pray7choice(), config.eventType7(), config.toggle7(), config.ignoreNonTargetEvents7());
		exportableConfig.setPrayer(7, config.activated8(), config.pray8Ids(), config.pray8delays(), config.pray8choice(), config.eventType8(), config.toggle8(), config.ignoreNonTargetEvents8());
		exportableConfig.setPrayer(8, config.activated9(), config.pray9Ids(), config.pray9delays(), config.pray9choice(), config.eventType9(), config.toggle9(), config.ignoreNonTargetEvents9());
		exportableConfig.setPrayer(9, config.activated10(), config.pray10Ids(), config.pray10delays(), config.pray10choice(), config.eventType10(), config.toggle10(), config.ignoreNonTargetEvents10());

		if(!PRESET_DIR.exists()) {
			PRESET_DIR.mkdirs();
		}

		File saveFile = new File(PRESET_DIR, presetNameFormatted + ".json");
		try(FileWriter fw = new FileWriter(saveFile)) {
			fw.write(gson.toJson(exportableConfig));
			fw.close();
			InteractionUtils.showNonModalMessageDialog("Successfully saved preset '" + presetNameFormatted + "' at " + saveFile.getAbsolutePath(), "Preset Save Success");
		} catch(Exception e) {
			InteractionUtils.showNonModalMessageDialog(e.getMessage(), "Save Preset Error");
			log.error(e.getMessage());
		}
	}

	private void loadPreset() {
		String presetName = config.presetName();
		String presetNameFormatted = presetName.replaceAll(FILENAME_SPECIAL_CHAR_REGEX, "").replaceAll(" ", "_").toLowerCase();

		if(presetNameFormatted.isEmpty()) {
			return;
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(PRESET_DIR + "/" + presetNameFormatted + ".json"));
			ExportableConfig loadedConfig = gson.fromJson(br, ExportableConfig.class);
			br.close();
			if(loadedConfig != null) {
				log.info("Loaded preset: " + presetNameFormatted);
			}

			for(int i = 0; i < 10; i++) {
				configManager.setConfiguration("freds-custom-prayers", "activated" + (i + 1), loadedConfig.getPrayerEnabled()[i]);
				configManager.setConfiguration("freds-custom-prayers", "pray" + (i + 1) + "Ids", loadedConfig.getPrayerIds()[i]);
				configManager.setConfiguration("freds-custom-prayers", "pray" + (i + 1) + "delays", loadedConfig.getPrayerDelays()[i]);
				configManager.setConfiguration("freds-custom-prayers", "pray" + (i + 1) + "choice", loadedConfig.getPrayChoice()[i]);
				configManager.setConfiguration("freds-custom-prayers", "eventType" + (i + 1), loadedConfig.getEventType()[i]);
				configManager.setConfiguration("freds-custom-prayers", "toggle" + (i + 1), loadedConfig.getToggle()[i]);
				configManager.setConfiguration("freds-custom-prayers", "ignoreNonTargetEvents" + (i + 1), loadedConfig.getIgnoreNonTargetEvents()[i]);
			}

			InteractionUtils.showNonModalMessageDialog("Successfully loaded preset '" + presetNameFormatted + "'", "Preset Load Success");
		} catch(Exception e) {
			InteractionUtils.showNonModalMessageDialog(e.getMessage(), "Preset Load Error");
			log.error(e.getMessage());
		}
	}
}