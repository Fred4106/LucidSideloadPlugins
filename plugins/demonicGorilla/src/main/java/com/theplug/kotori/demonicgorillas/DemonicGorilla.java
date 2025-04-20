/*
 * Copyright (c) 2022, Kotori <https://github.com/OreoCupcakes/>
 * Copyright (c) 2018, Woox <https://github.com/wooxsolo>
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
package com.theplug.kotori.demonicgorillas;

import ethanApiPlugin.EthanApiPlugin;
import net.runelite.api.Actor;
import net.runelite.api.HeadIcon;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldArea;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class DemonicGorilla {
	static final int MAX_ATTACK_RANGE = 10; // Needs <= 10 tiles to reach target
	static final int ATTACK_RATE = 5; // 5 ticks between each attack
	static final int ATTACKS_PER_SWITCH = 3; // 3 unsuccessful attacks per style switch

	static final int PROJECTILE_MAGIC_SPEED = 8; // Travels 8 tiles per tick
	static final int PROJECTILE_RANGED_SPEED = 6; // Travels 6 tiles per tick
	static final int PROJECTILE_MAGIC_DELAY = 12; // Requires an extra 12 tiles
	static final int PROJECTILE_RANGED_DELAY = 9; // Requires an extra 9 tiles

	static final AttackStyle[] ALL_REGULAR_ATTACK_STYLES =
		{
			AttackStyle.MELEE,
			AttackStyle.RANGED,
			AttackStyle.MAGIC
		};
	private final NPC npc;
	private List<AttackStyle> nextPossibleAttackStyles;

	private int attacksUntilSwitch;
	private int nextAttackTick;
	private boolean initiatedCombat;
	private boolean takenDamageRecently;
	private boolean changedAttackStyleThisTick;
	private boolean changedAttackStyleLastTick;
	private int disabledMeleeMovementForTicks;

	public String debugString() {
		return
		"Atk2Switch = " + String.valueOf(attacksUntilSwitch) + ", " +
		"nextAtkTick = " + String.valueOf(nextAttackTick) + ", " +
		"(initCbt, recDmg) = (" + String.valueOf(initiatedCombat) + ", " + String.valueOf(takenDamageRecently) + "), " +
		"ΔStyle(thisT, lastT) = (" + String.valueOf(changedAttackStyleThisTick) + ", " + String.valueOf(changedAttackStyleLastTick) + "), " +
		"DblMeleeMoveT = " +  String.valueOf(disabledMeleeMovementForTicks) + ", ";
	}

	private int lastTickAnimation;
	private WorldArea lastWorldArea;
	private Actor lastTickInteracting;
	private int recentProjectileId;
	private HeadIcon lastTickOverheadIcon;

	DemonicGorilla(NPC npc) {
		this.npc = npc;
		this.nextPossibleAttackStyles = List.of(AttackStyle.MELEE, AttackStyle.RANGED, AttackStyle.MAGIC);
		this.nextAttackTick = -100;
		this.attacksUntilSwitch = ATTACKS_PER_SWITCH;
		this.recentProjectileId = -1;
	}

	NPC getNpc() {
		return this.npc;
	}

	List<AttackStyle> getNextPossibleAttackStyles() {
		return this.nextPossibleAttackStyles;
	}

	boolean nextPossibleAttackStylesContains(Predicate<AttackStyle> predicate) {
		return this.nextPossibleAttackStyles.stream().anyMatch(predicate);
	}

	DemonicGorilla filterNextPossibleAttackStylesContains(Predicate<AttackStyle> predicate) {
		this.nextPossibleAttackStyles = this.nextPossibleAttackStyles.stream().filter(predicate).collect(Collectors.toUnmodifiableList());
		return this;
	}

	DemonicGorilla setNextPossibleAttackStyles(AttackStyle ... nextPossibleAttackStyles) {
		this.nextPossibleAttackStyles = Arrays.stream(nextPossibleAttackStyles).collect(Collectors.toUnmodifiableList());
		return this;
	}
	int getAttacksUntilSwitch() {
		return this.attacksUntilSwitch;
	}

	void setAttacksUntilSwitch(int attacksUntilSwitch) {
		this.attacksUntilSwitch = attacksUntilSwitch;
	}
	void resetAttacksUntilSwitch() {
		this.attacksUntilSwitch = DemonicGorilla.ATTACKS_PER_SWITCH;
	}
	void decrementAttacksUntilSwitch() {
		this.attacksUntilSwitch = attacksUntilSwitch - 1;
	}

	int getNextAttackTick() {
		return this.nextAttackTick;
	}

	void setNextAttackTick(int nextAttackTick) {
		this.nextAttackTick = nextAttackTick;
	}

	int getLastTickAnimation() {
		return this.lastTickAnimation;
	}

	void setLastTickAnimation(int lastTickAnimation) {
		this.lastTickAnimation = lastTickAnimation;
	}

	WorldArea getLastWorldArea() {
		return this.lastWorldArea;
	}

	void setLastWorldArea(WorldArea lastWorldArea) {
		this.lastWorldArea = lastWorldArea;
	}

	boolean isInitiatedCombat() {
		return this.initiatedCombat;
	}

	void setInitiatedCombat(boolean initiatedCombat) {
		this.initiatedCombat = initiatedCombat;
	}

	Actor getLastTickInteracting() {
		return this.lastTickInteracting;
	}

	void setLastTickInteracting(Actor lastTickInteracting) {
		this.lastTickInteracting = lastTickInteracting;
	}

	boolean isTakenDamageRecently() {
		return this.takenDamageRecently;
	}

	void setTakenDamageRecently(boolean takenDamageRecently) {
		this.takenDamageRecently = takenDamageRecently;
	}

	int getRecentProjectileId() {
		return this.recentProjectileId;
	}

	void setRecentProjectileId(int recentProjectileId) {
		this.recentProjectileId = recentProjectileId;
	}

//	boolean isChangedPrayerThisTick() {
//		return this.changedPrayerThisTick;
//	}
//
//	void setChangedPrayerThisTick(boolean changedPrayerThisTick) {
//		this.changedPrayerThisTick = changedPrayerThisTick;
//	}

	boolean isChangedAttackStyleThisTick() {
		return this.changedAttackStyleThisTick;
	}

	void setChangedAttackStyleThisTick(boolean changedAttackStyleThisTick) {
		this.changedAttackStyleThisTick = changedAttackStyleThisTick;
	}

	boolean isChangedAttackStyleLastTick() {
		return this.changedAttackStyleLastTick;
	}

	void setChangedAttackStyleLastTick(boolean changedAttackStyleLastTick) {
		this.changedAttackStyleLastTick = changedAttackStyleLastTick;
	}

	HeadIcon getLastTickOverheadIcon() {
		return this.lastTickOverheadIcon;
	}

	void setLastTickOverheadIcon(HeadIcon lastTickOverheadIcon) {
		this.lastTickOverheadIcon = lastTickOverheadIcon;
	}

	int getDisabledMeleeMovementForTicks() {
		return this.disabledMeleeMovementForTicks;
	}

	void setDisabledMeleeMovementForTicks(int disabledMeleeMovementForTicks) {
		this.disabledMeleeMovementForTicks = disabledMeleeMovementForTicks;
	}

	HeadIcon getOverheadIcon() {
		return EthanApiPlugin.getHeadIcon(npc);
	}

	void onGameTick() {
		if (isTakenDamageRecently()) {
			setInitiatedCombat(true);
		}

		if (getOverheadIcon() != getLastTickOverheadIcon()) {
			if (isChangedAttackStyleLastTick() ||
				isChangedAttackStyleThisTick()) {
				// Apparently if it changes attack style and changes
				// prayer on the same tick or 1 tick apart, it won't
				// be able to move for the next 2 ticks if it attempts
				// to melee
				setDisabledMeleeMovementForTicks(2);
			} else {
				// If it didn't change attack style lately,
				// it's only for the next 1 tick
				setDisabledMeleeMovementForTicks(1);
			}
		}
		setLastTickAnimation(getNpc().getAnimation());
		setLastWorldArea(getNpc().getWorldArea());
		setLastTickInteracting(getNpc().getInteracting());
		setTakenDamageRecently(false);
	//			gorilla.setChangedPrayerThisTick(false);
		setChangedAttackStyleLastTick(isChangedAttackStyleThisTick());
		setChangedAttackStyleThisTick(false);
		setLastTickOverheadIcon(getOverheadIcon());
		setRecentProjectileId(-1);
	}

	enum AttackStyle {
		MAGIC,
		RANGED,
		MELEE,
		BOULDER
	}
}