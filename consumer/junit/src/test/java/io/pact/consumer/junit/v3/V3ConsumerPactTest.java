package io.pact.consumer.junit.v3;

import io.pact.consumer.MockServer;
import io.pact.consumer.PactTestExecutionContext;
import io.pact.consumer.dsl.PactDslWithProvider;
import io.pact.consumer.junit.ConsumerPactTest;
import io.pact.consumer.junit.exampleclients.ConsumerClient;
import io.pact.core.model.PactSpecVersion;
import io.pact.core.model.RequestResponsePact;
import org.junit.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class V3ConsumerPactTest extends ConsumerPactTest {

    @Override
    protected RequestResponsePact createPact(PactDslWithProvider builder) {
        return builder
            .uponReceiving("v3 test interaction")
                .path("/")
                .method("GET")
            .willRespondWith()
                .status(200)
                .body("{\"responsetest\": true, \"version\": \"v3\"}")
            .toPact();
    }

    @Override
    protected String providerName() {
        return "test_provider";
    }

    @Override
    protected String consumerName() {
        return "v3_test_consumer";
    }

    @Override
    protected PactSpecVersion getSpecificationVersion() {
        return PactSpecVersion.V3;
    }

    @Override
    protected void runTest(MockServer mockServer, PactTestExecutionContext context) throws IOException {
        Map expectedResponse = new HashMap();
        expectedResponse.put("responsetest", true);
        expectedResponse.put("version", "v3");
        Assert.assertEquals(new ConsumerClient(mockServer.getUrl()).getAsMap("/", ""), expectedResponse);
    }
}
