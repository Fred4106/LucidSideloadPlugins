package com.fredplugins.kroovy.data

import com.fredplugins.kroovy.{ApiDelegatingScript, GroovyApi, KManager, KroovyPlugin}
import com.fredplugins.kroovy.jfx.{SNode, SPackageT}
import com.google.inject.Inject
import groovy.lang.{GroovyShell, MetaClass}
import net.runelite.client.callback.ClientThread
import net.runelite.client.eventbus.EventBus
import net.runelite.client.game.SpriteManager
import net.runelite.client.input.KeyManager
import org.reactfx.value.Var
import com.fredplugins.kroovy.jfx.autocomplete.KroovyParser

import scala.collection.mutable
import scala.jdk.CollectionConverters.MapHasAsScala

class SPackage @Inject()(val plugin: KroovyPlugin,
						 val kManager: KManager,
						 val spriteManager: SpriteManager,
						 val clientThread: ClientThread,
						 val eventBus: EventBus,
						 val keyManager: KeyManager) extends SPackageT {

	val _shell: GroovyShell = new GroovyShell(KroovyParser.getConfiguration())

	val api: GroovyApi = new GroovyApi(plugin.Kroovy2Hack)
	val apiMetaClass: Var[MetaClass] = Var.newSimpleVar[MetaClass](null)

	val autoCompleteProvider: AutoCompleteEngine.AutoCompleteProvider = AutoCompleteEngine.getAutoCompleteProvider(this)

	override def compile(source: String): ApiDelegatingScript = {
		log.debug("Compiling\n{}", source)
		val script: ApiDelegatingScript = _shell.parse(source).asInstanceOf[ApiDelegatingScript]
		script.setBinding(binding())
		script.setApiDelegate(api, apiMetaClass.getValue)
		if (apiMetaClass.getValue == null) {
			apiMetaClass.setValue(script.getApiMetaClass)
		}
		script
	}

	private class ScriptsCache(val cacheSize: Int, val threshold: Int) extends mutable.Map[String,  ApiDelegatingScript] {
		private val realBacking = mutable.ListBuffer.empty[(String,  ApiDelegatingScript)]
		override def get(key: String): Option[ ApiDelegatingScript] = {
			val eIdx = realBacking.indexWhere(_._1.contentEquals(key))
			if (eIdx == -1) {
				None
			} else {
				if (eIdx > 0) realBacking.prepend(realBacking.remove(eIdx))
				realBacking.headOption.map(_._2)
			}
		}
		override def addOne(elem: (String,  ApiDelegatingScript)): this.type = {
			subtractOne(elem._1)
			realBacking.prepend(elem)
			if (realBacking.length > cacheSize + threshold) realBacking.sliceInPlace(0, cacheSize)
			this
		}
		override def iterator: Iterator[(String,  ApiDelegatingScript)] = realBacking.iterator
		override def subtractOne(elem: String): this.type = {
			realBacking.filterInPlace(k => k._1 != elem && !k._1.contentEquals(elem))
			this
		}
		def getOrElseUpdate(key: String): Option[ ApiDelegatingScript] =
			get(key) match {
				case None => addOne((key, compile(key))).get(key)
				case s: Some[ ApiDelegatingScript] => s
			}
	}

	lazy val runtime: String => AnyRef = {
		val cachedCommands: ScriptsCache = new ScriptsCache(25, 10)
		k => {
			cachedCommands.getOrElseUpdate(k) match {
				case Some(value) => value.run()
				case None => throw new java.lang.AssertionError(s""""$k" would not compile to anything somehow. Fuck""")
			}
		}
	}
	override def getName(): Option[String] = kManager.getPackageName(this)

	override def getNodeName(node: SNode): Option[(String, String)] = {
		val pName = kManager.getPackageName(this)
		val nName = nodes.get().asScala.collectFirst {
			case (nameTemp, nodeTemp) if node == nodeTemp => nameTemp
		}
		(pName, nName) match {
			case (Some(p), Some(n)) => Some((p, n))
			case _ => None
		}
	}
}

