package com.fredplugins.kroovy.jfx.utils

import java.io.{IOException, OutputStream, PrintStream}

class StreamCapturer(val source: PrintStream, val consumer: (String) => Unit) extends OutputStream {
	val stringBuilder = new StringBuilder

	@throws(classOf[IOException])
	override def write(b: Int): Unit = {
		val char = b.toChar
		stringBuilder.append(char)
		if (char == '\n') {
			consumer(stringBuilder.toString())
			stringBuilder.clear()
		}
		source.print(char)
	}
}
