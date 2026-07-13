package com.drakanhelper;

import ch.qos.logback.classic.Level;
import com.fredplugins.common.constants.FontTypes;
import com.fredplugins.common.utils.ReflectionUtils$;
import com.fredplugins.common.utils.TWorldPoint;
import com.google.inject.Provides;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Inject;

import com.google.inject.Singleton;
import ethanApiPlugin.EthanApiPlugin;
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils;
import net.runelite.api.ActorSpotAnim;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for Lowerniel Drakan (Untethered, The Blood Moon Rises finale).
 * Danger tiles are read from the live blood-mark graphics (ground truth) rather than predicted,
 * because Drakan re-faces to track the player. Overlay handles the tile rendering; this class
 * tracks the boss, the attack state (spear combo / radial AoE), the incoming Pray-Magic
 * projectiles, and the current phase.
 */
@PluginDescriptor(
	name = "Drakan Helper",
	description = "Dodge tiles, AoE safe-spots and prayer warnings for Lowerniel Drakan",
	tags = {"drakan", "blood", "moon", "vampyre", "boss", "morytania"},
	enabledByDefault = false
)
@PluginDependency(EthanApiPlugin.class)
@Singleton
public class DrakanHelperPlugin extends Plugin
{
	private final static Logger log;
	static {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(DrakanHelperPlugin.class)).setLevel(Level.DEBUG);
		log = LoggerFactory.getLogger(DrakanHelperPlugin.class);
	}

	static final int DRAKAN_ID = 16204;
	static final int DANGER_MARK_GFX = 2953;

	private static final int BLOOM_ANIM = 14325;
	// Directional lunge strikes: 14321 = strike on Drakan's LEFT tile, 14323 = his RIGHT tile.
	private static final int LUNGE_LEFT_ANIM = 14321;
	private static final int LUNGE_RIGHT_ANIM = 14323;
	// Combo wind-up (non-directional). 14317 fires exactly 3 ticks before the first strike, 14319 one tick before.
	private static final int COMBO_WINDUP_ANIM = 14317;
	private static final int COMBO_PRESTRIKE_ANIM = 14319;
	private static final Set<Integer> BLOOM_WINDUP_SPOTANIM = Set.of(3933, 3943, 3939);
	private static final Set<Integer> MAGIC_PROJECTILES = Set.of(3874, 3875, 3876, 3877, 3878, 3879);

	// Forecast flash spot-anims, queued on Drakan at combo wind-up — ONE INSTANCE PER STRIKE,
	// side encoded in the id (verified over 90 recon combos; in every pair the lower id = right):
	// 3910 / 3920 = strike on his LEFT tile, 3909 / 3919 = his RIGHT tile.
	private static final Set<Integer> FLASH_LEFT = Set.of(3910, 3920);
	private static final Set<Integer> FLASH_RIGHT = Set.of(3909, 3919);
	// The radial AoE ("bloom") telegraph is its own 8-id cluster, one per segment.
	private static final int BLOOM_CLUSTER_MIN = 3921;
	private static final int BLOOM_CLUSTER_MAX = 3928;

	// Wind-up specials. Orientation locks at the wind-up (nearest cardinal = final strike facing,
	// 12/12 recon casts), so the safe zone can be painted the moment the wind-up starts.
	private static final int FB_WINDUP_ANIM = 14327;   // front/back wave: strike +4 ticks, safe = his sides
	private static final int SEMI_WINDUP_ANIM = 14333; // semicircle: strike +5 ticks, safe = behind him
	// P3 vanish/reappear: reappear + 14311 next tick = blood barrage (Pray Magic covers it);
	// reappear with NO 14311 = a CHARGE along the line to the player (~2 tiles/tick, lands in
	// ~3-4 ticks) — safe = perpendicular sidestep, running at/away stays on the line.
	private static final int BLOOD_BARRAGE_ANIM = 14311;

	@Inject
	private Client client;
	@Inject
	private DrakanHelperConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private DrakanHelperOverlay overlay;

	@Inject
	private EthanApiPlugin ethans;

	private NPC boss;
	private int comboTicks;
	private int bloomTicks;
	private int prayMagicTicks;
	private int lungeTicks;
	private int comboIncomingTicks;
	private int dangerLeft = -1; // 1 = Drakan's left tile is the struck (danger) side, 0 = his right tile

	// ---- combo forecast state ----
	// each element: {startCycle, side(0=L,1=R), discoveryIndex}
	private final List<long[]> flashes = new ArrayList<>();
	private final Set<Long> seenFlashKeys = new HashSet<>();
	private long bloomStartCycle = Long.MIN_VALUE;
	private int flashDiscovery;
	private int forecastTicks;
	private int strikesConsumed;

	// ---- wind-up special state ----
	private int specialType;          // 0 = none, 1 = front/back (safe: sides), 2 = semicircle (safe: behind)
	private int specialImpactTicks;   // ticks until the strike lands (goes negative during wave linger)
	private int specialLingerTicks;   // how long past impact the zone stays painted (traveling waves)
	private int specialOrientation;   // boss orientation locked at wind-up

	// ---- dodge plan: world-locked click tiles for the forecast combo ----
	// Built once per combo (and extended as late flashes surface); tiles do NOT track the player.
	// Only a genuine deviation from the path triggers a re-plan of the remaining steps.
	private final List<LocalPoint> plannedTiles = new ArrayList<>();
	private String plannedChain = "";
	private LocalPoint planAnchor;    // player tile when the plan was built (expected pos before dodge 1)
	private LocalPoint planC;         // boss anchor locked at plan build — he LUNGES during strikes,
	                                  // so his live position must never be used once dodging begins
	private int[] planF;              // facing frame locked at plan build
	private int[] planL;
	private int planRank;             // column rank of the last planned tile
	private int planLane;             // lane of the last planned tile (+1 = his left, -1 = his right)
	private int offPathStreak;        // consecutive ticks the player has been off the path
	private int recoveryTicks;        // post-hit window in which the plan re-tracks the player
	private int comboStartTick = Integer.MIN_VALUE;  // client tick of the wind-up anim
	private int comboStartCycle = Integer.MIN_VALUE; // game cycle (20ms) of the wind-up anim
	private boolean beatSnapped;                     // one-time re-anchor at the first beat done
	private boolean pendingHitReplan;                // a hit landed — rebuild unconditionally next tick

	// ---- P3 reappear-charge state ----
	private boolean bossVanished;
	private int chargeTicks;
	private LocalPoint chargeAnchor;  // player tile at the reappear
	private int[] chargePerp;         // perpendicular (sidestep) axis

	@Provides
	DrakanHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DrakanHelperConfig.class);
	}

	private Font cachedFont = null;//config.fontType().//FontManager.getRunescapeFont.deriveFont(if (config.getFontBold) 1 else 0, config.getFontSize)

	public Font getFont() {
		if(cachedFont == null) {
			cachedFont = (config.fontType() != FontTypes.REGULAR) ?
				new Font(config.fontType().toString(), config.fontStyle().getFont(), config.fontSize()) :
				FontManager.getRunescapeFont().deriveFont(config.fontStyle().getFont(), config.fontSize());
		}
		return cachedFont;
	}
	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		reset();
	}

	private void reset()
	{
		boss = null;
		comboTicks = 0;
		bloomTicks = 0;
		prayMagicTicks = 0;
		lungeTicks = 0;
		comboIncomingTicks = 0;
		dangerLeft = -1;
		resetForecast();
	}

	private void resetForecast()
	{
		flashes.clear();
		seenFlashKeys.clear();
		bloomStartCycle = Long.MIN_VALUE;
		flashDiscovery = 0;
		forecastTicks = 0;
		strikesConsumed = 0;
		specialType = 0;
		beatSnapped = false;
		pendingHitReplan = false;
		chargeTicks = 0;
		clearPlan();
	}

	private void clearPlan()
	{
		plannedTiles.clear();
		plannedChain = "";
		planAnchor = null;
		planC = null;
		offPathStreak = 0;
		recoveryTicks = 0;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned e)
	{
		if (e.getNpc().getId() != DRAKAN_ID)
		{
			return;
		}
		final boolean reappear = bossVanished;
		bossVanished = false;
		boss = e.getNpc();
		if (!reappear)
		{
			return;
		}
		// Reappear: assume a charge until the blood barrage (14311) reveals itself next tick.
		final Player local = client.getLocalPlayer();
		final LocalPoint p = local != null ? local.getLocalLocation() : null;
		final LocalPoint b = boss.getLocalLocation();
		if (p == null || b == null)
		{
			return;
		}
		final int dx = p.getX() - b.getX();
		final int dy = p.getY() - b.getY();
		// charge axis = dominant approach axis; sidestep axis = the other one
		chargePerp = (Math.abs(dx) >= Math.abs(dy)) ? new int[]{0, 1} : new int[]{1, 0};
		chargeAnchor = snapToTile(p);
		chargeTicks = 4;
		log.debug("chargePerp={{}, {}}, chargeAnchor={}, chargeTicks={}", chargePerp[0],chargePerp[1], chargeAnchor, chargeTicks);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned e)
	{
		if (e.getNpc() == boss)
		{
			boss = null;
			bossVanished = true;
		}
	}
	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if(e.getGroup().equalsIgnoreCase("drakanhelper")) {
			switch (e.getKey()) {
				case "fontType":
				case "fontSize":
				case "fontStyle":
					cachedFont = null;
					break;
				default:
			}
		}
	}

	@Subscribe
	public void onNpcChanged(NpcChanged e)
	{
		log.debug("NpcChanged: oldId = {}, newId = {}", ReflectionUtils$.MODULE$.getNpcName(e.getOld().getId()), ReflectionUtils$.MODULE$.getNpcName(e.getNpc().getId()));
		if (e.getNpc().getId() == DRAKAN_ID)
		{
			boss = e.getNpc();
		}
		else if (e.getNpc() == boss)
		{
			boss = null;
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged e)
	{
		if (boss == null || e.getActor() != boss)
		{
			return;
		}
		final int a = boss.getAnimation();
		if (a == BLOOM_ANIM)
		{
			bloomTicks = 2;
			strikesConsumed++;
		}
		else if (a == LUNGE_LEFT_ANIM)
		{
			dangerLeft = 1;
			lungeTicks = 2;
			comboTicks = 2;
			comboIncomingTicks = 0;
			strikesConsumed++;
		}
		else if (a == LUNGE_RIGHT_ANIM)
		{
			dangerLeft = 0;
			lungeTicks = 2;
			comboTicks = 2;
			comboIncomingTicks = 0;
			strikesConsumed++;
		}
		else if (a == COMBO_WINDUP_ANIM)
		{
			comboTicks = 2;
			comboIncomingTicks = 3;
			// a new combo's forecast flashes are queued now — start a fresh capture window
			resetForecast();
			forecastTicks = 14;
			specialType = 0;
			comboStartTick = client.getTickCount();
			comboStartCycle = client.getGameCycle();
		}
		else if (a == COMBO_PRESTRIKE_ANIM)
		{
			comboTicks = 2;
			comboIncomingTicks = Math.max(comboIncomingTicks, 1);
		}
		else if (a == FB_WINDUP_ANIM)
		{
			specialType = 1;
			specialImpactTicks = 4;
			specialLingerTicks = 4;
			specialOrientation = boss.getOrientation();
			forecastTicks = 0;
		}
		else if (a == SEMI_WINDUP_ANIM)
		{
			specialType = 2;
			specialImpactTicks = 5;
			specialLingerTicks = 3;
			specialOrientation = boss.getOrientation();
			forecastTicks = 0;
		}
		else if (a == BLOOD_BARRAGE_ANIM)
		{
			// reappear resolved as the blood barrage — no charge; the Pray Magic flash covers it
			chargeTicks = 0;
		}
		else if (a != -1)
		{
			// any non-combo animation (normal melee etc.) ends the forecast display;
			// the specials' own strike anims (14329/14335) land here without clearing their zone
			forecastTicks = 0;
		}
	}

	/**
	 * Collects the combo forecast: at wind-up Drakan queues one flash spot-anim INSTANCE per
	 * upcoming strike, side encoded in the id. Instances are keyed by (id, startCycle) so a
	 * restarted id (same side twice, e.g. an RR chain) counts as a new flash. Sorting by
	 * startCycle gives the strike order (instances are scheduled with staggered start delays).
	 */
	@Subscribe
	public void onClientTick(ClientTick e)
	{
		if (boss == null || forecastTicks <= 0)
		{
			return;
		}
		for (final ActorSpotAnim s : boss.getSpotAnims())
		{
			final int id = s.getId();
			final boolean left = FLASH_LEFT.contains(id);
			final boolean right = FLASH_RIGHT.contains(id);
			final boolean cluster = id >= BLOOM_CLUSTER_MIN && id <= BLOOM_CLUSTER_MAX;
			if (!left && !right && !cluster)
			{
				continue;
			}
			final long key = ((long) id << 32) ^ (s.getStartCycle() & 0xFFFFFFFFL);
			if (!seenFlashKeys.add(key))
			{
				continue;
			}
			if (cluster)
			{
				bloomStartCycle = bloomStartCycle == Long.MIN_VALUE
					? s.getStartCycle() : Math.min(bloomStartCycle, s.getStartCycle());
			}
			else
			{
				flashes.add(new long[]{s.getStartCycle(), left ? 0 : 1, flashDiscovery++});
			}
		}
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged e)
	{
		if (boss == null || e.getActor() != boss)
		{
			return;
		}
		if (BLOOM_WINDUP_SPOTANIM.contains(boss.getGraphic()))
		{
			bloomTicks = Math.max(bloomTicks, 3);
		}
	}

	@Subscribe
	public void onGraphicChangedAfterImages(GraphicChanged e)
	{
		if(e.getActor() instanceof NPC) {
			NPC n = (NPC) e.getActor();
			List<ActorSpotAnim> spotAnimations = StreamSupport.stream(n.getSpotAnims().spliterator(), false).collect(Collectors.toList());
			String spots = spotAnimations.stream().map(sa -> {
				String san = ReflectionUtils$.MODULE$.getSpotAnimationName(sa.getId());
				return san;
			}).collect(Collectors.joining(", ", "[", "]"));
			log.debug("graphicsChanged: npcId={}, 1x={}, loc={}, spots={}", n.getId(), n.getIndex(), TWorldPoint.get(n.getWorldLocation()), spots);
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved e)
	{
		if (boss == null)
		{
			return;
		}
		final Projectile p = e.getProjectile();
		if (MAGIC_PROJECTILES.contains(p.getId()) && p.getInteracting() == client.getLocalPlayer())
		{
			final int ticks = (int) Math.ceil(p.getRemainingCycles() / 30.0);
			prayMagicTicks = Math.max(prayMagicTicks, ticks);
		}
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		if (forecastTicks > 0)
		{
			forecastTicks--;
		}
		if (specialType != 0)
		{
			specialImpactTicks--;
			if (specialImpactTicks < -specialLingerTicks)
			{
				specialType = 0;
			}
		}
		if (comboTicks > 0)
		{
			comboTicks--;
		}
		if (bloomTicks > 0)
		{
			bloomTicks--;
		}
		if (lungeTicks > 0)
		{
			lungeTicks--;
		}
		if (comboIncomingTicks > 0)
		{
			comboIncomingTicks--;
		}
		if (chargeTicks > 0)
		{
			chargeTicks--;
		}
		if (prayMagicTicks > 0)
		{
			prayMagicTicks--;
		}

		updateDodgePlan();
	}

	@Subscribe(priority = 10)
	public void preGameTick(GameTick e)
	{
		if(boss == null) return;
		Prayer protectPrayer = prayMagic() ? Prayer.PROTECT_FROM_MAGIC : Prayer.PROTECT_FROM_MELEE;
		CombatUtils.activatePrayers(protectPrayer, Prayer.PIETY);
	}

	/**
	 * Maintains the dodge plan in two phases. FLUID (before the first strike): the plan tracks the
	 * player — rebuilt from their current tile whenever they reposition — until they commit to the
	 * path (step within 1 tile of the first click tile). LOCKED (dodging has begun): the boss
	 * anchor, frame, and tiles are frozen — Drakan lunges 2-3 tiles during the strike phase, so
	 * his live position must never re-enter the math. A mid-combo re-anchor happens only if the
	 * player stays off the path for 2 consecutive ticks (a slightly late roll lands within 1).
	 */
	private void updateDodgePlan()
	{
		if (boss == null || forecastTicks <= 0)
		{
			clearPlan();
			return;
		}
		final String chain = forecastChain();
		if (chain.isEmpty())
		{
			clearPlan();
			return;
		}
		final Player local = client.getLocalPlayer();
		if (local == null || local.getLocalLocation() == null)
		{
			return;
		}
		final LocalPoint p = snapToTile(local.getLocalLocation());
		final boolean fluid = strikesConsumed == 0;

		if (plannedChain.isEmpty())
		{
			buildFresh(chain, p);
		}
		else if (!chain.startsWith(plannedChain))
		{
			// chain re-ordered (e.g. a late bloom slot inserted mid-chain)
			if (fluid || planC == null || plannedTiles.size() < strikesConsumed || strikesConsumed < 1)
			{
				buildFresh(chain, p);
			}
			else
			{
				rebuildFrom(chain, strikesConsumed,
					rankOf(plannedTiles.get(strikesConsumed - 1)), laneOf(plannedTiles.get(strikesConsumed - 1)));
			}
		}
		else if (chain.length() > plannedChain.length())
		{
			// chain grew — extend from the tail state, against the LOCKED boss anchor
			appendPlanSteps(chain, plannedChain.length());
		}
		plannedChain = chain;

		if (fluid)
		{
			// FROZEN through the wind-up against small shuffling (1-tile melee micro-moves made
			// the tiles chase the player), but a REAL reposition (2+ tiles from the anchor)
			// re-anchors, and the first beat always takes one final snap to the player's actual
			// position as dodging begins.
			if (!beatSnapped && clickNow())
			{
				beatSnapped = true;
				if (!p.equals(planAnchor))
				{
					buildFresh(chain, p);
				}
			}
			else if (!beatSnapped && planAnchor != null && chebTiles(p, planAnchor) >= 2)
			{
				buildFresh(chain, p);
			}
			offPathStreak = 0;
		}
		else if (strikesConsumed < plannedTiles.size())
		{
			if (pendingHitReplan)
			{
				// a dodge failed — re-anchor the remaining steps unconditionally, right now
				// (the earlier <=2 damping gate cancelled this exact correction: after a missed
				// dodge the next tile is exactly 2 away, so it never fired)
				pendingHitReplan = false;
				rebuildFrom(chain, strikesConsumed, playerRank(p), latSign(p));
				offPathStreak = 0;
				return;
			}
			if (recoveryTicks > 0)
			{
				// residual post-hit window: keep re-anchoring only while genuinely out of reach
				recoveryTicks--;
				if (chebTiles(p, plannedTiles.get(strikesConsumed)) <= 2)
				{
					recoveryTicks = 0;
				}
				else
				{
					rebuildFrom(chain, strikesConsumed, playerRank(p), latSign(p));
				}
				offPathStreak = 0;
				return;
			}
			final LocalPoint expected = plannedTiles.get(strikesConsumed - 1);
			final LocalPoint next = plannedTiles.get(strikesConsumed);
			if (Math.min(chebTiles(p, expected), chebTiles(p, next)) > 1)
			{
				// 2-tick grace: a slightly late roll completes within a tick and must not re-plan
				if (++offPathStreak >= 2)
				{
					rebuildFrom(chain, strikesConsumed, playerRank(p), latSign(p));
					offPathStreak = 0;
				}
			}
			else
			{
				offPathStreak = 0;
			}
		}
	}

	/**
	 * A hitsplat on the player mid-combo means a dodge failed — the correct next click tile now
	 * derives from wherever they end up. Rather than one rebuild from a mid-roll position (an
	 * unstable anchor), this opens a short RECOVERY window in which the plan re-tracks the player
	 * each game tick, freezing again the moment they commit to the corrected next tile.
	 */
	@Subscribe
	public void onHitsplatApplied(HitsplatApplied e)
	{
		if (e.getActor() != client.getLocalPlayer())
		{
			return;
		}
		if (boss == null || forecastTicks <= 0 || strikesConsumed == 0
			|| planC == null || plannedChain.isEmpty() || plannedTiles.size() <= strikesConsumed)
		{
			return;
		}
		pendingHitReplan = true;
		recoveryTicks = 2;
		offPathStreak = 0;
	}

	/**
	 * Full rebuild from the player's current tile; boss anchor and frame are (re)locked now.
	 * The frame is derived from the PLAYER's direction rather than boss orientation: Drakan always
	 * turns to face the player before striking, but his orientation value lags mid-turn at wind-up
	 * (observed 90-180° stale), while the player's bearing predicts his final facing reliably.
	 */
	private void buildFresh(String chain, LocalPoint p)
	{
		final LocalPoint c = boss.getLocalLocation();
		if (c == null)
		{
			return;
		}
		plannedTiles.clear();
		planC = c;
		final int dx = p.getX() - c.getX();
		final int dy = p.getY() - c.getY();
		if (dx == 0 && dy == 0)
		{
			planF = cardinal(boss.getOrientation());
		}
		else if (Math.abs(dx) >= Math.abs(dy))
		{
			planF = new int[]{dx >= 0 ? 1 : -1, 0};
		}
		else
		{
			planF = new int[]{0, dy >= 0 ? 1 : -1};
		}
		planL = new int[]{-planF[1], planF[0]};
		planAnchor = p;
		planRank = playerRank(p);
		planLane = latSign(p);
		appendPlanSteps(chain, 0);
	}

	/** Rebuild steps from..end against the LOCKED anchor, keeping earlier tiles untouched. */
	private void rebuildFrom(String chain, int from, int anchorRank, int anchorLane)
	{
		while (plannedTiles.size() > from)
		{
			plannedTiles.remove(plannedTiles.size() - 1);
		}
		planRank = anchorRank;
		planLane = anchorLane;
		appendPlanSteps(chain, from);
	}

	private void appendPlanSteps(String chain, int from)
	{
		if (planC == null)
		{
			return;
		}
		final int t = Perspective.LOCAL_TILE_SIZE;
		final int h = t / 2;
		// Plain retreat cadence for every slot, flares included. Empirically (90%-run analysis)
		// the dodge is the ROLL, not the tile: identical positions were clean when rolling and
		// hit when standing, at every rank. The tiles keep the retreat orderly and in-column;
		// the click BEAT (clickNow) is what actually procs the roll.
		for (int i = from; i < chain.length(); i++)
		{
			final char s = chain.charAt(i);
			if (s == 'L')
			{
				planLane = -1; // his left tile struck → dodge in his right lane
			}
			else if (s == 'R')
			{
				planLane = 1;  // his right tile struck → dodge in his left lane
			}
			// 'B': keep the lane — while rolling, the detonation is i-framed like any strike
			planRank += 2;
			plannedTiles.add(new LocalPoint(
				planC.getX() + planF[0] * (h + planRank * t) + planL[0] * planLane * h,
				planC.getY() + planF[1] * (h + planRank * t) + planL[1] * planLane * h));
		}
	}

	/** Column rank of a planned tile, derived against the locked anchor. */
	private int rankOf(LocalPoint tile)
	{
		final int t = Perspective.LOCAL_TILE_SIZE;
		final int h = t / 2;
		final int depthUnits = (tile.getX() - planC.getX()) * planF[0] + (tile.getY() - planC.getY()) * planF[1];
		return Math.round((depthUnits - h) / (float) t);
	}

	private int laneOf(LocalPoint tile)
	{
		return (tile.getX() - planC.getX()) * planL[0] + (tile.getY() - planC.getY()) * planL[1] >= 0 ? 1 : -1;
	}

	private int playerRank(LocalPoint p)
	{
		final int t = Perspective.LOCAL_TILE_SIZE;
		final int h = t / 2;
		final int depthUnits = (p.getX() - planC.getX()) * planF[0] + (p.getY() - planC.getY()) * planF[1];
		return Math.max(0, Math.round((depthUnits - h) / (float) t));
	}

	private int latSign(LocalPoint p)
	{
		return (p.getX() - planC.getX()) * planL[0] + (p.getY() - planC.getY()) * planL[1] >= 0 ? 1 : -1;
	}

	private static LocalPoint snapToTile(LocalPoint p)
	{
		final int t = Perspective.LOCAL_TILE_SIZE;
		return new LocalPoint(
			Math.floorDiv(p.getX(), t) * t + t / 2,
			Math.floorDiv(p.getY(), t) * t + t / 2,
			p.getWorldView()
		);
	}

	private static int chebTiles(LocalPoint a, LocalPoint b)
	{
		final int t = Perspective.LOCAL_TILE_SIZE;
		return Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getY() - b.getY())) / t;
	}

	/** Snap a 0..2047 orientation to a cardinal unit vector (x east, y north). */
	private static int[] cardinal(int orientation)
	{
		final int s = Math.floorMod(Math.round(orientation / 512f), 4);
		switch (s)
		{
			case 0:
				return new int[]{0, -1}; // South
			case 1:
				return new int[]{-1, 0}; // West
			case 2:
				return new int[]{0, 1};  // North
			default:
				return new int[]{1, 0};  // East
		}
	}

	// ---- accessors for the overlay ----

	NPC getBoss()
	{
		return boss;
	}

	boolean comboActive()
	{
		return comboTicks > 0;
	}

	boolean lungeActive()
	{
		return lungeTicks > 0 && dangerLeft >= 0;
	}

	/** Ticks until the first spear strike lands (from the wind-up), else 0. */
	int comboIncoming()
	{
		return comboIncomingTicks;
	}

	/**
	 * Predicted strike sequence for the active combo, in order: 'L' = strike on Drakan's left
	 * tile, 'R' = his right tile, 'B' = the radial AoE. Empty when no forecast is active.
	 *
	 * The bloom slot is placed by the cluster's startCycle when the cluster has surfaced — but the
	 * cluster arrives a tick later than the flashes, so before that the slot is INFERRED from the
	 * flash spacing: flashes are one slot apart (~20 cycles; 15 seen late-fight), and a mid-chain
	 * bloom leaves a double gap between its neighbours. Inferring it up front means the chain is
	 * complete at first build and the click tiles never re-shuffle mid-wind-up.
	 */
	String forecastChain()
	{
		if (forecastTicks <= 0 || flashes.isEmpty())
		{
			return "";
		}
		final List<long[]> sorted = new ArrayList<>(flashes);
		sorted.sort((x, y) -> x[0] != y[0] ? Long.compare(x[0], y[0]) : Long.compare(x[2], y[2]));

		final boolean haveCluster = bloomStartCycle != Long.MIN_VALUE;
		// base slot spacing: smallest consecutive gap if one looks like a single slot, else 20
		long base = 20;
		for (int i = 1; i < sorted.size(); i++)
		{
			final long gap = sorted.get(i)[0] - sorted.get(i - 1)[0];
			if (gap > 0 && gap <= 25 && gap < base)
			{
				base = gap;
			}
		}

		final StringBuilder sb = new StringBuilder();
		boolean bloomPlaced = false;
		for (int i = 0; i < sorted.size(); i++)
		{
			final long[] fl = sorted.get(i);
			if (haveCluster)
			{
				if (!bloomPlaced && bloomStartCycle <= fl[0])
				{
					sb.append('B');
					bloomPlaced = true;
				}
			}
			else if (!bloomPlaced && i > 0 && fl[0] - sorted.get(i - 1)[0] >= base + 12)
			{
				sb.append('B');
				bloomPlaced = true;
			}
			sb.append(fl[1] == 0 ? 'L' : 'R');
		}
		if (haveCluster && !bloomPlaced)
		{
			sb.append('B');
		}
		return sb.toString();
	}

	/** How many strikes of the current combo have already fired (indexes into forecastChain). */
	int strikesConsumed()
	{
		return strikesConsumed;
	}

	/**
	 * True when clicking the next tile procs the dodge roll. The roll — not the tile — is the
	 * actual dodge (Drakan auto-hits anyone standing, anywhere): clicking early just walks the
	 * player there to stand and be hit ("clicking to dodge before that will lead to you taking
	 * hits" — wiki). Strike i lands at wind-up +3+i ticks; the window opens one tick before.
	 */
	boolean clickNow()
	{
		if (forecastTicks <= 0 || plannedTiles.isEmpty() || strikesConsumed >= plannedTiles.size())
		{
			return false;
		}
		// sub-tick precision: game cycles are 20ms; the window opens at wind-up + 2 ticks per
		// pending strike, shifted by the user-tuned beat delay
		final long elapsedMs = 20L * (client.getGameCycle() - comboStartCycle);
		return elapsedMs >= 600L * (2 + strikesConsumed) + config.beatDelayMs();
	}

	/** World-locked dodge tiles for the forecast combo, index-aligned with plannedChain(). */
	List<LocalPoint> plannedTiles()
	{
		return plannedTiles;
	}

	/** The chain the current plan was built for ('L'/'R'/'B' per step). */
	String plannedChain()
	{
		return plannedChain;
	}

	/** Active wind-up special: 0 = none, 1 = front/back wave (safe: sides), 2 = semicircle (safe: behind). */
	int specialType()
	{
		return specialType;
	}

	/** Ticks until the special's strike lands (negative during the post-impact wave linger). */
	int specialImpactTicks()
	{
		return specialImpactTicks;
	}

	/** Boss orientation locked at the special's wind-up (final strike facing = nearest cardinal). */
	int specialOrientation()
	{
		return specialOrientation;
	}

	/** Ticks remaining on the reappear-charge warning (0 = none/resolved as barrage). */
	int chargeTicks()
	{
		return chargeTicks;
	}

	/** Player tile at the reappear — sidestep tiles anchor here. */
	LocalPoint chargeAnchor()
	{
		return chargeAnchor;
	}

	/** Sidestep axis (perpendicular to his charge line). */
	int[] chargePerp()
	{
		return chargePerp;
	}

	boolean bloomActive()
	{
		return bloomTicks > 0;
	}

	boolean prayMagic()
	{
		return prayMagicTicks > 0;
	}

	String phase()
	{
		final int pct = hpPct();
		if (pct < 0)
		{
			return "P1";
		}
		if (pct > 70)
		{
			return "P1";
		}
		return pct > 33 ? "P2" : "P3";
	}

	int hpPct()
	{
		if (boss == null)
		{
			return -1;
		}
		final int r = boss.getHealthRatio();
		final int s = boss.getHealthScale();
		return (s > 0 && r >= 0) ? 100 * r / s : -1;
	}
}
