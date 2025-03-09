package com.fredplugins.kroovy.events

import com.fredplugins.kroovy.ActionEnum

case class ActionStoppedEvent(action : ActionEnum, productId: Int, actionCount: Int, startTick: Int, endTick: Int, interrupted: Boolean)  {
}
