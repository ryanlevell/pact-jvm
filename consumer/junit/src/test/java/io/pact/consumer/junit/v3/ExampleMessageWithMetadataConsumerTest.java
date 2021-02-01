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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ExampleMessageWithMetadataConsumerTest {

    @Rule
    public MessagePactProviderRule mockProvider = new MessagePactProviderRule(this);

    @Pact(provider = "test_provider", consumer = "test_consumer_v3")
    public MessagePact createPact(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody();
        body.stringValue("testParam1", "value1");
        body.stringValue("testParam2", "value2");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "metadataValue1");
        metadata.put("metadata2", "metadataValue2");

        return builder.given("SomeProviderState")
                .expectsToReceive("a test message with metadata")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Test
    @PactVerification({"test_provider", "SomeProviderState"})
    public void test() throws Exception {
        assertNotNull(mockProvider.getMessage());
        assertNotNull(mockProvider.getMetadata());
        assertEquals("metadataValue1", mockProvider.getMetadata().get("metadata1"));
        assertEquals("metadataValue2", mockProvider.getMetadata().get("metadata2"));
    }

}
