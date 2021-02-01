package io.pact.provider

import _root_.org.apache.http.HttpRequest

object GroovyScalaUtils {

  def testRequestFilter: HttpRequest => Unit = (httpRequest: HttpRequest) => httpRequest.addHeader("Scala", "Was Called")

}
