package com.fredplugins.pvmDebugger.bmr

import com.fredplugins.common.api.WorldRegion.given_Conversion_WorldArea_WorldRegion
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.extensions.MenuExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.overlays
import com.fredplugins.common.utils.ReflectionUtils
import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.common.utils.TWorldPoint
import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.bmr.FredsDrakanHelper.*
import ethanApiPlugin.lucidplugins.api.utils.CombatUtils
import ethanApiPlugin.services.localPlayer.events.LocalRegionChanged
import net.runelite.api.coords.Angle
import net.runelite.api.coords.Direction
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import net.runelite.api.coords.Direction
import net.runelite.api.coords.WorldArea
import net.runelite.api.events.*
import net.runelite.api.events.GameTick
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_PUNCH_LEFT
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_PUNCH_RIGHT
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_SCREECH01
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_SCREECH02
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_SCREECH03
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_TANTRUM01
import net.runelite.api.gameval.AnimationID.NPC_WYRD01_TANTRUM02
import net.runelite.api.gameval.AnimationID.NPC_WYRD02_MELEE01
import net.runelite.api.gameval.NpcID
import net.runelite.api.Client
import net.runelite.api.NPC
import net.runelite.api.Perspective
import net.runelite.api.Prayer
import net.runelite.api.Renderable
import net.runelite.api.gameval.AnimationID
import net.runelite.api.gameval.AnimationID.{LOWERNIEL_DRAKAN_STAB_HIT_TELEGRAPH01, LOWERNIEL_DRAKAN_SWIPE_HIT_TELEGRAPH01, NPC_LOWERNIEL_DRAKAN_COMBO_TELEGRAPH_TO_READY01, NPC_LOWERNIEL_DRAKAN_IDLE_TO_TELEGRAPH_LOOP01}
import net.runelite.client.callback.RenderCallback
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.game.NpcUtil
import net.runelite.client.ui.overlay.OverlayUtil
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity
import net.runelite.client.ui.overlay.components.LineComponent
import net.runelite.client.ui.overlay.components.TitleComponent
import net.runelite.client.util.ColorUtil

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.math.Ordered.orderingToOrdered
import scala.util.chaining.*
import scala.util.Random
import scala.util.Try

import scala.math.Ordered.orderingToOrdered
case class DustTile(location: WorldPoint, spawnCycle: Int, finishedCycle: Int, spawnTick: Int)
case class FlashElement(startCycle: Long, direction: "L" | "R", discorveryIndex: Long)

object FredsDrakanHelper {
	private val DANGER_MARK_GFX = 2953

	private val BLOOM_ANIM = 14325
	// Directional lunge strikes: 14321 = strike on Drakan's LEFT tile, 14323 = his RIGHT tile.
	private val LUNGE_LEFT_ANIM = 14321
	private val LUNGE_RIGHT_ANIM = 14323
	// Combo wind-up (non-directional). 14317 fires exactly 3 ticks before the first strike, 14319 one tick before.
	private val COMBO_WINDUP_ANIM = 14317
	private val COMBO_PRESTRIKE_ANIM = 14319
	private val BLOOM_WINDUP_SPOTANIM = Set(3933, 3943, 3939)
	private val MAGIC_PROJECTILES = Set(3874, 3875, 3876, 3877, 3878, 3879)

	// Forecast flash spot-anims, queued on Drakan at combo wind-up — ONE INSTANCE PER STRIKE,
	// side encoded in the id (verified over 90 recon combos; in every pair the lower id = right):
	// 3910 / 3920 = strike on his LEFT tile, 3909 / 3919 = his RIGHT tile.
	private val FLASH_LEFT = Set(3910, 3920)
	private val FLASH_RIGHT = Set(3909, 3919)
	// The radial AoE ("bloom") telegraph is its own 8-id cluster, one per segment.
	private val BLOOM_CLUSTER_MIN = 3921
	private val BLOOM_CLUSTER_MAX = 3928

