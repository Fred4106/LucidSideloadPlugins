package com.fredplugins.pvmDebugger

import com.google.common.collect.ImmutableMap
import net.runelite.api.NPC
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldPoint

import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

package object inferno {
	private[inferno] val waveMapping          : Map[Int, List[Int]] = Map(
			1 ->  List(32, 32, 32, 85),
			2 ->  List(32, 32, 32, 85, 85),
			3 ->  List(32, 32, 32, 32, 32, 32),
			4 ->  List(32, 32, 32, 165),
			5 ->  List(32, 32, 32, 85, 165),
			6 ->  List(32, 32, 32, 85, 85, 165),
			7 ->  List(32, 32, 32, 165, 165),
			8 ->  List(32, 32, 32, 32, 32, 32),
			9 ->  List(32, 32, 32, 240),
			10 -> List(32, 32, 32, 85, 240),
			11 -> List(32, 32, 32, 85, 85, 240),
			12 -> List(32, 32, 32, 165, 240),
			13 -> List(32, 32, 32, 85, 165, 240),
			14 -> List(32, 32, 32, 85, 85, 165, 240),
			15 -> List(32, 32, 32, 165, 165, 240),
			16 -> List(32, 32, 32, 240, 240),
			17 -> List(32, 32, 32, 32, 32, 32),
			18 -> List(32, 32, 32, 370),
			19 -> List(32, 32, 32, 85, 370),
			20 -> List(32, 32, 32, 85, 85, 370),
			21 -> List(32, 32, 32, 165, 370),
			22 -> List(32, 32, 32, 85, 165, 370),
			23 -> List(32, 32, 32, 85, 85, 165, 370),
			24 -> List(32, 32, 32, 165, 165, 370),
			25 -> List(32, 32, 32, 240, 370),
			26 -> List(32, 32, 32, 85, 240, 370),
			27 -> List(32, 32, 32, 85, 85, 240, 370),
			28 -> List(32, 32, 32, 165, 240, 370),
			29 -> List(32, 32, 32, 85, 165, 240, 370),
			30 -> List(32, 32, 32, 85, 85, 165, 240, 370),
			31 -> List(32, 32, 32, 165, 165, 240, 370),
			32 -> List(32, 32, 32, 240, 240, 370),
			33 -> List(32, 32, 32, 370, 370),
			34 -> List(32, 32, 32, 32, 32, 32),
			35 -> List(32, 32, 32, 490),
			36 -> List(32, 32, 32, 85, 490),
			37 -> List(32, 32, 32, 85, 85, 490),
			38 -> List(32, 32, 32, 165, 490),
			39 -> List(32, 32, 32, 85, 165, 490),
			40 -> List(32, 32, 32, 85, 85, 165, 490),
			41 -> List(32, 32, 32, 165, 165, 490),
			42 -> List(32, 32, 32, 240, 490),
			43 -> List(32, 32, 32, 85, 240, 490),
			44 -> List(32, 32, 32, 85, 85, 240, 490),
			45 -> List(32, 32, 32, 165, 240, 490),
			46 -> List(32, 32, 32, 85, 165, 240, 490),
			47 -> List(32, 32, 32, 85, 85, 165, 240, 490),
			48 -> List(32, 32, 32, 165, 165, 240, 490),
			49 -> List(32, 32, 32, 240, 240, 490),
			50 -> List(32, 32, 32, 370, 490),
			51 -> List(32, 32, 32, 85, 370, 490),
			52 -> List(32, 32, 32, 85, 85, 370, 490),
			53 -> List(32, 32, 32, 165, 370, 490),
			54 -> List(32, 32, 32, 85, 165, 370, 490),
			55 -> List(32, 32, 32, 85, 85, 165, 370, 490),
			56 -> List(32, 32, 32, 165, 165, 370, 490),
			57 -> List(32, 32, 32, 240, 370, 490),
			58 -> List(32, 32, 32, 85, 240, 370, 490),
			59 -> List(32, 32, 32, 85, 85, 240, 370, 490),
			60 -> List(32, 32, 32, 165, 240, 370, 490),
			61 -> List(32, 32, 32, 85, 165, 240, 370, 490),
			62 -> List(32, 32, 32, 85, 85, 165, 240, 370, 490),
			63 -> List(32, 32, 32, 165, 165, 240, 370, 490),
			64 -> List(32, 32, 32, 240, 240, 370, 490),
			65 -> List(32, 32, 32, 370, 370, 490),
			66 -> List(32, 32, 32, 490, 490),
			67 -> List(900),
			68 -> List(900, 900, 900),
			69 -> List(1400)
		)
//	private val npcNameMappingComplex: Map[Int, String]     = null
//	private val npcNameMappingSimple : Map[Int, String]     = null

	private[inferno] val npcNameMappings: Map[Int, (String, String)] = Map(
		32 -> ("Nibbler", "Jal-Nib"),
		85 -> ("Bat", "Jal-MejRah"),
		165 -> ("Blob", "Jal-Ak"),
		240 -> ("Meleer", "Jal-ImKot"),
		370 -> ("Ranger", "Jal-Xil"),
		490 -> ("Mage", "Jal-Zek"),
		900 -> ("Jad", "JalTok-Jad"),
		1400 -> ("Zuk","TzKal-Zuk")
	)

	class InfernoBlobDeathSpot(val location: LocalPoint) {
		val deathTime: Long = System.currentTimeMillis()
		private var ticksUntilDone: Int = InfernoData.BLOB_DEATH_TICKS

		def decrementTick(): Unit = {
			if (ticksUntilDone > 0) ticksUntilDone -= 1
		}

		def isDone: Boolean = ticksUntilDone eq 0

		def fillProgress: Double = (System.currentTimeMillis - deathTime) / ((InfernoData.BLOB_DEATH_TICKS - 1) * 600.0)

		def fillAlpha: Int = Math.min(Math.max(((1 - fillProgress) * InfernoData.FILL_START_ALPHA).asInstanceOf[Int], 0), 255)
	}
	class InfernoNpc(val npc: NPC) {
		lazy val tpe: InfernoNpcType           = InfernoNpcType.typeFromId(npc.getId)
		assert(tpe != null)

		private var nextAttack         : InfernoNpcAttack = tpe.getDefaultAttack
		private var ticksTillNextAttack: Int              = 0
		private var idleTicks          : Int              = 0
		private var lastAnimation                           = -1
		private var lastCanAttack                                   = false
		//0 = not in LOS, 1 = in LOS after move, 2 = in LOS
		private val safeSpotCache: mutable.Map[WorldPoint, Int] = mutable.HashMap.empty

		def getNextAttack: InfernoNpcAttack = nextAttack
		def getTicksTillNextAttack: Int = ticksTillNextAttack
		def getIdleTicks: Int = idleTicks
		def getLastAnimation: Int = lastAnimation
		def getLastCanAttack: Boolean = lastCanAttack
		def getSafespotCache: Map[WorldPoint, Int] = safeSpotCache.toMap
	}
}