package com.fred4106.improvedCharges.item.triggers;

import net.runelite.api.*;

public class OnStatChanged extends TriggerBase {
    public Skill skill;

    public OnStatChanged(Skill skill) {
        this.skill = skill;
    }
}
