package com.fredplugins.kroovy.jfx

import java.util.concurrent.Callable
import java.util.function.Consumer
import scala.language.implicitConversions

object RunnableImplicits {
	implicit def runnable(f: () => Unit): Runnable =
		new Runnable() {
			def run(): Unit = f()
		}
	implicit def runnable2(f: => Unit): Runnable =
		new Runnable() {
			def run(): Unit = f
		}
	implicit def callable[T](f: () => T): Callable[T] =
		new Callable[T]() {
			def call(): T = f()
		}
	implicit def callable2[T](f: => T): Callable[T] =
		new Callable[T]() {
			def call(): T = f
		}

	implicit def consumer[T](f: T => Unit): Consumer[T] = (t: T) => f(t)
}
