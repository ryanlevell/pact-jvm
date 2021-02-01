package io.pact.consumer.junit.v3;

import io.pact.consumer.MessagePactBuilder;
import io.pact.consumer.dsl.PactDslJsonBody;
import io.pact.consumer.dsl.PactDslWithProvider;
import io.pact.consumer.junit.MessagePactProviderRule;
import io.pact.consumer.junit.PactProviderRule;
import io.pact.consumer.junit.PactVerification;
import io.pact.consumer.junit.PactVerifications;
import io.pact.consumer.junit.exampleclients.ConsumerClient;
import io.pact.core.model.PactSpecVersion;
import io.pact.core.model.RequestResponsePact;
import io.pact.core.model.annotations.Pact;
import io.pact.core.model.messaging.MessagePact;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class PactVerificationsForHttpAndMessageTest {

    private static final String HTTP_PROVIDER_NAME = "a_http_provider";
    private static final String MESSAGE_PROVIDER_NAME = "a_message_provider";
    private static final String PACT_VERIFICATIONS_CONSUMER_NAME = "pact_verifications_http_and_message_consumer";

    @Rule
    public PactProviderRule httpProvider =
            new PactProviderRule(HTTP_PROVIDER_NAME, "localhost", 8075, PactSpecVersion.V3, this);

    @Rule
    public MessagePactProviderRule messageProvider = new MessagePactProviderRule(MESSAGE_PROVIDER_NAME, this);

    @Pact(provider = HTTP_PROVIDER_NAME, consumer = PACT_VERIFICATIONS_CONSUMER_NAME)
    public RequestResponsePact httpPact(PactDslWithProvider builder) {
        return builder
                .given("a good state")
                .uponReceiving("a query test interaction")
                .path("/")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body("{\"responsetest\": true, \"name\": \"harry\"}")
                .toPact();
    }

    @Pact(provider = MESSAGE_PROVIDER_NAME, consumer = PACT_VERIFICATIONS_CONSUMER_NAME)
    public MessagePact messagePact(MessagePactBuilder builder) {
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
    @PactVerifications({@PactVerification(HTTP_PROVIDER_NAME), @PactVerification(MESSAGE_PROVIDER_NAME)})
    public void shouldTestHttpAndMessagePacts() throws Exception {
        byte[] message = messageProvider.getMessage();
        assertNotNull(message);

        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("responsetest", true);
        expectedResponse.put("name", "harry");
        Assert.assertEquals(new ConsumerClient(httpProvider.getUrl()).getAsMap("/", ""), expectedResponse);
    }
}
