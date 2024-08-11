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

    public static TileItemQuery search() {
        List<ETileItem> tileItems = new ArrayList<>();
        for (Tile[] tiles : client.getTopLevelWorldView().getScene().getTiles()[client.getTopLevelWorldView().getPlane()]) {
            if (tiles == null) {
                continue;
            }
            for (Tile tile : tiles) {
                if (tile == null) {
                    continue;
                }
                if (tile.getGroundItems() != null) {
                    for (TileItem groundItem : tile.getGroundItems()) {
                        if (groundItem == null) {
                            continue;
                        }
                        tileItems.add(new ETileItem(tile.getWorldLocation(), groundItem));
                    }
                }
            }
        }
        return new TileItemQuery(tileItems);
    }
}
