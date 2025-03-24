package com.fredplugins.kroovy.services

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.KroovyConfig
import net.runelite.api.Client
import net.runelite.client.RuneLite
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.EventBus

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.reflect.ClassTag
import scala.swing.Publisher

trait ServiceBase extends Publisher{
	protected val log = ShimUtils.getLogger(this.getClass.getName, "DEBUG")
	def init(): Unit
	def teardown(): Unit
}
