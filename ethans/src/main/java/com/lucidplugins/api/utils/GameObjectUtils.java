package com.lucidplugins.api.utils;


//import ethanApiPlugin.collections.TileObjects;
//import interactionApi.TileObjectInteraction;
import ch.qos.logback.classic.Level;
import ethanApiPlugin.EthanApiPlugin;
import ethanApiPlugin.collections.query.TileObjectQuery;
import net.runelite.api.*;
import net.runelite.client.RuneLite;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GameObjectUtils
{
	private final static Logger log;

	static {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(GameObjectUtils.class)).setLevel(Level.DEBUG);
		log = LoggerFactory.getLogger(GameObjectUtils.class);
	}

    static Client client = RuneLite.getInjector().getInstance(Client.class);

    public static TileObject getFirstTileObjectAt(Tile tile, int... ids)
    {
        return Arrays.stream(tile.getGameObjects()).filter(gameObject -> gameObject != null && ArrayUtils.contains(ids, gameObject.getId())).findFirst().orElse(null);
    }

    public static TileObjectQuery search() {
        List<TileObject> tileObjects = new ArrayList<>();
//        TileItems.tileItems.clear();
		try
		{
        for (Tile[] tiles : client.getTopLevelWorldView().getScene().getTiles()[client.getTopLevelWorldView().getPlane()]) {
            if (tiles == null) {
                continue;
            }
            for (Tile tile : tiles) {
                if (tile == null) {
                    continue;
                }
//                if (tile.getGroundItems() != null) {
//                    for (TileItem groundItem : tile.getGroundItems()) {
//                        if (groundItem == null) {
//                            continue;
//                        }
//                        TileItems.tileItems.add(new ETileItem(tile.getWorldLocation(), groundItem));
//                    }
//                }
                for (GameObject gameObject : tile.getGameObjects()) {
                    if (gameObject == null) {
                        continue;
                    }
                    if (gameObject.getId() == -1) {
                        continue;
                    }
                    tileObjects.add(gameObject);
                }
                if (tile.getGroundObject() != null) {
                    if (tile.getGroundObject().getId() == -1) {
                        continue;
                    }
                    tileObjects.add(tile.getGroundObject());
                }
                if (tile.getWallObject() != null) {
                    if (tile.getWallObject().getId() == -1) {
                        continue;
                    }
                    tileObjects.add(tile.getWallObject());
                }
                if (tile.getDecorativeObject() != null) {
                    if (tile.getDecorativeObject().getId() == -1) {
                        continue;
                    }
                    tileObjects.add(tile.getDecorativeObject());
                }
            }
        }
	} catch (Exception e) {
			log.error("Failed to run game object query", e);
		}
        return new TileObjectQuery(tileObjects);
    }
//
//    public static void interact(GameObject object, String action)
//    {
//        TileObjectInteraction.interact(object, action);
//    }
//
//    public static void interact(TileObject object, String action)
//    {
//        TileObjectInteraction.interact(object, action);
//    }
//
//    public static void interact(WallObject object, String action)
//    {
//        TileObjectInteraction.interact(object, action);
//    }

    public static boolean hasAction(int objectId, String action)
    {
        ObjectComposition composition = client.getObjectDefinition(objectId);
        if (composition == null)
        {
            return false;
        }

        if (composition.getActions() == null)
        {
            return false;
        }

        return Arrays.stream(composition.getActions()).anyMatch(s -> s != null && s.equals(action));
    }

    public static TileObject nearest(String name)
    {
        return search().nameContains(name).nearestToPlayer().orElse(null);
    }

    public static TileObject nearest(int id)
    {
        return search().withId(id).nearestToPlayer().orElse(null);
    }

    public static TileObject nearest(Predicate<TileObject> filter)
    {
        return search().filter(filter).nearestToPlayer().orElse(null);
    }

    public static List<TileObject> getAll(Predicate<TileObject> filter)
    {
        return search().filter(filter).result();
    }
}
