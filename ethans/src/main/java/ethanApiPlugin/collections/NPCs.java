package ethanApiPlugin.collections;

import ethanApiPlugin.collections.query.NPCQuery;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class NPCs {
//    static Client client = RuneLite.getInjector().getInstance(Client.class);
    private static final List<NPC> npcList = new ArrayList<>();

    public static NPCQuery search() {
        return new NPCQuery(npcList);
    }
    
    public static void onGameTick(Client client) {
        npcList.clear();
        client.getTopLevelWorldView().npcs().forEach(npcList::add);
    }
}
