package io.pact.consumer.junit.examples;

import io.pact.consumer.ConsumerPactBuilder;
import io.pact.consumer.PactVerificationResult;
import io.pact.consumer.junit.exampleclients.ProviderClient;
import io.pact.consumer.model.MockProviderConfig;
import io.pact.core.model.RequestResponsePact;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.pact.consumer.ConsumerPactRunnerKt.runConsumerTest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Sometimes it is not convenient to use the ConsumerPactTest as it only allows one test per test class.
 * The DSL can be used directly in this case.
 */
public class DirectDSLConsumerPactTest {

    @Test
    public void testPact() {
        RequestResponsePact pact = ConsumerPactBuilder
                .consumer("Some Consumer")
                .hasPactWith("Some Provider")
                .uponReceiving("a request to say Hello")
                .path("/hello")
                .method("POST")
                .body("{\"name\": \"harry\"}")
                .willRespondWith()
                .status(200)
                .body("{\"hello\": \"harry\"}")
                .toPact();

        MockProviderConfig config = MockProviderConfig.createDefault();
        PactVerificationResult result = runConsumerTest(pact, config, (mockServer, context) -> {
            Map expectedResponse = new HashMap();
            expectedResponse.put("hello", "harry");
            try {
                Assert.assertEquals(new ProviderClient(mockServer.getUrl()).hello("{\"name\": \"harry\"}"),
                        expectedResponse);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        if (result instanceof PactVerificationResult.Error) {
            throw new RuntimeException(((PactVerificationResult.Error)result).getError());
        }

        assertThat(result, is(instanceOf(PactVerificationResult.Ok.class)));
    }

}
