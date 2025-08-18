package ethanApiPlugin.collections;

import ethanApiPlugin.collections.query.TileItemQuery;
import ethanApiPlugin.collections.query.TileObjectQuery;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.client.RuneLite;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Singleton
public class TileItems {
	static Client client = RuneLite.getInjector().getInstance(Client.class);
	static List<ETileItem> tileItems = new ArrayList<>();

	public static TileItemQuery search() {
		return new TileItemQuery(tileItems);
	}


	public static void onGameTick(Client client) {
//		HashSet<TileObject> tileObjectHashSet = new HashSet<>();
		HashSet<ETileItem> tileItemsHashSet = new HashSet<>();
		try {
			for (Tile[] tiles : client.getScene().getTiles()[client.getPlane()]) {
				if (tiles == null) {
					continue;
				}
				for (Tile tile : tiles) {
					if (tile == null || tile.getGroundItems() == null) {
						continue;
					}
					for(TileItem tm : tile.getGroundItems()) {
						if (tm == null) {
							continue;
						}
//						if (tm.getId() == -1) {
//							continue;
//						}
						tileItemsHashSet.add(new ETileItem(tile.getWorldLocation(), tm));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tileItems.clear();
			tileItems.addAll(tileItemsHashSet);
		}
	}
}
