package io.pact.consumer.junit.v3;

import io.pact.core.model.annotations.Pact;
import io.pact.consumer.junit.PactProviderRule;
import io.pact.consumer.junit.PactVerification;
import io.pact.consumer.dsl.PactDslWithProvider;
import io.pact.consumer.junit.exampleclients.ConsumerClient;
import io.pact.core.model.PactSpecVersion;
import io.pact.core.model.RequestResponsePact;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class V3PactProviderTest {

    @Rule
    public PactProviderRule mockTestProvider = new PactProviderRule("test_provider", PactSpecVersion.V3, this);

    @Pact(provider="test_provider", consumer="v3_test_consumer")
    public RequestResponsePact createFragment(PactDslWithProvider builder) {
        return builder
            .given("good state")
            .uponReceiving("V3 PactProviderTest test interaction")
                .path("/")
                .method("GET")
            .willRespondWith()
                .status(200)
                .body("{\"responsetest\": true, \"version\": \"v3\"}")
            .toPact();
    }

    @Test
    @PactVerification
    public void runTest() throws IOException {
        Map expectedResponse = new HashMap();
        expectedResponse.put("responsetest", true);
        expectedResponse.put("version", "v3");
        Assert.assertEquals(new ConsumerClient(mockTestProvider.getUrl()).getAsMap("/", ""), expectedResponse);
    }

}
