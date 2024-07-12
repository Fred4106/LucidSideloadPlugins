package com.fredplugins.layouthelper

import com.fredplugins.layouthelper.OverlayWidgetHelper.{buildMessage, componentIdField, resizableField, snappableField}
import com.lucidplugins.api.spells.WidgetInfo
import com.lucidplugins.api.utils.MessageUtils
import net.runelite.api.ChatMessageType
import net.runelite.client.chat.{ChatColorType, ChatMessageBuilder, ChatMessageManager, QueuedMessage}
import net.runelite.client.ui.overlay.{Overlay, OverlayManager, OverlayPosition, WidgetOverlay}
import org.slf4j.{Logger, LoggerFactory}

import java.awt.{Color, Dimension, Point}
import java.lang.reflect.{Field, Method}
import scala.util.chaining.*
import scala.jdk.CollectionConverters.*
object OverlayWidgetHelper {
  private val log: org.slf4j.Logger = LoggerFactory.getLogger(classOf[OverlayWidgetHelper])

  val componentIdField: Field = classOf[WidgetOverlay].getDeclaredField("componentId").tap(_.setAccessible(true))
  val snappableField: Field = classOf[Overlay].getDeclaredField("snappable").tap(_.setAccessible(true))
  val resizableField: Field = classOf[Overlay].getDeclaredField("resizable").tap(_.setAccessible(true))
  val overlayManager_getOverlaysMethod: Method = classOf[OverlayManager].getDeclaredMethod("getOverlays").tap(_.setAccessible(true))

  def getOverlays(manager: OverlayManager): List[OverlayWidgetHelper] = overlayManager_getOverlaysMethod.invoke(manager).asInstanceOf[java.util.List[Overlay]].asScala.toList.flatMap(o =>
    Option.when(o.isInstanceOf[WidgetOverlay]) {
      OverlayWidgetHelper(o.asInstanceOf[WidgetOverlay])
    }
  )

  def buildMessage(header: String, parts: (String, Any)*): String = {
    def add(func: ChatMessageBuilder => ChatMessageBuilder)(chatMessageBuilder: ChatMessageBuilder => ChatMessageBuilder): ChatMessageBuilder => ChatMessageBuilder = {
      chatMessageBuilder.andThen(func)
    }
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
      val offset: (Int, Int) = (reference.name(), tm.name()) match {
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
          tm.preferredLocation = (refLoc._1 + offset._1, refLoc._2 + offset._2)
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

        log.info("{} moved", m.name(false))
        val toMove = overlays.filterNot(_ == m)
        update(m, toMove*)
        overlays.foreach(o => o.cache())
        overlays.foreach(_.wo.revalidate())
        overlays.foreach(o => {
          log.info("{}", o)
        })
      }
      case None => {}
    }
  }
}
class OverlayWidgetHelper(private val wo: WidgetOverlay) {
  var cachedLocation : Point = wo.getPreferredLocation
  var cachedPosition : OverlayPosition = wo.getPreferredPosition
  def size: Dimension = wo.getBounds.getSize
//  def pretty(full: Boolean): String =  {
//    if(full) {
//      buildMessage(name(false),
//        ("widgetInfo", (groupId, childId)),
//        ("pos", preferredPosition),
//        ("loc", preferredLocation),
//        ("snap", snappable),
//        ("movable", movable),
//        ("resizeable", resizable)
//      )
//    } else {
//      buildMessage(name(),
//        ("widgetInfo", (groupId, childId)),
//        ("pos", preferredPosition),
//        ("loc", preferredLocation),
//      )
//    }
//  }

  def moved: Boolean = {
    (preferredLocation != cachedLocation || preferredPosition != cachedPosition)
  }

  def cache(): Unit = {
    cachedLocation = preferredLocation
    cachedPosition = preferredPosition
  }

  val componentId: Int = componentIdField.get(wo).asInstanceOf[Int]
  def groupId: Int = WidgetInfo.TO_GROUP(componentId)
  def childId: Int = WidgetInfo.TO_CHILD(componentId)

  def movable: Boolean = wo.isMovable
  def resettable: Boolean = wo.isResettable
  def resizable: Boolean = wo.isResizable
  def resizable_=(b: Boolean): Unit = resizableField.set(wo, b)
  def name(short: Boolean = true): String = {
    Option.when(short)((a: String) => a.reverse.takeWhile(_ != '_').reverse)
      .getOrElse((j: String) => j)(wo.getName)
  }

  def snappable: Boolean = wo.isSnappable
  def snappable_=(value: Boolean): Unit = snappableField.set(wo, value)
  
  def preferredLocation: Point = wo.getPreferredLocation

  def preferredLocation_=(p: (Int, Int)): Unit = {
    if (preferredLocation == null) {
      wo.setPreferredLocation(new Point(p._1, p._2))
    } else {
      preferredLocation.setLocation(p._1, p._2)
    }
  }
  def preferredSize: Dimension = {
    wo.getPreferredSize
  }

  def preferredPosition: OverlayPosition = {
    wo.getPreferredPosition
  }

  def preferredPosition_=(other: OverlayPosition): Unit = {
    wo.setPreferredPosition(other)
  }

  override def toString: String = {
      List.apply(
        s"widgetInfo=$groupId:$childId",
        s"pos=$preferredPosition",
        s"loc=$preferredLocation",
        s"snap=$snappable",
        s"movable=$movable",
        s"resizeable=$resizable"
      ).mkString("(", ", ", ")").prependedAll(s"${name(false)}")
  }
}
