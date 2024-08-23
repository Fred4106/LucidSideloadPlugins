package com.fredplugins.giantsfoundry;

import com.fredplugins.giantsfoundry.enums.SMould;
import com.fredplugins.giantsfoundry.enums.SMould$;
import com.google.inject.Provides;

import static com.fredplugins.giantsfoundry.FredsGiantsFoundryClientIDs.VARBIT_GAME_STAGE;
import static com.fredplugins.giantsfoundry.FredsGiantsFoundryClientIDs.VARBIT_HEAT;
import com.fredplugins.giantsfoundry.enums.Stage;

import javax.inject.Inject;
import javax.inject.Singleton;

import ethanApiPlugin.EthanApiPlugin;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Skill;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;

@Slf4j
@PluginDescriptor(
	name = "<html><font color=\"#32C8CD\">Freds</font> Giant's Foundry</html>",
	description = "Helpful overlays for the Giant's Foundry minigame",
	tags = {"smithing","giant","foundry","giantsfoundry","minigame", "fred4106"},
	conflicts = {"Easy Giant's Foundry"}
)
@Singleton
@PluginDependency(EthanApiPlugin.class)
public class FredsGiantsFoundryPlugin extends Plugin
{
	public Logger getLog() {
		return log;
	}

	private static final int TRIP_HAMMER = 44619;
	private static final int GRINDSTONE = 44620;
	private static final int POLISHING_WHEEL = 44621;

	private static final int LAVA_POOL = 44631;
	private static final int WATERFALL = 44632;

	private static final int CRUCIBLE = 44776;
	private static final int MOULD_JIG = 44777;

	private static final int KOVAC_NPC = 11472;

	private static final int PREFORM = 27010;

	private static final int REPUTATION_VARBIT = 3436;

	private Stage oldStage;

	private int lastBoost;

	private boolean bonusNotified = false;

	@Getter
	private int reputation;

	@Inject
	private FredsGiantsFoundryState state;

	@Inject
	private FredsGiantsFoundryHelper helper;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private FoundryOverlay2D overlay2d;

	@Inject
	private FoundryOverlay3D overlay3d;

	@Inject
	private MouldHelper mouldHelper;

	@Inject
	private FredsGiantsFoundryConfig config;

	@Inject
	private Notifier notifier;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private EthanApiPlugin ethanApiPlugin;

	@Inject
	private EventBus eventBus;

	private FoundryAutomation foundryAutomation;

