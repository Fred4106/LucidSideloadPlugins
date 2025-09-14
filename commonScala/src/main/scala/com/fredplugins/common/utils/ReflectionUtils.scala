package com.fredplugins.common.utils

import net.runelite.api.Actor

import java.lang.reflect.Method
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
