package io.pact.consumer.junit.v3;

import io.pact.consumer.MessagePactBuilder;
import io.pact.consumer.junit.MessagePactProviderRule;
import io.pact.core.model.annotations.Pact;
import io.pact.core.model.annotations.PactFolder;
import io.pact.consumer.junit.PactVerification;
import io.pact.consumer.dsl.Matchers;
import io.pact.consumer.dsl.PactDslJsonBody;
import io.pact.core.model.messaging.MessagePact;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;

@PactFolder("build/pacts/messages")
public class AsyncMessageTest {
  @Rule
  public MessagePactProviderRule mockProvider = new MessagePactProviderRule("test_provider", this);

  @Pact(provider = "test_provider", consumer = "test_consumer_v3")
  public MessagePact createPact(MessagePactBuilder builder) {
    PactDslJsonBody body = new PactDslJsonBody();
    body.stringValue("testParam1", "value1");
    body.stringValue("testParam2", "value2");

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("Content-Type", "application/json");
    metadata.put("destination", Matchers.regexp("\\w+\\d+", "X001"));

    return builder.given("SomeProviderState")
      .expectsToReceive("a test message")
      .withMetadata(metadata)
      .withContent(body)
      .toPact();
  }

  @Pact(provider = "test_provider", consumer = "test_consumer_v3")
  public MessagePact createPact2(MessagePactBuilder builder) {
    PactDslJsonBody body = new PactDslJsonBody();
    body.stringValue("testParam1", "value3");
    body.stringValue("testParam2", "value4");

    Map<String, String> metadata = new HashMap<String, String>();
    metadata.put("Content-Type", "application/json");

    return builder.given("SomeProviderState2")
      .expectsToReceive("a test message")
      .withMetadata(metadata)
      .withContent(body)
      .toPact();
  }

  @Test
  @PactVerification(value = "test_provider", fragment = "createPact")
  public void test() throws Exception {
    byte[] currentMessage = mockProvider.getMessage();
    assertThat(new String(currentMessage), is("{\"testParam1\":\"value1\",\"testParam2\":\"value2\"}"));
    assertThat(mockProvider.getMetadata(), hasEntry("destination", "X001"));
  }

  @Test
  @PactVerification(value = "test_provider", fragment = "createPact2")
  public void test2() {
    byte[] currentMessage = mockProvider.getMessage();
    assertThat(new String(currentMessage), is("{\"testParam1\":\"value3\",\"testParam2\":\"value4\"}"));
  }
}
