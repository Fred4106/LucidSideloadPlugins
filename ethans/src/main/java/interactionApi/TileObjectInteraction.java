package interactionApi;

import ethanApiPlugin.collections.TileObjects;
import ethanApiPlugin.collections.query.TileObjectQuery;
import packets.MousePackets;
import packets.ObjectPackets;
import net.runelite.api.ObjectComposition;
import net.runelite.api.TileObject;

import java.util.Optional;

public class TileObjectInteraction {


    public static boolean interact(String name, String... actions) {
        return TileObjects.search().withName(name).first().flatMap(tileObject ->
        {
            MousePackets.queueClickPacket(tileObject);
            ObjectPackets.queueObjectAction(tileObject, false, actions);
            return Optional.of(true);
        }).orElse(false);
    }

    public static boolean interact(int id, String... actions) {
        return TileObjects.search().withId(id).first().flatMap(tileObject ->
        {
            MousePackets.queueClickPacket(tileObject);
            ObjectPackets.queueObjectAction(tileObject, false, actions);
            return Optional.of(true);
        }).orElse(false);
    }

    public static boolean interact(TileObject tileObject, String... actions) {
        if (tileObject == null) {
            return false;
        }
        ObjectComposition comp = TileObjectQuery.getObjectComposition(tileObject);
        if (comp == null) {
            return false;
        }
        MousePackets.queueClickPacket(tileObject);
        ObjectPackets.queueObjectAction(tileObject, false, actions);
        return true;
    }
}
