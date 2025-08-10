package ethanApiPlugin.services.localPlayer.events;

import lombok.Getter;
import lombok.Value;

@Value
public class LocalRegionChanged {
	int oldRegion;
	int curRegion;

	public int currentRegion() {
		return getCurRegion();
	}

	public int previousRegion() {
		return getOldRegion();
	}
}
