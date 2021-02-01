package io.pact.consumer.junit5

import io.pact.consumer.MockServer
import io.pact.consumer.dsl.PactDslJsonBody
import io.pact.consumer.dsl.PactDslWithProvider
import io.pact.core.model.RequestResponsePact
import io.pact.core.model.annotations.Pact
import org.apache.http.HttpResponse
import org.apache.http.client.fluent.Request
import org.apache.http.entity.StringEntity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@ExtendWith(PactConsumerTestExt)
@PactTestFor(providerName = 'ProviderWithDateTime')
class DateTimeWithTimezoneTest {
  @Pact(consumer = 'Consumer')
  RequestResponsePact pactWithTimezone(PactDslWithProvider builder) {
    builder
      .uponReceiving('a request with some datetime info')
        .method('POST')
        .path('/values')
        .body(new PactDslJsonBody().datetime('datetime', "YYYY-MM-dd'T'HH:mm:ss.SSSXXX"))
      .willRespondWith()
        .status(200)
        .body(new PactDslJsonBody().datetime('datetime', "YYYY-MM-dd'T'HH:mm:ss.SSSXXX"))
      .toPact()
  }

  @Test
  void testFiles(MockServer mockServer) {
    HttpResponse httpResponse = Request.Post("${mockServer.url}/values")
      .body(new StringEntity('{"datetime": "' +
        DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss.SSSXXX").format(ZonedDateTime.now())
        + '"}', 'application/json', 'UTF-8'))
      .execute().returnResponse()
    assert httpResponse.statusLine.statusCode == 200
  }
}
