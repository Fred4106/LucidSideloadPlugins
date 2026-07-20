package com.fredplugins.attacktimer;

import lombok.Value;

@Value
public class LocalPlayerAttacked {
	int weaponId;
	int attackInterval;
	AttackStyle style;
}
