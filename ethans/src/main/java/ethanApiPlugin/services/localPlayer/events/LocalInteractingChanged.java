package ethanApiPlugin.services.localPlayer.events;

import lombok.Value;
import net.runelite.api.Actor;

@Value
public class LocalInteractingChanged {
	Actor oldInteracting;
	Actor curInteracting;
}
