package com.fredplugins.kroovy

import java.awt.event.KeyEvent
import java.io.{BufferedWriter, File, FileWriter}
import java.nio.charset.Charset
import com.google.inject.{Inject, Singleton}
import net.runelite.api.Client
import net.runelite.client.RuneLite
import net.runelite.client.eventbus.EventBus
import com.fredplugins.kroovy.data.{SEventNodeImpl, SKeyNodeImpl, SPackage}
import com.fredplugins.kroovy.jfx.{SEventNode, SKeyNode, SNode}
import javafx.beans.property.{ReadOnlyMapProperty, ReadOnlyMapWrapper}
import javafx.collections.FXCollections
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.*

import scala.collection.mutable
import scala.io.Source
import scala.jdk.CollectionConverters.MapHasAsScala
import scala.swing.Publisher
import scalaz.Leibniz.subst


@Singleton
class KManager @Inject()(val plugin: KroovyPlugin, val client: Client, val eventBus: EventBus) extends Publisher {
	def removeScript(s: SPackage): Unit = {
		save(s)
	}

	private lazy val log: Logger = LoggerFactory.getLogger(classOf[KManager])
	val packageExt: String = "k2"
	val folderName: String = "kroovy2"
	private val folder: File = new File(RuneLite.RUNELITE_DIR, folderName)

	private var $global: Option[SPackage] = None
	def global: SPackage = {
		$global = $global.orElse {
			val t = plugin.pkgProvider.get()
			t.setRunning(true)
			Option(t)
		}
		assert($global.isDefined)
		$global.get
	}
	def resetGlobal(): Unit = $global = $global.map(_.setRunning(false)).filter(_.isRunning)//if $global is defined, shut it down and if the package is not running (we shut it down so its probably isent), use None instead of the curent global

	def isValid: Boolean = (folder.exists() || folder.mkdir()) && folder.isDirectory
	import scala.jdk.CollectionConverters.CollectionHasAsScala
	private val scriptsProperty: ReadOnlyMapWrapper[String, SPackage] = new ReadOnlyMapWrapper(FXCollections.observableHashMap())
	def readOnlyScriptsProperty: ReadOnlyMapProperty[String, SPackage] = scriptsProperty.getReadOnlyProperty

	private def scripts = scriptsProperty.get.asScala

//	Option[F](x) is implicitly converted to either an List[F]() or List[F](x) depending on if it is defined
	def getRunningPackages: List[SPackage] = Array.from(readOnlyScriptsProperty.values().asScala.filter(_.isRunning)).toList.appendedAll($global)

	def loadAll(): Array[String] = {
		def getFileInfo(file: File): Option[(String, String)] = {
			file.getName.split("\\.(?=[^.]+$)").toList match {
				case List(a, b) => Some(a, b)
				case _ => None
			}
		}
		if(isValid) {
			log.debug("files: {}", folder.listFiles().map(getFileInfo).filter(_.isDefined).map(_.get))
			for {
				(n, e) <- folder.listFiles().map(getFileInfo).filter(_.isDefined).map(_.get)
				if !containsPackage(n) && e.contentEquals(packageExt)
				if load(n)
			} yield n
		} else {
			Array.empty[String]
		}
	}

	def unloadAll(): Array[String] = {
		val toRet = readOnlyScriptsProperty.get().entrySet().asScala.flatMap(f => Option(f.getKey).filter(k => save(f.getValue, Some(k)))).toArray
		scriptsProperty.clear()
		toRet
	}

