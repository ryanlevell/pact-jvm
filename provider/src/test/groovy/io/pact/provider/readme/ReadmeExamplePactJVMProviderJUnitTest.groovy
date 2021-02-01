package io.pact.provider.readme

import io.dropwizard.testing.ResourceHelpers
import io.dropwizard.testing.junit.DropwizardAppRule
import io.pact.core.model.DefaultPactReader
import io.pact.core.model.Interaction
import io.pact.core.model.Pact
import io.pact.core.model.ProviderState
import io.pact.core.model.RequestResponseInteraction
import io.pact.core.model.UrlSource
import io.pact.provider.ConsumerInfo
import io.pact.provider.HttpClientFactory
import io.pact.provider.ProviderClient
import io.pact.provider.ProviderInfo
import io.pact.provider.ProviderVerifier
import io.pact.provider.VerificationResult
import io.pact.provider.readme.dropwizard.DropwizardConfiguration
import io.pact.provider.readme.dropwizard.TestDropwizardApplication
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TestRule

import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

/**
 * This is the example from the README
 */
@SuppressWarnings(['ExplicitHashMapInstantiation', 'FieldName', 'JUnitPublicField', 'UnnecessaryGetter',
  'UnnecessaryReturnKeyword'])
class ReadmeExamplePactJVMProviderJUnitTest {

  @ClassRule
  public static final TestRule startServiceRule = new DropwizardAppRule<DropwizardConfiguration>(
    TestDropwizardApplication, ResourceHelpers.resourceFilePath('dropwizard/test-config.yaml'))

  private static ProviderInfo serviceProvider
  private static Pact testConsumerPact
  private static ConsumerInfo consumer

  @BeforeClass
  static void setupProvider() {
    serviceProvider = new ProviderInfo('Dropwizard App')
    serviceProvider.setProtocol('http')
    serviceProvider.setHost('localhost')
    serviceProvider.setPort(8080)
    serviceProvider.setPath('/')

    consumer = new ConsumerInfo()
    consumer.setName('test_consumer')
    consumer.setPactSource(new UrlSource(
      ReadmeExamplePactJVMProviderJUnitTest.getResource('/pacts/zoo_app-animal_service.json').toString()))

    testConsumerPact = DefaultPactReader.INSTANCE.loadPact(consumer.getPactSource()) as Pact<RequestResponseInteraction>
  }

  @Test
  void runConsumerPacts() {
    // grab the first interaction from the pact with consumer
    Interaction interaction = testConsumerPact.interactions.get(0)

    // setup the verifier
    ProviderVerifier verifier = setupVerifier(interaction, serviceProvider, consumer)

    // setup any provider state

    // setup the client and interaction to fire against the provider
    ProviderClient client = new ProviderClient(serviceProvider, new HttpClientFactory())
    def result = verifier.verifyResponseFromProvider(serviceProvider, interaction, interaction.getDescription(),
      [:], client)

    // normally assert all good, but in this example it will fail
    assertThat(result, is(instanceOf(VerificationResult.Failed)))

    verifier.displayFailures([result])
  }

  private ProviderVerifier setupVerifier(Interaction interaction, ProviderInfo provider, ConsumerInfo consumer) {
    ProviderVerifier verifier = new ProviderVerifier()

    verifier.initialiseReporters(provider)
    verifier.reportVerificationForConsumer(consumer, provider, new UrlSource('http://example.example'))

    if (!interaction.getProviderStates().isEmpty()) {
      for (ProviderState providerState: interaction.getProviderStates()) {
        verifier.reportStateForInteraction(providerState.getName(), provider, consumer, true)
      }
    }

    verifier.reportInteractionDescription(interaction)

    return verifier
  }
}
