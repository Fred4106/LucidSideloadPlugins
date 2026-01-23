package com.fredplugins.dialogAssist;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

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
}
