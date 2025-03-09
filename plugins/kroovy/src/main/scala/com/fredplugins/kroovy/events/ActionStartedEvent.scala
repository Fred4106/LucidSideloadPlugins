package com.fredplugins.kroovy.events

import com.fredplugins.kroovy.ActionEnum


case class ActionStartedEvent(action : ActionEnum, productId: Int, actionCount: Int, startTick: Int, endTick: Int) {}