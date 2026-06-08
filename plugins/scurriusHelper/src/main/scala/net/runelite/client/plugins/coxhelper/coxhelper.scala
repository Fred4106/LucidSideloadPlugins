package net.runelite.client.plugins

import com.fredplugins.common.ProjectileID
import com.fredplugins.common.api.WorldRegion
import com.fredplugins.common.api.WorldRegion.*
import com.fredplugins.common.utils.{ShimUtils, TWorldPoint}
import net.runelite.api.*
import net.runelite.api.coords.{LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.events.*
import net.runelite.api.gameval.{AnimationID, InterfaceID, ItemID, NpcID}

import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

package object coxhelper {
	case class DangerousProjectile(p: Projectile, tpe: CoxProjectile, impactOnTick: Int)

	sealed trait CoxProjectile(val pid: Int) extends enumeratum.EnumEntry {}

	object CoxProjectiles extends enumeratum.Enum[CoxProjectile] with ShimUtils.Logging() {
		case object Olm_FallingCrystal extends CoxProjectile(ProjectileID.OLM_FALLING_CRYSTAL)
		case object Olm_FallingCrystalTrail extends CoxProjectile(ProjectileID.OLM_FALLING_CRYSTAL_TRAIL)
		case object Olm_Burning extends CoxProjectile(ProjectileID.OLM_BURNING)
		case object Olm_AcidTrail extends CoxProjectile(ProjectileID.OLM_ACID_TRAIL)
		case object Olm_FireLine extends CoxProjectile(ProjectileID.OLM_FIRE_LINE)
		case object Olm_MageAttack extends CoxProjectile(ProjectileID.OLM_MAGE_ATTACK)
		case object Olm_RangeAttack extends CoxProjectile(ProjectileID.OLM_RANGE_ATTACK)
		case object Shaman_Aoe extends CoxProjectile(ProjectileID.LIZARDMAN_SHAMAN_AOE)
		case object IceDemon_Ranged extends CoxProjectile(ProjectileID.ICE_DEMON_RANGED_AOE)
		case object IceDemon_Mage extends CoxProjectile(ProjectileID.ICE_DEMON_ICE_BARRAGE_AOE)
		case object Tekton_Meteor extends CoxProjectile(ProjectileID.TEKTON_METEOR_AOE)
		def unapply(v: Projectile): Option[CoxProjectile] = {
			Option(v).map(_.getId).flatMap(pid => values.find(_.pid == pid))
		}

		override def values: IndexedSeq[CoxProjectile] = findValues
	}
}