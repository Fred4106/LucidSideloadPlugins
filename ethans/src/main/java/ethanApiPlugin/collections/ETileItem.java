package ethanApiPlugin.collections;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.RuneLite;
import packets.MousePackets;
import packets.TileItemPackets;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;

import java.awt.*;
import java.util.Optional;

public class ETileItem {
	static Client client = RuneLite.getInjector().getInstance(Client.class);
	public WorldPoint location;
	public TileItem tileItem;

	public ETileItem(WorldPoint worldLocation, TileItem tileItem) {
		this.location = worldLocation;
		this.tileItem = tileItem;
	}

	public WorldPoint getLocation() {
		return location;
	}

	public TileItem getTileItem() {
		return tileItem;
	}

	public void interact(boolean ctrlDown) {
		Polygon hull = Optional.ofNullable(LocalPoint.fromWorld(client, location)).map(lp -> Perspective.getCanvasTilePoly(client, lp)).orElse(null);
		MousePackets.queueClickPacket(hull);
		TileItemPackets.queueTileItemAction(this, ctrlDown);
	}
}
