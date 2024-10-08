package com.fredplugins.attacktimer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.NPCManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.Arrays;

@PluginDescriptor(
		name = "<html><font color=\"#32C8CD\">Freds</font> Attack Timer</html>",
		description = "Shows a visual cue on an overlay every game tick to help timing based activities",
		tags = {"timers", "overlays", "tick", "skilling", "fred4106"}
)
@Singleton
@Slf4j
public class AttackTimerMetronomePlugin extends Plugin {
	public static final int SALAMANDER_SET_ANIM_ID = 952; // Used by all 4 types of salamander https://oldschool.runescape.wiki/w/Salamander
	public static final int EQUIPPING_MONOTONIC = 384; // From empirical testing this clientint seems to always increase whenever the player equips an item
	static final int BloodMoonSetAnimId = 2792;
	private static final int TARGET_DUMMY_ID = 10507;
	public int tickPeriod = 0;
	public int attackDelayHoldoffTicks = 0;
	public AttackState attackState = AttackState.NOT_ATTACKING;
	// The state of the renderer, will lag a few cycles behind the plugin's state. "cycles" in this comment
	// refers to the client.getGameCycle() method, a cycle occurs every 20ms, meaning 30 of them occur per
	// game tick.
	public AttackState renderedState = AttackState.NOT_ATTACKING;
	public Color CurrentColor = Color.WHITE;
	public int DEFAULT_SIZE_UNIT_PX = 25;
	public Dimension DEFAULT_SIZE = new Dimension(DEFAULT_SIZE_UNIT_PX, DEFAULT_SIZE_UNIT_PX);
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ConfigManager configManager;
	@Inject
	private AttackTimerMetronomeTileOverlay overlay;
	@Inject
	private AttackTimerBarOverlay barOverlay;
	@Inject
	private AttackTimerMetronomeConfig config;
	@Inject
	private ItemManager itemManager;
	@Inject
	private Client client;
	@Inject
	private NPCManager npcManager;
	private int uiUnshowDebounceTickCount = 0;
	private Spellbook currentSpellBook = Spellbook.STANDARD;
	private int lastEquippingMonotonicValue = -1;
	private int soundEffectTick = -1;

