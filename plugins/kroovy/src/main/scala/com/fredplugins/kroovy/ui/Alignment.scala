package com.fredplugins.kroovy.ui

import com.fredplugins.kroovy.ui.AlignmentParam.{Max, Mid, Min}

enum AlignmentParam {
	case Min
	case Mid
	case Max
}

enum Alignment(val alignmentX: AlignmentParam, val alignmentY: AlignmentParam) {
	case TOP_LEFT extends Alignment (Min, Min)
	case TOP extends Alignment (Mid, Min)
	case TOP_RIGHT extends Alignment (Max, Min)
	case LEFT extends Alignment (Min, Mid)
	case CENTER extends Alignment (Mid, Mid)
	case RIGHT extends Alignment (Max, Mid)
	case BOTTOM_LEFT extends Alignment (Min, Max)
	case BOTTOM extends Alignment (Mid, Max)
	case BOTTOM_RIGHT extends Alignment (Max, Max)

	def getAlignmentX: AlignmentParam = return this.alignmentX
	def getAlignmentY: AlignmentParam = return this.alignmentY
}