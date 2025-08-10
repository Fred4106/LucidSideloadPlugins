package ethanApiPlugin.services.localPlayer.events;

import lombok.Value;

@Value
public class LocalAnimationChanged {
	int oldAnimation;
	int curAnimation;
}
