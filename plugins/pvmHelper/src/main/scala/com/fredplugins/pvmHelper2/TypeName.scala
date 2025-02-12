package com.fredplugins.pvmHelper2

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

import scala.compiletime.uninitialized

object TypeName {
	import scala.quoted.{Type, Expr, Quotes}
	inline def of[A]: String = ${impl[A]}
	def impl[A](using Type[A], Quotes): Expr[String] = {
		val name = Type.show[A]
		Expr(name)
	}

//	inline def typeName[A: Type] = ${ tpeNmeMacro[A] }
}