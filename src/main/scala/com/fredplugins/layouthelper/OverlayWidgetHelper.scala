package com.fredplugins.layouthelper

import com.fredplugins.layouthelper.OverlayWidgetHelper.{componentIdField, snappableField}
import com.lucidplugins.api.spells.WidgetInfo
import com.lucidplugins.api.utils.MessageUtils
import net.runelite.client.ui.overlay.{Overlay, OverlayManager, OverlayPosition, WidgetOverlay}

import java.awt.{Color, Dimension, Point}
import java.lang.reflect.{Field, Method}
import scala.util.chaining.*
import scala.jdk.CollectionConverters.*
object OverlayWidgetHelper {
  val componentIdField: Field = classOf[WidgetOverlay].getDeclaredField("componentId").tap(_.setAccessible(true))
  val snappableField: Field = classOf[WidgetOverlay].getDeclaredField("snappable").tap(_.setAccessible(true))
  val overlayManager_getOverlaysMethod: Method = classOf[OverlayManager].getDeclaredMethod("getOverlays").tap(_.setAccessible(true))
//  def setup(componentIdFieldTemp: Field,overlayManager_getOverlaysMethodTemp: Method ): Unit = {
//    componentIdField.setAccessible(true)
//    overlayManager_getOverlaysMethod.setAccessible(true)
//  }

  def getOverlays(manager: OverlayManager): List[OverlayWidgetHelper] = overlayManager_getOverlaysMethod.invoke(manager).asInstanceOf[java.util.List[Overlay]].asScala.toList.flatMap(o =>
    Option.when(o.isInstanceOf[WidgetOverlay]) {
      OverlayWidgetHelper(o.asInstanceOf[WidgetOverlay])
    }
  )
  def getOverlaysJava(manager: OverlayManager): java.util.Collection[OverlayWidgetHelper] = getOverlays(manager).asJavaCollection
}
class OverlayWidgetHelper(wo: WidgetOverlay) {
  var cachedLocation : Point = wo.getPreferredLocation

  def moved: Boolean = {
    (preferredLocation != cachedLocation).tap(result => {
//      MessageUtils.addMessage(name.reverse.takeWhile(_ != '_').reverse.toString + " | " + preferredLocation + " | " + cachedLocation, if(result) Color.RED else Color.WHITE);
    })
  }

  def cache(): Unit = {
    cachedLocation = preferredLocation
  }

  val componentId: Int = componentIdField.get(wo).asInstanceOf[Int]
  def groupId: Int = WidgetInfo.TO_GROUP(componentId)
  def childId: Int = WidgetInfo.TO_CHILD(componentId)

  def movable: Boolean = wo.isMovable
  def resettable: Boolean = wo.isResettable
  def resizable: Boolean = wo.isResizable
  def name: String = wo.getName

  def snappable: Boolean = wo.isSnappable
  def snappable_=(value: Boolean): Unit = snappableField.set(wo, value)

//  def getPreferredLocation: Point = {
//      wo.getPreferredLocation
//  }
  def preferredLocation: Point = wo.getPreferredLocation
//  def preferredLocation_=(p: Point): Unit = {
//    if(preferredLocation == null || p == null) {
//      wo.setPreferredLocation(p)
//    } else {
//      preferredLocation.setLocation(p)
//    }
//  }

  def preferredLocation_=(p: (Int, Int)): Unit = {
    if (preferredLocation == null) {
      wo.setPreferredLocation(new Point(p._1, p._2))
    } else {
      preferredLocation.setLocation(p._1, p._2)
    }
  }

  def preferredLocationEqJava(p1: Int, p2: Int): Unit = {
    preferredLocation = (p1, p2)
  }

//  def setPreferredLocation(pl: Option[(Int, Int)]): Unit = {
//    Option(getPreferredLocation).tap(xx => {
//      if(xx.isDefined)
//        xx.foreach(plp => plp.setLocation(pl.map(x => new Point(x._1, x._2)).orNull))
//      else
//        wo.setPreferredLocation(pl.map(t => new Point(t._1, t._2)).orNull)
//    })
//  }
//
//  def setPreferredLocation(x: Int, y: Int): Unit = {
//    setPreferredLocation(Option.apply((x, y)))
//  }

  def reset(): Unit = {
    wo.setPreferredLocation(null)
    wo.revalidate()
  }

  def preferredSize: Dimension = {
    wo.getPreferredSize
  }

  def preferredPosition: OverlayPosition = {
    wo.getPreferredPosition
  }
}
