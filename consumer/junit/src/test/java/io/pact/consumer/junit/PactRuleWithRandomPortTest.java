package io.pact.consumer.junit;

import io.pact.consumer.dsl.PactDslWithProvider;
import io.pact.consumer.junit.exampleclients.ConsumerClient;
import io.pact.core.model.RequestResponsePact;
import io.pact.core.model.annotations.Pact;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PactRuleWithRandomPortTest {

    @Rule
    public PactProviderRule rule = new PactProviderRule("test_provider", this);

    @Pact(provider="test_provider", consumer="test_consumer")
    public RequestResponsePact createFragment(PactDslWithProvider builder) {
        RequestResponsePact pact = builder
          .given("test state")
          .uponReceiving("random port test interaction")
          .path("/")
          .method("GET")
          .willRespondWith()
          .status(200)
          .toPact();
        return pact;
    }

    @Test
    @PactVerification("test_provider")
    public void runTest() throws IOException {
        Map expectedResponse = new HashMap();
        Assert.assertEquals(new ConsumerClient("http://localhost:" + rule.getPort()).getAsMap("/", ""), expectedResponse);
    }
}
