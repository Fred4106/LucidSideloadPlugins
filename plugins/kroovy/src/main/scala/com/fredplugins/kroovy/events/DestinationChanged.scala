package com.fredplugins.kroovy.events

import net.runelite.api.coords.LocalPoint

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

case class DestinationChanged(from: LocalPoint, to: LocalPoint) {

}