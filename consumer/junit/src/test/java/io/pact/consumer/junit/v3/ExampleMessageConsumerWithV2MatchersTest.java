package io.pact.consumer.junit.v3;

import io.pact.consumer.junit.MatcherTestUtils;
import io.pact.consumer.MessagePactBuilder;
import io.pact.consumer.junit.MessagePactProviderRule;
import io.pact.core.model.annotations.Pact;
import io.pact.consumer.junit.PactVerification;
import io.pact.consumer.dsl.PactDslJsonBody;
import io.pact.core.model.messaging.MessagePact;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ExampleMessageConsumerWithV2MatchersTest {

    @Rule
    public MessagePactProviderRule mockProvider = new MessagePactProviderRule(this);
    private byte[] currentMessage;

    @Pact(provider = "test_provider_v3", consumer = "test_consumer_v3")
    public MessagePact createPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody()
          .uuid("workflowId")
          .stringType("domain")
          .eachLike("values")
            .stringType("key")
            .stringType("value")
            .closeObject()
          .closeArray()
          .asBody();

        Map<String, String> metaData = new HashMap<>();
        metaData.put("contentType", "application/json");

      MessagePact messagePact = builder.given("executing a workflow with rabbitmq")
        .expectsToReceive("execution payload")
        .withContent(body)
        .withMetadata(metaData)
        .toPact();

      MatcherTestUtils.assertMessageMatcherKeysEqualTo(messagePact, "body",
        "$.workflowId",
        "$.domain",
        "$.values",
        "$.values",
        "$.values[*].key",
        "$.values[*].value"
      );

      return messagePact;
    }

    @Test
    @PactVerification("test_provider_v3")
    public void test() throws Exception {
        Assert.assertNotNull(new String(currentMessage));
    }

    public void setMessage(byte[] messageContents) {
        currentMessage = messageContents;
    }
}
