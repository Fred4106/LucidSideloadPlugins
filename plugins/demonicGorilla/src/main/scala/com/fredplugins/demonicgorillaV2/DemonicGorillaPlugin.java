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
package com.fredplugins.demonicgorillaV2;

import ch.qos.logback.classic.Level;
import com.fredplugins.common.utils.ShimUtils$;
import com.fredplugins.demonicgorillaV2.DemonicGorilla.AttackStyle;
import com.google.common.collect.ImmutableSet;
import com.lucidplugins.api.utils.CombatUtils;
import com.lucidplugins.api.utils.InventoryUtils;
import ethanApiPlugin.EthanApiPlugin;
import net.runelite.api.AnimationID;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.HeadIcon;
import net.runelite.api.HitsplatID;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@PluginDependency(EthanApiPlugin.class)
@PluginDescriptor(
	name = "<html><font color=#6b8af6>[P]</font> Demonic Gorillas</html>",
	enabledByDefault = false,
	description = "Count demonic gorilla attacks and display their next possible attack styles",
	tags = {"combat", "overlay", "pve", "pvm", "demonics", "gorilla", "ported", "kotori"}
)
public class DemonicGorillaPlugin extends Plugin {
	private final static Logger log;
	static {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(DemonicGorillaPlugin.class)).setLevel(Level.DEBUG);
		log = LoggerFactory.getLogger(DemonicGorillaPlugin.class);
	}
	private static final Set<Integer> DEMONIC_PROJECTILES = ImmutableSet.of(1302, 1304, 856);
	private static final Set<Integer> REGION_IDS = Set.of(8280, 8536);
	private static final int DEMONIC_GORILLA_AOE_ATTACK = 7228;
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private DemonicGorillaOverlay overlay;
	@Inject
	private ClientThread clientThread;
	private Map<NPC, DemonicGorilla> gorillas;
	private List<Projectile> recentBoulders;
	private List<PendingGorillaAttack> pendingAttacks;
	private Map<Player, MemorizedPlayer> memorizedPlayers;
	private ArrayList<Projectile> gorillaProjectiles;

	private List<WorldPoint> cachedBoulders = List.of();
	private boolean atGorillas;

	private static boolean isNpcGorilla(int npcId) {
		return npcId == NpcID.DEMONIC_GORILLA ||
			npcId == NpcID.DEMONIC_GORILLA_7145 ||
			npcId == NpcID.DEMONIC_GORILLA_7146 ||
			npcId == NpcID.DEMONIC_GORILLA_7147 ||
			npcId == NpcID.DEMONIC_GORILLA_7148 ||
			npcId == NpcID.DEMONIC_GORILLA_7149;
	}

	@Override
	protected void startUp() {
		if (client.getGameState() != GameState.LOGGED_IN || !atDemonicGorillas()) {
			return;
		}

		init();
	}

	@Override
	protected void shutDown() {
		atGorillas = false;

		overlayManager.remove(overlay);
		gorillas = null;
		recentBoulders = null;
		pendingAttacks = null;
		memorizedPlayers = null;
		gorillaProjectiles = null;
	}

	private void init() {
		atGorillas = true;

		overlayManager.add(overlay);
		gorillas = new HashMap<>();
		recentBoulders = new ArrayList<>();
		pendingAttacks = new ArrayList<>();
		gorillaProjectiles = new ArrayList<>();
		memorizedPlayers = new HashMap<>();
		clientThread.invoke(() -> {
			recentBoulders.clear();
			pendingAttacks.clear();
			resetGorillas();
			resetPlayers();
		}); // Updates the list of gorillas and players
	}

	private void resetGorillas() {
		gorillas.clear();
		for (NPC npc : client.getNpcs()) {
			if (isNpcGorilla(npc.getId())) {
				gorillas.put(npc, new DemonicGorilla(npc));
			}
		}
	}

	private void resetPlayers() {
		memorizedPlayers.clear();
		for (Player player : client.getPlayers()) {
			memorizedPlayers.put(player, new MemorizedPlayer(player));
		}
	}

	private void checkGorillaAttackStyleSwitch(DemonicGorilla gorilla, final AttackStyle ... protectedStyles) {
		if (gorilla.getAttacksUntilSwitch() <= 0 ||
			gorilla.getNextPossibleAttackStyles().isEmpty()) {

			gorilla.setNextPossibleAttackStyles(AttackStyle.MELEE, AttackStyle.RANGED, AttackStyle.MAGIC)
				.filterNextPossibleAttackStylesContains(x -> !ArrayUtils.contains(protectedStyles, x));

			gorilla.resetAttacksUntilSwitch();
			gorilla.setChangedAttackStyleThisTick(true);
		}
	}

	private AttackStyle getProtectedStyle(Player player) {
		HeadIcon headIcon = player.getOverheadIcon();
		if (headIcon == null) {
			return null;
		}
		switch (headIcon) {
			case MELEE:
				return AttackStyle.MELEE;
			case RANGED:
				return AttackStyle.RANGED;
			case MAGIC:
				return AttackStyle.MAGIC;
			default:
				return null;
		}
	}

	private void onGorillaAttack(DemonicGorilla gorilla, final AttackStyle attackStyle) {
		gorilla.setInitiatedCombat(true);

		Player target = (Player) gorilla.getNpc().getInteracting();

		AttackStyle protectedStyle = null;
		if (target != null) {
			protectedStyle = getProtectedStyle(target);
		}
		boolean correctPrayer =
			target == null || // If player is out of memory, assume prayer was correct
				(attackStyle != null &&
					attackStyle.equals(protectedStyle));

		if (attackStyle == AttackStyle.BOULDER) {
			// The gorilla can't throw boulders when it's meleeing
			gorilla.filterNextPossibleAttackStylesContains(x -> x != AttackStyle.MELEE);
//			gorilla.setNextPossibleAttackStyles(gorilla.getNextPossibleAttackStyles().stream().filter(x -> x != DemonicGorilla.AttackStyle.MELEE).collect(Collectors.toUnmodifiableList()));
		} else {
			if (correctPrayer) {
				gorilla.decrementAttacksUntilSwitch();
//				gorilla.setAttacksUntilSwitch(gorilla.getAttacksUntilSwitch() - 1);
			} else {
				// We're not sure if the attack will hit a 0 or not,
				// so we don't know if we should decrease the counter or not,
				// so we keep track of the attack here until the damage splat
				// has appeared on the player.

				int damagesOnTick = client.getTickCount();
				if (attackStyle == AttackStyle.MAGIC) {
					MemorizedPlayer mp = memorizedPlayers.get(target);
					WorldArea lastPlayerArea = mp.getLastWorldArea();
					if (lastPlayerArea != null) {
						int dist = gorilla.getNpc().getWorldArea().distanceTo(lastPlayerArea);
						damagesOnTick += (dist + DemonicGorilla.PROJECTILE_MAGIC_DELAY) /
							DemonicGorilla.PROJECTILE_MAGIC_SPEED;
					}
				} else if (attackStyle == AttackStyle.RANGED) {
					MemorizedPlayer mp = memorizedPlayers.get(target);
					WorldArea lastPlayerArea = mp.getLastWorldArea();
					if (lastPlayerArea != null) {
						int dist = gorilla.getNpc().getWorldArea().distanceTo(lastPlayerArea);
						damagesOnTick += (dist + DemonicGorilla.PROJECTILE_RANGED_DELAY) /
							DemonicGorilla.PROJECTILE_RANGED_SPEED;
					}
				}
				pendingAttacks.add(new PendingGorillaAttack(gorilla, attackStyle, target, damagesOnTick));
			}

			gorilla.filterNextPossibleAttackStylesContains(x -> x == attackStyle);

			if (gorilla.getNextPossibleAttackStyles().isEmpty()) {
				// Sometimes the gorilla can switch attack style before it's supposed to
				// if someone was fighting it earlier and then left, so we just
				// reset the counter in that case.

				gorilla.setNextPossibleAttackStyles(attackStyle);
//				gorilla.filterNextPossibleAttackStylesContains(x -> x == attackStyle);
//				gorilla.setNextPossibleAttackStyles(Arrays
//					.stream(DemonicGorilla.ALL_REGULAR_ATTACK_STYLES)
//					.filter(x -> x == attackStyle)
//					.collect(Collectors.toUnmodifiableList()));
//

				gorilla.resetAttacksUntilSwitch();
				if (correctPrayer) {
					gorilla.decrementAttacksUntilSwitch();
				}
			}
		}

		checkGorillaAttackStyleSwitch(gorilla, protectedStyle);

		int tickCounter = client.getTickCount();
		gorilla.setNextAttackTick(tickCounter + DemonicGorilla.ATTACK_RATE);
	}

	private DemonicGorilla targetGorilla = null;

	@Subscribe
	private void onInteractionChanged(InteractingChanged event) {
		if (event.getSource() == client.getLocalPlayer() && event.getTarget() != null) {
			if (event.getTarget() instanceof NPC  && gorillas.containsKey((NPC)event.getTarget())) {
				targetGorilla = gorillas.get(((NPC) event.getTarget()));
			} else {
				targetGorilla = null;
			}
		}
	}

	@Subscribe
	private void onProjectileMoved(ProjectileMoved event) {
		if (!atGorillas) {
			return;
		}

		final Projectile projectile = event.getProjectile();
		final int projectileId = projectile.getId();

		if (!DEMONIC_PROJECTILES.contains(projectileId)) {
			return;
		}

		if (gorillaProjectiles.contains(projectile)) {
			return;
		}
		gorillaProjectiles.add(projectile);

		final WorldPoint loc = WorldPoint.fromLocal(client.getTopLevelWorldView(), projectile.getX1(), projectile.getY1(), client.getTopLevelWorldView().getPlane());

		if (projectileId == 856) {
			log.debug("Projectile {} with class {}",  event.getProjectile(), event.getProjectile().getClass());
			recentBoulders.add(projectile);
		} else {
			for (DemonicGorilla gorilla : gorillas.values()) {
				if (gorilla.getNpc().getWorldLocation().distanceTo(loc) == 0) {
					gorilla.setRecentProjectileId(projectile.getId());
				}
			}
		}
	}

	private void checkPendingAttacks() {

	}

	@Subscribe
	private void onHitsplatApplied(HitsplatApplied event) {
		if (!atGorillas || gorillas.isEmpty()) {
			return;
		}

		if (event.getActor() instanceof Player) {
			Player player = (Player) event.getActor();
			MemorizedPlayer mp = memorizedPlayers.get(player);
			if (mp != null) {
				mp.addHitsplat(event.getHitsplat());
			}
		} else if (event.getActor() instanceof NPC) {
			DemonicGorilla gorilla = gorillas.get((NPC) event.getActor());
			int hitsplatType = event.getHitsplat().getHitsplatType();
			if (gorilla != null && (hitsplatType == HitsplatID.BLOCK_ME ||
				hitsplatType == HitsplatID.DAMAGE_ME)) {
				gorilla.setTakenDamageRecently(true);
			}
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event) {
		final GameState gs = event.getGameState();

		switch (gs) {
			case LOGGED_IN:
				if (atDemonicGorillas()) {
					if (!atGorillas) {
						init();
					}
				} else {
					if (atGorillas) {
						shutDown();
					}
				}
				break;
			case HOPPING:
			case LOGGING_IN:
			case CONNECTION_LOST:
			case LOGIN_SCREEN:
				if (atGorillas) {
					shutDown();
				}
				break;
			default:
				break;
		}
	}

	@Subscribe
	private void onPlayerSpawned(PlayerSpawned event) {
		if (!atGorillas || gorillas.isEmpty()) {
			return;
		}

		Player player = event.getPlayer();
		memorizedPlayers.put(player, new MemorizedPlayer(player));
	}

	@Subscribe
	private void onPlayerDespawned(PlayerDespawned event) {
		if (!atGorillas || gorillas.isEmpty()) {
			return;
		}

		memorizedPlayers.remove(event.getPlayer());
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event) {
		if (!atGorillas) {
			return;
		}
		NPC npc = event.getNpc();
		if (isNpcGorilla(npc.getId())) {
			if (gorillas.isEmpty()) {
				// Players are not kept track of when there are no gorillas in
				// memory, so we need to add the players that were already in memory.
				resetPlayers();
			}

			gorillas.put(npc, new DemonicGorilla(npc));
		}
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event) {
		if (!atGorillas) {
			return;
		}
		DemonicGorilla demonicGorillaDespawned = gorillas.remove(event.getNpc());
		if(demonicGorillaDespawned != null && targetGorilla == demonicGorillaDespawned) {
			targetGorilla = null;
		}
		if (demonicGorillaDespawned != null && gorillas.isEmpty()) {
			recentBoulders.clear();
			pendingAttacks.clear();
			memorizedPlayers.clear();
			gorillas.clear();
		}
	}

	private WorldPoint projectileToTargetLocation(Projectile projectile) {
		return WorldPoint.fromLocal(client, projectile.getTarget());
	}

	@Subscribe
	private void onGameTick(GameTick event) {
		if (!atGorillas) {
			cachedBoulders = List.of();
			return;
		}
		//region checkGorillaAttacks
		int tickCounter = client.getTickCount();
		for (DemonicGorilla gorilla : gorillas.values()) {
			Player interacting = (Player) gorilla.getNpc().getInteracting();
			MemorizedPlayer mp = memorizedPlayers.get(interacting);

			if (gorilla.getLastTickInteracting() != null && interacting == null) {
				gorilla.setInitiatedCombat(false);
			} else if (mp != null && mp.getLastWorldArea() != null &&
				!gorilla.isInitiatedCombat() &&
				tickCounter < gorilla.getNextAttackTick() &&
				gorilla.getNpc().getWorldArea().isInMeleeDistance(mp.getLastWorldArea())) {
				gorilla.setInitiatedCombat(true);
				gorilla.setNextAttackTick(tickCounter + 1);
			}

			int animationId = gorilla.getNpc().getAnimation();

			if (gorilla.isTakenDamageRecently() &&
				tickCounter >= gorilla.getNextAttackTick() + 4) {
				// The gorilla was flinched, so its next attack gets delayed
				gorilla.setNextAttackTick(tickCounter + DemonicGorilla.ATTACK_RATE / 2);
				gorilla.setInitiatedCombat(true);

				if (mp != null && mp.getLastWorldArea() != null &&
					!gorilla.getNpc().getWorldArea().isInMeleeDistance(mp.getLastWorldArea()) &&
					!gorilla.getNpc().getWorldArea().intersectsWith(mp.getLastWorldArea())) {
					// Gorillas stop meleeing when they get flinched
					// and the target isn't in melee distance
					gorilla.filterNextPossibleAttackStylesContains(x -> x != AttackStyle.MELEE);
					if (interacting != null) {
						checkGorillaAttackStyleSwitch(gorilla, AttackStyle.MELEE,
							getProtectedStyle(interacting));
					}
				}
			} else if (animationId != gorilla.getLastTickAnimation()) {
				if (animationId == AnimationID.DEMONIC_GORILLA_MELEE_ATTACK) {
					onGorillaAttack(gorilla, AttackStyle.MELEE);
				} else if (animationId == AnimationID.DEMONIC_GORILLA_MAGIC_ATTACK) {
					onGorillaAttack(gorilla, AttackStyle.MAGIC);
				} else if (animationId == AnimationID.DEMONIC_GORILLA_RANGED_ATTACK) {
					onGorillaAttack(gorilla, AttackStyle.RANGED);
				} else if (animationId == DEMONIC_GORILLA_AOE_ATTACK && interacting != null &&
					gorilla.nextPossibleAttackStylesContains(x -> x == AttackStyle.MAGIC || x == AttackStyle.RANGED)
				) {
					// Note that AoE animation is the same as prayer switch animation
					// so we need to check if the prayer was switched or not.
					// It also does this animation when it spawns, so
					// we need the interacting != null check.

					if (gorilla.getOverheadIcon() == gorilla.getLastTickOverheadIcon()) {
						// Confirmed, the gorilla used the AoE attack
						onGorillaAttack(gorilla, AttackStyle.BOULDER);
					} else {
						if (tickCounter >= gorilla.getNextAttackTick()) {
//							gorilla.setChangedPrayerThisTick(true);

							// This part is more complicated because the gorilla may have
							// used an attack, but the prayer switch animation takes
							// priority over normal attack animations.

							int projectileId = gorilla.getRecentProjectileId();
							if (projectileId == 1304) {
								onGorillaAttack(gorilla, AttackStyle.MAGIC);
							} else if (projectileId == 1302) {
								onGorillaAttack(gorilla, AttackStyle.RANGED);
							} else if (mp != null) {
								WorldArea lastPlayerArea = mp.getLastWorldArea();
								if (lastPlayerArea != null && recentBoulders.stream()
									.anyMatch(x -> projectileToTargetLocation(x).distanceTo(lastPlayerArea) == 0)) {
									// A boulder started falling on the gorillas target,
									// so we assume it was the gorilla who shot it
									onGorillaAttack(gorilla, AttackStyle.BOULDER);
								} else if (mp.takenDamage()) {
									// It wasn't any of the three other attacks,
									// but the player took damage, so we assume
									// it's a melee attack
									onGorillaAttack(gorilla, AttackStyle.MELEE);
								}
							}
						}

						// The next attack tick is always delayed if the
						// gorilla switched prayer
						gorilla.setNextAttackTick(tickCounter + DemonicGorilla.ATTACK_RATE);
//						gorilla.setChangedPrayerThisTick(true);
					}
				}
			}

			if (gorilla.getDisabledMeleeMovementForTicks() > 0) {
				gorilla.setDisabledMeleeMovementForTicks(gorilla.getDisabledMeleeMovementForTicks() - 1);
			} else if (gorilla.isInitiatedCombat() &&
				gorilla.getNpc().getInteracting() != null &&
				!gorilla.isChangedAttackStyleThisTick() &&
				gorilla.getNextPossibleAttackStyles().size() >= 2 &&
				gorilla.nextPossibleAttackStylesContains(x -> x == AttackStyle.MELEE)
			) {
				// If melee is a possibility, we can check if the gorilla
				// is or isn't moving toward the player to determine if
				// it is actually attempting to melee or not.
				// We only run this check if the gorilla is in combat
				// because otherwise it attempts to travel to melee
				// distance before attacking its target.

				if (mp != null && mp.getLastWorldArea() != null && gorilla.getLastWorldArea() != null) {
					WorldArea predictedNewArea = WorldAreaExtended.calculateNextTravellingPoint(
						client, gorilla.getLastWorldArea(), mp.getLastWorldArea(), true, x ->
						{
							// Gorillas can't normally walk through other gorillas
							// or other players
							final WorldArea area1 = new WorldArea(x, 1, 1);
							return gorillas.values().stream().noneMatch(y ->
							{
								if (y == gorilla) {
									return false;
								}
								final WorldArea area2 =
									y.getNpc().getIndex() < gorilla.getNpc().getIndex() ?
										y.getNpc().getWorldArea() : y.getLastWorldArea();
								return area2 != null && area1.intersectsWith(area2);
							}) && memorizedPlayers.values().stream().noneMatch(y ->
							{
								final WorldArea area2 = y.getLastWorldArea();
								return area2 != null && area1.intersectsWith(area2);
							});

							// There is a special case where if a player walked through
							// a gorilla, or a player walked through another player,
							// the tiles that were walked through becomes
							// walkable, but I didn't feel like it's necessary to handle
							// that special case as it should rarely happen.
						});
					if (predictedNewArea != null) {
						int distance = gorilla.getNpc().getWorldArea().distanceTo(mp.getLastWorldArea());
						WorldPoint predictedMovement = predictedNewArea.toWorldPoint();
						if (distance <= DemonicGorilla.MAX_ATTACK_RANGE && mp.getLastWorldArea().hasLineOfSightTo(client.getTopLevelWorldView(), gorilla.getLastWorldArea())) {
							if (predictedMovement.distanceTo(gorilla.getLastWorldArea().toWorldPoint()) != 0) {
								if (predictedMovement.distanceTo(gorilla.getNpc().getWorldLocation()) == 0) {
									gorilla.filterNextPossibleAttackStylesContains(x -> x == AttackStyle.MELEE);
								} else {
									gorilla.filterNextPossibleAttackStylesContains(x -> x != AttackStyle.MELEE);
								}
							} else if (tickCounter >= gorilla.getNextAttackTick() &&
								gorilla.getRecentProjectileId() == -1 &&
								recentBoulders.stream().noneMatch(x -> projectileToTargetLocation(x).distanceTo(mp.getLastWorldArea()) == 0)) {
								gorilla.filterNextPossibleAttackStylesContains(x -> x == AttackStyle.MELEE);
							}
						}
					}
				}
			}

			gorilla.onGameTick();
		}
		//endregion

		//region checkPendingAttacks
		Iterator<PendingGorillaAttack> it = pendingAttacks.iterator();
		while (it.hasNext()) {
			PendingGorillaAttack attack = it.next();
			if (tickCounter >= attack.getFinishesOnTick()) {
				DemonicGorilla gorilla = attack.getAttacker();
				MemorizedPlayer target = memorizedPlayers.get(attack.getTarget());
				if (target == null || !target.takenDamage() || target.blockedDamage()) {
					gorilla.decrementAttacksUntilSwitch();
					checkGorillaAttackStyleSwitch(gorilla);
				}

				it.remove();
			}
		}
		//endregions

		for (MemorizedPlayer mp : memorizedPlayers.values()) {
			mp.onGameTick();
		}
		cachedBoulders = recentBoulders
			.stream()
			.map(p -> WorldPoint.fromLocal(client, p.getTarget()))
			.collect(Collectors.toList());
		recentBoulders.removeIf(p -> p.getRemainingCycles() <= 0);
		gorillaProjectiles.removeIf(p -> p.getRemainingCycles() <= 0);

		if (targetGorilla != null && gorillas.containsValue(targetGorilla)) {
			var playerProtectedAgainst = getProtectedStyle(client.getLocalPlayer());
			if (targetGorilla.getNextPossibleAttackStyles().size() == 1) {
				var usingAttackStyle = targetGorilla.getNextPossibleAttackStyles().get(0);
				if (playerProtectedAgainst != usingAttackStyle) {
					switch (usingAttackStyle) {
						case MAGIC:
							CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MAGIC);
							break;
						case MELEE:
							CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MELEE);
							break;
						case RANGED:
							CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MISSILES);
							break;
					}
				}
			} else if (targetGorilla.getNextPossibleAttackStyles().contains(AttackStyle.MELEE)) {
				targetGorilla.getNextPossibleAttackStyles().stream().filter(x -> x != AttackStyle.MELEE).filter(x -> x != playerProtectedAgainst).findFirst().ifPresent(ats -> {
					switch (ats) {
						case MAGIC:
							CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MAGIC);
							break;
						case RANGED:
							CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MISSILES);
							break;
					}
				});
			} else if (playerProtectedAgainst != AttackStyle.MAGIC) {
				CombatUtils.activatePrayer(Prayer.PROTECT_FROM_MAGIC);
			}

			switch (targetGorilla.getOverheadIcon()) {
				case MELEE:
					if (InventoryUtils.contains(ItemID.BOW_OF_FAERDHINEN_INFINITE)) {
						InventoryUtils.wieldItem(ItemID.BOW_OF_FAERDHINEN_INFINITE);
					}
					break;
				case RANGED:
					if (InventoryUtils.contains(ItemID.ARCLIGHT)) {
						InventoryUtils.wieldItem(ItemID.ARCLIGHT);
					}
					if (InventoryUtils.contains(ItemID.DRAGON_PARRYINGDAGGER)) {
						InventoryUtils.wieldItem(ItemID.DRAGON_PARRYINGDAGGER);
					}
					break;
				default:
			}
		} else {
			targetGorilla = null;
		}
	}


	public int getPlayerRegionID() {
		return WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
	}

	private boolean atDemonicGorillas() {
		return REGION_IDS.contains(getPlayerRegionID());
	}

	Map<NPC, DemonicGorilla> getGorillas() {
		return this.gorillas;
	}

	List<WorldPoint> getBoulderTargets() {
		return this.cachedBoulders;
	}

	DemonicGorilla getTargetGorilla() {
		return this.targetGorilla;
	}
}
