package com.fred4106.improvedCharges.item.triggers;

import net.runelite.api.Skill;

public class OnStatChanged extends TriggerBase {
    public final Skill skill;

    public OnStatChanged(final Skill skill) {
        this.skill = skill;
    }
}
