package com.fredplugins.common.utils

import net.runelite.api.Actor

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

object ReflectionUtils extends ShimUtils.Logging("DEBUG") {
	object iterableNodeDequeueData {
		val clazz   : Class[?] = this.getClass.getClassLoader.loadClass("qn")
//		@ObfuscatedName("at")
//		@ObfuscatedSignature(
//			descriptor = "(Luk;)V"
//		)
//		@Export("addFirst")
		val addFirst: Method   = clazz.getDeclaredMethods.find(m => {
			m.getName.equals("at") && m.getReturnType == Void.TYPE && m.getParameterCount == 1 && m.getParameters.apply(0).getType.getName.equals("uk")
		}).get.tap(_.setAccessible(true))

//		@ObfuscatedName("ac")
//		@ObfuscatedSignature(
//			descriptor = "(Luk;)V"
//		)
//		@Export("addLast")
		val addLastMethod: Method = clazz.getDeclaredMethods.find(m => {
			m.getName.equals("ac") && m.getReturnType == Void.TYPE && m.getParameterCount == 1 && m.getParameters.apply(0).getType.getName.equals("uk")
		}).get.tap(_.setAccessible(true))
//		@ObfuscatedName("ap")
//		@ObfuscatedSignature(
//			descriptor = "()Luk;"
//		)
//		@Export("last")
		val lastMethod   : Method = clazz.getDeclaredMethods.find(m => {
			m.getName.equals("ap") && m.getReturnType.getName.equals("uk")
		}).get.tap(_.setAccessible(true))

//		@ObfuscatedName("ao")
//		@ObfuscatedSignature(
//			descriptor = "()Luk;"
//		)
//		@Export("previous")
		val previousMethod: Method = clazz.getDeclaredMethods.find(m => {
			m.getName.equals("ao") && m.getReturnType.getName.equals("uk")
		}).get.tap(_.setAccessible(true))
	}

	def getHealthbars(a: Actor): Unit = {
		val healthbarsField = a.getClass.getDeclaredField("bq").tap(_.setAccessible(true))
		val healthBarsValue = healthbarsField.get(a)

		val node = iterableNodeDequeueData.lastMethod.invoke(healthBarsValue)
		log.debug(s"healthBars: ${healthBarsValue}, healthBars.last: ${node}")
	}
	private def classToFieldNameMap(clazzes: Class[?]*)(childOnly: Boolean = false): Map[Int, String] = {

		def checkClassModifier(c: Class[?]): Boolean = c.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)

