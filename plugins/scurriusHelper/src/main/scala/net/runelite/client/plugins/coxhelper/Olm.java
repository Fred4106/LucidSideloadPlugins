package net.runelite.client.plugins.coxhelper;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.DynamicObject;
import net.runelite.api.GameObject;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Prayer;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.SpotanimID;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Singleton
public class Olm {
	public static final int HEAD_GAMEOBJECT_RISING = 29880;
	public static final int HEAD_GAMEOBJECT_READY = 29881;
	public static final int LEFT_HAND_GAMEOBJECT_RISING = 29883;
	public static final int LEFT_HAND_GAMEOBJECT_READY = 29884;
	public static final int RIGHT_HAND_GAMEOBJECT_RISING = 29886;
	public static final int RIGHT_HAND_GAMEOBJECT_READY = 29887;

	private final Client client;
	private final CoxPlugin plugin;
	private final CoxConfig config;

	private final List<WorldPoint> healPools = new ArrayList<>();
	private final List<WorldPoint> portals = new ArrayList<>();
	private final Set<Victim> victims = new HashSet<>();
	private int portalTicks = 10;

	private boolean active = false; // in fight
	private boolean firstPhase = false;
	private boolean finalPhase = false;
	private PhaseType phaseType = PhaseType.UNKNOWN;

	private GameObject hand = null;
	private OlmAnimation handAnimation = OlmAnimation.UNKNOWN;
	private GameObject head = null;
	private OlmAnimation headAnimation = OlmAnimation.UNKNOWN;

	private int ticksUntilNextAttack = -1;
	private int attackCycle = 1;
	private int specialCycle = 1;

	private boolean crippled = false;
	private int crippleTicks = 45;

	private Prayer prayer = null;
	private long lastPrayTime = 0;

