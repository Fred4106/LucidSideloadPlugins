package ethanApiPlugin.services.localPlayer.events;

import lombok.Value;
import net.runelite.api.coords.WorldPoint;

@Value
public class LocalDestinationChanged {
	WorldPoint from;
	WorldPoint to;
}