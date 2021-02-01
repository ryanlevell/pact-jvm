package io.pact.consumer.specs2

import io.pact.consumer.specs2.dispatch.HttpClient
import io.pact.core.model.{OptionalBody, PactReaderKt, Request}

import java.util.concurrent.Executors
import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

case class ConsumerService(serverUrl: String) {
  import Fixtures._
  implicit val executionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool)

  private def extractFrom(body: OptionalBody): Boolean = {
    body.valueAsString == "{\"responsetest\": true}"
  }

  def extractResponseTest(path: String = request.getPath): Future[Boolean] = {
    val r = request.copy()
    r.setPath(s"$serverUrl$path")
    toScala[Boolean](HttpClient.run(r).thenApply(response => response.getStatus == 200 && extractFrom(response.getBody)))
  }

  def simpleGet(path: String): Future[(Int, String)] = {
    toScala[(Int, String)](HttpClient.run(new Request("GET", serverUrl + path)).thenApply { response =>
      (response.getStatus, response.getBody.valueAsString)
    })
  }

  def simpleGet(path: String, query: String): Future[(Int, String)] = {
    toScala[(Int, String)](HttpClient.run(new Request("GET", serverUrl + path, PactReaderKt.queryStringToMap(query, true))).thenApply { response =>
      (response.getStatus, response.getBody.valueAsString)
    })
  }

  def options(path: String): Future[(Int, String, Map[String, String])] = {
    toScala[(Int, String, Map[String, String])](HttpClient.run(new Request("OPTION", serverUrl + path)).thenApply { response =>
      (response.getStatus, response.getBody.valueAsString, response.getHeaders.asScala.view
        .mapValues(v => v.asScala.mkString(", ")).filterKeys(_ == "Option").toMap)
    })
  }
}
