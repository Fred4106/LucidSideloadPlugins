package com.fredplugins.kroovy.swing

import javax.swing.table.AbstractTableModel as JTableModel
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.collection.{immutable, mutable}
import scala.reflect.ClassTag

sealed abstract class MField[RowType <: AnyRef: ClassTag as rt, Name <: String with scala.Singleton : ValueOf as nValue, FieldType <: AnyRef: ClassTag as ft] {
	type Row = RowType
	type Field = FieldType
	def name: String = nValue.value
	def ftTag: ClassTag[FieldType] = ft
	def rtTag: ClassTag[RowType] = rt
}
trait MFieldGetter {
	self: MField[?, ?, ?] =>
	def get(rt: Row): Field
	//	def get(r: rt): FieldType
}

trait MFieldSetter {
	self: MField[?, ?, ?] =>
	def set(r: Row, v: Field): Unit
}
object MTableModel {
	def apply[RowType <: AnyRef: ClassTag as rt](op: mutable.ListBuffer[MField[RowType, ?, ?]] ?=> Unit): MTableModel[RowType] = {
		val builder = mutable.ListBuffer.empty[MField[RowType, ?, ?]]
		import MTableModel.*
		op(using builder)
		builder.result()
		new MTableModel[RowType](builder.result() *)
	}
	def getter[RowType <: AnyRef: ClassTag as rt, Name <: String & scala.Singleton : ValueOf as nValue, FieldType <: AnyRef: ClassTag as ft](getOp: RowType => FieldType): MField[RowType, Name, FieldType] with MFieldGetter = new MField[RowType, Name, FieldType] with MFieldGetter {
		override def get(rt: Row): Field = getOp(rt)
	}
	def setter[RowType <: AnyRef: ClassTag as rt, Name <: String & scala.Singleton : ValueOf as nValue, FieldType <: AnyRef: ClassTag as ft](getOp: RowType => FieldType, setOp: (RowType, FieldType) => Unit):  MField[RowType, Name, FieldType] with MFieldGetter with MFieldSetter =  new MField[RowType,Name, FieldType] with MFieldGetter with MFieldSetter {
		override def get(rt: Row): Field = getOp.apply(rt)
		override def set(r: Row, v: Field): Unit = setOp.apply(r, v)
	}
	def getter2[Name <: String & scala.Singleton : ValueOf as nValue, FieldType <: AnyRef: ClassTag as ft, RowType <: AnyRef: ClassTag as rt](getOp: RowType => FieldType)(using b: mutable.ListBuffer[MField[RowType, ?, ?]]): Unit = b.addOne(new MField[RowType, Name, FieldType] with MFieldGetter {
		override def get(rt: Row): Field = getOp(rt)
	})
	def setter2[Name <: String & scala.Singleton : ValueOf as nValue, FieldType <: AnyRef: ClassTag as ft, RowType <: AnyRef: ClassTag as rt](getOp: RowType => FieldType, setOp: (RowType, FieldType) => Unit)(using b: mutable.ListBuffer[MField[RowType, ?, ?]]): Unit = b.addOne(new MField[RowType, Name, FieldType] with MFieldGetter with MFieldSetter {
		override def get(rt: Row): Field = getOp.apply(rt)
		override def set(r: Row, v: Field): Unit = setOp.apply(r, v)
	})
//	def ftTag: ClassTag[FieldType] = ft
//	def rtTag: ClassTag[RowType] = rt
//	def getter(r: RowType): FieldType
}

class MTableModel[RowType <: AnyRef](val fieldsAccessors: MField[RowType, ?, ?] *) extends JTableModel {
	private val data: mutable.ListBuffer[RowType] = scala.collection.mutable.ListBuffer.empty[RowType]

	override def getRowCount: Int = data.length
	override def getColumnCount: Int = fieldsAccessors.length
	override def getColumnName(columnIndex: Int): String = fieldsAccessors.apply(columnIndex).name
	override def getColumnClass(columnIndex: Int): Class[_] = fieldsAccessors.apply(columnIndex).ftTag.runtimeClass

	override def getValueAt(rowIndex: Int, columnIndex: Int): AnyRef = {
		val row = data(rowIndex)
		fieldsAccessors(columnIndex) match {
			case f: MFieldGetter => f.get(row)
//			case x => println(s"Cant handle ${x.getClass} with value ${x}")
		}
	}

	override def setValueAt(aValue: AnyRef, rowIndex: Int, columnIndex: Int): Unit = {
		val row = data(rowIndex)
		val setterOp = Option(fieldsAccessors(columnIndex)).map(fa => fa -> fa.ftTag.unapply(aValue)).collect {
			case (value: MField[RowType, ?, ?] with MFieldSetter, Some(value1)) if value.ftTag.runtimeClass == value1.getClass => (r: RowType) => value.set(r.asInstanceOf[value.Row], value1.asInstanceOf[value.Field])
		}
		setterOp.foreach(so => {
			so.apply(row)
			fireTableCellUpdated(rowIndex, columnIndex)
		})
	}
	override def isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = {
		fieldsAccessors.apply(columnIndex) match {
			case setter: MFieldSetter => true
			case _ => false
		}
	}

	def addRows(ridx: Int, r: RowType *): Unit = {
		r.zipWithIndex.map(rt => (rt._2 + ridx, rt._1)).foreach {
			case (i, rowType) => addRow(i, rowType)
		}
	}
	def addRow(ridx: Int, r: RowType): Unit = {
		data.insert(ridx, r)
		fireTableRowsInserted(ridx, ridx)
	}

	def deleteRow(ridx: Int): Unit = {
		data.remove(ridx)
		fireTableRowsDeleted(ridx, ridx)
	}

	def setData(nData: immutable.SortedSet[RowType]): Unit = {
		data.clear()
		data.addAll(nData)
		fireTableDataChanged()
	}
	def updateRow(rIdx: Int, dataOp: RowType => RowType): Unit = {
		val modifiedRow = data.apply(rIdx).pipe(dataOp(_))
		data.update(rIdx, modifiedRow)
		fireTableRowsUpdated(rIdx, rIdx)
	}
}
