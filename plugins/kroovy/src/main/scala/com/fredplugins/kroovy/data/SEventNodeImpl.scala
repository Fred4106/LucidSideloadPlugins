package com.fredplugins.kroovy.data

import com.fredplugins.kroovy.jfx.SEventNode
import net.runelite.client.eventbus.EventBus.Subscriber
import net.runelite.client.eventbus.Subscribe


final class SEventNodeImpl(val pkg: SPackage) extends SEventNode {
	var subscriberKey: Subscriber = _

	override def enabledCallback(): Unit = {
		if(subscriberKey != null) {
			log.error(s"SubscriberKey was somehow nonNull: ${subscriberKey.toString}")
		} else if(eventType.get.isDefined) {
			val tag = eventType.get.get
			subscriberKey = pkg.eventBus.register[tag.T](tag.clazz, t => onTrigger.invoke(t), 0)
		}
	}

	override def disabledCallback(): Unit = {
		pkg.eventBus.unregister(subscriberKey)
		subscriberKey = null
	}
}
