package com.fred4106.improvedCharges.item.triggers;

import java.util.Map;

public class OnVarbitsMapChanged extends TriggerBase {
	public final Map<Integer, Integer> varbitsMap;

	public OnVarbitsMapChanged(final Map<Integer, Integer> varbitsMap) {
		this.varbitsMap = varbitsMap;
	}
}