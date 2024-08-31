package com.fredplugins.dt2

import enumeratum._

trait MyEnumEntry extends EnumEntry {
	def id: Int
}

trait MyEnum[Q <: MyEnumEntry] extends Enum[Q] {
	//	def values: List[Q] = internal.map(_._2)
	def find(i: Int): Option[Q] = values.find(_.id == i)
}
