package io.pact.consumer.junit.examples;

import io.pact.core.model.annotations.Pact;
import io.pact.consumer.junit.PactProviderRule;
import io.pact.consumer.junit.PactVerification;
import io.pact.consumer.dsl.PactDslWithProvider;
import io.pact.consumer.junit.exampleclients.ConsumerClient;
import io.pact.core.model.RequestResponsePact;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ExampleJavaConsumerPactRuleTest {

    @Rule
    public PactProviderRule provider = new PactProviderRule("test_provider", this);

    @Pact(provider="test_provider", consumer="test_consumer")
    public RequestResponsePact createFragment(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("testreqheader", "testreqheadervalue");

        return builder
            .given("test state")
            .uponReceiving("ExampleJavaConsumerPactRuleTest test interaction")
                .path("/")
                .method("GET")
                .headers(headers)
            .willRespondWith()
                .status(200)
                .headers(headers)
                .body("{\"responsetest\": true, \"name\": \"harry\"}")
            .given("test state")
            .uponReceiving("ExampleJavaConsumerPactRuleTest second test interaction")
                .method("OPTIONS")
                .headers(headers)
                .path("/second")
                .body("")
            .willRespondWith()
                .status(200)
                .headers(headers)
                .body("")
            .toPact();
    }

    @Test
    @PactVerification("test_provider")
    public void runTest() throws IOException {
        Assert.assertEquals(new ConsumerClient(provider.getUrl()).options("/second"), 200);
        Map expectedResponse = new HashMap();
        expectedResponse.put("responsetest", true);
        expectedResponse.put("name", "harry");
        assertEquals(new ConsumerClient(provider.getUrl()).getAsMap("/", ""), expectedResponse);
    }
}
