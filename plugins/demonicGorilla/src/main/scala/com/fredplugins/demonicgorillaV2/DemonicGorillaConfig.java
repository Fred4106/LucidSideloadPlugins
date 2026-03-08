package com.fredplugins.demonicgorillaV2;

import lombok.Getter;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

import java.awt.event.KeyEvent;

@ConfigGroup(value = DemonicGorillaConfig.GroupName)
public interface DemonicGorillaConfig extends Config {
	static final String GroupName = "DemonicGorillaHelper";

//	@ConfigSection(
//			name = "General Settings",
//			description = "Full Auto or Combat only",
//			position = 0
//	)
//	String generalSettings = "generalSettings";

	@ConfigItem(
			keyName = "rangeGear",
			name = "Range Gear",
			description = "List of items to equip for range attacks (one per line)",
			position = 10
	)
	default String rangeGear() {
		return "" +
			"25867\n" +//"Bow of faerdhinen (c)\n" +
			"23975\n" + //"Crystal body\n" +
			"23979\n" + //"Crystal legs\n" +
			"22109"; //"Ava's assembler";
	}

	@ConfigItem(
			keyName = "meleeGear",
			name = "Melee Gear",
			description = "List of items to equip for melee attacks (one per line)",
			position = 20
	)
	default String meleeGear() {
		return "" +
			"29589\n" + //"Emberlight\n" +
			"12954\n" + //"Dragon defender\n" +
			"10551\n" + //"Fighter torso\n" +
			"4087\n" + //"Dragon platelegs\n" +
			"6570";//"Fire cape";
	}
}