	// Wind-up specials. Orientation locks at the wind-up (nearest cardinal = final strike facing,
	// 12/12 recon casts), so the safe zone can be painted the moment the wind-up starts.
	private val FB_WINDUP_ANIM = 14327 // front/back wave: strike +4 ticks, safe = his sides

	private val SEMI_WINDUP_ANIM = 14333 // semicircle: strike +5 ticks, safe = behind him

	// P3 vanish/reappear: reappear + 14311 next tick = blood barrage (Pray Magic covers it);
	// reappear with NO 14311 = a CHARGE along the line to the player (~2 tiles/tick, lands in
	// ~3-4 ticks) — safe = perpendicular sidestep, running at/away stays on the line.
	private val BLOOD_BARRAGE_ANIM = 14311
}

class FredsDrakanHelper(parent: FredsBmrHelper, config: FredsBmrConfig) extends RenderCallback with ShimUtils.Logging() {
	private def clientThread = parent.parent.getClientThread
	private def client = parent.parent.getClient
	given Client = client

	private def snapToTile(p: LocalPoint): LocalPoint = {
		val t = Perspective.LOCAL_TILE_SIZE
		return new LocalPoint(Math.floorDiv(p.getX, t) * t + t / 2, Math.floorDiv(p.getY, t) * t + t / 2, p.getWorldView)
	}

	def compare(x: Long, y: Long): Int = return if ((x < y)) -(1) else (if ((x == y)) 0 else 1)

	//<editor-fold desc="State area">
	object State {
		//<editor-fold desc="reset cleans thesen">
		private[FredsDrakanHelper] var boss: NPC = null
		private[FredsDrakanHelper] var comboTicks = 0
		private[FredsDrakanHelper] var bloomTicks = 0
		private[FredsDrakanHelper] var prayMagicTicks = 0
		private[FredsDrakanHelper] var lungeTicks = 0
		private[FredsDrakanHelper] var comboIncomingTicks = 0
		private[FredsDrakanHelper] var dangerLeft = -1 // 1 = Drakan's left tile is the struck (danger) side, 0 = his right tile

		private[FredsDrakanHelper] def reset(): Unit = {
			boss = null
			comboTicks = 0
			bloomTicks = 0
			prayMagicTicks = 0
			lungeTicks = 0
			comboIncomingTicks = 0
			dangerLeft = -(1)
			resetForecast()

		}
		// </editor-fold>

		//<editor-fold desc="combo forecast state">
		// each element: {startCycle, side(0=L,1=R), discoveryIndex}
		private[FredsDrakanHelper] val flashes = mutable.ArrayBuffer.empty[FlashElement]
		private[FredsDrakanHelper] val seenFlashKeys = mutable.HashSet.empty[Long]
		private[FredsDrakanHelper] var bloomStartCycle = Long.MaxValue
		private[FredsDrakanHelper] var flashDiscovery = 0
		private[FredsDrakanHelper] var forecastTicks = 0
		private[FredsDrakanHelper] var strikesConsumed = 0


		private[FredsDrakanHelper] def resetForecast(): Unit = {
			flashes.clear()
			seenFlashKeys.clear()
			bloomStartCycle = Long.MinValue
			flashDiscovery = 0
			forecastTicks = 0
			strikesConsumed = 0
			specialType = 0
			beatSnapped = false
			pendingHitReplan = false
			chargeTicks = 0
			clearPlan()
		}
		//</editor-fold>


		//<editor-fold desc="wind-up special state">
		private[FredsDrakanHelper] var specialType = 0 // 0 = none, 1 = front/back (safe: sides), 2 = semicircle (safe: behind)
		private[FredsDrakanHelper] var specialImpactTicks = 0 // ticks until the strike lands (goes negative during wave linger)
		private[FredsDrakanHelper] var specialLingerTicks = 0 // how long past impact the zone stays painted (traveling waves)
		private[FredsDrakanHelper] var specialOrientation = 0 // boss orientation locked at wind-up
		//</editor-fold>


