package ethanApiPlugin.collections;

import ethanApiPlugin.collections.query.TileItemQuery;
import ethanApiPlugin.collections.query.TileObjectQuery;
import net.runelite.api.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TileObjects {
	//    static Client client = RuneLite.getInjector().getInstance(Client.class);
	static List<TileObject> tileObjects = new ArrayList<>();
//	static List<TileItem> tileItems = new ArrayList<>();

	public static TileObjectQuery search() {
		return new TileObjectQuery(tileObjects);
	}
//	public static TileItemQuery searchItems() {
//		return new TileItemQuery(
//	}

	public static void onGameTick(Client client) {
		HashSet<TileObject> tileObjectHashSet = new HashSet<>();
//		HashSet<ETileItem> tileItemsHashSet = new HashSet<>();
		try {
			for (Tile[] tiles : client.getScene().getTiles()[client.getPlane()]) {
				if (tiles == null) {
					continue;
				}
				for (Tile tile : tiles) {
					if (tile == null) {
						continue;
					}
//					if (tile.getGroundItems() != null) {
//						for (TileItem groundItem : tile.getGroundItems()) {
//							if (groundItem == null) {
//								continue;
//							}
//							TileItems.tileItems.add(new ETileItem(tile.getWorldLocation(), groundItem));
//						}
//					}
					for (GameObject gameObject : tile.getGameObjects()) {
						if (gameObject == null) {
							continue;
						}
						if (gameObject.getId() == -1) {
							continue;
						}
						tileObjectHashSet.add(gameObject);
					}
					if (tile.getGroundObject() != null) {
						if (tile.getGroundObject().getId() == -1) {
							continue;
						}
						tileObjectHashSet.add(tile.getGroundObject());
					}
					if (tile.getWallObject() != null) {
						if (tile.getWallObject().getId() == -1) {
							continue;
						}
						tileObjectHashSet.add(tile.getWallObject());
					}
					if (tile.getDecorativeObject() != null) {
						if (tile.getDecorativeObject().getId() == -1) {
							continue;
						}
						tileObjectHashSet.add(tile.getDecorativeObject());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tileObjects.clear();
			tileObjects.addAll(tileObjectHashSet);
		}
	}
}