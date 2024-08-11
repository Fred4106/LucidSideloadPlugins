
package com.fredplugins.mta.enchantment;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.eventbus.Subscribe;
import com.fredplugins.mta.MTAConfig;
import com.fredplugins.mta.MTARoom;

@Slf4j
public class EnchantmentRoom extends MTARoom
{
	private static final int MTA_ENCHANT_REGION = 13462;

	private final Client client;
	private final List<WorldPoint> dragonstones = new ArrayList<>();
	private boolean hintSet;

	@Inject
	private EnchantmentRoom(MTAConfig config, Client client)
	{
		super(config);
		this.client = client;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING)
		{
			dragonstones.clear();
			if (hintSet)
			{
				client.clearHintArrow();
				hintSet = false;
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!inside() || !config.enchantment())
		{
			return;
		}

		WorldPoint nearest = findNearestStone();
		if (nearest != null)
		{
			client.setHintArrow(nearest);
			hintSet = true;
		}
		else
		{
			client.clearHintArrow();
			hintSet = false;
		}
	}

	private WorldPoint findNearestStone()
	{
		WorldPoint nearest = null;
		double dist = Double.MAX_VALUE;
		WorldPoint local = client.getLocalPlayer().getWorldLocation();
		for (WorldPoint worldPoint : dragonstones)
		{
			double currDist = local.distanceTo(worldPoint);
			if (nearest == null || currDist < dist)
			{
				dist = currDist;
				nearest = worldPoint;
			}
		}
		return nearest;
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		final TileItem item = itemSpawned.getItem();
		final Tile tile = itemSpawned.getTile();

		if (item.getId() == ItemID.DRAGONSTONE_6903)
		{
			WorldPoint location = tile.getWorldLocation();
			log.debug("Adding dragonstone at {}", location);
			dragonstones.add(location);
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		final TileItem item = itemDespawned.getItem();
		final Tile tile = itemDespawned.getTile();

		if (item.getId() == ItemID.DRAGONSTONE_6903)
		{
			WorldPoint location = tile.getWorldLocation();
			log.debug("Removed dragonstone at {}", location);
			dragonstones.remove(location);
		}
	}

	@Override
	public boolean inside()
	{
		Player player = client.getLocalPlayer();
		return player != null && player.getWorldLocation().getRegionID() == MTA_ENCHANT_REGION
			&& player.getWorldLocation().getPlane() == 0;
	}
}
