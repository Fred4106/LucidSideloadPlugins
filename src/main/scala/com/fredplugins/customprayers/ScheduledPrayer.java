package com.fredplugins.customprayers;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;

@Data
@AllArgsConstructor
public class ScheduledPrayer
{

    private Prayer prayer;

    private int activationTick;

    private boolean toggle;

    private NPC attached;
}
