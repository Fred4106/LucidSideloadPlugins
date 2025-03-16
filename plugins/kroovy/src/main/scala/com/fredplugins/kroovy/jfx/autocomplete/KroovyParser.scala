package com.fredplugins.kroovy.jfx.autocomplete

import com.fredplugins.kroovy.ApiDelegatingScript
import com.fredplugins.kroovy.jfx.DemoJfxWindow
import com.fredplugins.kroovy.jfx.editor.BetterKroovyArea

import java.io.PrintWriter
import java.security.{AccessController, PrivilegedAction}
import java.util
import java.util.stream.Collectors
import java.util.{LinkedList, List, Properties}
import groovy.lang.{GroovyClassLoader, GroovyCodeSource}
import org.apache.groovy.parser.antlr4.Antlr4PluginFactory
import org.codehaus.groovy.ast.{ASTNode, ClassNode, ModuleNode}
import org.codehaus.groovy.ast.builder.{AstBuilder, AstStringCompiler}
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.{CompilationUnit, CompilePhase, CompilerConfiguration, ErrorCollector, Janitor, SourceUnit}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

object KroovyParser {
	def getConfiguration(): CompilerConfiguration = {
		val compilerConfiguration = new CompilerConfiguration()
		compilerConfiguration.setTolerance(1)
		compilerConfiguration.setPluginFactory(new Antlr4PluginFactory())
		val importCustomizer = new ImportCustomizer()
		importCustomizer.addStaticStars("net.runelite.api.ItemID", "net.runelite.api.Prayer", "net.runelite.api.Skill", "net.runelite.api.NpcID")
		importCustomizer.addStarImports(
			"com.github.therapi.runtimejavadoc",
			"com.google.inject",
			"java.awt",
			"java.awt.event",
			"java.awt.image",
			"java.io",
			"java.lang.math",
			"java.lang.reflect",
			"java.time",
			"java.time.chrono",
			"java.time.format",
			"java.time.temporal",
			"java.time.zone",
			"java.util",
			"java.util.stream",
			"javax.sound",
			"javax.sound.sampled",
			"javax.swing",
			"javax.swing.text",
			"net.runelite.api",
			"net.runelite.api.annotations",
			"net.runelite.api.coords",
			"net.runelite.api.events",
			"net.runelite.api.events.inventory",
			"net.runelite.api.events.player.headicon",
			"net.runelite.api.geometry",
			"net.runelite.api.hooks",
			"net.runelite.api.kit",
			"net.runelite.api.model",
			"net.runelite.api.overlay",
			"net.runelite.api.queries",
			"net.runelite.api.util",
			"net.runelite.api.vars",
			"net.runelite.api.widgets",
			"net.runelite.client",
			"net.runelite.client.account",
			"net.runelite.client.callback",
			"net.runelite.client.chat",
			"net.runelite.client.config",
			"net.runelite.client.database",
			"net.runelite.client.database.data",
			"net.runelite.client.database.data.tables",
			"net.runelite.client.database.data.tables.records",
			"net.runelite.client.discord",
			"net.runelite.client.discord.events",
			"net.runelite.client.eventbus",
			"net.runelite.client.events",
			"net.runelite.client.game",
			"net.runelite.client.game.chatbox",
			"net.runelite.client.graphics",
			"net.runelite.client.input",
			"net.runelite.client.menus",
			"net.runelite.client.plugins",
			"net.runelite.client.plugins.dialogskip",
			"net.runelite.client.plugins.kroovy",
			"net.runelite.client.plugins.kroovy.data",
			"net.runelite.client.plugins.kroovy.experimental",
			"net.runelite.client.plugins.kroovy.script",
			"net.runelite.client.plugins.kroovy.ui",
			"net.runelite.client.plugins.kroovy.ui.overlay",
			"net.runelite.client.plugins.kroovy.utils",
			"net.runelite.client.rs",
			"net.runelite.client.task",
			"net.runelite.client.ui",
			"net.runelite.client.ui.components",
			"net.runelite.client.ui.components.colorpicker",
			"net.runelite.client.ui.components.materialtabs",
			"net.runelite.client.ui.components.shadowlabel",
			"net.runelite.client.ui.overlay",
			"net.runelite.client.ui.overlay.arrow",
			"net.runelite.client.ui.overlay.components",
			"net.runelite.client.ui.overlay.components.table",
			"net.runelite.client.ui.overlay.infobox",
			"net.runelite.client.ui.overlay.tooltip",
			"net.runelite.client.ui.overlay.worldmap",
			"net.runelite.client.ui.skin",
			"net.runelite.client.util",
			"net.runelite.client.util.ping",
			"net.runelite.client.ws",
			"net.runelite.http.api",
			"net.runelite.http.api.account",
			"net.runelite.http.api.cache",
			"net.runelite.http.api.chat",
			"net.runelite.http.api.config",
			"net.runelite.http.api.discord",
			"net.runelite.http.api.discord.embed",
			"net.runelite.http.api.examine",
			"net.runelite.http.api.feed",
			"net.runelite.http.api.ge",
			"net.runelite.http.api.hiscore",
			"net.runelite.http.api.item",
			"net.runelite.http.api.loottracker",
			"net.runelite.http.api.npc",
			"net.runelite.http.api.osbuddy",
			"net.runelite.http.api.util",
			"net.runelite.http.api.worlds",
			"net.runelite.http.api.ws",
			"net.runelite.http.api.ws.messages",
			"net.runelite.http.api.ws.messages.party",
			"net.runelite.http.api.xp",
			"net.runelite.http.api.xtea"
		)
		compilerConfiguration.addCompilationCustomizers(importCustomizer)
		compilerConfiguration.setScriptBaseClass(classOf[ApiDelegatingScript].getName)
		compilerConfiguration
	}

