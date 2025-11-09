package com.fredplugins.pvmDebugger.inferno;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.runelite.api.Prayer;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;

@AllArgsConstructor
@Getter
@ToString
public enum InfernoNpcAttack {
	MELEE(Prayer.PROTECT_FROM_MELEE,
		Color.ORANGE,
		Color.RED,
		new int[]{
			InfernoData.JAL_NIB,
			InfernoData.JAL_AK_MELEE_ATTACK,
			InfernoData.JAL_IMKOT,
			InfernoData.JAL_XIL_MELEE_ATTACK,
			InfernoData.JAL_ZEK_MELEE_ATTACK, //TODO: Yt-HurKot attack animation
		}),
	RANGED(Prayer.PROTECT_FROM_MISSILES,
		Color.GREEN,
		new Color(0, 128, 0),
		new int[]{
			InfernoData.JAL_MEJRAH,
			InfernoData.JAL_AK_RANGE_ATTACK,
			InfernoData.JAL_XIL_RANGE_ATTACK,
			InfernoData.JALTOK_JAD_RANGE_ATTACK,
		}),
	MAGIC(Prayer.PROTECT_FROM_MAGIC,
		Color.CYAN,
		Color.BLUE,
		new int[]{
			InfernoData.JAL_AK_MAGIC_ATTACK,
			InfernoData.JAL_ZEK_MAGE_ATTACK,
			InfernoData.JALTOK_JAD_MAGE_ATTACK
		}),
	UNKNOWN(null, Color.WHITE, Color.GRAY, new int[]{});

	private final Prayer prayer;
	private final Color normalColor;
	private final Color criticalColor;
	private final int[] animationIds;
	static InfernoNpcAttack attackFromId(int animationId) {
		for (InfernoNpcAttack attack : InfernoNpcAttack.values()) {
			if (ArrayUtils.contains(attack.getAnimationIds(), animationId)) {
				return attack;
			}
		}

		return UNKNOWN;
	}
}
