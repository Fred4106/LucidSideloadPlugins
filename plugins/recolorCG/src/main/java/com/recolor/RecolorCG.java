package com.recolor;

import javax.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import java.awt.*;
import java.util.*;
import java.util.List;

@Slf4j
@PluginDescriptor(
		name = "<html><font color=\"#32C8CD\">Freds</font> CG recolor</html>",
		description = "Escape the red prison today!",
		conflicts = "117 HD"	// might be adressed in a future update of this plugin, but currently not compatible
)
public class RecolorCG extends Plugin
{

	// Declarations
	private static final List<Integer> OBJECT_IDS = Arrays.asList(35965, 35966, 35967, 35968, 35968, 35969, 35970, 35971, 35972, 35972, 35973, 35974, 35975, 35976, 35977, 35978, 35979, 35980, 35992, 35994, 35994, 35995, 35996, 35997, 35998, 35999, 36000, 36001, 36002, 36003, 36004, 36005, 36006, 36007, 36008, 37337);
	private static final List<Integer> GROUND_IDS = Arrays.asList(36046, 36047, 36048, 36052, 36053, 36054, 36055, 36056, 36057, 36058, 36059);
	private static final List<Integer> NPC_IDS = Arrays.asList(9035, 9036, 9037, 9038, 9039, 9040, 9041, 9042, 9043, 9044, 9045, 9046, 9047, 9048);
	private static final List<Integer> PROJECTILE_IDS = Arrays.asList(1702, 1708, 1712, 1714, 1723);
	private static final int REGION_ID_GAUNTLET_LOBBY = 12127;
	private static final int REGION_ID_GAUNTLET_CORRUPTED = 7768;
	//private static final int REGION_ID_GAUNTLET_NORMAL = 7512;

	// Will likely use the ITEM_IDS in a future update of this plugin
	//private static final List<Integer> ITEM_IDS = Arrays.asList(23824, 23834, 23821, 23822, 23823, 23820, 23835, 23837, 23838, 23585, 23582, 23583, 23584, 23849, 23850, 23851, 23855, 23856, 23857);


	ModelDataProcessor dataProcessor;
	Random rand;
	int regionId;
	Color randomColor1;
	Color randomColor2;


	@Inject
	private ClientThread clientThread;

	@Inject
	private Client client;

	@Inject
	private RecolorCGConfig config;

