package com.fredplugins.kroovy.jfx

import java.io.InputStream
import java.net.URL

import scala.util.control.NonFatal


object KResource {
	def apply(resourceName: String): URL = {
		KResource.getClass.getResource(resourceName)
	}
	private def self: Class[_] = KResource.getClass
	def string(resourceName: String): String = self.getResource(resourceName).toExternalForm
	def url(resourceName: String): URL = self.getResource(resourceName)
	def stream(resourceName: String): InputStream = self.getResourceAsStream(resourceName)
	def withResources[V](name: String)(f: InputStream => V): V = withResources[InputStream, V](stream(name))(f)
	def withResources[T <: AutoCloseable, V](r: => T)(f: T => V): V = {
		val resource: T = r
		require(resource != null, "resource is null")
		var exception: Throwable = null
		try {
			f(resource)
		} catch {
			case NonFatal(e) =>
				exception = e
				throw e
		} finally {
			closeAndAddSuppressed(exception, resource)
		}
	}

	private def closeAndAddSuppressed(e: Throwable,
		resource: AutoCloseable): Unit = {
		if (e != null) {
			try {
				resource.close()
			} catch {
				case NonFatal(suppressed) =>
					e.addSuppressed(suppressed)
			}
		} else {
			resource.close()
		}
	}
}