		// ---- dodge plan: world-locked click tiles for the forecast combo ----
		// Built once per combo (and extended as late flashes surface); tiles do NOT track the player.
		// Only a genuine deviation from the path triggers a re-plan of the remaining steps.
		private[FredsDrakanHelper] val plannedTiles = mutable.ArrayBuffer.empty[LocalPoint]
		private[FredsDrakanHelper] var plannedChain = ""
		private[FredsDrakanHelper] var planAnchor: LocalPoint = null // player tile when the plan was built (expected pos before dodge 1)
		private[FredsDrakanHelper] var planC: LocalPoint = null // boss anchor locked at plan build — he LUNGES during strikes,

		// so his live position must never be used once dodging begins
		private[FredsDrakanHelper] var planF: Array[Int] = null // facing frame locked at plan build
		private[FredsDrakanHelper] var planL: Array[Int] = null
		private[FredsDrakanHelper] var planRank = 0 // column rank of the last planned tile
		private[FredsDrakanHelper] var planLane = 0 // lane of the last planned tile (+1 = his left, -1 = his right)
		private[FredsDrakanHelper] var offPathStreak = 0 // consecutive ticks the player has been off the path
		private[FredsDrakanHelper] var recoveryTicks = 0 // post-hit window in which the plan re-tracks the player
		private[FredsDrakanHelper] var comboStartTick = Int.MaxValue // client tick of the wind-up anim
		private[FredsDrakanHelper] var comboStartCycle = Int.MinValue // game cycle (20ms) of the wind-up anim
		private[FredsDrakanHelper] var beatSnapped = false // one-time re-anchor at the first beat done
		private[FredsDrakanHelper] var pendingHitReplan = false // a hit landed — rebuild unconditionally next tick

		// ---- P3 reappear-charge state ----
		private[FredsDrakanHelper] var bossVanished = false
		private[FredsDrakanHelper] var chargeTicks = 0
		private[FredsDrakanHelper] var chargeAnchor: LocalPoint = null // player tile at the reappear
		private[FredsDrakanHelper] var chargePerp: Array[Int] = null // perpendicular (sidestep) axis

		private[FredsDrakanHelper] def clearPlan(): Unit = {
			plannedTiles.clear()
			plannedChain = ""
			planAnchor = null
			planC = null
			offPathStreak = 0
			recoveryTicks = 0
		}
	}


	private var curInRegion: Boolean = false

	//</editor-fold>

	def inRegion(wp: WorldPoint): Boolean =
		Option(wp).map(TWorldPoint.get(_)).map(_.getRegionID).fold(false)(inRegion)

	def inRegion(rid: Int): Boolean = Seq(14132, 10106).contains(rid)

	def init(): Unit = {
		curInRegion = Option(client.getLocalPlayer).map(_.templateRegion).fold(false)(inRegion) //.map(inRegion).getOrElse(false)
		parent.parent.getRenderCallbackManager.register(this)
	}

	def cleanup(): Unit = {
		parent.parent.getRenderCallbackManager.unregister(this)
		curInRegion = false
		State.reset()
	}

	@Subscribe
	def onNpcSpawned(e: NpcSpawned): Unit = {
		if (e.getNpc.getId != NpcID.MYQ6_LOWERNIEL_FINAL_COMBAT) return

		val reappear = State.bossVanished
		State.bossVanished = false
		State.boss = e.getNpc
		if (!reappear) return

		// Reappear: assume a charge until the blood barrage (14311) reveals itself next tick.
		val local = client.getLocalPlayer
		val p = if (local != null) local.getLocalLocation else null
		val b = State.boss.getLocalLocation
		if (p == null || b == null) return
		val dx = p.getX - b.getX
		val dy = p.getY - b.getY
		// charge axis = dominant approach axis; sidestep axis = the other one
		State.chargePerp = if ((Math.abs(dx) >= Math.abs(dy))) Array[Int](0, 1) else Array[Int](1, 0)
		State.chargeAnchor = snapToTile(p)
		State.chargeTicks = 4
		log.debug("chargePerp={{}, {}}, chargeAnchor={}, chargeTicks={}", State.chargePerp(0), State.chargePerp(1), State.chargeAnchor, State.chargeTicks)
	}

