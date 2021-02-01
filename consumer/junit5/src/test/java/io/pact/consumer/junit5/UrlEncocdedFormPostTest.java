package io.pact.consumer.junit5;

import io.pact.consumer.MockServer;
import io.pact.consumer.dsl.FormPostBuilder;
import io.pact.consumer.dsl.PactDslWithProvider;
import io.pact.core.model.RequestResponsePact;
import io.pact.core.model.annotations.Pact;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "FormPostProvider")
public class UrlEncocdedFormPostTest {
  @Pact(consumer = "FormPostConsumer")
  public RequestResponsePact formpost(PactDslWithProvider builder) {
    return builder
      .uponReceiving("FORM POST request")
        .path("/form")
        .method("POST")
        .body(
          new FormPostBuilder()
            .uuid("id")
            .stringMatcher("value", "\\d+", "1", "2", "3"))
      .willRespondWith()
        .status(200)
      .toPact();
  }

  @Test
  void testFormPost(MockServer mockServer) throws IOException {
    HttpResponse httpResponse = Request.Post(mockServer.getUrl() + "/form")
      .bodyForm(
        new BasicNameValuePair("id", UUID.randomUUID().toString()),
        new BasicNameValuePair("value", "3"),
        new BasicNameValuePair("value", "1"),
        new BasicNameValuePair("value", "2")).execute().returnResponse();
    assertThat(httpResponse.getStatusLine().getStatusCode(), is(equalTo(200)));
  }
}
