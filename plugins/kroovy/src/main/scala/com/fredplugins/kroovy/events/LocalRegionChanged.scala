package com.fredplugins.kroovy.events

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

import scala.compiletime.uninitialized

case class LocalRegionChanged(from: Int, to: Int) {}