	@Override
	protected void startUp()
	{
		if(foundryAutomation == null) {
			foundryAutomation = new FoundryAutomation(this, state, ethanApiPlugin);
		}

		overlayManager.add(overlay2d);
		overlayManager.add(overlay3d);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			reputation = client.getVarpValue(REPUTATION_VARBIT);
		}
		eventBus.register(foundryAutomation);
	}

	@Override
	protected void shutDown()
	{
		eventBus.unregister(foundryAutomation);
		foundryAutomation = null;
		overlayManager.remove(overlay2d);
		overlayManager.remove(overlay3d);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();
		switch (gameObject.getId())
		{
			case POLISHING_WHEEL:
				state.setEnabled(true);
				overlay3d.polishingWheel = gameObject;
				break;
			case GRINDSTONE:
				overlay3d.grindstone = gameObject;
				break;
			case LAVA_POOL:
				overlay3d.lavaPool = gameObject;
				break;
			case WATERFALL:
				overlay3d.waterfall = gameObject;
				break;
			case TRIP_HAMMER:
				overlay3d.tripHammer = gameObject;
				break;
			case MOULD_JIG:
				overlay3d.mouldJig = gameObject;
				break;
			case CRUCIBLE:
				overlay3d.crucible = gameObject;
				break;
		}
	}


	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState().equals(GameState.LOADING))
		{
			state.setEnabled(false);
		}

		if (event.getGameState().equals(GameState.LOGGED_IN))
		{
			reputation = client.getVarpValue(REPUTATION_VARBIT);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		final int curBoost = statChanged.getBoostedLevel();
		// if the difference between current and last boost is != 0 then a stat boost (or drop) change occurred
		if (!statChanged.getSkill().equals(Skill.SMITHING) ||
			curBoost != lastBoost ||
			!state.isEnabled() ||
			state.getCurrentStage() == null)
		{
			lastBoost = curBoost;
			return;
		}

		if (config.showGiantsFoundryStageNotifications() &&
			state.getActionsLeftInStage() == config.StageNotificationsThreshold() &&
			(oldStage == null || oldStage != state.getCurrentStage()))
		{
			notifier.notify("About to finish the current stage!");
			oldStage = state.getCurrentStage();
		}
		else if (config.showGiantsFoundryHeatNotifications() &&
			state.getActionsForHeatLevel() == config.HeatNotificationsThreshold())
		{
			notifier.notify("About to run out of heat!");
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject gameObject = event.getGameObject();
		switch (gameObject.getId())
		{
			case POLISHING_WHEEL:
				state.setEnabled(false);
				overlay3d.polishingWheel = null;
				break;
			case GRINDSTONE:
				overlay3d.grindstone = null;
				break;
			case LAVA_POOL:
				overlay3d.lavaPool = null;
				break;
			case WATERFALL:
				overlay3d.waterfall = null;
				break;
			case TRIP_HAMMER:
				overlay3d.tripHammer = null;
				break;
			case MOULD_JIG:
				overlay3d.mouldJig = null;
				break;
			case CRUCIBLE:
				overlay3d.crucible = null;
				break;
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		if (event.getNpc().getId() == KOVAC_NPC)
		{
			overlay3d.kovac = event.getNpc();
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (event.getNpc().getId() == KOVAC_NPC)
		{
			overlay3d.kovac = null;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.EQUIPMENT.getId()
			&& event.getItemContainer().count(PREFORM) == 0)
		{
			state.reset();
			oldStage = null;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!state.isEnabled()) return;

		if (event.getMenuTarget().contains("Crucible "))
		{
			if (event.getMenuOption().equals("Pour"))
			{
				// add persistent game message of the alloy value so user can reference later.
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "The quality of the alloy poured is " + (int) state.getCrucibleQuality(), null);
			}
		}

		// Could not find a varbit to capture, so capture the menu-option directly.
		// start the HeatActionStateMachine when varbit begins to update in onVarbitChanged()
		if (event.getMenuOption().startsWith("Heat-preform"))
		{
			state.heatingCoolingState.stop();
			state.heatingCoolingState.setup(7, 0, "heats");
		}
		else if (event.getMenuOption().startsWith("Dunk-preform"))
		{
			state.heatingCoolingState.stop();
			state.heatingCoolingState.setup(27, 2, "dunks");
		}
		else if (event.getMenuOption().startsWith("Cool-preform"))
		{
			state.heatingCoolingState.stop();
			state.heatingCoolingState.setup(-7, 0, "cools");
		}
		else if (event.getMenuOption().startsWith("Quench-preform"))
		{
			state.heatingCoolingState.stop();
			state.heatingCoolingState.setup(-27, -2, "quenches");
		}
		else // canceled heating/cooling, stop the heating state-machine
		{
			state.heatingCoolingState.stop();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() == MouldHelper.DRAW_MOULD_LIST_SCRIPT
			|| event.getScriptId() == MouldHelper.REDRAW_MOULD_LIST_SCRIPT
			|| event.getScriptId() == MouldHelper.SELECT_MOULD_SCRIPT
			|| event.getScriptId() == MouldHelper.RESET_MOULD_SCRIPT)
		{
			mouldHelper.selectBest(event.getScriptId());
			SMould$.MODULE$.debug(log, client);
		}
	}

	// previous heat varbit value, used to filter out passive heat decay.
	private int previousHeat = 0;

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() == REPUTATION_VARBIT)
		{
			reputation = client.getVarpValue(REPUTATION_VARBIT);
		}

		// start the heating state-machine when the varbit updates
		// if heat varbit updated and the user clicked, start the state-machine
		if (event.getVarbitId() == VARBIT_HEAT && state.heatingCoolingState.getActionName() != null)
		{
			// ignore passive heat decay, one heat per two ticks
			if (event.getValue() - previousHeat != -1)
			{
				// if the state-machine is idle, start it
				if (state.heatingCoolingState.isIdle())
				{
					state.heatingCoolingState.start(state, config, state.getHeatAmount());
				}

				state.heatingCoolingState.onTick();
			}
			previousHeat = event.getValue();
		}
	}

	@Subscribe
	protected void onConfigChanged(ConfigChanged configChanged)
	{
		if (!FredsGiantsFoundryConfig.GROUP.equals(configChanged.getGroup()))
		{
			return;
		}

		if (FredsGiantsFoundryConfig.SOUND_ID.equals(configChanged.getKey()))
		{
			clientThread.invoke(() -> client.playSoundEffect(config.soundId()));
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getVarbitValue(VARBIT_GAME_STAGE) == 1)
		{
			SMould sm = SMould$.MODULE$.getSelected(client);
			log.info("SMould={}[{}]", sm, sm.values());
		}
		checkBonus();
	}

	private void checkBonus()
	{
		if (!state.isEnabled() || state.getCurrentStage() == null
			|| state.getCurrentStage().getHeat() != state.getCurrentHeat()
			|| !BonusWidget.isActive(client))
		{
			bonusNotified = false;
			return;
		}

		if (bonusNotified)
		{
			return;
		}

		if (config.bonusNotification())
		{
			notifier.notify("Bonus - Click tool");
		}
		if (config.bonusSoundNotify())
		{
			client.playSoundEffect(config.soundId());
		}

		bonusNotified = true;
	}

	@Provides
	FredsGiantsFoundryConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FredsGiantsFoundryConfig.class);
	}
}
