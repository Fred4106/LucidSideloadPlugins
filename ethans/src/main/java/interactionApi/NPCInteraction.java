package interactionApi;

import ethanApiPlugin.collections.NPCs;
import ethanApiPlugin.collections.query.NPCQuery;
import packets.MousePackets;
import packets.NPCPackets;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;

import java.awt.*;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

public class NPCInteraction {
    public static boolean interact(String name, String... actions) {
        return NPCs.search().withName(name).first().flatMap(npc ->
        {
            MousePackets.queueClickPacket(npc);
            NPCPackets.queueNPCAction(npc, actions);
            return Optional.of(true);
        }).orElse(false);
    }

    public static boolean interact(int id, String... actions) {
        return NPCs.search().withId(id).first().flatMap(npc ->
        {
            MousePackets.queueClickPacket(npc);
            NPCPackets.queueNPCAction(npc, actions);
            return Optional.of(true);
        }).orElse(false);
    }

    public static boolean interact(Predicate<? super NPC> predicate, String... actions) {
        return NPCs.search().filter(predicate).first().flatMap(npc ->
        {
            MousePackets.queueClickPacket(npc);
            NPCPackets.queueNPCAction(npc, actions);
            return Optional.of(true);
        }).orElse(false);
    }

    public static boolean interactIndex(int index, String... actions) {
        return NPCs.search().indexIs(index).first().flatMap(npc ->
        {
            MousePackets.queueClickPacket(npc);
            NPCPackets.queueNPCAction(npc, actions);
            return Optional.of(true);
        }).orElse(false);
    }

    public static boolean interact(NPC npc, String... actions) {
        if (npc == null) {
            return false;
        }
        NPCComposition comp = NPCQuery.getNPCComposition(npc);
        if (comp == null) {
            return false;
        }
        MousePackets.queueClickPacket(npc);
        NPCPackets.queueNPCAction(npc, actions);
        return true;
    }
}