	@Subscribe
	def onNpcDespawned(e: NpcDespawned): Unit = {
		if (e.getNpc == State.boss) {
			State.boss = null
			State.bossVanished = true
		}
	}

	@Subscribe
	def onNpcChanged(e: NpcChanged): Unit = {
		log.debug("NpcChanged: oldId = {}, newId = {}", ReflectionUtils.getNpcName(e.getOld.getId), ReflectionUtils.getNpcName(e.getNpc.getId))
		if (e.getNpc.getId == NpcID.MYQ6_LOWERNIEL_FINAL_COMBAT) State.boss = e.getNpc
		else if (e.getNpc == State.boss) State.boss = null
	}

	@Subscribe
	def onAnimationChanged(e: AnimationChanged): Unit = {
		if (State.boss == null || (e.getActor != State.boss)) return
		val a = State.boss.getAnimation
		if (a ==  BLOOM_ANIM) {
			State.bloomTicks = 2
			State.strikesConsumed += 1
		}
		else if (a == LUNGE_LEFT_ANIM) {
			State.dangerLeft = 1
			State.lungeTicks = 2
			State.comboTicks = 2
			State.comboIncomingTicks = 0
			State.strikesConsumed += 1
		}
		else if (a == LUNGE_RIGHT_ANIM) {
			State.dangerLeft = 0
			State.lungeTicks = 2
			State.comboTicks = 2
			State.comboIncomingTicks = 0
			State.strikesConsumed += 1
		}
		else if (a == COMBO_WINDUP_ANIM) {
			State.comboTicks = 2
			State.comboIncomingTicks = 3
			// a new combo's forecast flashes are queued now — start a fresh capture window
			State.resetForecast()
			State.forecastTicks = 14
			State.specialType = 0
			State.comboStartTick = client.getTickCount
			State.comboStartCycle = client.getGameCycle
		}
		else if (a == COMBO_PRESTRIKE_ANIM) {
			State.comboTicks = 2
			State.comboIncomingTicks = Math.max(State.comboIncomingTicks, 1)
		}
		else if (a == FB_WINDUP_ANIM) {
			State.specialType = 1
			State.specialImpactTicks = 4
			State.specialLingerTicks = 4
			State.specialOrientation = State.boss.getOrientation
			State.forecastTicks = 0
		}
		else if (a == SEMI_WINDUP_ANIM) {
			State.specialType = 2
			State.specialImpactTicks = 5
			State.specialLingerTicks = 3
			State.specialOrientation = State.boss.getOrientation
			State.forecastTicks = 0
		}
		else if (a == BLOOD_BARRAGE_ANIM) {
			// reappear resolved as the blood barrage — no charge; the Pray Magic flash covers it
			State.chargeTicks = 0
		}
		else if (a != -(1)) {
			// any non-combo animation (normal melee etc.) ends the forecast display;
			// the specials' own strike anims (14329/14335) land here without clearing their zone
			State.forecastTicks = 0
		}
	}

	/**
	 * Collects the combo forecast: at wind-up Drakan queues one flash spot-anim INSTANCE per
	 * upcoming strike, side encoded in the id. Instances are keyed by (id, startCycle) so a
	 * restarted id (same side twice, e.g. an RR chain) counts as a new flash. Sorting by
	 * startCycle gives the strike order (instances are scheduled with staggered start delays).
	 */
	@Subscribe
	def onClientTick(e: ClientTick): Unit = {
		if (State.boss == null || State.forecastTicks <= 0) return
		for (s <- State.boss.getSpotAnims.asScala.toList) {
			val id = s.getId
			val left = FLASH_LEFT.contains(id)
			val right = FLASH_RIGHT.contains(id)
			val cluster = id >= BLOOM_CLUSTER_MIN && id <= BLOOM_CLUSTER_MAX

			if (!(left) && !(right) && !(cluster)) {

			} else {
				val key = (id.toLong << 32) ^ (s.getStartCycle & 0xFFFFFFFFL)
				if (!(State.seenFlashKeys.add(key))) {
					//continue
				} else {
					if (cluster) State.bloomStartCycle = if ((State.bloomStartCycle == Long.MinValue)) s.getStartCycle
					else Math.min(State.bloomStartCycle, s.getStartCycle)
					else State.flashes.addOne(
						FlashElement(
							s.getStartCycle, 
							if (left) "L" else "R", 
							{State.flashDiscovery += 1; State.flashDiscovery - 1}
						)
					)
				}
			}
		}
	}

