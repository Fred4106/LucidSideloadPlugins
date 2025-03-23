package com.fredplugins.kroovy.swing

import javax.swing.table.AbstractTableModel
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized

class EventsTableModel extends AbstractTableModel {
	override def getRowCount: Int = ???
	override def getColumnCount: Int = ???
	override def getValueAt(rowIndex: Int, columnIndex: Int): AnyRef = ???
}