	def getAst(script: String): util.List[ASTNode] = {
		val configuration = KroovyParser.getConfiguration()
		val scriptClassName = "Script" + System.nanoTime
		val statementsOnly = false

		val codeSource = new GroovyCodeSource(script, scriptClassName + ".groovy", "/groovy/script")
		val cu = new CompilationUnit(configuration, codeSource.getCodeSource, AccessController.doPrivileged[GroovyClassLoader](new PrivilegedAction[GroovyClassLoader] {
			override def run(): GroovyClassLoader = new GroovyClassLoader()
		}))
		cu.setConfiguration(configuration)
		cu.addSource(codeSource.getName, script)
		var result: util.List[ASTNode] = null
		try {
			cu.compile(CompilePhase.CANONICALIZATION.getPhaseNumber)
			result = cu.getAST.getModules.stream.reduce(new util.LinkedList[ASTNode], (acc: util.LinkedList[ASTNode], node: ModuleNode) => {
				def foo(acc: util.LinkedList[ASTNode], node: ModuleNode) = {
					val statementBlock = node.getStatementBlock
					if (null != statementBlock) acc.add(statementBlock)
					acc.addAll(node.getClasses.asScala.filter((c: ClassNode) => !(statementsOnly && scriptClassName == c.getName)).asJava)
					acc
				}
				foo(acc, node)
			}, (o1: util.LinkedList[ASTNode], o2: util.LinkedList[ASTNode]) => o1)
		} catch {
			case e: Throwable => e.printStackTrace()
		}
		val janitor = new Janitor
		cu.getErrorCollector.write(new PrintWriter(System.out), janitor)
		janitor.cleanup()
		// collect all the ASTNodes into the result, possibly ignoring the script body if desired
		result
	}
}

class PrivateMethodCaller(x: AnyRef)(methodName: String) {
	def call(maybeArgs: Option[Seq[Any]]): Unit = {
		def _parents: LazyList[Class[_]] = LazyList(x.getClass) #::: _parents.map(_.getSuperclass)
		val parents = _parents.takeWhile(_ != null).toList
		val methods = parents.flatMap(_.getDeclaredMethods)
		val method = methods.find(_.getName == methodName).getOrElse(throw new IllegalArgumentException("Method " + methodName + " not found"))
		method.setAccessible(true)

		maybeArgs match {
			case Some(args) => method.invoke(x, args.map(_.asInstanceOf[AnyRef]) : _*)
			case None => method.invoke(x)
		}
	}
	def call(_args: Any*): Any = call(Some(_args))
	def call(): Any = call(None)
}
import javafx.scene.Scene
import javafx.stage.Stage
import org.fxmisc.flowless.VirtualizedScrollPane


object KroovyEditorDemo extends DemoJfxWindow {
	val sampleScript =
		"""def npc = findNearestNPC("Dust devil");jim = findNearestNPC(455)
		  |if(npc == jim) {
		  |	doNpcOption("Attack", npc)
		  |}""".stripMargin
	override def init(stage: Stage) = {
		val scene = new Scene(BetterKroovyArea(sampleScript), 600, 400)
		stage.setScene(scene)
		stage.setTitle("Editor Demo")
		stage.show()
	}
}

object TestSimpleParser {
	def main(args: Array[String]) = {
		val sampleScript =
			"""def npc = findNearestNPC("Dust devil");im = findNearestNPC(455)
			  |if(npc == jim) {
			  |	doNpcOption("Attack", npc)
			  |}""".stripMargin
		val log = LoggerFactory.getLogger("main")
		val y = KroovyParser.getAst(sampleScript)
		Option(y).foreach(_.asScala.foreach(k => log.info("line: {}, col: {} => {}", k.getLineNumber, k.getColumnNumber, k)))
	}
}