	@Subscribe
	def onGraphicChanged(e: GraphicChanged): Unit = {
		if (State.boss == null || (e.getActor != State.boss)) return
		if (BLOOM_WINDUP_SPOTANIM.contains(State.boss.getGraphic)) State.bloomTicks = Math.max(State.bloomTicks, 3)
	}

	@Subscribe
	def onProjectileMoved(e: ProjectileMoved): Unit = {
		if (State.boss == null) return
		val p = e.getProjectile
		if (MAGIC_PROJECTILES.contains(p.getId) && (p.getInteracting eq client.getLocalPlayer)) {
			val ticks = Math.ceil(p.getRemainingCycles / 30.0).toInt
			State.prayMagicTicks = Math.max(State.prayMagicTicks, ticks)
		}
	}

	@Subscribe
	def onGameTick(e: GameTick): Unit = {
		if (State.forecastTicks > 0) State.forecastTicks -= 1
		if (State.specialType != 0) {
			State.specialImpactTicks -= 1
			if (State.specialImpactTicks < -(State.specialLingerTicks)) State.specialType = 0
		}
		if (State.comboTicks > 0) State.comboTicks -= 1
		if (State.bloomTicks > 0) State.bloomTicks -= 1
		if (State.lungeTicks > 0) State.lungeTicks -= 1
		if (State.comboIncomingTicks > 0) State.comboIncomingTicks -= 1
		if (State.chargeTicks > 0) State.chargeTicks -= 1
		if (State.prayMagicTicks > 0) State.prayMagicTicks -= 1
		updateDodgePlan()
	}

	@Subscribe(priority = 10)
	def preGameTick(e: GameTick): Unit = {
		if (State.boss == null) return
		val protectPrayer = if (prayMagic) Prayer.PROTECT_FROM_MAGIC
		else Prayer.PROTECT_FROM_MELEE
		CombatUtils.activatePrayers(protectPrayer, Prayer.PIETY)
	}

