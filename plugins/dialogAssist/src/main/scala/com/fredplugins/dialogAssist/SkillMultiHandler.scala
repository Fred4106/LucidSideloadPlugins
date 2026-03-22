package com.fredplugins.dialogAssist

import com.fredplugins.common.utils.ShimUtils
import com.fredplugins.dialogAssist.ScriptApi.Script
import com.fredplugins.dialogAssist.ScriptApi.ScriptDefinitions
import com.fredplugins.dialogAssist.SkillMultiHandler.findScriptData
import net.runelite.api.events.ScriptPostFired
import net.runelite.api.events.ScriptPreFired
import net.runelite.client.eventbus.Subscribe

import java.io.BufferedReader
import java.io.InputStreamReader
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try, Using}
import scala.compiletime.uninitialized
import org.slf4j.Logger

object SkillMultiHandler {
	val log: Logger = ShimUtils.getLogger(classOf[SkillMultiHandler].getName, "DEBUG")

	def getResourceAsStream[T](name: String)(body: BufferedReader => T): T = {
		classOf[SkillMultiHandler].getResourceAsStream(name)
			.pipe(new InputStreamReader(_))
			.pipe(new BufferedReader(_))
			.pipe(br => Using.resource(br)(r => {
				body(r)
			}))
	}

	private lazy val scriptSymbols: Map[Int, Script] = {
		getResourceAsStream("clientscript.sym")(br => {
			br.lines().iterator.asScala.flatMap(line => {
				val tabIdx= line.indexOf('\t')
				val openBrIdx = line.indexOf('[', tabIdx)
				val commaIdx = line.indexOf(',', openBrIdx)
				val closeBrIdx = line.indexOf(']', commaIdx)
				val idStr = line.substring(0, tabIdx)
				val tpeStr = line.substring(openBrIdx+1, commaIdx)
				val nameStr = line.substring(commaIdx+1, closeBrIdx)
				idStr.toIntOption.zip(ScriptType.fromString(tpeStr).toScala)
					.map((id, tpe) => id -> Script(tpe, nameStr))
			}).toMap
		})
	}

	def findScriptData(id: Int): Option[Script] = scriptSymbols.get(id)
}

