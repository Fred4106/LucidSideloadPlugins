package com.fredplugins.common

import scala.jdk.CollectionConverters._
import com.lucidplugins.api.utils.GameObjectUtils
import net.runelite.api.GameObject
object GameObjectSearch {
  def find(ids: Int *): List[GameObject] = {
    GameObjectUtils.getAll {
      case go: GameObject if ids.contains(go.getId) => true
      case _ => false
    }.asScala.toList.flatMap(to => {
      to match {
        case go: GameObject => Option(go)
        case _ => None
      }
    })
  }
}