	@Provides
	RecolorCGConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RecolorCGConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Recolor started!");
		rand = new Random();
		randomColor1 = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
		randomColor2 = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));

		// Vanilla model facecolors are stored in a .txt -> the new model colors can be calculated before the models even appear making the spawnEvents less expensive
		if(config.secondcolor_active() && config.random())
		{
			this.dataProcessor = new ModelDataProcessor("/model_facecolors.txt", randomColor1, randomColor2, config.harmonize());
		}
		else if (config.secondcolor_active() && !config.random())
		{
			this.dataProcessor = new ModelDataProcessor("/model_facecolors.txt", config.color(), config.secondcolor(), config.harmonize());
		}
		else if (!config.secondcolor_active() && config.random())
		{
			this.dataProcessor = new ModelDataProcessor("/model_facecolors.txt", randomColor1, randomColor1, config.harmonize());
		}
		else
		{
			this.dataProcessor = new ModelDataProcessor("/model_facecolors.txt", config.color(), config.color(), config.harmonize());
		}

		// If the user is already logged in AND inside the gauntlet, Hun still needs to be recolored
		// Hun gets recolored on GamestateChanges
		if(client.getGameState() == GameState.LOGGED_IN)
		{
			regionId = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
			if(regionId == REGION_ID_GAUNTLET_CORRUPTED)
			{
				clientThread.invoke(()->
				{
					client.setGameState(GameState.LOADING);
				});
			}
		}
	}

	@Override
	protected void shutDown()
	{
		clientThread.invoke(() ->
		{
			rand = null;

			if(client.getGameState() == GameState.LOGGED_IN)
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Some game models may still be affected by the plugin. Please re-log to ensure that everything is properly reset.", null);
				clearAll();
				client.setGameState(GameState.LOADING);
			}

			synchronized (dataProcessor)
			{
				dataProcessor.cleanUp();
				dataProcessor = null;
			}
		});
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if(event.getGroup().equals("recolorCG"))
		{
			if(event.getKey().equals("random"))
			{
				randomColor1 = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
				randomColor2 = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
			}
			if (!config.random())
			{
				if (config.secondcolor_active())
				{
					synchronized (dataProcessor)
					{
						dataProcessor.recolorData(config.color(), config.secondcolor(), config.harmonize());
					}
				}
				else
				{
					synchronized (dataProcessor)
					{
						dataProcessor.recolorData(config.color(), config.color(), config.harmonize());
					}
				}
			}
			else
			{
				if(config.secondcolor_active())
				{
					synchronized (dataProcessor)
					{
						dataProcessor.recolorData(randomColor1, randomColor2, config.harmonize());
					}
				}
				else
				{
					synchronized (dataProcessor)
					{
						dataProcessor.recolorData(randomColor1, randomColor1, config.harmonize());
					}
				}
			}
			clientThread.invoke(() ->
			{
				recolorNPCs(true);
				client.setGameState(GameState.LOADING);
			});
		}

	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (OBJECT_IDS.contains(event.getGameObject().getId()))
		{
			recolorGameObject(event.getGameObject(), false, true);
		}
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		if (GROUND_IDS.contains(event.getGroundObject().getId()))
		{
			recolorGroundObject(event.getGroundObject(), false);
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		if (NPC_IDS.contains(event.getNpc().getId()))
		{
			recolorNPC(event.getNpc(), true);
		}
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		if (NPC_IDS.contains(event.getNpc().getId()))
		{
			recolorNPC(event.getNpc(), true);
		}
	}


	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		if(PROJECTILE_IDS.contains(event.getProjectile().getId()))
		{
			recolorProjectile(event.getProjectile());
		}
	}


	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if(event.getGameState() == GameState.LOGGED_IN)
		{
			regionId = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
			if(regionId == REGION_ID_GAUNTLET_LOBBY)
			{
				if (config.random())
				{
					randomColor1 = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
					randomColor2 = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
					if(config.secondcolor_active())
					{
						synchronized (dataProcessor)
						{
							dataProcessor.recolorData(randomColor1, randomColor2, config.harmonize());
						}
					}
					else
					{
						synchronized (dataProcessor)
						{
							dataProcessor.recolorData(randomColor1, randomColor1, config.harmonize());
						}
					}
				}
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if(event.getVarbitId() == 9177 && event.getValue() == 1)	// varbit 9177 is 1 if player is in the boss room
		{
			clientThread.invoke(() ->
			{
				client.setGameState(GameState.LOADING);
			});
		}
	}


	@Subscribe
	public void onPreMapLoad(PreMapLoad event)
	{
		regionId = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
		if(regionId == REGION_ID_GAUNTLET_CORRUPTED || regionId == REGION_ID_GAUNTLET_LOBBY) {
			Scene scene = event.getScene();
			Tile[][][] sceneTiles = scene.getTiles();
			for (Tile[][] tiles : sceneTiles) {
				for (Tile[] tiles1 : tiles) {
					for (Tile tile : tiles1) {
						if (tile == null)
							continue;
						GameObject[] gameObjects = tile.getGameObjects();
						GroundObject groundObject = tile.getGroundObject();
						for (GameObject object : gameObjects) {
							if (object == null) {
								continue;
							}
							int ID = object.getId();
							if (OBJECT_IDS.contains(ID)) {
								recolorGameObject(object, true, true);
							}
						}
						if (groundObject != null) {
							int ID = groundObject.getId();
							if (GROUND_IDS.contains(ID)) {
								recolorGroundObject(groundObject, true);
							}
						}
					}
				}
			}
		}
	}

	public void recolorGameObject(GameObject gameObject, Boolean preMapLoad, Boolean useRecolored)
	{

		Renderable renderable = gameObject.getRenderable();
		Model model;

		if(preMapLoad)
		{
			model = verifyPreMapLoadModel(renderable);
		}
		else
		{
			model = verifyModel(renderable);
		}

		if (model == null)
		{
			if(!preMapLoad)
			{
				log.debug("recolorGameObject returned null!" + " , PreMapLoad: " + preMapLoad);
			}
			return;
		}

		synchronized (dataProcessor)
		{
			dataProcessor.applyColors(gameObject.getId(), "GameObject", model, useRecolored);
		}
	}

	public void recolorGroundObject(GroundObject groundObject, Boolean preMapLoad)
	{

		Renderable renderable = groundObject.getRenderable();
		Model model;

		if(preMapLoad)
		{
			model = verifyPreMapLoadModel(renderable);
		}
		else
		{
			model = verifyModel(renderable);
		}

		if (model == null)
		{
			log.debug("recolorGroundObject returned null!");
			return;
		}
		if(groundObject.getId() == 36047 || groundObject.getId() == 36048)
		{	//Damaging ground recolor depends on config
			if(!config.groundRecolor())
			{
				dataProcessor.applyColors(groundObject.getId(), "GroundObject", model, false);
				return;
			}
			else
			{
				dataProcessor.applyColors(groundObject.getId(), "GroundObject", model, true);
				return;
			}
		}

		synchronized (dataProcessor)
		{
			dataProcessor.applyColors(groundObject.getId(), "GroundObject", model, true);
		}

	}

	public void recolorNPC(NPC npc, Boolean useRecolored)
	{
		if(!useRecolored)
		{
			if (npc.getModel() == null)
			{
				log.debug("recolorAll returned null! - NPC");
				return;
			}
			synchronized (dataProcessor)
			{
				dataProcessor.applyColors(npc.getId(), "NPC", npc.getModel(), false);
			}
			return;
		}
		// Spotanim needs to be set if we mage the npc that has to be recolored
		if (client.getLocalPlayer().getInteracting() != null)
		{
			if(client.getLocalPlayer().getInteracting().hasSpotAnim(1724) || client.getLocalPlayer().getInteracting().hasSpotAnim(85))
			{
				client.getLocalPlayer().getInteracting().createSpotAnim(0,-1,0,0);
			}
		}

		if (config.tornado())
		{
			if (config.npcRecolor())    // tornados AND npcs are to be recolored - we can use the same call for both
			{
				if (npc.getModel() == null)
				{
					log.debug("recolorAll returned null! - NPC");
					return;
				}
				synchronized (dataProcessor)
				{
					dataProcessor.applyColors(npc.getId(), "NPC", npc.getModel(), true);
				}
			}
			else	// tornados are to be recolored, npcs not - we need to differentiate
			{
				if(npc.getModel() == null)
				{
					log.debug("recolorAll returned null! - NPC");
					return;
				}
				synchronized (dataProcessor)
				{
					if(npc.getId() == 9039)
					{
						dataProcessor.applyColors(npc.getId(), "NPC", npc.getModel(), true);
					}
					else
					{
						dataProcessor.applyColors(npc.getId(), "NPC", npc.getModel(), false);
					}
				}
			}
		}
		else	// if-case inverted
		{
			if(config.npcRecolor())	// tornados are NOT to be recolored, npcs are - we need to differentiate
			{
				if(npc.getModel() == null)
				{
					log.debug("recolorAll returned null! - NPC");
					return;
				}
				synchronized (dataProcessor)
				{
					if(npc.getId() == 9039)
					{
						dataProcessor.applyColors(npc.getId(), "NPC", npc.getModel(), false);
					}
					else
					{
						dataProcessor.applyColors(npc.getId(), "NPC", npc.getModel(), true);
					}
				}
			}
			else	//tornados AND npcs are NOT to be recolored - we can use the same call for both
			{
				if(npc.getModel() == null)
				{
					log.debug("recolorAll returned null! - NPC");
					return;
				}
				synchronized (dataProcessor)
				{
					dataProcessor.applyColors(npc.getId(), "NPC", npc.getModel(), false);
				}
			}
		}
	}

	public void recolorProjectile(Projectile projectile)
	{
		Model model = projectile.getModel();
		if (model == null)
		{
			log.debug("recolorProjectile returned null!");
			return;
		}

		if(config.projectileRecolor())
		{
			synchronized (dataProcessor)
			{
				dataProcessor.applyColors(projectile.getId(), "Projectile", model, true);
			}
		}
		else
		{
			synchronized (dataProcessor)
			{
				dataProcessor.applyColors(projectile.getId(), "Projectile", model, false);
			}
		}
	}

	public void recolorNPCs(Boolean useRecolor)
	{
		IndexedObjectSet<? extends NPC> npcs = client.getWorldView(-1).npcs();
		for (NPC npc:npcs) {
			recolorNPC(npc, useRecolor);
		}
	}

	// Model.getModel() returns null, therefore we need to do an instanceof check
	private Model verifyModel(Renderable renderable)
	{
		if (renderable instanceof Model)
		{
			return (Model) renderable;
		}
		else
		{
			try
			{
				Model model = renderable.getModel();
				if (model == null)
				{
					log.debug("verifyModel returned null!");
					return null;
				}
				return model;
			}
			catch (NullPointerException e)
			{
				return null;
			}
		}
	}

	private Model verifyPreMapLoadModel(Renderable renderable)
	{
		if (renderable instanceof Model)
		{
			return (Model) renderable;
		}
		return null;
	}


	public void clearAll()
	{
		Scene scene = client.getTopLevelWorldView().getScene();
		Tile[][][] sceneTiles = scene.getTiles();
		recolorNPCs(false);
		for (Tile[][] tiles : sceneTiles)
		{
			for (Tile[] tiles1 : tiles)
			{
				for (Tile tile : tiles1)
				{
					if (tile == null)
						continue;
					GameObject[] gameObjects = tile.getGameObjects();
					for (GameObject object : gameObjects) {
						if(object == null)
						{
							continue;
						}
						int ID = object.getId();
						if (OBJECT_IDS.contains(ID)) {
							recolorGameObject(object, false, false);
						}
					}
				}
			}
		}
	}
}