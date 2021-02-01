package io.pact.consumer.junit.resultstests;

import io.pact.consumer.junit.ConsumerPactTest;
import io.pact.consumer.MockServer;
import io.pact.consumer.PactTestExecutionContext;
import io.pact.consumer.dsl.PactDslWithProvider;
import io.pact.consumer.junit.exampleclients.ConsumerClient;
import io.pact.core.model.RequestResponsePact;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PactVerifiedConsumerPassesTest extends ConsumerPactTest {

    @Override
    protected RequestResponsePact createPact(PactDslWithProvider builder) {
        return builder
            .uponReceiving("PactVerifiedConsumerPassesTest test interaction")
                .path("/")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body("{\"responsetest\": true, \"name\": \"harry\"}")
            .toPact();
    }


    @Override
    protected String providerName() {
        return "resultstests_provider";
    }

    @Override
    protected String consumerName() {
        return "resultstests_consumer";
    }

    @Override
    protected void runTest(MockServer mockServer, PactTestExecutionContext context) throws IOException {
        Map<String, Object> expectedResponse = new HashMap<String, Object>();
        expectedResponse.put("responsetest", true);
        expectedResponse.put("name", "harry");
        assertEquals(new ConsumerClient(mockServer.getUrl()).getAsMap("/", ""), expectedResponse);
    }
}
