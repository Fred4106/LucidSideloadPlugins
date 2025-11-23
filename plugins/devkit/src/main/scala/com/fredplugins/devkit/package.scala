package com.fredplugins

import net.runelite.api.coords.WorldPoint
import com.fredplugins.common.api.WorldRegion
import com.fredplugins.devkit.ConfigKeyParser.term
import com.fredplugins.common.extensions.ActorExtensions.*
import com.fredplugins.common.extensions.GeneralExtensions.*
import com.fredplugins.common.extensions.ObjectExtensions.*
import com.fredplugins.common.extensions.ProjectileExtensions.*
import com.fredplugins.common.extensions.LocationExtensions.*
import com.fredplugins.common.utils.ShimUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.runelite.client.RuneLite
import net.runelite.http.api.RuneLiteAPI
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Dispatcher
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.Logger

import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}
import scala.compiletime.uninitialized
import scala.util.Failure
import scala.util.Success
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

package object devkit extends ShimUtils.Logging("DEBUG") {
	inline val GROUP = "DevKit"

	case class RegionKey(archive: Int, group: Int, name_hash: Int, name: String, mapsquare: Int, key: (Int, Int, Int, Int)) {}

	val gson: Gson = RuneLiteAPI.GSON.newBuilder()
		.registerTypeAdapter(classOf[RegionKey], {
			//			val delegate: TypeAdapter[RegionKeyRaw] = RuneLiteAPI.GSON.getAdapter(classOf[RegionKeyRaw])
			new TypeAdapter[RegionKey] {
				case class RegionKeyRaw(archive: Int, group: Int, name_hash: Int, name: String, mapsquare: Int, key: Array[Int]) {}
				def rawToKey = (raw: RegionKeyRaw) => RegionKey(raw.archive, raw.group, raw.name_hash, raw.name, raw.mapsquare, raw.key.pipe(k => (k(0), k(1), k(2), k(3))))
				def keyToRaw = (key: RegionKey) => RegionKeyRaw(key.archive, key.group, key.name_hash, key.name, key.mapsquare, key.key.pipe(k => Array(k._1, k._2, k._3, k._4)))

				override def write(out: JsonWriter, value: RegionKey): Unit =  gson.toJson(keyToRaw(value), classOf[RegionKeyRaw], out)
				override def read(in: JsonReader): RegionKey = gson.fromJson[RegionKeyRaw](in, classOf[RegionKeyRaw]).pipe(k => rawToKey(k))
			}
		}).create()
	private val httpClient: OkHttpClient = RuneLiteAPI.CLIENT.newBuilder().dispatcher(new Dispatcher( )).build

	def getKeys(cacheId: Int): Set[RegionKey] = {
		val url     = HttpUrl.get("https://archive.openrs2.org/caches/runescape").newBuilder.addPathSegment(s"${cacheId}").addPathSegment("keys.json").build
		val request = new Request.Builder().url(url).build
//		val completableFuture = new CompletableFuture[Seq[RegionKey]]()
		val call = httpClient.newCall(request)
//		new Thread(() => {
		(Try(call.execute()).flatMap(response => {
				val bodyStr = response.body().string()
				Try(gson.fromJson[Array[RegionKey]](bodyStr, new TypeToken[Array[RegionKey]](){}.getType)).map(_.toSet)
			}))
			.fold(exception => {
				devkit.log.warn(s"Failed to parse ${cacheId}", exception)
				Set.empty[RegionKey]
			}, e2 => e2)
//		})
		}
//		Try(call.execute()).map(r => {
//				gson.fromJson[Array[RegionKey]](r.body().string(), classOf[Array[RegionKey]])
////				val jsonTree    = new JsonParser().parse(responsebody)
////				val jsonObjects = if (jsonTree.isJsonArray) jsonTree.getAsJsonArray.asScala.toList.flatMap(e => Option.when(e.isJsonObject)(e.getAsJsonObject)) else List.empty[JsonObject]
////				jsonObjects.headOption.foreach(head => {
////					Try(gson.fromJson(head, classOf[RegionKey])) match {
////						case Failure(exception: JsonSyntaxException) => {
////							log.warn(s"Failed to parse ${config.cacheId} \"${head}\"", exception)
////						}
////						case Success(res) => {
////							log.debug(s"Parsed ${config.cacheId} \"${head}\" = ${res}")
////						}
////					}
//				})
//				Set.empty[RegionKey]
//			}).getOrElse(Set.empty[RegionKey])
//		});
//		t.start()

	trait DevkitParser extends RegexParsers {
		given Conversion[String, Parser[String]] = literal(_)
		given Conversion[Regex, Parser[String]] = regex(_)

		protected def log: Logger = {
			devkit.log
		}

		def ident: Parser[String] =
			"" ~> // handle whitespace
				rep1(
					acceptIf(Character.isJavaIdentifierStart)("identifier expected but '" + _ + "' found"),
					elem("identifier part", Character.isJavaIdentifierPart(_: Char))) ^^ (_.mkString)

		/** An integer, without sign or with a negative sign. */
		def wholeNumber: Parser[Int] =
			"" ~> """-?\d+""".r ^^ {_.toInt}

		/** Number following one of these rules:
			*
			*  - An integer. For example: `13`
			*  - An integer followed by a decimal point. For example: `3.`
			*  - An integer followed by a decimal point and fractional part. For example: `3.14`
			*  - A decimal point followed by a fractional part. For example: `.1`
			*/
		def decimalNumber: Parser[Double] =
			"""(\d+(\.\d*)?|\d*\.\d+)""".r ^^ {_.toDouble}

		/** Double quotes (`"`) enclosing a sequence of:
			*
			*  - Any character except double quotes, control characters or backslash (`\`)
			*  - A backslash followed by another backslash, a single or double quote, or one
			*    of the letters `b`, `f`, `n`, `r` or `t`
			*  - `\` followed by `u` followed by four hexadecimal digits
			*/
		def stringLiteral: Parser[String] =
			("\"" + """([^"\x00-\x1F\x7F\\]|\\[\\'"bfnrt]|\\u[a-fA-F0-9]{4})*""" + "\"").r

		/** A number following the rules of `decimalNumber`, with the following
			*  optional additions:
			*
			*  - Preceded by a negative sign
			*  - Followed by `e` or `E` and an optionally signed integer
			*  - Followed by `f`, `f`, `d` or `D` (after the above rule, if both are used)
			*/
		def floatingPointNumber: Parser[Double] =
			"""-?(\d+(\.\d*)?|\d*\.\d+)([eE][+-]?\d+)?[fFdD]?""".r ^^ {_.toDouble}

		val anyParser: Parser[String] = Parser { in =>
				if (in.atEnd) Failure("end of input", in)
				else Success(in.first.toString, in.rest)
		}
	}

	object ConfigKeyParser extends DevkitParser {
		val seperator: Parser[String] = "."
		val term: Parser[String] = rep1(not(seperator) ~> anyParser) ^^ {_.mkString("") }//regex(raw"[^.]+".r)
		def address: Parser[Seq[String]] = rep1(term, seperator ~> term)
		//		def number: Parser[Double] = """\d+(\.\d*)?""".r ^^ {_.toDouble}
		//		def factor: Parser[Double] = number | "(" ~> expr <~ ")"
		//		def term: Parser[Double] = factor ~ rep("*" ~ factor | "/" ~ factor) ^^ {
		//			case number ~ list => list.foldLeft(number) {
		//				case (x, "*" ~ y) => x * y
		//				case (x, "/" ~ y) => x / y
		//			}
		//		}
		//		def expr: Parser[Double] = term ~ rep("+" ~ log(term)("Plus term") | "-" ~ log(term)("Minus term")) ^^ {
		//			case number ~ list => list.foldLeft(number) {
		//				case (x, "+" ~ y) => x + y
		//				case (x, "-" ~ y) => x - y
		//			}
		//		}

		def apply(key: String): Seq[String] = parseAll(address, key).get
	}

	object WorldRegionParser extends DevkitParser {
		val worldPointParser: Parser[WorldPoint] = wholeNumber ^^ WorldPoint.fromCoord

		val worldPointSeqParser     : Parser[Seq[WorldPoint]]            = repsep(worldPointParser, ",")
		val worldRegionParser: Parser[WorldRegion] = {
			"WorldRegion" ~> "(" ~> worldPointSeqParser <~ ")"
		} ^^ {
			x => WorldRegion.ComplexRegion(x.sortBy(_.packed))
		}
		val worldRegionsParser: Parser[Map[String, WorldRegion]] = {
			rep(
				((ident <~ ":") ~ worldRegionParser).map(x => x._1 -> x._2),
			).map(_.sortBy(_._1).toMap)
		}
		//		def number: Parser[Double] = """\d+(\.\d*)?""".r ^^ {_.toDouble}
		//		def factor: Parser[Double] = number | "(" ~> expr <~ ")"
		//		def term: Parser[Double] = factor ~ rep("*" ~ factor | "/" ~ factor) ^^ {
		//			case number ~ list => list.foldLeft(number) {
		//				case (x, "*" ~ y) => x * y
		//				case (x, "/" ~ y) => x / y
		//			}
		//		}
		//		def expr: Parser[Double] = term ~ rep("+" ~ log(term)("Plus term") | "-" ~ log(term)("Minus term")) ^^ {
		//			case number ~ list => list.foldLeft(number) {
		//				case (x, "+" ~ y) => x + y
		//				case (x, "-" ~ y) => x - y
		//			}
		//		}

		def apply(input: String): Map[String, WorldRegion] = parseAll(worldRegionsParser, input) match {
			case Success(result, _) => result
			case NoSuccess.I(msg, _) => log.debug(s"${this.getClass.getSimpleName}: ${msg}"); Map.empty[String, WorldRegion]
		}

		def stringify(map: Map[String, WorldRegion]): String = {
			map.map{
				case (k,v) => s"${k}: ${v.worldPoints.map(_.packed).mkString("WorldRegion(", ", ", ")")}"
			}.mkString("\n")
		}
	}
}
