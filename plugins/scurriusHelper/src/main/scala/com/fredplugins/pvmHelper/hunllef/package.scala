package com.fredplugins.pvmHelper

import com.fredplugins.common.Locatable.{LocatableType, distanceTo, findSceneCord, findWorldCord}
import com.fredplugins.common.utils.{ShimUtils, WorldPointUtils}
import com.lucidplugins.api.utils.{CombatUtils, EquipmentUtils, InventoryUtils}
import ethanApiPlugin.collections.Inventory
import net.runelite.api.coords.{LocalPoint, WorldArea, WorldPoint}
import net.runelite.api.events.GameTick
import net.runelite.api.{Client, NPC, NpcID, NullNpcID, Prayer, Projectile, Skill, Tile}
import net.runelite.client.eventbus.EventBus
import org.slf4j.Logger

import scala.compiletime.uninitialized
import scala.util.chaining.*
import java.awt.image.BufferedImage
import scala.collection.mutable
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.util.Try

package object hunllef {
	val log: Logger = ShimUtils.getLogger("com.fredplugins.pvmHelper.hunllef", "DEBUG")
//	enum RoamingTornadoSegment(tiles: IndexedSeq[(Int, Int, Int, Int)]) {
//		case NORTH extends RoamingTornadoSegment(
//			(0 until 11).map(i => 50 -> (51+i)).map(x => (7768, x._1, x._2, 1))
//		)
//		case SE extends RoamingTornadoSegment(
//			(0 until 11).map(i => (51+i) -> (60-i)).map(x => (7768, x._1, x._2, 1))
//		)
//		case WEST extends RoamingTornadoSegment(
//			(0 until 11).map(i => (60-i) -> 50).map(x => (7768, x._1, x._2, 1))
//		)
//		case NE extends RoamingTornadoSegment(
//			(0 until 11).map(i => (51+i) -> (51+i)).map(x => (7768, x._1, x._2, 1))
//		)
//		case SOUTH extends RoamingTornadoSegment(
//			(0 until 11).map(i => 51 -> (60 - i)).map(x => (7768, x._1, x._2, 1))
//		)
//		case NW extends RoamingTornadoSegment(
//			(0 until 11).map(i => (60-i) -> (51+i)).map(x => (7768, x._1, x._2, 1))
//		)
//		case EAST extends RoamingTornadoSegment(
//			(0 until 11).map(i => (51+i) -> 61).map(x => (7768, x._1, x._2, 1))
//		)
//		case SW extends RoamingTornadoSegment(
//			(0 until 11).map(i => (60-i) -> (60-i)).map(x => (7768, x._1, x._2, 1))
//		)
//
//		lazy val worldPoints: Seq[WorldPoint] = tiles.map((a, b, c, d) => WorldPoint.fromRegion(a, b,c,d))
//		def instancePoints(using client: Client): Seq[(WorldPoint, Seq[WorldPoint])] =  worldPoints.map(wp => wp -> WorldPoint.toLocalInstance(client.getScene, wp).asScala.toSeq)
//	}
//
//	object RoamingTornadoSegment {
//		type RoamingTornadoAddress = (RoamingTornadoSegment, Int)
//		def getFromSpawnRoomLoc(wp: WorldPoint)(using client: Client): RoamingTornadoAddress = {
//			client.getScene.getTiles
//			println(wp)
//
////			val temp = Seq(NORTH, WEST, SOUTH, EAST).filter(seg => {
////				seg.instancePoints.exists(xx => {xx._2.contains(wp)})
////			}).map(seg => {
////				seg.instancePoints.indexWhere(_._2.contains(wp)).map(seg.instancePoints.indexOf(xx) => xx -> seg).map(_.swap)
////			})
//
//			log.debug("Searching for {} and found {}", wp, temp)
//		}
//		extension (in: RoamingTornadoAddress) {
//			def toWorldPoint(using client: Client): WorldPoint = {
//				assert(in._2 >= 0 && in._2 < 11)
//				in._1.instancePoints.toMap.apply(in._2).head
//			}
//			def getNext(jump: Int = 1): RoamingTornadoAddress = {
//				val nextIdx = ((in._1.ordinal * 11) + in._2 + jump)%88
//				val (segIdx, tIdx) = ((nextIdx / 11) -> (nextIdx%11))
////				val tIdx = (in._2 + jump) % 11
////				val segIdx = (in._1.ordinal + ((in._2 + jump)/11)) % RoamingTornadoSegment.values.length
//				val res = RoamingTornadoSegment.fromOrdinal(segIdx) -> tIdx
//				res
//			}
//		}
//	}

	sealed trait HunllefCycle {}

	case class Range(couldBeInverted: Boolean = false) extends HunllefCycle {}
	case object Mage extends HunllefCycle {}

//
	val HunllefIds: List[Int] = List(NpcID.CRYSTALLINE_HUNLLEF, NpcID.CRYSTALLINE_HUNLLEF_9022,
				NpcID.CRYSTALLINE_HUNLLEF_9023, NpcID.CRYSTALLINE_HUNLLEF_9024,
				NpcID.CORRUPTED_HUNLLEF, NpcID.CORRUPTED_HUNLLEF_9036,
				NpcID.CORRUPTED_HUNLLEF_9037, NpcID.CORRUPTED_HUNLLEF_9038,NpcID.CRYSTALLINE_HUNLLEF_12123)
	def isHunllef(n: NPC): Boolean = {
		HunllefIds.contains(n.getId)
	}

//	class HunllefInstanceData {
//		def worldPointTo
//	}

	abstract class Tornado(val wrapped: NPC)(using client: Client) {
		val spawnTick: Int = client.getTickCount
		//this is in tile space coords
		val spawnLoc: WorldPoint = getWorldLocation
		def getWorldLocation: WorldPoint = wrapped.getWorldLocation
		def getRoomPos: (Int, Int) = (getWorldLocation).pipe(a => (a.getX, a.getY))

		def age: Int = client.getTickCount - spawnTick
//		def getRoomCoord: (Int, Int) = WorldPwrapped.getWorldLocation
//		private var lastLoc: Option[(Int, Int)] =  None
//		private var lastdirectionV: Option[Direction] = None
//		private var directionV: Option[Direction] = None
//		def getHistory: List[(Int, (Int, Int))]  = tileHistory.toList
	}

	object Tornado {
		class ChaseTornado(w: NPC)(using client: Client) extends Tornado(w) {
			val diesOnTick: Int = spawnTick + 21
			def timeToLive: Int = Math.max(diesOnTick - client.getTickCount, 0)
		}

		class RoamingTornado(w: NPC)(using client: Client) extends Tornado(w) {
			def scenePos: (Int, Int) = LocalPoint.fromWorld(client, WorldPointUtils.fromInstance(w.getWorldLocation)).pipe(ll => (ll.getSceneX, ll.getSceneY))
		}

		def isTornado(wrapped: NPC): Boolean = {
			Set(NullNpcID.NULL_9025, /*NullNpcID.NULL_9039,*/ NullNpcID.NULL_14142).contains(wrapped.getId)
		}

		def apply(npc: NPC)(using client: Client): Option[Tornado] = {
			Option(npc.getId).collect {
				case NullNpcID.NULL_14142 => ChaseTornado(npc)
				case NullNpcID.NULL_9025 => RoamingTornado(npc)
			}
		}
	}

	sealed trait Action {
		val name: String
		def shouldRun(using client: Client): Boolean
		def run(): Unit
	}
	object Action {
		transparent trait ProductActionMixin {
			this: Action with	 Product =>
			override val name: String = this.asInstanceOf[Product].toString
		}

		transparent trait WieldActionMixin(id: Int)(using client: Client) {
			this: Action =>
			override val name: String = client.getItemDefinition(id).getName.pipe(n => {
				n.toLowerCase.stripSuffix("(perfected)").stripPrefix("corrupted").trim.capitalize
			}).pipe(in => s"Wield${in}")

			override def shouldRun(using client: Client): Boolean = !Option(EquipmentUtils.getWepSlotItem).exists(_.getId == id) && InventoryUtils.contains(id) && InventoryUtils.itemHasAction(id, "Wield")

			override def run(): Unit = {
				InventoryUtils.wieldItem(id)
			}
		}

		transparent trait PrayerActionMixin(prayers: Prayer*) {
			this: Action =>
			override val name: String = prayers.map(_.name.split('_').map(_.toLowerCase.capitalize).mkString).mkString("Pray(", ", ", ")")

			override def shouldRun(using client: Client): Boolean = prayers.exists(!client.isPrayerActive(_))

			override def run(): Unit = {
				prayers.foreach(p => {
					CombatUtils.activatePrayer(p)
				})
			}
		}

		case object ActivateSpec extends Action with ProductActionMixin {
			override def shouldRun(using client: Client): Boolean = CombatUtils.getSpecEnergy >= 25 && !CombatUtils.isSpecEnabled

			override def run(): Unit = {
				if (!CombatUtils.isSpecEnabled) CombatUtils.toggleSpec()
			}
		}

		case object DisableOverheads extends Action with ProductActionMixin {
			override def shouldRun(using client: Client): Boolean = CombatUtils.getActiveOverhead != null

			override def run(): Unit = {
				CombatUtils.deactivatePrayer(CombatUtils.getActiveOverhead)
			}
		}

		case object PrayRange extends Action with PrayerActionMixin(Prayer.PROTECT_FROM_MISSILES)
		case object PrayMagic extends Action with PrayerActionMixin(Prayer.PROTECT_FROM_MAGIC)

		def wield(id: Int)(using client: Client): Action = {
			new Action with WieldActionMixin(id) {}
		}

		def pray(p: Prayer*)(using client: Client): Action = {
			new Action with PrayerActionMixin(p *) {}
		}
	}
}
