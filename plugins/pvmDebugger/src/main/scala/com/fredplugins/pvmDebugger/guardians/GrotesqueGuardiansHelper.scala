package com.fredplugins.pvmDebugger.guardians

import com.fredplugins.pvmDebugger.PvmDebuggerPlugin
import com.fredplugins.pvmDebugger.guardians.GrotesqueGuardiansConfig.AttackStyle
import com.fredplugins.pvmDebugger.guardians.Guardian.Variant
import net.runelite.api.NPC
import net.runelite.api.NpcID
import net.runelite.api.Projectile
import net.runelite.client.eventbus.Subscribe

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
object Guardian {
	private val DUSK_PHASE_1_ANIMATION_MELEE = 7785

	private val DUSK_PHASE_2_ANIMATION_MELEE_7786 = 7786
	private val DUSK_PHASE_2_ANIMATION_MELEE_7788 = 7788
	val DUSK_PHASE_2_ECLIPSE_EXPLOSION = 7802

	private val DUSK_PHASE_3_ANIMATION_MELEE_7785 = 7785
	private val DUSK_PHASE_3_ANIMATION_MELEE_7787 = 7787

	private val DUSK_PHASE_4_ANIMATION_MELEE = 7800
	private val DUSK_PHASE_4_ANIMATION_RANGE = 7801

	val ECHO_DUSK_PHASE_2_TRANSITION = 7799

	private val DUSK_ATTACK_TICK_SPEED                = 6
	private val DEFINITELY_NOT_DUSK_ATTACK_TICK_SPEED = 12

	private val DAWN_PROJECTILE_STONE_ORB     = 1445
	private val DAWN_PROJECTILE_RANGED_ATTACK = 1444

	private val DAWN_ANIMATION_STONE_ORB     = 7771
	private val DAWN_ANIMATION_RANGED_ATTACK = 7770
	private val DAWN_ANIMATION_MELEE_ATTACK  = 7769

	private val DAWN_ATTACK_TICK_SPEED      = 6
	private val ECHO_DAWN_ATTACK_TICK_SPEED = 12

	sealed trait Variant(val id: Int, val echo: Boolean, val attackAnimationIds:Set[Int], val projectileIds: Set[Int], val attackSpeed: Int, val attackStyle: AttackStyle) {
	}
	object Variants {
		case object Dusk_Phase_1 extends Variant(NpcID.DUSK_7851, false, Set(DUSK_PHASE_1_ANIMATION_MELEE), Set(), DUSK_ATTACK_TICK_SPEED, AttackStyle.MELEE)
		case object Dusk_Phase_2 extends Variant(NpcID.DUSK_7882, false, Set(DUSK_PHASE_2_ANIMATION_MELEE_7786, DUSK_PHASE_2_ANIMATION_MELEE_7788, DUSK_PHASE_2_ECLIPSE_EXPLOSION), Set(), DUSK_ATTACK_TICK_SPEED, AttackStyle.MELEE)
		case object Dusk_Phase_3 extends Variant(NpcID.DUSK_7883, false, Set(DUSK_PHASE_3_ANIMATION_MELEE_7785, DUSK_PHASE_3_ANIMATION_MELEE_7787), Set(), DUSK_ATTACK_TICK_SPEED, AttackStyle.MELEE)
		case object Dusk_Phase_4 extends Variant(NpcID.DUSK_7888, false, Set(DUSK_PHASE_4_ANIMATION_MELEE, DUSK_PHASE_4_ANIMATION_RANGE), Set(DAWN_PROJECTILE_RANGED_ATTACK), DUSK_ATTACK_TICK_SPEED, AttackStyle.RANGE)
		case object Dusk_Despawn extends Variant(NpcID.DUSK_7889, false, Set(), Set(), DUSK_ATTACK_TICK_SPEED, AttackStyle.UNKNOWN)
		case object Dawn_Phase_1 extends Variant(NpcID.DAWN_7852, false, Set(DAWN_ANIMATION_MELEE_ATTACK, DAWN_ANIMATION_RANGED_ATTACK, DAWN_ANIMATION_STONE_ORB), Set(DAWN_PROJECTILE_RANGED_ATTACK, DAWN_PROJECTILE_STONE_ORB), DAWN_ATTACK_TICK_SPEED, AttackStyle.RANGE)
		case object Dawn_Phase_3 extends Variant(NpcID.DAWN_7884, false, Set(DAWN_ANIMATION_MELEE_ATTACK, DAWN_ANIMATION_RANGED_ATTACK, DAWN_ANIMATION_STONE_ORB), Set(DAWN_PROJECTILE_RANGED_ATTACK, DAWN_PROJECTILE_STONE_ORB), DAWN_ATTACK_TICK_SPEED, AttackStyle.RANGE)
		case object Dawn_Despawn extends Variant(NpcID.DAWN_7885, false, Set(), Set(), DAWN_ATTACK_TICK_SPEED, AttackStyle.UNKNOWN)
		case object Echo_Dusk extends Variant(NpcID.DUSK_7888, true, Set(DUSK_PHASE_4_ANIMATION_RANGE), Set(DAWN_PROJECTILE_RANGED_ATTACK), DUSK_ATTACK_TICK_SPEED, AttackStyle.RANGE)
		case object Echo_Dawn extends Variant(NpcID.DAWN_7852, true, Set(DAWN_ANIMATION_STONE_ORB), Set(DAWN_PROJECTILE_STONE_ORB), ECHO_DAWN_ATTACK_TICK_SPEED, AttackStyle.RANGE)
		case object Definitely_not_dusk extends Variant(NpcID.DUSK_7882, true, Set(DUSK_PHASE_2_ECLIPSE_EXPLOSION), Set(), DEFINITELY_NOT_DUSK_ATTACK_TICK_SPEED, AttackStyle.UNKNOWN)

		val values: Seq[Variant] = Seq(
			Dusk_Phase_1,
				Dusk_Phase_2,
				Dusk_Phase_3,
				Dusk_Phase_4,
				Dusk_Despawn,
				Dawn_Phase_1,
				Dawn_Phase_3,
				Dawn_Despawn,
				Echo_Dusk,
				Echo_Dawn,
				Definitely_not_dusk
			)

		def of(id: Int, echoVariant: Boolean): Variant = {
			values.find(v => v.id == id && v.echo == echoVariant).getOrElse(new Variant(id, echoVariant, Set.empty, Set.empty, (if (echoVariant) ECHO_DAWN_ATTACK_TICK_SPEED else DUSK_ATTACK_TICK_SPEED), AttackStyle
				.UNKNOWN){

			})
		}
	}
}
class Guardian(val npc: NPC, val echoVariant: Boolean) {
	val npcId = npc.getId
	val npcName = npc.getName
	val variant: Variant = Guardian.Variants.of(npcId, echoVariant)
	var ticksUntilNextAttack: Int = -1;

	private val attackAnimations: Set[Int] = null
	private val projectileIds: Set[Int] = null
	private val attackTickSpeed: Int = 0
	private var attackStyle: AttackStyle = null
	private var lastAttackProjectile: Projectile = null

	private var echoVariantPhased: Boolean = false
}
class GrotesqueGuardiansHelper(pvmDebuggerPlugin: PvmDebuggerPlugin, config: GrotesqueGuardiansConfig) {
//	@Subscribe
//	def on
}
