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
import java.util.List;
@Singleton
public class TileItems {
    static Client client = RuneLite.getInjector().getInstance(Client.class);
    static List<ETileItem> tileItems = new ArrayList<>();

    public static TileItemQuery search() {
        return new TileItemQuery(tileItems);
    }
}