	@Inject
	private Olm(final Client client, final CoxPlugin plugin, final CoxConfig config) {
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	public void startPhase() {
		firstPhase = !active;
		active = true;
		ticksUntilNextAttack = -1;
		attackCycle = 1;
		specialCycle = 1;
		crippled = false;
		crippleTicks = 45;
		prayer = null;
		lastPrayTime = 0;
		headAnimation = OlmAnimation.UNKNOWN;
		handAnimation = OlmAnimation.UNKNOWN;
	}

	public void hardRest() {
		active = false;
		firstPhase = false;
		finalPhase = false;
		phaseType = PhaseType.UNKNOWN;
		hand = null;
		head = null;
		headAnimation = OlmAnimation.UNKNOWN;
		handAnimation = OlmAnimation.UNKNOWN;
		ticksUntilNextAttack = -1;
		attackCycle = 1;
		specialCycle = 1;
		healPools.clear();
		portals.clear();
		portalTicks = 10;
		victims.clear();
		crippled = false;
		crippleTicks = 45;
		prayer = null;
		lastPrayTime = 0;
	}

	void setPrayer(Prayer pray) {
		prayer = pray;
		lastPrayTime = System.currentTimeMillis();
	}

	void cripple() {
		crippled = true;
		crippleTicks = 45;
	}

	void uncripple() {
		crippled = false;
		crippleTicks = 45;
	}

	public void update() {
		updateVictims();
		updateCrippleSticks();
		updateSpecials();
		incrementTickCycle();
		headAnimations();
		handAnimations();
	}

	public void incrementTickCycle() {
		if (ticksUntilNextAttack == 1) {
			ticksUntilNextAttack = 4;
			incrementAttackCycle();
		} else if (ticksUntilNextAttack != -1) {
			ticksUntilNextAttack--;
		}
	}

	public void incrementAttackCycle() {
		if (attackCycle == 4) {
			attackCycle = 1;
			incrementSpecialCycle();
		} else {
			attackCycle++;
		}
	}

	public void incrementSpecialCycle() {
		if ((specialCycle == 3 && !finalPhase) || specialCycle == 4) {
			specialCycle = 1;
		} else {
			specialCycle++;
		}
	}

	public void specialSync(OlmAnimation currentAnimation) {
		ticksUntilNextAttack = 4;
		attackCycle = 1;
		switch (currentAnimation) {
			case LEFT_HAND_CRYSTALS1:
			case LEFT_HAND_CRYSTALS2:
				specialCycle = 2;
				break;
			case LEFT_HAND_LIGHTNING1:
			case LEFT_HAND_LIGHTNING2:
				specialCycle = 3;
				break;
			case LEFT_HAND_PORTALS1:
			case LEFT_HAND_PORTALS2:
				specialCycle = finalPhase ? 4 : 1;
				break;
			case LEFT_HAND_HEAL1:
			case LEFT_HAND_HEAL2:
				specialCycle = 1;
				break;
		}
	}

	void updateCrippleSticks() {
		if (!crippled) {
			return;
		}

		crippleTicks--;
		if (crippleTicks <= 0) {
			crippled = false;
			crippleTicks = 45;
		}
	}

	void updateVictims() {
		if (victims.size() > 0) {
			victims.forEach(Victim::updateTicks);
			victims.removeIf(victim -> victim.getTicks() <= 0);
		}
	}

	void updateSpecials() {
		healPools.clear();
		portals.clear();
		client.clearHintArrow();

		for (GraphicsObject o : client.getGraphicsObjects()) {
			if (o.getId() == SpotanimID.OLM_PLAYERSWAP_0) {
				portals.add(WorldPoint.fromLocal(client, o.getLocation()));
			}
			if (o.getId() == SpotanimID.OLM_HEALME_SPOTANIM) {
				healPools.add(WorldPoint.fromLocal(client, o.getLocation()));
			}
			if (!portals.isEmpty()) {
				portalTicks--;
				if (portalTicks <= 0) {
					client.clearHintArrow();
					portalTicks = 10;
				}
			}
		}
	}

	private void headAnimations() {
		if (head == null || head.getRenderable() == null) {
			return;
		}

		int olmAnimId = Optional.ofNullable((DynamicObject) head.getRenderable())
			.map(o -> o.getAnimation()).map(a -> a.getId()).orElse(-1).intValue();
		OlmAnimation currentAnimation = OlmAnimation.fromId(olmAnimId);

		if (currentAnimation == headAnimation) {
			return;
		}

		switch (currentAnimation) {
			case HEAD_RISING_2:
			case HEAD_ENRAGED_RISING_2:
				ticksUntilNextAttack = firstPhase ? 6 : 8;
				attackCycle = 1;
				specialCycle = 1;
				break;
			case HEAD_ENRAGED_LEFT:
			case HEAD_ENRAGED_MIDDLE:
			case HEAD_ENRAGED_RIGHT:
				finalPhase = true;
				break;
		}

		headAnimation = currentAnimation;
	}

	private void handAnimations() {
		if (hand == null || hand.getRenderable() == null) {
			return;
		}

		int olmAnimId = Optional.ofNullable((DynamicObject) hand.getRenderable())
			.map(o -> o.getAnimation()).map(a -> a.getId()).orElse(-1);
		OlmAnimation currentAnimation = OlmAnimation.fromId(olmAnimId);

		if (currentAnimation == handAnimation) {
			return;
		}

		switch (currentAnimation) {
			case LEFT_HAND_CRYSTALS1:
			case LEFT_HAND_CRYSTALS2:
			case LEFT_HAND_LIGHTNING1:
			case LEFT_HAND_LIGHTNING2:
			case LEFT_HAND_PORTALS1:
			case LEFT_HAND_PORTALS2:
			case LEFT_HAND_HEAL1:
			case LEFT_HAND_HEAL2:
				specialSync(currentAnimation);
				break;
			case LEFT_HAND_CRIPPLING:
				cripple();
				break;
			case LEFT_HAND_UNCRIPPLING1:
			case LEFT_HAND_UNCRIPPLING2:
				uncripple();
				break;
		}

		handAnimation = currentAnimation;
	}

	public Client getClient() {
		return this.client;
	}

	public CoxPlugin getPlugin() {
		return this.plugin;
	}

	public CoxConfig getConfig() {
		return this.config;
	}

	public List<WorldPoint> getHealPools() {
		return this.healPools;
	}

	public List<WorldPoint> getPortals() {
		return this.portals;
	}

	public Set<Victim> getVictims() {
		return this.victims;
	}

	public int getPortalTicks() {
		return this.portalTicks;
	}

	public boolean isActive() {
		return this.active;
	}

	public boolean isFirstPhase() {
		return this.firstPhase;
	}

	public boolean isFinalPhase() {
		return this.finalPhase;
	}

	public PhaseType getPhaseType() {
		return this.phaseType;
	}

	public GameObject getHand() {
		return this.hand;
	}

	public OlmAnimation getHandAnimation() {
		return this.handAnimation;
	}

	public GameObject getHead() {
		return this.head;
	}

	public OlmAnimation getHeadAnimation() {
		return this.headAnimation;
	}

	public int getTicksUntilNextAttack() {
		return this.ticksUntilNextAttack;
	}

	public int getAttackCycle() {
		return this.attackCycle;
	}

	public int getSpecialCycle() {
		return this.specialCycle;
	}

	public boolean isCrippled() {
		return this.crippled;
	}

	public int getCrippleTicks() {
		return this.crippleTicks;
	}

	public Prayer getPrayer() {
		return this.prayer;
	}

	public long getLastPrayTime() {
		return this.lastPrayTime;
	}

	public void setPortalTicks(int portalTicks) {
		this.portalTicks = portalTicks;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setFirstPhase(boolean firstPhase) {
		this.firstPhase = firstPhase;
	}

	public void setFinalPhase(boolean finalPhase) {
		this.finalPhase = finalPhase;
	}

	public void setPhaseType(PhaseType phaseType) {
		this.phaseType = phaseType;
	}

	public void setHand(GameObject hand) {
		this.hand = hand;
	}

	public void setHandAnimation(OlmAnimation handAnimation) {
		this.handAnimation = handAnimation;
	}

	public void setHead(GameObject head) {
		this.head = head;
	}

	public void setHeadAnimation(OlmAnimation headAnimation) {
		this.headAnimation = headAnimation;
	}

	public void setTicksUntilNextAttack(int ticksUntilNextAttack) {
		this.ticksUntilNextAttack = ticksUntilNextAttack;
	}

	public void setAttackCycle(int attackCycle) {
		this.attackCycle = attackCycle;
	}

	public void setSpecialCycle(int specialCycle) {
		this.specialCycle = specialCycle;
	}

	public void setCrippled(boolean crippled) {
		this.crippled = crippled;
	}

	public void setCrippleTicks(int crippleTicks) {
		this.crippleTicks = crippleTicks;
	}

	public void setLastPrayTime(long lastPrayTime) {
		this.lastPrayTime = lastPrayTime;
	}

	public enum PhaseType {
		FLAME,
		ACID,
		CRYSTAL,
		UNKNOWN,
	}
}
