package io.pact.consumer.junit.v3;

import io.pact.consumer.MessagePactBuilder;
import io.pact.consumer.junit.MessagePactProviderRule;
import io.pact.core.model.annotations.Pact;
import io.pact.consumer.junit.PactVerification;
import io.pact.consumer.dsl.PactDslJsonBody;
import io.pact.core.model.messaging.MessagePact;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class ExampleMessageConsumerWithGetMessageFromRuleTest {

    @Rule
    public MessagePactProviderRule messageProvider = new MessagePactProviderRule(this);

    @Pact(provider = "message_test_provider", consumer = "message_test_consumer")
    public MessagePact createPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody();
        body.stringValue("testParam1", "value1");
        body.stringValue("testParam2", "value2");

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("contentType", "application/json");

        return builder.given("SomeProviderState")
                .expectsToReceive("a test message")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Test
    @PactVerification({"message_test_provider", "SomeProviderState"})
    public void test() throws Exception {
        assertNotNull(new String(messageProvider.getMessage()));
    }
}