class SkillMultiHandler(plugin: FredsDialogueAssistantPlugin) {
	import SkillMultiHandler.log
//	var inScriptMulti: Boolean = false
//	var indent: Int = 0
	@Subscribe
	def onPreFired(e: ScriptPreFired): Unit = {
		if(!plugin.getConfig.debugScripts()) return;
		findScriptData(e.getScriptId).flatMap(ScriptApi.getScriptDefinition).foreach(d =>  {
			import com.fredplugins.dialogAssist.ScriptApi.ScriptDefinitions._
			if(d.tpe == ScriptType.CLIENT) {
				val argsList = Option(e.getScriptEvent).flatMap(_.getArguments.pipe(Option(_))).map(_.toList.drop(1)).getOrElse(List.empty[AnyRef])
				var intArgP = 0
				var strArgP = 0
				def nextArg(a: AnyRef): (String, AnyRef) = {
					a match {
						case s: String => {
							val res = (if(d.objArgs.size > strArgP) d.objArgs(strArgP)
							else s"?${strArgP}?")
							strArgP += 1
							res -> s
						}
						case s: Integer => {
							val res = (if(d.intArgs.size > intArgP) d.intArgs(intArgP)
							else s"?${intArgP}?")
							intArgP += 1
							res -> s
						}
					}
				}
				val namedArgsList = for {
					a <- argsList
				} yield nextArg(a)
//				val argumentStr = .map(a => s"$a: ${a.getClass.getSimpleName}").mkString("[", ", ", "]")
				val argumentStr = namedArgsList.map(a => s"${a._1} = ${a._2}").mkString("[", ", ", "]")
				val argSuffix   = (if (argumentStr.equals("[]")) "" else s" => ${argumentStr}")
				log.debug("PreFired{}{}", d.scriptHeader.fullName, argSuffix)
			} else if (d.tpe == ScriptType.PROCEDURAL) {
//				val intStackStr = plugin.getClient.getIntStack.toList.take(plugin.getClient.getIntStackSize).mkString("Ints=[", ", ", "]")
//				val strStackStr = plugin.getClient.getObjectStack.toList.take(plugin.getClient.getObjectStackSize).map(_.toString).map(_.appended('\"').prepended('\"')).mkString("Strs=[", ", ", "]")

				val intStackStr = (0 until Math.min(plugin.getClient.getIntStackSize, d.intArgs.size)).map(idx => (if(d.intArgs.size>idx) d.intArgs(idx) else s"int$$${idx}") -> plugin.getClient.getIntStack.apply(idx))
					.map(u => s"${u._1} = ${u._2}").mkString("Ints=[", ", ", "]").pipe(s => if(s.endsWith("[]")) "" else s)
				val strStackStr = (0 until Math.min(plugin.getClient.getObjectStackSize, d.objArgs.size)).map(idx => (if (d.objArgs.size > idx) d.objArgs(idx) else s"obj$$${idx}") -> plugin.getClient.getObjectStack.apply(idx))
					.map(u => s"${u._1} = ${u._2}").mkString("Strs=[", ", ", "]").pipe(s => if(s.endsWith("[]")) "" else s)

				val argsStr = Seq(intStackStr, strStackStr).filterNot(_.isBlank).mkString(" | ").pipe(s => if(s.isBlank) "" else s" => ${s}")
				log.debug("PreFired{}{}", d.scriptHeader.fullName, argsStr)
			}
		})
//		findScriptData(e.getScriptId).foreach(sd => {
//			if(sd == Script(ScriptType.CLIENT, "skillmulti_setup")) {
//				inScriptMulti = true
//				indent = 0
//			}
//			if(inScriptMulti && sd.tpe == ScriptType.CLIENT) {
//				val argumentStr = Option(e.getScriptEvent).flatMap(_.getArguments.pipe(Option(_))).getOrElse(Array.empty[AnyRef]).toList.drop(1).map(a => s"$a: ${a.getClass.getSimpleName}").mkString("[", ", ", "]")
//				val argSuffix = (if(argumentStr.equals("[]")) "" else s" => ${argumentStr}")
//				log.debug("{}PreFired{}{}", "  ".repeat(indent), sd.fullName,argSuffix)
//				indent = (indent + 1)
//			} else if(inScriptMulti && sd.tpe == ScriptType.PROCEDURAL) {
//				val intStackStr = plugin.getClient.getIntStack.toList.take(plugin.getClient.getIntStackSize).mkString("Ints=[", ", ", "]")
//				val strStackStr = plugin.getClient.getObjectStack.toList.take(plugin.getClient.getObjectStackSize).map(_.toString).map(_.appended('\"').prepended('\"')).mkString("Strs=[", ", ", "]")
//				log.debug("{}PreFired{} => {} | {}", "  ".repeat(indent), sd.fullName,intStackStr, strStackStr)
//				indent = (indent + 1)
//			}
//		})
	}

	@Subscribe
	def onPostFired(e: ScriptPostFired): Unit = {
		if (!plugin.getConfig.debugScripts()) return;
		findScriptData(e.getScriptId).flatMap(ScriptApi.getScriptDefinition).foreach(d =>  {
				val returnStateStr = if(d.tpe == ScriptType.PROCEDURAL) {
					val intStackStr = (0 until Math.min(plugin.getClient.getIntStackSize, d.intReturns.size)).map(idx => (if (d.intReturns.size > idx) d.intReturns.apply(idx) else s"int$$${idx}") -> plugin.getClient.getIntStack.apply(idx))
						.map(u => s"${u._1} = ${u._2}").mkString("Ints=[", ", ", "]").pipe(s => if(s.endsWith("[]")) "" else s)
					val strStackStr = (0 until Math.min(plugin.getClient.getObjectStackSize, d.objReturns.size)).map(idx => (if (d.objReturns.size > idx) d.objReturns.apply(idx) else s"obj$$${idx}") -> plugin.getClient.getObjectStack.apply(idx))
						.map(u => s"${u._1} = ${u._2}").mkString("Strs=[", ", ", "]").pipe(s => if(s.endsWith("[]")) "" else s)

//					val intStackStr = plugin.getClient.getIntStack.toList.take(plugin.getClient.getIntStackSize).mkString("Ints=[", ", ", "]")
//					val strStackStr = plugin.getClient.getObjectStack.toList.take(plugin.getClient.getObjectStackSize).map(_.toString).map(_.appended('\"').prepended('\"')).mkString("Strs=[", ", ", "]")
					Seq(intStackStr, strStackStr).filterNot(_.isBlank).mkString(" | ")
				} else ""

				log.debug("PostFired{}{}", d.scriptHeader.fullName, returnStateStr.pipe(s => if(s.isBlank) "" else s" => ${s}"))
		})
	}
}
