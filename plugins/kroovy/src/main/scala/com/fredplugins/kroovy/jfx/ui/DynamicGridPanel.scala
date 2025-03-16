package com.fredplugins.kroovy.jfx.ui

/*
 * scala-swing (https://www.scala-lang.org)
 */

import net.runelite.client.ui.DynamicGridLayout

import scala.swing.{Panel, SequentialContainer}

object DynamicGridPanel {
	val Adapt = 0
}

/**
 * A panel that lays out its contents in a uniform grid.
 *
 * @see net.runelite.client.ui.DynamicGridLayout
 */
class DynamicGridPanel(rows0: Int, cols0: Int) extends Panel with SequentialContainer.Wrapper {
	override lazy val peer =
		new javax.swing.JPanel(new DynamicGridLayout(rows0, cols0)) with SuperMixin

	private def layoutManager: DynamicGridLayout = peer.getLayout.asInstanceOf[DynamicGridLayout]

	def rows: Int = layoutManager.getRows
	def rows_=(n: Int): Unit = layoutManager.setRows(n)
	def columns: Int = layoutManager.getColumns
	def columns_=(n: Int): Unit = layoutManager.setColumns(n)

	def vGap: Int = layoutManager.getVgap
	def vGap_=(n: Int): Unit = layoutManager.setVgap(n)
	def hGap: Int = layoutManager.getHgap
	def hGap_=(n: Int): Unit = layoutManager.setHgap(n)
}