	private def updateDodgePlan(): Unit = {
		if (State.boss == null || State.forecastTicks <= 0) {
			State.clearPlan()
			return
		}
		val chain = forecastChain
		if (chain.isEmpty) {
			State.clearPlan()
			return
		}
		val local = client.getLocalPlayer
		if (local == null || local.getLocalLocation == null) return
		val p = snapToTile(local.getLocalLocation)
		val fluid = strikesConsumed eq 0
		if (plannedChain.isEmpty) buildFresh(chain, p)
		else if (!(chain.startsWith(plannedChain))) {
			// chain re-ordered (e.g. a late bloom slot inserted mid-chain)
			if (fluid || State.planC == null || plannedTiles.size < strikesConsumed || strikesConsumed < 1) buildFresh(chain, p)
			else rebuildFrom(chain, strikesConsumed, rankOf(plannedTiles(strikesConsumed - 1)), laneOf(plannedTiles(strikesConsumed - 1)))
		}
		else if (chain.length > plannedChain.length) {
			// chain grew — extend from the tail state, against the LOCKED boss anchor
			appendPlanSteps(chain, plannedChain.length)
		}
		State.plannedChain = chain
		if (fluid) {
			// FROZEN through the wind-up against small shuffling (1-tile melee micro-moves made
			// the tiles chase the player), but a REAL reposition (2+ tiles from the anchor)
			// re-anchors, and the first beat always takes one final snap to the player's actual
			// position as dodging begins.
			if (!(State.beatSnapped) && clickNow) {
				State.beatSnapped = true
				if (!(p == (State.planAnchor))) buildFresh(chain, p)
			}
			else if (!(State.beatSnapped) && State.planAnchor != null && chebTiles(p, State.planAnchor) >= 2) buildFresh(chain, p)
			State.offPathStreak = 0
		}
		else if (strikesConsumed < plannedTiles.size) {
			if (State.pendingHitReplan) {
				// a dodge failed — re-anchor the remaining steps unconditionally, right now
				// (the earlier <=2 damping gate cancelled this exact correction: after a missed
				// dodge the next tile is exactly 2 away, so it never fired)
				State.pendingHitReplan = false
				rebuildFrom(chain, strikesConsumed, playerRank(p), latSign(p))
				State.offPathStreak = 0
				return
			}
			if (State.recoveryTicks > 0) {
				// residual post-hit window: keep re-anchoring only while genuinely out of reach
				State.recoveryTicks -= 1
				if (chebTiles(p, plannedTiles(strikesConsumed)) <= 2) State.recoveryTicks = 0
				else rebuildFrom(chain, strikesConsumed, playerRank(p), latSign(p))
				State.offPathStreak = 0
				return
			}
			val expected = plannedTiles(strikesConsumed - 1)
			val next = plannedTiles(strikesConsumed)
			if (Math.min(chebTiles(p, expected), chebTiles(p, next)) > 1) {
				// 2-tick grace: a slightly late roll completes within a tick and must not re-plan
				if ( {State.offPathStreak += 1; State.offPathStreak} >= 2) {
					rebuildFrom(chain, strikesConsumed, playerRank(p), latSign(p))
					State.offPathStreak = 0
				}
			}
			else State.offPathStreak = 0
		}
	}

	/**
	 * A hitsplat on the player mid-combo means a dodge failed — the correct next click tile now
	 * derives from wherever they end up. Rather than one rebuild from a mid-roll position (an
	 * unstable anchor), this opens a short RECOVERY window in which the plan re-tracks the player
	 * each game tick, freezing again the moment they commit to the corrected next tile.
	 */
	@Subscribe
	def onHitsplatApplied(e: HitsplatApplied): Unit = {
		if (e.getActor ne client.getLocalPlayer) return
		if (State.boss == null || State.forecastTicks <= 0 || (strikesConsumed eq 0) || State.planC == null || plannedChain.isEmpty || plannedTiles.size <= strikesConsumed) return
		State.pendingHitReplan = true
		State.recoveryTicks = 2
		State.offPathStreak = 0
	}

	/**
	 * Full rebuild from the player's current tile; boss anchor and frame are (re)locked now.
	 * The frame is derived from the PLAYER's direction rather than boss orientation: Drakan always
	 * turns to face the player before striking, but his orientation value lags mid-turn at wind-up
	 * (observed 90-180° stale), while the player's bearing predicts his final facing reliably.
	 */
	private def buildFresh(chain: String, p: LocalPoint): Unit = {
		val c = State.boss.getLocalLocation
		if (c == null) return
		State.plannedTiles.clear
		State.planC = c
		val dx = p.getX - c.getX
		val dy = p.getY - c.getY
		if (dx == 0 && dy == 0) State.planF = cardinal(State.boss.getOrientation)
		else if (Math.abs(dx) >= Math.abs(dy)) State.planF = Array[Int](
			if (dx >= 0) 1
			else -(1), 0
		)
		else State.planF = Array[Int](
			0, if (dy >= 0) 1
			else -(1)
		)
		State.planL = Array[Int](-(State.planF(1)), State.planF(0))
		State.planAnchor = p
		State.planRank = playerRank(p)
		State.planLane = latSign(p)
		appendPlanSteps(chain, 0)
	}

