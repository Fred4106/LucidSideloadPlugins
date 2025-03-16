package com.fredplugins.kroovy.data

import groovy.lang.{MetaClass, MetaMethod}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.ObservableList
import com.fredplugins.kroovy.data.AutoCompleteEngine.KMethod
import javafx.beans.property.{ReadOnlyBooleanWrapper, ReadOnlyMapProperty, ReadOnlyMapWrapper}
import scala.collection.mutable
import scala.jdk.CollectionConverters._

object AutoCompleteEngine {
	case class KMethod(name: String, retType: Class[_], params: Array[Class[_]])
	object KMethod {
		def compareWith(self: KMethod, other: KMethod): Boolean = {
			(self.name.compare(other.name), self.retType.getName.compareTo(other.retType.getName), (self.params, other.params)) match {
				case (0, 0, (lp, rp)) => {
					if(lp.length == rp.length) {
						lp.zip(rp).map(k => k._1.getName.compareTo(k._2.getName)).find(_ != 0).exists(_ < 0)
					} else {
						lp.length < rp.length
					}
				}
				case(0, rDif, (_, _)) => rDif < 0
				case(nDif, _, (_, _)) => nDif < 0
			}
		}
	}

	private def getMethods(meta: MetaClass, filter: (MetaMethod) => Boolean): List[KMethod] = {
		meta.getMetaMethods.asScala.toList.filter(filter).map(mm => KMethod(mm.getName, mm.getReturnType, mm.getNativeParameterTypes)).sortWith(KMethod.compareWith)
	}

	private val map: mutable.Map[SPackage, AutoCompleteProvider] = mutable.HashMap.empty[SPackage, AutoCompleteProvider].withDefault(pkg => {
		new AutoCompleteProvider(pkg)
	})

	def getAutoCompleteProvider(sPkg: SPackage): AutoCompleteProvider = map(sPkg)

	class AutoCompleteProvider(sPkg: SPackage) {
		private var apiMethods: List[KMethod] = sPkg.apiMetaClass.getOpt.map(meta => getMethods(meta, _.isPublic)).orElse(List.empty[KMethod])

		sPkg.apiMetaClass.addListener(new ChangeListener[MetaClass] {
			override def changed(observable: ObservableValue[_ <: MetaClass], oldValue: MetaClass, newValue: MetaClass): Unit = {
				apiMethods = Option(newValue).map(meta => getMethods(meta, _.isPublic)).getOrElse(List.empty[KMethod])
			}
		})

		def matchMethod(nameFragment: String, retType: Class[_] = classOf[Object]): List[KMethod] = {
			apiMethods.filter(r => retType.isAssignableFrom(r.retType)).filter(_.name.contains(nameFragment) || nameFragment.length == 0)
		}
	}
}