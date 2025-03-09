package com.fredplugins.kroovy.events

import net.runelite.api.Actor

case class LocalInteractingChanged(from: Actor, to: Actor)