	/** Rebuild steps from..end against the LOCKED anchor, keeping earlier tiles untouched. */
	private def rebuildFrom(chain: String, from: Int, anchorRank: Int, anchorLane: Int): Unit = {
		while (plannedTiles.size > from) State.plannedTiles.remove(plannedTiles.size - 1)
		State.planRank = anchorRank
		State.planLane = anchorLane
		appendPlanSteps(chain, from)
	}

	private def appendPlanSteps(chain: String, from: Int): Unit = {
		if (State.	planC == null) return
		val t = Perspective.LOCAL_TILE_SIZE
		val h = t / 2
		// Plain retreat cadence for every slot, flares included. Empirically (90%-run analysis)
		// the dodge is the ROLL, not the tile: identical positions were clean when rolling and
		// hit when standing, at every rank. The tiles keep the retreat orderly and in-column;
		// the click BEAT (clickNow) is what actually procs the roll.
		for (i <- from until chain.length) {
			val s = chain.charAt(i)
			if (s == 'L') State.planLane = -(1) // his left tile struck → dodge in his right lane
			else if (s == 'R') State.planLane = 1 // his right tile struck → dodge in his left lane
			// 'B': keep the lane — while rolling, the detonation is i-framed like any strike
			State.planRank += 2
			State.plannedTiles.addOne(new LocalPoint(State.planC.getX + State.planF(0) * (h + State.planRank * t) + State.planL(0) * State.planLane * h, State.planC.getY + State.planF(1) * (h + State.planRank * t) + State.planL(1) * State.planLane * h))
		}
	}

	/** Column rank of a planned tile, derived against the locked anchor. */
	private def rankOf(tile: LocalPoint): Int = {
		val t = Perspective.LOCAL_TILE_SIZE
		val h = t / 2
		val depthUnits = (tile.getX - State.planC.getX) * State.planF(0) + (tile.getY - State.planC.getY) * State.planF(1)
		return (depthUnits - h) / t.toFloat.round
	}

	private def laneOf(tile: LocalPoint): Int = return if ((tile.getX - State.planC.getX) * State.planL(0) + (tile.getY - State.planC.getY) * State.planL(1) >= 0) 1
	else -(1)

	private def playerRank(p: LocalPoint): Int = {
		val t = Perspective.LOCAL_TILE_SIZE
		val h = t / 2
		val depthUnits = (p.getX - State.planC.getX) * State.planF(0) + (p.getY - State.planC.getY) * State.planF(1)
		return Math.max(0, (depthUnits - h) / t.toFloat.round)
	}

	private def latSign(p: LocalPoint): Int = return if ((p.getX - State.planC.getX) * State.planL(0) + (p.getY - State.planC.getY) * State.planL(1) >= 0) 1
	else -(1)

	private def chebTiles(a: LocalPoint, b: LocalPoint): Int = {
		val t = Perspective.LOCAL_TILE_SIZE
		return Math.max(Math.abs(a.getX - b.getX), Math.abs(a.getY - b.getY)) / t
	}

	/** Snap a 0..2047 orientation to a cardinal unit vector (x east, y north). */
	private def cardinal(orientation: Int): Array[Int] = {
		val s = Math.floorMod(orientation / 512f.round, 4)
		s match {
			case 0 => {
				return Array[Int](0, -(1))
			} // South

			case 1 => {
				return Array[Int](-(1), 0)
			} // West

			case 2 => {
				return Array[Int](0, 1)
			} // North

			case _ => {
				return Array[Int](1, 0)
			} // East

		}
	}

	// ---- accessors for the overlay ----// ---- accessors for the overlay ----

	 def getBoss: NPC = return State.boss

	 def comboActive: Boolean = return State.comboTicks > 0

	 def lungeActive: Boolean = return State.lungeTicks > 0 && State.dangerLeft >= 0

