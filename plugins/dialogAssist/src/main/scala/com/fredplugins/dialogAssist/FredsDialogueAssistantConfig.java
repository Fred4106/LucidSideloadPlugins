package com.fredplugins.dialogAssist;

import net.runelite.api.gameval.ItemID;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import ujson.Str;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@ConfigGroup(FredsDialogueAssistantPlugin.CONFIG_GROUP)
public interface FredsDialogueAssistantConfig extends Config
{
	@ConfigItem(
			keyName = "optionHighlightColour",
			name = "Option highlight colour",
			description = "The colour of dialogue choices highlighted by the plugin."
	)
	default Color optionHighlightColor()
	{
		return Color.CYAN.darker();
	}

	@ConfigItem(
		keyName = "optionLockedColour",
		name = "Option locked colour",
		description = "The colour of dialogue choices locked by the plugin."
	)
	default Color optionLockedColor()
	{
		return new Color(95, 95, 95);
	}

	@ConfigItem(
		keyName = "hiddenMakeXItems",
		name = "MakeX hidden products",
		description = "List of Items to hide from the make x screen"
	)
	default String hiddenMakeXItems()
	{
		return "ARROW_SHAFT\n" +
			"BEER_GLASS, CANDLE_LANTERN_EMPTY, OIL_LAMP_EMPTY, VIAL_EMPTY, FISHBOWL_EMPTY, STAFFORB, BULLSEYE_LANTERN_LENS";
	}

	@ConfigItem(
		keyName = "hiddenMakeXItems",
		name = "MakeX hidden products",
		description = "List of Items to hide from the make x screen"
	)
	void setHiddenMakeXItems(String raw);

	@ConfigItem(
		keyName = "autoMakeXItems",
		name = "MakeX priority products",
		description = "List of Items to automatically select from the make x screen"
	)
	default String autoMakeXItems()
	{
		return "UNSTRUNG_MAPLE_LONGBOW, UNSTRUNG_YEW_LONGBOW, UNSTRUNG_MAGIC_LONGBOW";
	}

	@ConfigItem(
		keyName = "autoMakeXItems",
		name = "MakeX priority products",
		description = "List of Items to automatically select from the make x screen"
	)
	void setAutoMakeXItems(String raw);
}
