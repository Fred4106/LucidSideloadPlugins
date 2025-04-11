package com.fredplugins.kroovy.api

enum KOwnership {
	case None
	case Self
	case Other
	case Group
}

object KOwnership {
	inline def get(i: Int): KOwnership = if(KOwnership.values.length > i) KOwnership.values(i) else None
}