	def load(name: String): Boolean = {
		def decode(pkg: SPackage, jsValue: JsValue): Option[SNode] = jsValue match {
			case json: JsObject =>
				val node: SNode = (json \ "type").as[String] match {
					case "key" =>	new SKeyNodeImpl(pkg)
					case "event" =>	new SEventNodeImpl(pkg)
				}
				(json \ "onEnabled").asOpt[String].foreach(node.onEnabled.source.setValue)
				(json \ "onDisabled").asOpt[String].foreach(node.onDisabled.source.setValue)
				node match {
					case snode: SEventNode => {
						(json \ "onTrigger").asOpt[String].foreach(snode.onTrigger.source.setValue)
						snode.setEventTypeString((json \ "eventType").asOpt[String])
					}
					case knode: SKeyNode => {
						(json \ "onPress").asOpt[String].foreach(knode.onPress.source.setValue)
						(json \ "onRelease").asOpt[String].foreach(knode.onRelease.source.setValue)
						(json \ "keyCode").asOpt[Int].orElse(Some(KeyEvent.VK_UNDEFINED)).foreach(knode.keyCode.set)
					}
				}
				(json \ "enabled").asOpt[Boolean].orElse(Some(false)).foreach(node.enabled.set)
				Some(node)
			case _ => None
		}

		if (readOnlyScriptsProperty.containsKey(name)) {
			log.error("script \"{}\" is already loaded", name)
			false
		} else {
			val bufferedSource = Source.fromFile(new File(folder, s"$name.$packageExt"))
			val str = bufferedSource.getLines().mkString
			bufferedSource.close
			Some(Json.parse(str)).collect {
				case json: JsObject =>
					val pkg: SPackage = plugin.pkgProvider.get()
					(json \ "onStart").asOpt[String].foreach(pkg.onStart.source.setValue)
					(json \ "onStop").asOpt[String].foreach(pkg.onStop.source.setValue)
					(json \ "nodes").asOpt[Map[String, JsObject]].getOrElse(Map.empty).foreach(k => {
						if (!pkg.containsNode(k._1)) decode(pkg, k._2).foreach(p => pkg.addNode(k._1, p))
					})
					pkg.setRunning((json \ "running").asOpt[Boolean].getOrElse(false))
					pkg
			} match {
				case Some(value) =>
					scriptsProperty.put(name, value)
				case None => log.error("Invalid json in file \"{}\"", name)
			}
			readOnlyScriptsProperty.containsKey(name)
		}
	}

	def getPackage(name: String): Option[SPackage] = Option(readOnlyScriptsProperty.get(name))
	def getPackageName(pkg: SPackage): Option[String] = {
		readOnlyScriptsProperty.asScala.collectFirst {
			case (nameTemp, pkgTemp) if pkg.equals(pkgTemp) => nameTemp
		}
	}
	def getLoadedPackages: Array[String] = {
		readOnlyScriptsProperty.keySet().asScala.toArray
	}

	def containsPackage(name: String): Boolean = getPackage(name).isDefined

	def save(pkg: SPackage, name: Option[String] = None): Boolean = {
		val file = new File(folder, s"${name.orElse(pkg.getName()).get}.$packageExt")
		if (file.exists() || file.createNewFile()) {
			val bw = new BufferedWriter(new FileWriter(file, Charset.defaultCharset()))
			Option(pkg).map(pkg => JsObject(Seq(
				"running" -> JsBoolean(pkg.isRunning),
				"nodes" -> JsObject(pkg.nodes.asScala.toSeq.map(k => (k._1, SNode.encode(k._2)))),
				"onStart" -> pkg.onStart.sourceOpt.map(JsString).getOrElse(JsNull),
				"onStop" -> pkg.onStop.sourceOpt.map(JsString).getOrElse(JsNull)
			).filter(k => !k._2.equals(JsNull)))).map(Json.prettyPrint(_)).foreach(bw.write)
			bw.close()
			true
		} else {
			log.error("Error writing to file {}", file.getName)
			false
		}
	}

	def registerNew(name: String, pkg: SPackage): Boolean = {
		if (readOnlyScriptsProperty.containsKey(name)) {
			log.error("script \"{}\" is already defined", name)
			false
		} else {
			save(pkg, Some(name))
			load(name)
		}
	}
}
