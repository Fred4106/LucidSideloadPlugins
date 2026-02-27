/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.fredplugins.pvmDebugger.cerberus;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(value = CerberusConfig.GROUP, secondaryConfig = true)
public interface CerberusConfig extends Config
{
	static final String GROUP = "FredsCerberus";
	@ConfigItem(
		name = "Enabled",
		description = "Is Cerberus helper enabled?",
		position = 0,
		keyName = "enabled"
	)
	default boolean enabled()
	{
		return false;
	}
	@ConfigSection(
			name = "Helper Settings",
			description = "Automatic helper features.",
			position = 1
	)
	String helperSection = "Helper";

	@ConfigSection(
			name = "Tick counters",
			description = "Tick counter settings.",
			position = 2
	)
	String tickCounterSection = "Tick counters";

	@ConfigSection(
			name = "Highlights",
			description = "Highlight settings.",
			position = 3
	)
	String highlightsSection = "Highlights";

	//region Tick counters
	@ConfigItem(
		name = "Next Attack Counter",
		description = "Enable ticks until next attack overlays.",
		position = 0,
		keyName = "nextAttackCounter",
		section = tickCounterSection
	)
	default boolean nextAttackCounter()
	{
		return true;
	}
	//endregion
	
	//region Highlights
	@ConfigItem(
		name = "Show King's True Tile",
		description = "Show true tiles for all Dagannoth Kings.",
		position = 0,
		keyName = "highlightKingTrueTile",
		section = highlightsSection
	)
	default boolean highlightKingTrueTile()
	{
		return true;
	}
	//endregion

	//region Helper Section
	@ConfigItem(
		name = "Auto-Pray Protection",
		description = "Automatically switch protection prayer based on what will attack next.",
		position = 0,
		keyName = "autoPrayProtection",
		section = helperSection
	)
	default boolean autoPrayProtection()
	{
		return false;
	}

	@ConfigItem(
		name = "Auto-Pray Offense",
		description = "Automatically switch offensive prayer based on what weapon is equiped.",
		position = 1,
		keyName = "autoPrayOffensive",
		section = helperSection
	)
	default boolean autoPrayOffensive()
	{
		return false;
	}
	//endregion

	//region Constants
	//endregion
}
