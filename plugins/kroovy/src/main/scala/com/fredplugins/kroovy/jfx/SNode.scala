package com.fredplugins.kroovy.jfx

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.kroovy.ApiDelegatingScript

import java.awt.event.KeyEvent
import java.lang
import javafx.beans.property.{BooleanProperty, IntegerProperty, ObjectProperty, ReadOnlyObjectProperty, ReadOnlyObjectWrapper, SimpleBooleanProperty, SimpleIntegerProperty, SimpleObjectProperty, SimpleStringProperty, StringProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import play.api.libs.json.*
import com.fredplugins.kroovy.jfx.RunnableImplicits.*

import scala.reflect.ClassTag

sealed trait SNode extends ShimUtils.Logging {
	def getDistinguishing: String = this match {
		case _: SEventNode => "event"
		case _: SKeyNode => "key"
	}

	def pkg: SPackageT

	val onEnabled: ScriptTuple = new ScriptTuple(pkg)
	val onDisabled: ScriptTuple = new ScriptTuple(pkg)

	val enabled: BooleanProperty = new SimpleBooleanProperty(false)
	enabled.addListener(new ChangeListener[lang.Boolean] {
		override def changed(observable: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean, newValue: lang.Boolean): Unit = {
			if(newValue != oldValue) {
				if(pkg.isRunning) {
					runEnabledDisabled(newValue)
				}
			}
		}
	})

	def isEnabled: Boolean = enabled.getValue()

	final def runEnabledDisabled(boolean: Boolean): Unit = {
		if(boolean) {
			runEnabled()
		} else {
			runDisabled()
		}
	}

	final def runEnabled(): Unit = {
		onEnabled.invoke()
		enabledCallback()
	}

	final def runDisabled(): Unit = {
		disabledCallback()
		onDisabled.invoke()
	}

	def getName: Option[String] = pkg.getNodeName(this).map(_._2)

	def enabledCallback(): Unit
	def disabledCallback(): Unit
}

trait SKeyNode extends SNode {
	val keyCode: IntegerProperty = new SimpleIntegerProperty(KeyEvent.VK_UNDEFINED)
	def keyString: String = if (keyCode.get() == KeyEvent.VK_UNDEFINED) "Not set" else KeyEvent.getKeyText(keyCode.get())

	val onPress: ScriptTuple = new ScriptTuple(pkg)
	val onRelease: ScriptTuple = new ScriptTuple(pkg)
}
import RunnableImplicits.*
class ScriptTuple(val pkg: SPackageT) {
	val source: StringProperty = new SimpleStringProperty()
	def script: ReadOnlyObjectProperty[ApiDelegatingScript] = _script.getReadOnlyProperty
	private val _script: ReadOnlyObjectWrapper[ApiDelegatingScript] = new ReadOnlyObjectWrapper[ApiDelegatingScript]
	source.addListener(new ChangeListener[String] {
		override def changed(observable: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
			val nVal = Option(newValue).filter(_.length > 0)
			val oVal = Option(oldValue).filter(_.length > 0)
			if(oVal != nVal) {
				_script.set(pkg.compile(newValue))
			}
		}
	})

	def sourceOpt: Option[String] = Option(source.get()).filter(_.length > 0)
	def invoke(): Unit = {
		Option(script.get()) match {
			case Some(apiScript) => pkg.clientThread.invoke(() => apiScript.hook())
			case _ =>
		}
	}
	def invoke(event: AnyRef): Unit = {
		Option(script.get()) match {
			case Some(apiScript) => pkg.clientThread.invoke(() => apiScript.hook(event))
			case _ =>
		}
	}
}

object SNode {
	def encode(node: SNode): JsValue = {
		JsObject((Seq(
			"type" -> JsString(node.getDistinguishing),
			"enabled" -> JsBoolean(node.isEnabled),
			"onEnabled" -> node.onEnabled.sourceOpt.map(JsString).getOrElse(JsNull),
			"onDisabled" -> node.onDisabled.sourceOpt.map(JsString).getOrElse(JsNull)
		) ++ (node match {
			case node: SEventNode =>
				Seq(
					"eventType" -> node.eventType.get.map(_.clazz.getName).map(JsString).getOrElse(JsNull),
					"onTrigger" -> node.onTrigger.sourceOpt.map(JsString).getOrElse(JsNull)
				)
			case node: SKeyNode =>
				Seq(
					"keyCode" -> Option(node.keyCode.get).filter(_ != KeyEvent.VK_UNDEFINED).map(JsNumber(_)).getOrElse(JsNull),
					"onPress" -> node.onPress.sourceOpt.map(JsString).getOrElse(JsNull),
					"onRelease" -> node.onRelease.sourceOpt.map(JsString).getOrElse(JsNull)
				)
		})).filter(k => k._2 != JsNull)
		)
	}
}

trait SEventNode extends SNode {
//	private var _eventType: Option[Class[_ <: Event]] = None
//	def eventType: Option[Class[_ <: Event]] = _eventType
//	type clazzType = EventType[_]
//	type optClazzType = Option[clazzType]
	val eventType: ObjectProperty[Option[EventType]] = new SimpleObjectProperty(None)
	eventType.addListener(new ChangeListener[Option[EventType]] {
		override def changed(observable: ObservableValue[_ <: Option[EventType]], oldValue: Option[EventType], newValue: Option[EventType]): Unit = {
			if (newValue != oldValue) {
				if(oldValue.isDefined && isEnabled && pkg.isRunning) {
					disabledCallback()
				}
				if(newValue.isDefined && isEnabled && pkg.isRunning) {
					enabledCallback()
				}
			}
		}
	})
	val onTrigger: ScriptTuple = new ScriptTuple(pkg)

	def setEventType[E: ClassTag as ct](): SEventNode = {
		eventType.setValue(Option(EventType(ct.runtimeClass.asInstanceOf[Class[E]])))
		this
	}

	def setEventTypeString(nVal: Option[String]): SEventNode = {
		val  c = nVal.map(Class.forName).map(EventType(_))
		eventType.setValue(c)
		this
	}
}

