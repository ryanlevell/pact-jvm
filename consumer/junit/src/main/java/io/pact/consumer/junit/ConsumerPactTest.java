package io.pact.consumer.junit;

import io.pact.consumer.ConsumerPactBuilder;
import io.pact.consumer.MockServer;
import io.pact.consumer.PactMismatchesException;
import io.pact.consumer.PactTestExecutionContext;
import io.pact.consumer.PactVerificationResult;
import io.pact.consumer.dsl.PactDslWithProvider;
import io.pact.consumer.model.MockProviderConfig;
import io.pact.core.model.PactSpecVersion;
import io.pact.core.model.RequestResponsePact;
import org.junit.Test;

import java.io.IOException;

import static io.pact.consumer.ConsumerPactRunnerKt.runConsumerTest;

public abstract class ConsumerPactTest {

    protected abstract RequestResponsePact createPact(PactDslWithProvider builder);
    protected abstract String providerName();
    protected abstract String consumerName();

    protected abstract void runTest(MockServer mockServer, PactTestExecutionContext context) throws IOException;

    @Test
    public void testPact() throws Throwable {
        RequestResponsePact pact = createPact(ConsumerPactBuilder.consumer(consumerName()).hasPactWith(providerName()));
        final MockProviderConfig config = MockProviderConfig.createDefault(getSpecificationVersion());

        PactVerificationResult result = runConsumerTest(pact, config, (mockServer, context) -> {
          runTest(mockServer, context);
          return null;
        });

        if (!(result instanceof PactVerificationResult.Ok)) {
            if (result instanceof PactVerificationResult.Error) {
              PactVerificationResult.Error error = (PactVerificationResult.Error) result;
              if (!(error.getMockServerState() instanceof PactVerificationResult.Ok)) {
                throw new AssertionError("Pact Test function failed with an exception, possibly due to " +
                  error.getMockServerState(), ((PactVerificationResult.Error) result).getError());
              } else {
                throw new AssertionError("Pact Test function failed with an exception: " +
                  error.getError().getMessage(), error.getError());
              }
            } else {
                throw new PactMismatchesException(result);
            }
        }
    }

    protected PactSpecVersion getSpecificationVersion() {
        return PactSpecVersion.V3;
    }

}
