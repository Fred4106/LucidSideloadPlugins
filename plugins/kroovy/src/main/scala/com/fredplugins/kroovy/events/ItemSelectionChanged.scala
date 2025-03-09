package com.fredplugins.kroovy.events

import net.runelite.api.Item

case class ItemSelectionChanged(from: Item | Null, to: Item | Null) {

}
