package com.fredplugins.valeTotems

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.valeTotems.TotemVarbits.DECORATIONS
import com.fredplugins.valeTotems.TotemVarbits.POINTS
import com.google.inject.Inject
import com.google.inject.Singleton
import ethanApiPlugin.collections.TileObjects
import net.runelite.api.Client
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.VarbitChanged
import net.runelite.client.callback.ClientThread

import scala.jdk.OptionConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

case class TotemState(carved: Boolean, decoration: Int, decay: Int, base: Int, animals: (Int, Int, Int), progress: (Int, Int, Int), points: Int) {
}
object TotemState {
	def changeAnimal(idx: Int, v: Int): TotemState => TotemState = (s: TotemState) => {
		s.copy(animals =
			(idx match {
				case 1 => s.animals.copy(_1 = v)
				case 2 => s.animals.copy(_2 = v)
				case 3 => s.animals.copy(_3 = v)
			})
		)
	}
	def changeProgress(idx: Int, v: Int): TotemState => TotemState = (s: TotemState) => {
		s.copy(progress =
			(idx match {
				case 1 => s.progress.copy(_1 = v)//(v, s.progress._1, s.progress._2)//(_1 = v)
				case 2 => s.progress.copy(_2 = v)
				case 3 => s.progress.copy(_3 = v)
			})
		)
	}
}
//", carved=" + this.isCarved + ", decoration=" + this.getDecoration + ", decay=" + this.getDecay + ", base=" + this.getBase + ", animals=" + java.util.Arrays.toString(this.getAnimals) + ", progress=" + this.getProgress + ", points=" + this.getPoints + ")"

@Singleton
class TotemService @Inject()(val client: Client, val clientThread: ClientThread) extends ShimUtils.Logging() {
	private lazy val state: collection.mutable.Map[Totem, TotemState]= {
		import TotemVarbits.{ANIMAL_1, ANIMAL_2, ANIMAL_3, BASE, BASE_CARVED, DECAY, LOW, MID, TOP}
		
		def vbValue(vb: TotemVarbit)(using totem: Totem): Int = client.getVarbitValue(vb.getRealVarbitId(totem))

		clientThread.runOnClientThread(() => {
			Totems.values.map(t => {
				given Totem = t
				TotemState(
					carved = vbValue(BASE_CARVED) == 1,
					decoration = vbValue(DECORATIONS),
					decay = vbValue(DECAY),
					base = vbValue(BASE),
					animals = (vbValue(ANIMAL_1), vbValue(ANIMAL_2), vbValue(ANIMAL_3)),
					progress = (vbValue(LOW), vbValue(MID), vbValue(TOP)),
					points = vbValue(POINTS)
				) -> summon[Totem]
			}).map(_.swap).pipe(collection.mutable.Map.from(_))
		})
	}

	def onVarbitChanged(vb: VarbitChanged): Unit = {
		val found = Totems.values.flatMap(_.isVarbitRelated(vb.getVarbitId)).headOption

		found.foreach {
			case (t, vbDef) => log.debug(s"${t.entryName}.${vbDef.entryName}(${vbDef.getRealVarbitId(t)}) = ${vb.getValue}")
		}

		log.debug(s"TotemService.onVarbitChanged(${vb.toString}) found ${found}")
		if(found.isEmpty) {
			return
		} else {

		found.flatMap(f => Option(f._2).collect{
			case TotemVarbits.ANIMAL_1 => TotemState.changeAnimal(1, vb.getValue)
			case TotemVarbits.ANIMAL_2 => TotemState.changeAnimal(2, vb.getValue)
			case TotemVarbits.ANIMAL_3 => TotemState.changeAnimal(3, vb.getValue)
			case TotemVarbits.BASE => (_: TotemState).copy(base = vb.getValue)
			case TotemVarbits.BASE_CARVED => (_: TotemState).copy(carved = vb.getValue == 1)
			case TotemVarbits.DECAY => (_: TotemState).copy(decay = vb.getValue)
			case TotemVarbits.DECORATIONS => (_: TotemState).copy(decoration = vb.getValue)
			case TotemVarbits.POINTS => (_: TotemState).copy(points = vb.getValue)
			case TotemVarbits.LOW => TotemState.changeProgress(1, vb.getValue)
			case TotemVarbits.MID => TotemState.changeProgress(2, vb.getValue)
			case TotemVarbits.TOP => TotemState.changeProgress(3, vb.getValue)
		}.map(updateFunc => f._1 -> updateFunc))
			.foreach{
				case (totem, updateFunc) => {
					val oValue = getState(totem)
					val nValue = updateFunc(oValue)
					log.debug(s"state(${totem}) changed from $oValue to $nValue")
					state.update(totem, nValue)
				}
			}
		}
	}

	private var closestTotem: Totem = null

	def updateClosestTotem(p: WorldPoint): Unit = {
		val foundTotemObject = TileObjects.search().withId(Totems.values.map(_.baseObjId).toList *)
			//.filter(to => to.getWorldLocation.distanceTo(p) < 15)
			.nearestToPoint(p).toScala
		val foundTotem = foundTotemObject.flatMap(to => {
			Totems.values.find(_.baseObjId == to.getId)
		}).orNull
		if(foundTotem != closestTotem) {
			log.debug(s"Updating closest totem from ${closestTotem} to ${foundTotem} on tick ${client.getTickCount}")
			closestTotem = foundTotem
		}
	}

	def getNearest: Option[Totem] = {
		Option(closestTotem)
	}

	def getState(totem: Totem): TotemState = {
		state.getOrElseUpdate(totem, TotemState(carved = false, decoration = 0, decay = 0, base = 0, animals = (0, 0, 0), progress = (0, 0, 0), points = 0))
	}
}