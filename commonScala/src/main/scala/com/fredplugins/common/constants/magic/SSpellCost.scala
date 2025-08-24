package com.fredplugins.common.constants.magic

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

import scala.compiletime.uninitialized

case class SSpellInfo()
case class SSpellCost(runes: Map[SRune, Int], otherConsumables: Map[SConsumable, (Int, Int)]) {}
