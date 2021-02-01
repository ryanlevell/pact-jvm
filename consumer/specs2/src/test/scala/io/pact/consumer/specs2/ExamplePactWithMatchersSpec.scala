package io.pact.consumer.specs2

import io.pact.consumer.dsl.PactDslJsonBody
import org.json.JSONObject
import org.junit.Ignore
import org.specs2.mutable.Specification

import java.util.concurrent.TimeUnit.MILLISECONDS
import scala.concurrent.Await
import scala.concurrent.duration.Duration

//@RunWith(classOf[JUnitRunner])
@Ignore
class ExamplePactWithMatchersSpec extends Specification with PactSpec {

  val consumer = "My Consumer"
  val provider = "My Provider"

  val timeout = Duration(5000, MILLISECONDS)

  val body = new PactDslJsonBody()
    .stringMatcher("foo", "\\d{1,9}", "100")
    .stringMatcher("bar", "[aA]+", "aaAA")

  override def is = uponReceiving("a request for foo with a body")
      .matching(path = "/foo")
      .willRespondWith(
        status = 200,
        headers = Map.empty[String, List[String]],
        bodyAndMatchers = body
      )
    .withConsumerTest((mockServer, _) => {
      val (status, body) = Await.result(ConsumerService(mockServer.getUrl).simpleGet("/foo"), timeout)
      val bodyJson = new JSONObject(body)

      (status ==== 200) and
        (bodyJson.getInt("foo") must be >= 0) and
        ((bodyJson.getString("bar").length > 0) ==== true)
    })
}
