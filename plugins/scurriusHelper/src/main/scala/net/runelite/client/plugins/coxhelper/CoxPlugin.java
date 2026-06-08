/*
 * Copyright (c) 2019, xzact <https://github.com/xzact>
 * Copyright (c) 2019, ganom <https://github.com/Ganom>
 * Copyright (c) 2019, gazivodag <https://github.com/gazivodag>
 * Copyright (c) 2019, lyzrds <https://discord.gg/5eb9Fe>
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

package net.runelite.client.plugins.coxhelper;

import com.fredplugins.common.ProjectileID;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PluginDescriptor(
	name = "Freds CoX Helper",
	enabledByDefault = false,
	description = "All-in-one plugin for Chambers of Xeric",
	tags = {"CoX", "chamber", "xeric", "helper"}
)
@Slf4j
public class CoxPlugin extends Plugin {
	private static final int ANIMATION_ID_G1 = 430;
	private static final Pattern TP_REGEX = Pattern.compile("You have been paired with <col=ff0000>(.*)</col>! The magical power will enact soon...");
	private final Map<NPC, NPCContainer> npcContainers = new HashMap<>();
	@Inject
	private Client client;
	@Inject
	private ChatMessageManager chatMessageManager;
	@Inject
	private CoxOverlay coxOverlay;
	@Inject
	private CoxInfoBox coxInfoBox;
	@Inject
	private CoxDebugBox coxDebugBox;
	@Inject
	private CoxConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private EventBus eventBus;
	@Inject
	private Olm olm;
	//other
	private int vanguards;
	private boolean tektonActive;
	private int tektonAttackTicks;

	public static final int TEKTON_ANVIL = 7475;
	public static final int TEKTON_AUTO1 = 7482;
	public static final int TEKTON_AUTO2 = 7483;
	public static final int TEKTON_AUTO3 = 7484;
	public static final int TEKTON_FAST_AUTO1 = 7478;
	public static final int TEKTON_FAST_AUTO2 = 7488;
	public static final int TEKTON_ENRAGE_AUTO1 = 7492;
	public static final int TEKTON_ENRAGE_AUTO2 = 7493;
	public static final int TEKTON_ENRAGE_AUTO3 = 7494;

	@Provides
	CoxConfig getConfig(ConfigManager configManager) {
		return configManager.getConfig(CoxConfig.class);
	}


	private AutoMode auto = null;

	@Override
	protected void startUp() {
		if (auto == null) auto = new AutoMode(this);

		this.overlayManager.add(this.coxOverlay);
		this.overlayManager.add(this.coxInfoBox);
		this.overlayManager.add(this.coxDebugBox);
		this.olm.hardRest();
	}

	@Override
	protected void shutDown() {
		this.overlayManager.remove(this.coxOverlay);
		this.overlayManager.remove(this.coxInfoBox);
		this.overlayManager.remove(this.coxDebugBox);
	}

	@Subscribe
	private void onChatMessage(ChatMessage event) {
		if (!this.inRaid()) {
			return;
		}

		if (event.getType() == ChatMessageType.GAMEMESSAGE) {
			final Matcher tpMatcher = TP_REGEX.matcher(event.getMessage());

			if (tpMatcher.matches()) {
				for (Player player : this.client.getPlayers()) {
					final String rawPlayerName = player.getName();

					if (rawPlayerName != null) {
						final String fixedPlayerName = net.runelite.client.util.Text.sanitize(rawPlayerName);

						if (fixedPlayerName.equals(Text.sanitize(tpMatcher.group(1)))) {
							this.olm.getVictims().add(new Victim(player, Victim.Type.TELEPORT));
						}
					}
				}
			}

			switch (Text.standardize(event.getMessageNode().getValue())) {
				case "the great olm rises with the power of acid.":
					olm.setPhaseType(Olm.PhaseType.ACID);
					break;
				case "the great olm rises with the power of crystal.":
					olm.setPhaseType(Olm.PhaseType.CRYSTAL);
					break;
				case "the great olm rises with the power of flame.":
					olm.setPhaseType(Olm.PhaseType.FLAME);
					break;
				case "the great olm fires a sphere of aggression your way. your prayers have been sapped.":
				case "the great olm fires a sphere of aggression your way.":
					this.olm.setPrayer(Prayer.PROTECT_FROM_MELEE);
					break;
				case "the great olm fires a sphere of magical power your way. your prayers have been sapped.":
				case "the great olm fires a sphere of magical power your way.":
					this.olm.setPrayer(Prayer.PROTECT_FROM_MAGIC);
					break;
				case "the great olm fires a sphere of accuracy and dexterity your way. your prayers have been sapped.":
				case "the great olm fires a sphere of accuracy and dexterity your way.":
					this.olm.setPrayer(Prayer.PROTECT_FROM_MISSILES);
					break;
			}
		}
	}

	@Subscribe
	private void onProjectileSpawned(ProjectileMoved event) {
		if (!this.inRaid()) {
			return;
		}

		final Projectile projectile = event.getProjectile();
		if (projectile == null || projectile.getRemainingCycles() != (projectile.getEndCycle() - projectile.getStartCycle())) {
			return;
		}

		switch (projectile.getId()) {
			case ProjectileID.OLM_MAGE_ATTACK:
				this.olm.setPrayer(Prayer.PROTECT_FROM_MAGIC);
				break;
			case ProjectileID.OLM_RANGE_ATTACK:
				this.olm.setPrayer(Prayer.PROTECT_FROM_MISSILES);
				break;
			case ProjectileID.OLM_ACID_TRAIL:
				Actor actor = projectile.getInteracting();
				if (actor instanceof Player) {
					this.olm.getVictims().add(new Victim((Player) actor, Victim.Type.ACID));
				}
				break;
		}

		auto.projectileSpawned(projectile);
	}

	@Subscribe
	private void onGraphicChanged(GraphicChanged event) {
		if (!this.inRaid()) {
			return;
		}

		if (!(event.getActor() instanceof Player)) {
			return;
		}

		final Player player = (Player) event.getActor();

		if (player.getGraphic() == SpotanimID.OLM_BURNWITHME_PL_SPOT) {
			this.olm.getVictims().add(new Victim(player, Victim.Type.BURN));
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event) {
		if (!this.inRaid()) {
			return;
		}

		final NPC npc = event.getNpc();

		switch (npc.getId()) {
			case NpcID.RAIDS_TEKTON_WAITING:
			case NpcID.RAIDS_TEKTON_WALKING_STANDARD:
			case NpcID.RAIDS_TEKTON_FIGHTING_STANDARD:
			case NpcID.RAIDS_TEKTON_WALKING_ENRAGED:
			case NpcID.RAIDS_TEKTON_FIGHTING_ENRAGED:
			case NpcID.RAIDS_TEKTON_HAMMERING:
				this.npcContainers.put(npc, new NPCContainer(npc));
				this.tektonAttackTicks = 27;
				break;
			case NpcID.RAIDS_DOGODILE_SUBMERGED://MUTTADILE:
			case NpcID.RAIDS_DOGODILE_JUNIOR://MUTTADILE_7562:
			case NpcID.RAIDS_DOGODILE://MUTTADILE_7563:
			case NpcID.RAIDS_STONEGUARDIANS_LEFT:
			case NpcID.RAIDS_STONEGUARDIANS_RIGHT:
				this.npcContainers.put(npc, new NPCContainer(npc));
				break;
			case NpcID.RAIDS_VANGUARD_DORMANT://VANGUARD:
			case NpcID.RAIDS_VANGUARD_WALKING://VANGUARD_7526:
			case NpcID.RAIDS_VANGUARD_MELEE://VANGUARD_7527:
			case NpcID.RAIDS_VANGUARD_RANGED://VANGUARD_7528:
			case NpcID.RAIDS_VANGUARD_MAGIC://VANGUARD_7529:
				this.vanguards++;
				this.npcContainers.put(npc, new NPCContainer(npc));
				break;
		}
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event) {
		if (!this.inRaid()) {
			return;
		}

		final NPC npc = event.getNpc();

		switch (npc.getId()) {
			case NpcID.RAIDS_TEKTON_WAITING:
			case NpcID.RAIDS_TEKTON_WALKING_STANDARD:
			case NpcID.RAIDS_TEKTON_FIGHTING_STANDARD:
			case NpcID.RAIDS_TEKTON_WALKING_ENRAGED:
			case NpcID.RAIDS_TEKTON_FIGHTING_ENRAGED:
			case NpcID.RAIDS_TEKTON_HAMMERING:
			case NpcID.RAIDS_DOGODILE_SUBMERGED:
			case NpcID.RAIDS_DOGODILE_JUNIOR:
			case NpcID.RAIDS_DOGODILE:
			case NpcID.RAIDS_STONEGUARDIANS_LEFT:
			case NpcID.RAIDS_STONEGUARDIANS_RIGHT:
			case NpcID.RAIDS_STONEGUARDIANS_LEFT_DEAD:
			case NpcID.RAIDS_STONEGUARDIANS_RIGHT_DEAD:
				if (this.npcContainers.remove(event.getNpc()) != null && !this.npcContainers.isEmpty()) {
					this.npcContainers.remove(event.getNpc());
				}
				break;
			case NpcID.RAIDS_VANGUARD_DORMANT:
			case NpcID.RAIDS_VANGUARD_WALKING:
			case NpcID.RAIDS_VANGUARD_MELEE:
			case NpcID.RAIDS_VANGUARD_RANGED:
			case NpcID.RAIDS_VANGUARD_MAGIC:
				if (this.npcContainers.remove(event.getNpc()) != null && !this.npcContainers.isEmpty()) {
					this.npcContainers.remove(event.getNpc());
				}
				this.vanguards--;
				break;
		}
	}

	@Subscribe
	private void onGameTick(GameTick event) {
		if (!this.inRaid()) {
			this.olm.hardRest();
			return;
		}

		this.handleNpcs();

		if (this.olm.isActive()) {
			this.olm.update();
		}

		auto.tick();
	}

	private void handleNpcs() {
		for (NPCContainer npcs : this.getNpcContainers().values()) {
			switch (npcs.getNpc().getId()) {
				case NpcID.RAIDS_TEKTON_WAITING:
				case NpcID.RAIDS_TEKTON_WALKING_STANDARD:
				case NpcID.RAIDS_TEKTON_FIGHTING_STANDARD:
				case NpcID.RAIDS_TEKTON_WALKING_ENRAGED:
				case NpcID.RAIDS_TEKTON_FIGHTING_ENRAGED:
				case NpcID.RAIDS_TEKTON_HAMMERING:
					npcs.setTicksUntilAttack(npcs.getTicksUntilAttack() - 1);
					npcs.setAttackStyle(NPCContainer.Attackstyle.MELEE);
					switch (npcs.getNpc().getAnimation()) {
						case TEKTON_AUTO1:
						case TEKTON_AUTO2:
						case TEKTON_AUTO3:
						case TEKTON_ENRAGE_AUTO1:
						case TEKTON_ENRAGE_AUTO2:
						case TEKTON_ENRAGE_AUTO3:
							this.tektonActive = true;
							if (npcs.getTicksUntilAttack() < 1) {
								npcs.setTicksUntilAttack(4);
							}
							break;
						case TEKTON_FAST_AUTO1:
						case TEKTON_FAST_AUTO2:
							this.tektonActive = true;
							if (npcs.getTicksUntilAttack() < 1) {
								npcs.setTicksUntilAttack(3);
							}
							break;
						case TEKTON_ANVIL:
							this.tektonActive = false;
							this.tektonAttackTicks = 47;
							if (npcs.getTicksUntilAttack() < 1) {
								npcs.setTicksUntilAttack(15);
							}
					}
					break;
				case NpcID.RAIDS_STONEGUARDIANS_LEFT:
				case NpcID.RAIDS_STONEGUARDIANS_RIGHT:
				case NpcID.RAIDS_STONEGUARDIANS_LEFT_DEAD:
				case NpcID.RAIDS_STONEGUARDIANS_RIGHT_DEAD:
					npcs.setTicksUntilAttack(npcs.getTicksUntilAttack() - 1);
					npcs.setAttackStyle(NPCContainer.Attackstyle.MELEE);
					if (npcs.getNpc().getAnimation() == ANIMATION_ID_G1 &&
						npcs.getTicksUntilAttack() < 1) {
						npcs.setTicksUntilAttack(5);
					}
					break;
				case NpcID.RAIDS_VANGUARD_MAGIC:
					if (npcs.getAttackStyle() == NPCContainer.Attackstyle.UNKNOWN) {
						npcs.setAttackStyle(NPCContainer.Attackstyle.MAGE);
					}
					break;
				case NpcID.RAIDS_VANGUARD_RANGED:
					if (npcs.getAttackStyle() == NPCContainer.Attackstyle.UNKNOWN) {
						npcs.setAttackStyle(NPCContainer.Attackstyle.RANGE);
					}
					break;
				case NpcID.RAIDS_VANGUARD_MELEE:
					if (npcs.getAttackStyle() == NPCContainer.Attackstyle.UNKNOWN) {
						npcs.setAttackStyle(NPCContainer.Attackstyle.MELEE);
					}
					break;
			}
		}
		if (this.tektonActive && this.tektonAttackTicks > 0) {
			this.tektonAttackTicks--;
		}
	}

	boolean inRaid() {
		return this.client.getVarbitValue(VarbitID.RAIDS_CLIENT_INDUNGEON) == 1;
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event) {
		if (event.getGameObject() == null) {
			return;
		}

		int id = event.getGameObject().getId();
		switch (id) {
			case Olm.HEAD_GAMEOBJECT_RISING:
			case Olm.HEAD_GAMEOBJECT_READY:
				if (this.olm.getHead() == null) {
					this.olm.startPhase();
				}
				this.olm.setHead(event.getGameObject());
				break;
			case Olm.LEFT_HAND_GAMEOBJECT_RISING:
			case Olm.LEFT_HAND_GAMEOBJECT_READY:
				this.olm.setHand(event.getGameObject());
				break;
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event) {
		if (event.getGameObject() == null) {
			return;
		}

		int id = event.getGameObject().getId();
		if (id == Olm.HEAD_GAMEOBJECT_READY) {
			this.olm.setHead(null);
		}
	}

	Map<NPC, NPCContainer> getNpcContainers() {
		return this.npcContainers;
	}

	Client getClient() {
		return this.client;
	}

	CoxConfig getConfig() {
		return this.config;
	}

	Olm getOlm() {
		return this.olm;
	}

	int getVanguards() {
		return this.vanguards;
	}

	boolean isTektonActive() {
		return this.tektonActive;
	}

	int getTektonAttackTicks() {
		return this.tektonAttackTicks;
	}

	AutoMode getAuto() {
		return this.auto;
	}
}