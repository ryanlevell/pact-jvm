package io.pact.consumer.junit;

import io.pact.consumer.MockServer;
import io.pact.consumer.PactTestExecutionContext;
import io.pact.consumer.dsl.PactDslWithProvider;
import io.pact.consumer.junit.exampleclients.ConsumerClient;
import io.pact.core.model.RequestResponsePact;

import java.io.IOException;

public class QueryParameterEncodingTest extends ConsumerPactTest {

    @Override
    protected RequestResponsePact createPact(PactDslWithProvider builder) {
        return builder
                .uponReceiving("java test interaction with a query string")
                .path("/some path")
                .method("GET")
                .query("datetime=2011-12-03T10:15:30+01:00")
                .willRespondWith()
                .status(200)
                .body("{}")
                .toPact();
    }

    @Override
    protected String providerName() {
        return "test_provider";
    }

    @Override
    protected String consumerName() {
        return "test_consumer";
    }

    @Override
    protected void runTest(MockServer mockServer, PactTestExecutionContext context) throws IOException {
        new ConsumerClient(mockServer.getUrl()).getAsMap("/some path", "datetime=2011-12-03T10:15:30+01:00");
    }
}
