package com.fredplugins.pvmDebugger.inferno;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.runelite.api.gameval.NpcID;
import org.apache.commons.lang3.ArrayUtils;

@AllArgsConstructor
@Getter
@ToString
public enum InfernoNpcType {
	NIBBLER(new int[]{NpcID.INFERNO_NIBBLER}, InfernoNpcAttack.MELEE, 4, 99, 100),
	BAT(new int[]{NpcID.INFERNO_CREATURE_HARPIE}, InfernoNpcAttack.RANGED, 3, 4, 7),
	BLOB(new int[]{NpcID.INFERNO_CREATURE_SPLITTER}, InfernoNpcAttack.UNKNOWN, 6, 15, 4),
	MELEE(new int[]{NpcID.INFERNO_CREATURE_MELEE}, InfernoNpcAttack.MELEE, 4, 1, 3),
	RANGER(new int[]{NpcID.INFERNO_CREATURE_RANGER, NpcID.INFERNO_RANGER_FINALWAVE}, InfernoNpcAttack.RANGED, 4, 98, 2),
	MAGE(new int[]{NpcID.INFERNO_CREATURE_MAGER, NpcID.INFERNO_MAGER_FINALWAVE}, InfernoNpcAttack.MAGIC, 4, 98, 1),
	JAD(new int[]{NpcID.INFERNO_JAD, NpcID.INFERNO_JAD_FINALWAVE, NpcID.JAD_CHALLENGE_JAD}, InfernoNpcAttack.UNKNOWN, 3, 99, 0),
	HEALER_JAD(new int[]{NpcID.TZHAAR_FIGHTCAVE_SWARM_BOSS_CLERIC, NpcID.INFERNO_JAD_HEALER, NpcID.INFERNO_JAD_HEALER_FINALWAVE}, InfernoNpcAttack.MELEE, 4, 1, 6),
	ZUK(new int[]{NpcID.INFERNO_TZKALZUK_PLACEHOLDER}, InfernoNpcAttack.UNKNOWN, 10, 99, 99),
	HEALER_ZUK(new int[]{NpcID.INFERNO_ZUK_HEALER, NpcID.JAD_CHALLENGE_HEALER}, InfernoNpcAttack.UNKNOWN, -1, 99, 100);

	private final int[] npcIds;
	private final InfernoNpcAttack defaultAttack;
	private final int ticksAfterAnimation;
	private final int range;
	private final int priority;

	static InfernoNpcType typeFromId(int npcId) {
		for (InfernoNpcType type : InfernoNpcType.values()) {
			if (ArrayUtils.contains(type.getNpcIds(), npcId)) {
				return type;
			}
		}

		return null;
	}
}
