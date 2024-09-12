
package com.fredplugins.mta.graveyard;

import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import static net.runelite.api.ItemID.ANIMALS_BONES;
import static net.runelite.api.ItemID.ANIMALS_BONES_6905;
import static net.runelite.api.ItemID.ANIMALS_BONES_6906;
import static net.runelite.api.ItemID.ANIMALS_BONES_6907;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import com.fredplugins.mta.MTAConfig;
import com.fredplugins.mta.FredsMTAPlugin;
import com.fredplugins.mta.MTARoom;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

public class GraveyardRoom extends MTARoom
{
	private static final int MTA_GRAVEYARD_REGION = 13462;

	static final int MIN_SCORE = 16;

	private final Client client;
	private final FredsMTAPlugin plugin;
	private final ItemManager itemManager;
	private final InfoBoxManager infoBoxManager;
	private int score;

	private GraveyardCounter counter;

	@Inject
	private GraveyardRoom(MTAConfig config, Client client, FredsMTAPlugin plugin,
						  ItemManager itemManager, InfoBoxManager infoBoxManager)
	{
		super(config);
		this.client = client;
		this.plugin = plugin;
		this.itemManager = itemManager;
		this.infoBoxManager = infoBoxManager;
	}

	@Override
	public boolean inside()
	{
		Player player = client.getLocalPlayer();
		return player != null && player.getWorldLocation().getRegionID() == MTA_GRAVEYARD_REGION
			&& player.getWorldLocation().getPlane() == 1;
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (!inside() || !config.graveyard())
		{
			if (this.counter != null)
			{
				infoBoxManager.removeIf(e -> e instanceof GraveyardCounter);
				this.counter = null;
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!inside())
		{
			return;
		}

		ItemContainer container = event.getItemContainer();

		if (container == client.getItemContainer(InventoryID.INVENTORY))
		{
			this.score = score(container.getItems());

			if (counter == null)
			{
				BufferedImage image = itemManager.getImage(ANIMALS_BONES);
				counter = new GraveyardCounter(image, plugin);
				infoBoxManager.addInfoBox(counter);
			}
			counter.setCount(score);
		}
	}

	private int score(Item[] items)
	{
		int score = 0;

		if (items == null)
		{
			return score;
		}

		for (Item item : items)
		{
			score += getPoints(item.getId());
		}

		return score;
	}

	private int getPoints(int id)
	{
		switch (id)
		{
			case ANIMALS_BONES:
				return 1;
			case ANIMALS_BONES_6905:
				return 2;
			case ANIMALS_BONES_6906:
				return 3;
			case ANIMALS_BONES_6907:
				return 4;
			default:
				return 0;
		}
	}
}