	// region subscribers
	private int soundEffectId = -1;

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged) {
		if(varbitChanged.getVarbitId() == Varbits.SPELLBOOK) {
			currentSpellBook = Spellbook.fromVarbit(varbitChanged.getValue());
		}
	}

	// onVarbitChanged happens when the user causes some interaction therefore we can't rely on some fixed
	// timing relative to a tick. A player can swap many items in the duration of the a tick.
	@Subscribe
	public void onVarClientIntChanged(VarClientIntChanged varClientIntChanged) {
		final int currentMagicVarBit = client.getVarcIntValue(EQUIPPING_MONOTONIC);
		if(currentMagicVarBit <= lastEquippingMonotonicValue) {
			return;
		}
		lastEquippingMonotonicValue = currentMagicVarBit;

		// This windowing safe guards of from late swaps inside a tick, if we have already rendered the tick
		// then we shouldn't perform another attack.
		boolean preAttackWindow = attackState == AttackState.DELAYED_FIRST_TICK && renderedState != attackState;
		if(preAttackWindow) {
			// "Perform an attack" this is overwrites the last attack since we now know the user swapped
			// "Something" this tick, the equipped weapon detection will pick up specific weapon swaps. Even
			// swapping more than 1 weapon inside a single tick.
			performAttack();
		}
	}

	// endregion

	// onSoundEffectPlayed used to track spell casts, for when the player casts a spell on first tick coming
	// off cooldown, in some cases (e.g. ice barrage) the player will have no animation. Also they don't have
	// a projectile to detect instead :/
	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event) {
		// event.getSource() will be null if the player cast a spell, it's only for area sounds.
		soundEffectTick = client.getTickCount();
		soundEffectId = event.getSoundId();
	}

	@Provides
	AttackTimerMetronomeConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(AttackTimerMetronomeConfig.class);
	}

	private int getItemIdFromContainer(ItemContainer container, int slotID) {
		if(container == null) {
			return -1;
		}
		final Item item = container.getItem(slotID);
		return (item != null) ? item.getId() : -1;
	}

	private int getWeaponId() {
		return getItemIdFromContainer(client.getItemContainer(InventoryID.EQUIPMENT),
				EquipmentInventorySlot.WEAPON.getSlotIdx());
	}

	private ItemStats getWeaponStats(int weaponId) {
		return itemManager.getItemStats(weaponId, false);
	}

	private AttackStyle getAttackStyle() {
		final int currentAttackStyleVarbit = client.getVarpValue(VarPlayer.ATTACK_STYLE);
		final int currentEquippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
		AttackStyle[] attackStyles = WeaponType.getWeaponType(currentEquippedWeaponTypeVarbit).getAttackStyles();

		if(currentAttackStyleVarbit < attackStyles.length) {
			return attackStyles[currentAttackStyleVarbit];
		}

		return AttackStyle.ACCURATE;
	}

	private int applyRangedAndMeleeRelicSpeed(int baseSpeed) {
		if(baseSpeed >= 4) {
			return baseSpeed / 2;
		} else {
			return (baseSpeed + 1) / 2;
		}
	}

	private boolean isRedKerisSpecAnimation(AnimationData animation) {
		return animation == AnimationData.MELEE_RED_KERIS_SPEC;
	}

	private boolean getBloodMoonProc() {
		return client.getLocalPlayer().hasSpotAnim(BloodMoonSetAnimId);
	}

	private boolean getSalamanderAttack() {
		return client.getLocalPlayer().hasSpotAnim(SALAMANDER_SET_ANIM_ID);
	}

	private int adjustSpeedForLeaguesIfApplicable(int baseSpeed) {
		int leagueRelicVarbit = 0;
		if(client.getWorldType().contains(WorldType.SEASONAL)) {
			leagueRelicVarbit = client.getVarbitValue(Varbits.LEAGUE_RELIC_4);
		}

		AttackStyle attackStyle = getAttackStyle();

		switch(leagueRelicVarbit) {
			case 0:
				// No league relic active - player does not have t4 relic or is not in leagues.
				return baseSpeed;
			case 1:
				// Archer's Embrace (ranged).
				if(attackStyle == AttackStyle.RANGING ||
						attackStyle == AttackStyle.LONGRANGE) {
					return applyRangedAndMeleeRelicSpeed(baseSpeed);
				}
				break;
			case 2:
				// Brawler's Resolve (melee)
				if(attackStyle == AttackStyle.ACCURATE ||
						attackStyle == AttackStyle.AGGRESSIVE ||
						attackStyle == AttackStyle.CONTROLLED ||
						attackStyle == AttackStyle.DEFENSIVE) {
					return applyRangedAndMeleeRelicSpeed(baseSpeed);
				}
				break;
			case 3:
				// Superior Sorcerer (magic)
				if(attackStyle == AttackStyle.CASTING ||
						attackStyle == AttackStyle.DEFENSIVE_CASTING) {
					return 2;
				}
				break;
		}

		return baseSpeed;
	}

	private void setAttackDelay() {
		int weaponId = getWeaponId();
		AnimationData curAnimation = AnimationData.fromId(client.getLocalPlayer().getAnimation());
		PoweredStaves stave = PoweredStaves.getPoweredStaves(weaponId, curAnimation);
		boolean matchesSpellbook = matchesSpellbook(curAnimation);
		attackDelayHoldoffTicks = getWeaponSpeed(weaponId, stave, curAnimation, matchesSpellbook);
	}

	// matchesSpellbook tries two methods, matching the animation the spell book based on the enum of
	// pre-coded matches, and then the second set of matches against the known sound id of the spell (which
	// unfortunately doesn't work if the player has them disabled).
	private boolean matchesSpellbook(AnimationData curAnimation) {
		if(curAnimation != null && curAnimation.matchesSpellbook(currentSpellBook)) {
			return true;
		}
		if(client.getTickCount() == soundEffectTick) {
			return CastingSoundData.getSpellBookFromId(soundEffectId) == currentSpellBook;
		}
		return false;
	}

	private int getWeaponSpeed(int weaponId, PoweredStaves stave, AnimationData curAnimation, boolean matchesSpellbook) {
		if(stave != null && stave.getAnimations().contains(curAnimation)) {
			// We are currently dealing with a staves in which case we can make decisions based on the
			// spellbook flag. We can only improve this by using a deprecated API to check the projectile
			// matches the stave rather than a manual spell, but this is good enough for now.
			return adjustSpeedForLeaguesIfApplicable(4);
		}

		if(matchesSpellbook && isManualCasting(curAnimation)) {
			// You can cast with anything equipped in which case we shouldn't look to invent for speed, it will instead always be 5.
			return adjustSpeedForLeaguesIfApplicable(5);
		}

		ItemStats weaponStats = getWeaponStats(weaponId);
		if(weaponStats == null) {
			return adjustSpeedForLeaguesIfApplicable(4); // Assume barehanded == 4t
		}
		ItemEquipmentStats e = weaponStats.getEquipment();
		int speed = e.getAspeed();

		if(getAttackStyle() == AttackStyle.RANGING && client.getVarpValue(VarPlayer.ATTACK_STYLE) == 1) { // Hack for index 1 => rapid
			speed -= 1; // Assume ranging == rapid. Also works for salamanders which attack 1 tick faster when using the ranged style
		}
		if(getBloodMoonProc()) { // Similar hack as rapid, blood moon saves a tick when it proc's
			speed -= 1;
		}

		if(isRedKerisSpecAnimation(curAnimation)) {
			speed += 4; // If the spec missed we are just wrong by 4-ticks IDC, requires spec tracking code similar to the spec plugin if we want this to be correct when we miss.
		}

		return adjustSpeedForLeaguesIfApplicable(speed); // Deadline for next available attack.
	}

	private boolean isPlayerAttacking() {
		int animationId = client.getLocalPlayer().getAnimation();
		if(AnimationData.isBlockListAnimation(animationId)) {
			return false;
		}

		// Not walking is either any player animation or the edge cases which don't trigger an animation, e.g Salamander.
		boolean notWalking = animationId != -1 || getSalamanderAttack();

		// Testing if we are attacking by checking the target is more future
		// proof to new weapons which don't need custom code and the weapon
		// stats are enough.
		Actor target = client.getLocalPlayer().getInteracting();
		if(target != null && (target instanceof NPC)) {
			final NPC npc = (NPC) target;
			boolean containsAttackOption = Arrays.stream(npc.getComposition().getActions()).anyMatch("Attack"::equals);
			Integer health = npcManager.getHealth(npc.getId());
			boolean hasHealthAndLevel = health != null && health > 0 && target.getCombatLevel() > 0;
			boolean attackingNPC = hasHealthAndLevel || npc.getId() == TARGET_DUMMY_ID || containsAttackOption;
			// just having a target is not enough the player may be out of range, we must wait for any
			// animation which isn't running/walking/etc
			return attackingNPC && notWalking;
		}
		if(target != null && (target instanceof Player)) {
			return notWalking;
		}

		AnimationData fromId = AnimationData.fromId(animationId);
		if(fromId == AnimationData.RANGED_BLOWPIPE || fromId == AnimationData.RANGED_BLAZING_BLOWPIPE) {
			// These two animations are the only ones which exceed the duration of their attack cooldown (when
			// on rapid), so in this case DO NOT fall back the animation as it is un-reliable.
			return false;
		}
		// fall back to animations.
		return fromId != null;
	}

	private boolean isManualCasting(AnimationData curId) {
		// If you use a weapon like a blow pipe which has an animation longer than it's cool down then cast an
		// ancient attack it wont have an animation at all. We can therefore need to detect this with a list
		// of sounds instead. This obviously doesn't work if the player is muted. ATM I can't think of a way
		// to detect this type of attack as a cast, only sound is an indication that the player is on
		// cooldown, melee attacks, etc will trigger an animation overwriting the last frame of the blowpipe's
		// idle animation.
		boolean castingFromSound = client.getTickCount() == soundEffectTick && CastingSoundData.isCastingSound(soundEffectId);
		boolean castingFromAnimation = AnimationData.isManualCasting(curId);
		return castingFromSound || castingFromAnimation;
	}

	private void performAttack() {
		attackState = AttackState.DELAYED_FIRST_TICK;
		setAttackDelay();
		tickPeriod = attackDelayHoldoffTicks;
		uiUnshowDebounceTickCount = 1;
	}

	public int getTicksUntilNextAttack() {
		return 1 + Math.max(attackDelayHoldoffTicks, 0);
	}

	public int getWeaponPeriod() {
		return tickPeriod;
	}

	public boolean isAttackCooldownPending() {
		return attackState == AttackState.DELAYED
				|| attackState == AttackState.DELAYED_FIRST_TICK
				|| uiUnshowDebounceTickCount > 0;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if(event.getType() != ChatMessageType.SPAM) {
			return;
		}

		final String message = event.getMessage();

		if(message.startsWith("You eat") ||
				message.startsWith("You drink the wine")) {
			int KARAMBWAN_ATTACK_DELAY_TICKS = 2;
			int DEFAULT_FOOD_ATTACK_DELAY_TICKS = 3;
			int attackDelay = (message.toLowerCase().contains("karambwan")) ?
					KARAMBWAN_ATTACK_DELAY_TICKS :
					DEFAULT_FOOD_ATTACK_DELAY_TICKS;

			if(attackState == AttackState.DELAYED) {
				attackDelayHoldoffTicks += attackDelay;
			}
		}
	}

	// onInteractingChanged is the driver for detecting if the player attacked out side the usual tick window
	// of the onGameTick events.
	@Subscribe
	public void onInteractingChanged(InteractingChanged interactingChanged) {
		Actor source = interactingChanged.getSource();
		Actor target = interactingChanged.getTarget();

		Player p = client.getLocalPlayer();

		if(source.equals(p) && (target instanceof NPC)) {
			switch(attackState) {
				case NOT_ATTACKING:
					// If not previously attacking, this action can result in a queued attack or
					// an instant attack. If its queued, don't trigger the cooldown yet.
					if(isPlayerAttacking()) {
						performAttack();
					}
					break;
				case DELAYED_FIRST_TICK:
					// fallthrough
				case DELAYED:
					// Don't reset tick counter or tick period.
					break;
			}
		}
	}

	@Subscribe(priority = 9000)
	public void onGameTick(GameTick tick) {
		boolean isAttacking = isPlayerAttacking();
		switch(attackState) {
			case NOT_ATTACKING:
				if(isAttacking) {
					performAttack(); // Sets state to DELAYED_FIRST_TICK.
				} else {
					uiUnshowDebounceTickCount--;
				}
				break;
			case DELAYED_FIRST_TICK:
				// we stay in this state for one tick to allow for 0-ticking
				attackState = AttackState.DELAYED;
				// fallthrough
			case DELAYED:
				if(attackDelayHoldoffTicks <= 0) { // Eligible for a new attack
					if(isAttacking) {
						performAttack();
					} else {
						attackState = AttackState.NOT_ATTACKING;
					}
				}
		}
		attackDelayHoldoffTicks--;

//		if(config.enablePrayerFlicking()) {
////			Optional<Boolean> targetState = Optional.empty();
//			boolean isPrayerActive = client.isPrayerActive(config.attackPrayer());
//
//			log.info("State: attackState={}, isAttacking={}, attackDelayHoldoffTicks={}, isAttackCooldownPending()={}, getTicksUntilNextAttack()={}, uiUnshowDebounceTickCount={}", attackState, isAttacking, attackDelayHoldoffTicks, isAttackCooldownPending(), getTicksUntilNextAttack(), uiUnshowDebounceTickCount);
//			if ((getTicksUntilNextAttack() > config.prayerOnAtTicksRemaining()/* || attackState == AttackState.NOT_ATTACKING*/) && isPrayerActive) {
//				log.info("Disabling {} with {} ticks till next attack.", config.attackPrayer(), getTicksUntilNextAttack());
//				PrayerInteraction.setPrayerState(config.attackPrayer(), false);
//			} else if (getTicksUntilNextAttack() <= config.prayerOnAtTicksRemaining()  && !isPrayerActive) {
//				log.info("Enabling {} with {} ticks till next attack.", config.attackPrayer(), getTicksUntilNextAttack());
//				PrayerInteraction.setPrayerState(config.attackPrayer(), true);
//			}
//
////			targetState.ifPresent(ts -> {
////				log.info("Settings {} with state {} to {} with {} ticks till next attack.", config.attackPrayer(), isPrayerActive,  ts, getTicksUntilNextAttack());
////				PrayerInteraction.setPrayerState(config.attackPrayer(), ts);
////			});
//		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if(event.getGroup().equals("Fredsattacktimermetronome")) {
			attackDelayHoldoffTicks = 0;
		}
	}

	@Override
	protected void startUp() throws Exception {
		overlayManager.add(overlay);
		overlay.setPreferredSize(DEFAULT_SIZE);
		overlayManager.add(barOverlay);
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
		overlayManager.remove(barOverlay);
		attackDelayHoldoffTicks = 0;
	}

	public enum AttackState {
		NOT_ATTACKING,
		DELAYED_FIRST_TICK,
		DELAYED,
	}
}
