package ethanApiPlugin.collections;

import ethanApiPlugin.collections.query.PlayerQuery;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Players {
	//    static Client client = RuneLite.getInjector().getInstance(Client.class);
	static List<Player> players = new ArrayList<Player>();

	public static PlayerQuery search() {
		return new PlayerQuery(players);
	}

	public static void onGameTick(Client client) {
		players.clear();
		client.getTopLevelWorldView().players().forEach(players::add);//.stream().filter(Objects::nonNull).collect(Collectors.toList());
	}
}