	/** Ticks until the first spear strike lands (from the wind-up), else 0. */
	 def comboIncoming: Int = return State.comboIncomingTicks

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
	 def forecastChain: String = {
		if (State.forecastTicks <= 0 || State.flashes.isEmpty) return ""
		val sorted = State.flashes.toList
			.sortWith((x: FlashElement, y: FlashElement) =>
				(if (x.startCycle != y.startCycle) compare(x.startCycle, y.startCycle)
				else compare(x.discorveryIndex, y.discorveryIndex)) > 0
			)

		val haveCluster = State.bloomStartCycle != Long.MinValue
		// base slot spacing: smallest consecutive gap if one looks like a single slot, else 20
		var base = 20
		for (i <- 1 until sorted.size) {
			val gap: Int = (sorted(i).startCycle - sorted(i - 1).startCycle).toInt
			if (gap > 0 && gap <= 25 && gap < base) base = gap
		}
		val sb = new StringBuilder
		var bloomPlaced = false
		for (i <- 0 until sorted.size) {
			val fl = sorted(i)
			if (haveCluster) if (!(bloomPlaced) && State.bloomStartCycle <= fl.startCycle) {
				sb.append('B')
				bloomPlaced = true
			}
			else if (!(bloomPlaced) && i > 0 && fl.startCycle - sorted(i - 1).startCycle >= base + 12) {
				sb.append('B')
				bloomPlaced = true
			}
			sb.append(fl.direction)
		}
		if (haveCluster && !(bloomPlaced)) sb.append('B')
		return sb.toString
	}

	/** How many strikes of the current combo have already fired (indexes into forecastChain). */
	 def strikesConsumed: Int = return strikesConsumed

	/**
	 * True when clicking the next tile procs the dodge roll. The roll — not the tile — is the
	 * actual dodge (Drakan auto-hits anyone standing, anywhere): clicking early just walks the
	 * player there to stand and be hit ("clicking to dodge before that will lead to you taking
	 * hits" — wiki). Strike i lands at wind-up +3+i ticks; the window opens one tick before.
	 */
	 def clickNow: Boolean = {
		if (State.forecastTicks <= 0 || plannedTiles.isEmpty || strikesConsumed >= plannedTiles.size) return false
		// sub-tick precision: game cycles are 20ms; the window opens at wind-up + 2 ticks per
		// pending strike, shifted by the user-tuned beat delay
		val elapsedMs = 20L * (client.getGameCycle - State.comboStartCycle)
		return elapsedMs >= 600L * (2 + strikesConsumed) + config.beatDelayMs
	}

	/** World-locked dodge tiles for the forecast combo, index-aligned with plannedChain(). */
	 def plannedTiles: List[LocalPoint] = return State.plannedTiles.toList

	/** The chain the current plan was built for ('L'/'R'/'B' per step). */
	 def plannedChain: String = return State.plannedChain

	/** Active wind-up special: 0 = none, 1 = front/back wave (safe: sides), 2 = semicircle (safe: behind). */
	 def specialType: Int = return State.specialType

	/** Ticks until the special's strike lands (negative during the post-impact wave linger). */
	 def specialImpactTicks: Int = return State.specialImpactTicks

	/** Boss orientation locked at the special's wind-up (final strike facing = nearest cardinal). */
	 def specialOrientation: Int = return State.specialOrientation

	/** Ticks remaining on the reappear-charge warning (0 = none/resolved as barrage). */
	 def chargeTicks: Int = return State.chargeTicks

	/** Player tile at the reappear — sidestep tiles anchor here. */
	 def chargeAnchor: LocalPoint = return State.chargeAnchor

	/** Sidestep axis (perpendicular to his charge line). */
	 def chargePerp: Array[Int] = return State.chargePerp

	 def bloomActive: Boolean = return State.bloomTicks > 0

	 def prayMagic: Boolean = return State.prayMagicTicks > 0

	 def phase: String = {
		val pct = hpPct
		if (pct < 0) return "P1"
		if (pct > 70) return "P1"
		return if (pct > 33) "P2"
		else "P3"
	}

	 def hpPct: Int = {
		if (State.boss == null) return -(1)
		val r = State.boss.getHealthRatio
		val s = State.boss.getHealthScale
		return if ((s > 0 && r >= 0)) 100 * r / s
		else -(1)
	}
}