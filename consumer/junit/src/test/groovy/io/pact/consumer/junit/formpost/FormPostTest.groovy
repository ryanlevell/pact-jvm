package io.pact.consumer.junit.formpost

import io.pact.consumer.junit.ConsumerPactTest
import io.pact.consumer.MockServer
import io.pact.consumer.PactTestExecutionContext
import io.pact.consumer.dsl.PactDslJsonBody
import io.pact.consumer.dsl.PactDslWithProvider
import io.pact.core.model.RequestResponsePact

class FormPostTest extends ConsumerPactTest {

    @Override
    protected RequestResponsePact createPact(PactDslWithProvider builder) {
        builder.given('grizzly bear can be added to zoo')
          .uponReceiving('a POST request to add a grizzly bear named Bubbles')
            .path('/zoo-ws/animals')
            .method('POST')
            .headers(['Content-Type': 'application/x-www-form-urlencoded'])
            .body('type=grizzly+bear&name=Bubbles')
          .willRespondWith()
            .status(200)
            .headers(['Content-Type': 'application/json'])
            .body(new PactDslJsonBody()
              .stringValue('animalType', 'grizzly bear')
              .stringType('name', 'Bubbles')
              .array('feedingLog')// we expect the feeding logs to be empty when you first add an animal
              .closeArray())
          .toPact()
    }

    @Override
    protected String providerName() {
        'zoo-ws'
    }

    @Override
    protected String consumerName() {
        'zoo-client'
    }

    @Override
    protected void runTest(MockServer mockServer, PactTestExecutionContext context) {
        ZooClient fakeZooClient = new ZooClient(mockServer.url)

        Animal grizzly = fakeZooClient.saveAnimal('grizzly bear', 'Bubbles')

        assert grizzly.animalType == 'grizzly bear'
        assert grizzly.name == 'Bubbles'
        assert grizzly.feedingLog.empty
    }
}
