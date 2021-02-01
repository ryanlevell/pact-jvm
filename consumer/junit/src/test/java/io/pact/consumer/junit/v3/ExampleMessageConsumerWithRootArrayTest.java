package io.pact.consumer.junit.v3;

import io.pact.consumer.MessagePactBuilder;
import io.pact.consumer.junit.MessagePactProviderRule;
import io.pact.core.model.annotations.Pact;
import io.pact.consumer.junit.PactVerification;
import io.pact.consumer.dsl.PactDslJsonArray;
import io.pact.core.model.messaging.MessagePact;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class ExampleMessageConsumerWithRootArrayTest {

    @Rule
    public MessagePactProviderRule mockProvider = new MessagePactProviderRule(this);
    private byte[] currentMessage;

    @Pact(provider = "test_provider", consumer = "test_consumer_v3")
    public MessagePact createPact(MessagePactBuilder builder) {
        PactDslJsonArray body = new PactDslJsonArray()
          .decimalType(100.10)
          .stringType("Should be in an array");

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("contentType", "application/json");

        return builder.given("SomeProviderState")
                .expectsToReceive("a test message with an array")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Test
    @PactVerification({"test_provider", "SomeProviderState"})
    public void test() throws Exception {
        assertThat(new String(currentMessage), is(equalTo("[100.1,\"Should be in an array\"]")));
    }

    public void setMessage(byte[] messageContents) {
        currentMessage = messageContents;
    }
}
