package com.fredplugins.layouthelper

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.layouthelper.OverlayWidgetHelper.movableField
import com.fredplugins.layouthelper.OverlayWidgetHelper.originField
import com.fredplugins.layouthelper.OverlayWidgetHelper.originXField
import com.fredplugins.layouthelper.OverlayWidgetHelper.originYField
import com.fredplugins.layouthelper.OverlayWidgetHelper.{componentIdField, resizableField, snappableField}
import net.runelite.api.Client
import net.runelite.client.RuneLite
import net.runelite.client.chat.{ChatColorType, ChatMessageBuilder}
import net.runelite.client.ui.overlay.OverlayOrigin
import net.runelite.client.ui.overlay.OverlayOriginX
import net.runelite.client.ui.overlay.OverlayOriginY
import net.runelite.client.ui.overlay.WidgetOverlays
import net.runelite.client.ui.overlay.{Overlay, OverlayManager, OverlayPosition}
import org.slf4j.LoggerFactory
import packetUtils.WidgetInfoExtended

import java.awt.{Color, Dimension, Point}
import java.lang.reflect.{Field, Method}
import scala.jdk.CollectionConverters.*
import scala.util.chaining.*
object OverlayWidgetHelper extends ShimUtils.Logging("TRACE") {
  val componentIdField: Field = classOf[WidgetOverlays#WidgetOverlay].getDeclaredField("componentId").tap(_.setAccessible(true))
  val snappableField: Field = classOf[Overlay].getDeclaredField("snappable").tap(_.setAccessible(true))
  val resizableField: Field = classOf[Overlay].getDeclaredField("resizable").tap(_.setAccessible(true))
  val movableField: Field = classOf[Overlay].getDeclaredField("movable").tap(_.setAccessible(true))

  val originField: Field = classOf[Overlay].getDeclaredField("origin").tap(_.setAccessible(true))
  val originXField: Field = classOf[Overlay].getDeclaredField("originX").tap(_.setAccessible(true))
  val originYField: Field = classOf[Overlay].getDeclaredField("originY").tap(_.setAccessible(true))

  val overlayManager_getOverlaysMethod: Method = classOf[OverlayManager].getDeclaredMethod("getOverlays").tap(_.setAccessible(true))

  private val client = RuneLite.getInjector.getInstance(classOf[Client])

  def getOverlays(manager: OverlayManager): List[OverlayWidgetHelper] = overlayManager_getOverlaysMethod.invoke(manager).asInstanceOf[java.util.List[Overlay]].asScala.toList.flatMap(o =>
    Option.when(o.isInstanceOf[WidgetOverlays#WidgetOverlay]) {
      OverlayWidgetHelper(o.asInstanceOf[WidgetOverlays#WidgetOverlay])
    }
  )
  def getOverlay(manager: OverlayManager, componentId: Int): Option[OverlayWidgetHelper] = getOverlays(manager).find(w => w.componentId == componentId)
  def getOverlays(manager: OverlayManager, componentIds: Array[Int]): List[OverlayWidgetHelper] = getOverlays(manager).filter(w => componentIds.contains(w.componentId)).sortBy(x => componentIds.indexOf(x.componentId))

  def buildMessage(header: String, parts: (String, Any)*): String = {
    def add(func: ChatMessageBuilder => ChatMessageBuilder)(chatMessageBuilder: ChatMessageBuilder => ChatMessageBuilder) = chatMessageBuilder.andThen(func)
    parts.foldLeft(new ChatMessageBuilder().append(ChatColorType.NORMAL).append(header + "\n"))((a, b) => {
      (b._2 match {
        case (c: Color, s: Any) => Option(add(_.append(c, s.toString)))
        case null => Option.empty
        case x => Option(add(_.append(Color.BLUE, x.toString)))
      }).map(_.apply(_.append("[").append(b._1).append(": ")).andThen(_.append(ChatColorType.NORMAL).append("]\n"))).map(_.apply(a)).getOrElse(a)
    }).build().stripTrailing().stripSuffix(",").stripSuffix("<br>")
  }

//  private def sendMessage(chatMessageManager:ChatMessageManager, tpe: ChatMessageType, message: String, color: Color = null): Unit = {
//    chatMessageManager.queue(QueuedMessage.builder.`type`(tpe).runeLiteFormattedMessage(message).build)
//  }
  def update(reference: OverlayWidgetHelper, toMove: OverlayWidgetHelper *): Unit = {
    toMove.foreach(tm => {
      val offset = (reference.name(), tm.name()) match {
        case ("PARENT", "TABS1") => (-14, 273 + 36)
        case ("PARENT", "TABS2") => (-14, 273)
        case ("TABS1", "TABS2") => (0, -36)
        case ("TABS2", "TABS1") => (0, 36)
        case ("TABS1", "PARENT") => (14, -273 - 36)
        case ("TABS2", "PARENT") => (14, -273)
      }
      if(reference.preferredLocation != null) {
        if(reference.preferredPosition == null) {
          val refLoc = reference.preferredLocation.pipe(p => (p.x, p.y))
          tm.preferredLocation = offset//(refLoc._1 + offset._1, refLoc._2 + offset._2)
        } else {
          tm.preferredPosition = reference.preferredPosition
          tm.wo.setPreferredLocation(null)
        }
      } else {
        tm.preferredPosition = reference.preferredPosition
        tm.wo.setPreferredLocation(null)
      }
    })
  }

  def tick(overlays: OverlayWidgetHelper*): Unit = {
    overlays.find(o => o.moved) match {
      case Some(m) => {
        log.trace("{} moved", m.name(false))
        val toMove = overlays.filterNot(_ == m)
        update(m, toMove*)
        overlays.foreach(o => o.cache())
        overlays.foreach(_.wo.revalidate())
        overlays.foreach(o => {
          log.trace("{}", o)
        })
      }
      case None => {}
    }
  }
}

class OverlayWidgetHelper(private val wo: WidgetOverlays#WidgetOverlay) {
  var cachedLocation : Point = wo.getPreferredLocation
  var cachedPosition : OverlayPosition = wo.getPreferredPosition
  var cachedOrigin: (OverlayOrigin, OverlayOriginX, OverlayOriginY) = getOrigin

  def size: Dimension = wo.getBounds.getSize
  def moved: Boolean = preferredLocation != cachedLocation || preferredPosition != cachedPosition
  def originChanged: Boolean = (origin, originX, originY) != cachedOrigin
  def cache(): Unit = {
    cachedLocation = preferredLocation
    cachedPosition = preferredPosition
    cachedOrigin = getOrigin
  }

  val componentId: Int = componentIdField.get(wo).asInstanceOf[Int]
  def groupId: Int = WidgetInfoExtended.TO_GROUP(componentId)
  def childId: Int = WidgetInfoExtended.TO_CHILD(componentId)

  def movable: Boolean = wo.isMovable
  def movable_=(b: Boolean): Unit = movableField.set(wo, b)

  def resettable: Boolean = wo.isResettable
  def resettable_=(b: Boolean): Unit = wo.setResettable(b)
  def resizable: Boolean = wo.isResizable
  def reset(om: OverlayManager): Unit = om.resetOverlay(wo)
  def revalidate(): Unit = wo.revalidate();
  def resizable_=(b: Boolean): Unit = resizableField.set(wo, b)
  def name(short: Boolean = true): String = Option.when(short)((a: String) => a.reverse.takeWhile(_ != '_').reverse)
                                                .getOrElse((j: String) => j)(wo.getName)

  def snappable: Boolean = wo.isSnappable
  def snappable_=(value: Boolean): Unit = snappableField.set(wo, value)

  def origin: OverlayOrigin = originField.get(wo).asInstanceOf[OverlayOrigin]
  def origin_=(value: OverlayOrigin): Unit = originField.set(wo, value)

  def originX: OverlayOriginX = originXField.get(wo).asInstanceOf[OverlayOriginX]
  def originX_=(value: OverlayOriginX): Unit = originXField.set(wo, value)

  def originY: OverlayOriginY = originYField.get(wo).asInstanceOf[OverlayOriginY]
  def originY_=(value: OverlayOriginY): Unit = originYField.set(wo, value)

  def getOrigin: (OverlayOrigin, OverlayOriginX, OverlayOriginY) = (origin, originX, originY)

  def preferredLocation: Point = wo.getPreferredLocation

  def preferredLocation_=(p: (Int, Int)): Unit = {
    if (preferredLocation == null) wo.setPreferredLocation(new Point(p._1, p._2)) else preferredLocation.setLocation(p._1, p._2)
  }
  def preferredSize: Dimension = wo.getPreferredSize

  def preferredPosition: OverlayPosition = wo.getPreferredPosition

  def preferredPosition_=(other: OverlayPosition): Unit = {
    wo.setPreferredPosition(other)
  }

  override def toString: String = List.apply(
    s"widgetInfo=$groupId:$childId",
    s"pos=$preferredPosition",
    s"loc=$preferredLocation",
    s"snap=$snappable",
    s"movable=$movable",
    s"resizeable=$resizable",
    s"origin=${getOrigin}"
  ).mkString("(", ", ", ")").prependedAll(s"${name(false)}")
}
