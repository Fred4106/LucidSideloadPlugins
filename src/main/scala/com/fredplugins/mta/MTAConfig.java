
package com.fredplugins.mta;

import net.runelite.client.config.*;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

@ConfigGroup(MTAConfig.ConfigGroup)
public interface MTAConfig extends Config
{
	public final String ConfigGroup = "fredMTA";
	@ConfigSection(
			name = "General",
			description = "General section.",
			position = 0,
			closedByDefault = false
	)
	String generalSection = "General";
	@ConfigSection(
			name = "Telekinetic",
			description = "Telekinetic section.",
			position = 1,
			closedByDefault = false
	)
	String telekineticSection = "Telekinetic";

	@ConfigItem(
		keyName = "alchemy",
		name = "Enable alchemy room",
		description = "Configures whether or not the alchemy room overlay is enabled.",
		position = 0,
		section = generalSection
	)
	default boolean alchemy()
	{
		return true;
	}


	@ConfigItem(
		keyName = "graveyard",
		name = "Enable graveyard room",
		description = "Configures whether or not the graveyard room overlay is enabled.",
		position = 1,
		section = generalSection
	)
	default boolean graveyard()
	{
		return true;
	}

	@ConfigItem(
		keyName = "telekinetic",
		name = "Enable telekinetic room",
		description = "Configures whether or not the telekinetic room overlay is enabled.",
		position = 2,
		section = generalSection
	)
	default boolean telekinetic()
	{
		return true;
	}

	@ConfigItem(
		keyName = "enchantment",
		name = "Enable enchantment room",
		description = "Configures whether or not the enchantment room overlay is enabled.",
		position = 3,
		section = generalSection
	)
	default boolean enchantment()
	{
		return true;
	}

	@ConfigItem(
			keyName = "castTkGrab",
			name = "Cast tk grab",
			description = "When you press this key you'll cast spell on npc",
			position = 0,
			section = telekineticSection
	)
	default Keybind castTkGrab()
	{
		return new Keybind(KeyEvent.VK_Q, 0);
	}

}
