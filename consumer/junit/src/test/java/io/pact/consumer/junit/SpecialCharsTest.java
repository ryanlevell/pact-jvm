package io.pact.consumer.junit;

import io.pact.consumer.dsl.PactDslJsonRootValue;
import io.pact.consumer.dsl.PactDslWithProvider;
import io.pact.core.model.RequestResponsePact;
import io.pact.core.model.annotations.Pact;
import org.apache.http.client.fluent.Request;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SpecialCharsTest {

  @Rule
  public PactProviderRule provider = new PactProviderRule("specialchars_provider", this);

  @Pact(provider = "specialchars_provider", consumer = "test_consumer")
  public RequestResponsePact createFragment(PactDslWithProvider builder) {
    return builder
      .uponReceiving("Request für ping")
      .path("/ping")
      .method("GET")
      .willRespondWith()
      .status(200)
      .body(PactDslJsonRootValue.stringType("Pong"))
      .toPact();
  }

  @Test
  @PactVerification("specialchars_provider")
  public void runTest() throws IOException {
    assertEquals("\"Pong\"", Request.Get(provider.getUrl() + "/ping")
      .execute().returnContent().asString());
  }
}