		def checkFieldModifier(f: Field): Boolean = f.getType == Integer.TYPE && f.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)

		val topLevel: Seq[(Int, String)] = clazzes.flatMap(c => c.getDeclaredFields.filter(checkFieldModifier))
			.map(f => f.getInt(null) -> f.getName)

		val subLevel = clazzes.flatMap(c => c.getDeclaredClasses.filter(checkClassModifier))
			.map(sc => sc.getDeclaredFields.filter(checkFieldModifier).map(f => f.getInt(null) -> s"${sc.getSimpleName}.${f.getName}"))
			.flatten

		val merged = (if(childOnly) subLevel else (topLevel.appendedAll(subLevel))).groupBy(_._1).map(j => j._1 -> j._2.map(_._2))
		val dups   = merged.filter(_._2.size > 1)
		if (dups.size > 0) {
			log.warn(s"Found {} dups\n{}", dups.size, dups.map(j => s"\t${j._1} = ${j._2.mkString("[", ", ", "]")}").mkString("\n"))
		}
		merged.filter(_._2.size == 1).map(x => x._1 -> x._2.head)
	}
	private lazy val npcIdToNameMap          : Map[Int, String] = classToFieldNameMap(classOf[net.runelite.api.gameval.NpcID])()
	private lazy val animationIdToNameMap    : Map[Int, String] = classToFieldNameMap(classOf[net.runelite.api.gameval.AnimationID])()
	private lazy val itemIdToNameMap         : Map[Int, String] = classToFieldNameMap(classOf[net.runelite.api.gameval.ItemID])()
	private lazy val spriteIdToNameMap       : Map[Int, String] = classToFieldNameMap(classOf[net.runelite.api.gameval.SpriteID])()
	private lazy val interfaceIdToNameMap    : Map[Int, String] = classToFieldNameMap(classOf[net.runelite.api.gameval.InterfaceID])(true)
	private lazy val inventoryIdToNameMap    : Map[Int, String] = classToFieldNameMap(classOf[net.runelite.api.gameval.InventoryID])()
	private lazy val spotAnimationIdToNameMap: Map[Int, String] = classToFieldNameMap(classOf[net.runelite.api.gameval.SpotanimID])()
	private lazy val objectIdToNameMap       : Map[Int, String] = classToFieldNameMap(
		classOf[net.runelite.api.gameval.ObjectID],
		classOf[net.runelite.api.gameval.ObjectID1]
	)()
	val varbitIdToNameMap: Map[Int, String] = classToFieldNameMap(classOf[net.runelite.api.gameval.VarbitID])()

	def getNpcName(id: Int): String = if(id == -1) "Null" else npcIdToNameMap.getOrElse(id, s"Npc(${id})")
	def getNpcId(name: String): Int = npcIdToNameMap.map(_.swap).get(name).getOrElse(name.stripPrefix("Npc(").stripSuffix(")").toIntOption.getOrElse(-1))

	def getAnimationName(id: Int): String = if(id == -1) "Idle" else animationIdToNameMap.getOrElse(id, s"Animation(${id})")
	def getItemName(id: Int): String = itemIdToNameMap.getOrElse(id, s"Item(${id})")
	def getItemId(name: String): Int = itemIdToNameMap.map(_.swap).get(name).getOrElse(name.stripPrefix("Item(").stripSuffix(")").toIntOption.getOrElse(-1))

	def getSpriteName(id: Int): String = spriteIdToNameMap.getOrElse(id, s"Sprite(${id})")
	def getInterfaceName(id: Int): String = interfaceIdToNameMap.getOrElse(id, s"Interface(${id})")
	def getInventoryName(id: Int): String = inventoryIdToNameMap.getOrElse(id, s"Inventory(${id})")
	def getSpotAnimationName(id: Int): String = spotAnimationIdToNameMap.getOrElse(id, s"SpotAnimation(${id})")
	def getObjectName(id: Int): String = objectIdToNameMap.getOrElse(id, s"Object(${id})")
	def getVarbitName(id: Int): String = varbitIdToNameMap.getOrElse(id, s"Varbit(${id})")
//	  public int getHeadbarPercent(RSHealthBar headbar) {
	//    if (headbar == null || headbar.getDefinition() == null) {
	//      return 100;
	//    }
	//
	//    RSHitUpdate update = (RSHitUpdate) headbar.getHitsplats().getSentinel().getNext();
	//    if (update == null) {
	//      return 100;
	//    }
	//
	//    RSHealthBarDefinition def = headbar.getDefinition();
	//    int scale;
	//    if (def == null || def.getUnderlaySpriteId() == -1 || def.getOverlaySpriteId() == -1) {
	//      scale = getMaxHealthBarWidth();
	//    } else {
	//      int pad = def.getPadding();
	//      RSSprite overlaySprite = def.getOverlaySprite(); //make sure to load on game thread
	//      if (overlaySprite == null) {
	//        scale = Math.max(1, def.getMaxWidth() - 2 * pad);
	//      } else {
	//        int spriteWidth = overlaySprite.getWidth();
	//        scale = spriteWidth - 2 * (pad * 2 < spriteWidth ? pad : 0);
	//      }
	//    }
	//
	//    int elapsed = Game.getEngineCycle() - update.getStartCycle();
	//    int widthNow = scale * update.getCurrentWidth() / scale;
	//
	//    int interpolatedWidth;
	//    if (update.getCurrentCycle() <= elapsed) {
	//      interpolatedWidth = widthNow;
	//    } else {
	//      int startScaled = scale * update.getStartWidth() / scale;
	//      interpolatedWidth = elapsed * (widthNow - startScaled) / update.getCurrentCycle() + startScaled;
	//    }
	//
	//    if (update.getCurrentWidth() > 0 && interpolatedWidth < 1) {
	//      interpolatedWidth = 1;
	//    }
	//
	//    return (int) Math.ceil(100.0 * interpolatedWidth / scale);
	//  }
	//
	//  private int getMaxHealthBarWidth() {
	//    RSHealthBar provider = getHealthBar();
	//    if (provider == null || provider.getDefinition() == null) {
	//      return 30;
	//    }
	//
	//    return provider.getDefinition().getMaxWidth();
	//  }
}
