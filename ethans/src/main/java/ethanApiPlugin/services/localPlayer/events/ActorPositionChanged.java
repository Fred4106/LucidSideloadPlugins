package ethanApiPlugin.services.localPlayer.events;

import lombok.Value;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

import java.util.Optional;

@Value
public class ActorPositionChanged
{
	Actor actor;
	WorldPoint from;
	WorldPoint to;

	public boolean isNpc() {
		return actor instanceof NPC;
	}
	public boolean isPlayer() {
		return actor instanceof Player;
	}

	public Optional<NPC> getNpc() {
		if(isNpc()) {
			return Optional.of((NPC)actor);
		}
		return Optional.empty();
	}
	public Optional<Player> getPlayer() {
		if(isPlayer()) {
			return Optional.of((Player) actor);
		}
		return Optional.empty();
	}
}
