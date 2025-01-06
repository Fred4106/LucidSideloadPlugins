package com.fredplugins.pvmHelper;

import com.fredplugins.pvmHelper.config.NamedAnimationEntry;
import com.fredplugins.pvmHelper.config.NamedAnimationEntry$;
import net.runelite.client.config.*;

import java.util.ArrayList;
import java.util.Set;

@ConfigGroup(FredsPvmHelperConfig.GroupName)
public interface FredsPvmHelperConfig extends Config {
	final String GroupName = "fredspvmhelper";
	//region Debug
	@ConfigSection(
		name = "Debug",
		description = "Debug details",
		position = 0
	)
	String DEBUG_SECTION = "Debug";

	@ConfigItem(
		name = "Debug NPCs",
		description = "Enter the the npc ids that should be logged, separated by commas",
		position = 0,
		keyName = "debugNpcIds",
		section = DEBUG_SECTION
	)
	default String getNpcIds() {
		return "";
	}

	@ConfigItem(
		name = "Debug Projectiles",
		description = "Enter the projectile ids that should be logged, separated by commas",
		position = 1,
		keyName = "debugProjectileIds",
		section = DEBUG_SECTION
	)
	default String getProjectileIds() {
		return "";
	}
	//endregion

	//region Animation Names
	@ConfigSection(
		name = "Animation Names",
		description = "Know animation names to improve gui",
		position = 3
	)
	String ANIMATION_NAMES_SECTION = "AnimationNames";
	@ConfigItem(
		name = "0",
		description = "List of animation lookups",
		position = 0,
		keyName = "namedAnimationEntries",
		section = ANIMATION_NAMES_SECTION
	)
	default String namedAnimationEntries() {
		return NamedAnimationEntry$.MODULE$.decode(NamedAnimationEntry$.MODULE$.apply("npc1", 224, "test1")) + "\n"
			+ NamedAnimationEntry$.MODULE$.decode(NamedAnimationEntry$.MODULE$.apply("npc2", 233, "test2")) + "\n"
			+ NamedAnimationEntry$.MODULE$.decode(NamedAnimationEntry$.MODULE$.apply("hunllef", 22562, "test3")) +"\n";
	}
	@ConfigItem(
		name = "1",
		description = "Name lookup for animation[1]",
		position = 1,
		keyName = "animation1",
		section = ANIMATION_NAMES_SECTION
	)
	default String animation1() {
		return NamedAnimationEntry$.MODULE$.decode(NamedAnimationEntry$.MODULE$.empty());
	}

	@ConfigItem(
		name = "2",
		description = "Name lookup for animation[2]",
		position = 2,
		keyName = "animation2",
		section = ANIMATION_NAMES_SECTION
	)
	default String animation2() {
		return NamedAnimationEntry$.MODULE$.decode(NamedAnimationEntry$.MODULE$.empty());
	}

	@ConfigItem(
		name = "2",
		description = "Name lookup for animation[2] with format \"animationName:npcName|animationId\"",
		position = 3,
		keyName = "animation3",
		section = ANIMATION_NAMES_SECTION
	)
	default String animation3() {
		return NamedAnimationEntry$.MODULE$.decode(NamedAnimationEntry$.MODULE$.empty());
	}
	//endregion


	//region Panel
	@ConfigSection(
		name = "Panel",
		description = "Debug Panel details",
		position = 5
	)
	String PANEL_SECTION = "Panel";

	@Range(
		min = 6,
		max = 32
	)
	@ConfigItem(
		keyName = "fontSize",
		name = "Font Size",
		description = "sets font size for overlay",
		position = 0,
		section = PANEL_SECTION
	)
	default int getFontSize() {
		return 14;
	}


	@ConfigItem(
		keyName = "fontBold",
		name = "Bold Font",
		description = "sets bold font for overlay",
		position = 1,
		section = PANEL_SECTION
	)
	default boolean getFontBold() {
		return true;
	}
	//endregion
}