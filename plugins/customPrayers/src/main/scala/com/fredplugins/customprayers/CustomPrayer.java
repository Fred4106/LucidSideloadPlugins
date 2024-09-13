package com.fredplugins.customprayers;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Prayer;

@Data
public class CustomPrayer {
		private int activationId;

    private Prayer prayerToActivate;

    private int tickDelay;

    private boolean toggle;

    private boolean ignoreNonTargetEvent;

    public CustomPrayer(int activationId, Prayer prayerToActivate, int tickDelay, boolean toggle, boolean ignoreNonTargetEvent) {
        this.activationId = activationId;
        this.prayerToActivate = prayerToActivate;
        this.tickDelay = tickDelay;
        this.toggle = toggle;
        this.ignoreNonTargetEvent = ignoreNonTargetEvent;
    }
}
