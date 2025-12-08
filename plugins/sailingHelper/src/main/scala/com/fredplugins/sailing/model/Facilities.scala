package com.fredplugins.sailing.model

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.sailing.FredsSailingConfig
import com.fredplugins.sailing.PluginLifecycleComponent
import com.fredplugins.sailing.SailingUtils
import com.fredplugins.sailing.features.BarracudaTrialHelper.LOST_CARGO_IDS
import com.fredplugins.sailing.features.BarracudaTrialHelper.OBJECTIVE_VARBIT_IDS
import com.fredplugins.sailing.features.BarracudaTrialHelper.PILLAR_VARBIT_IDS
import com.google.inject.Inject
import net.runelite.api.Client
import net.runelite.api.DynamicObject
import net.runelite.api.GameObject
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.GameObjectDespawned
import net.runelite.api.events.GameObjectSpawned
import net.runelite.api.events.VarbitChanged
import net.runelite.api.events.WorldViewUnloaded
import net.runelite.api.gameval.ObjectID1.{SAILING_BT_JUBBLY_JIVE_COLLECTABLE_1, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_10, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_11, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_12, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_13, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_14, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_15, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_16, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_17, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_18, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_19, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_2, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_20, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_21, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_22, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_23, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_24, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_25, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_26, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_27, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_28, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_29, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_3, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_30, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_31, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_32, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_33, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_34, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_35, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_36, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_37, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_38, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_39, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_4, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_40, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_41, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_42, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_43, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_44, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_45, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_46, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_47, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_48, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_49, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_5, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_50, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_51, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_52, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_53, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_54, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_55, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_56, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_6, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_7, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_8, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_9, SAILING_BT_JUBBLY_JIVE_COLLECTABLE_SUPPLIES}
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.ui.FontManager
import net.runelite.client.ui.overlay.Overlay
import net.runelite.client.ui.overlay.OverlayLayer
import net.runelite.client.ui.overlay.OverlayPosition
import net.runelite.client.ui.overlay.OverlayUtil
//import com.fredplugins.common.extensions.ObjectExtensions.{composition, impostorComposition, isImpostor, morphId, wrapped}
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.api.WorldRegion

import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics2D
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

sealed trait SubFacility extends enumeratum.EnumEntry {
	def parent: Facility
}

sealed trait Facility extends enumeratum.EnumEntry {self=>
	sealed trait SubType extends SubFacility {
		def parent: self.type = self
	}
	def Configurations: enumeratum.Enum[? <: SubType]
	def get(obj: GameObject): Option[? <: SubType]
}

object Facilities extends enumeratum.Enum[Facility] with ShimUtils.Logging() {
	case object CrystalExtractor extends Facility {
		sealed trait ObjIdTrait(val objId: Int, val animId: Int = -1) extends SubType
		object Configurations extends enumeratum.Enum[ObjIdTrait] {
			case object Finished extends ObjIdTrait(59702, 13177)
			case object Enabled extends ObjIdTrait(59702)
			case object Disabled extends ObjIdTrait(59703)
			override def values: IndexedSeq[ObjIdTrait] = findValues
		}
		def get(obj: GameObject): Option[ObjIdTrait] = {
			val objId =  Option(obj).map(_.getId).getOrElse(-1)
			val objAnimation = Option(obj).map(_.getRenderable).collect {
				case d: DynamicObject => d.getAnimation.getId
			}.getOrElse(-1)
//			log.debug("objId: {}, objAnim: {}", objId, objAnimation)
			Configurations.values
				.groupBy(_.objId)
				.getOrElse(objId, Seq.empty[ObjIdTrait])
				.sortBy(yy => (if(yy.animId != -1) 0 else 1000000) + yy.animId).find(yy => yy.animId == objAnimation || yy.animId == -1)
		}
	}

	case object WindCatcher extends Facility {

		sealed trait ObjIdTrait(val objId: Int) extends SubType
		object Configurations extends enumeratum.Enum[ObjIdTrait] {
			case object WindEnabled extends  ObjIdTrait(59683)
			case object WindDisabled extends ObjIdTrait(59684)
			case object GaleDisabled extends ObjIdTrait(59685)
			case object GaleEnabled extends  ObjIdTrait(59686)
			override def values: IndexedSeq[ObjIdTrait] = findValues
		}
		def get(obj: GameObject): Option[ObjIdTrait] = {
			val id: Int = Option(obj).map(_.getId).getOrElse(-1)
			Configurations.values.find(_.objId == id)
		}
	}

	override def values: IndexedSeq[Facility] = findValues

	def getForObj(go: GameObject): Option[Facility#SubType] = {
		val x: Option[Facility#SubType] = values.flatMap(f => f.get(go).map(fs => f->fs)).headOption.map(_._2)
		x
	}
}