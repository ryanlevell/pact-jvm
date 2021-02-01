package io.pact.provider.gradle

import io.pact.core.model.OptionalBody
import io.pact.core.model.ProviderState
import io.pact.core.model.Request
import io.pact.core.model.RequestResponseInteraction
import io.pact.core.model.Response
import io.pact.provider.ConsumerInfo
import io.pact.provider.DefaultStateChange
import io.pact.provider.ProviderClient
import io.pact.provider.ProviderInfo
import io.pact.provider.ProviderVerifier
import io.pact.provider.StateChange
import io.pact.provider.StateChangeResult
import com.github.michaelbull.result.Ok
import spock.lang.Specification

class ProviderVerifierStateChangeSpec extends Specification {

  private ProviderVerifier providerVerifier
  private ProviderInfo providerInfo
  private ConsumerInfo consumer
  private ProviderClient providerClient

  def setup() {
    providerInfo = new ProviderInfo()
    providerVerifier = new ProviderVerifier()
    providerClient = Mock()
  }

  def 'if teardown is set then a statechage teardown request is made after the test'() {
    given:
    def state = new ProviderState('state of the nation')
    def interaction = new RequestResponseInteraction('provider state test', [state],
      new Request(), new Response(200, [:], OptionalBody.body('{}'.bytes)))
    def failures = [:]
    consumer = new ConsumerInfo('Bob', 'http://localhost:2000/hello')
    providerInfo.stateChangeTeardown = true
    def statechange = Mock(StateChange)
    providerVerifier.stateChangeHandler = statechange

    when:
    providerVerifier.verifyInteraction(providerInfo, consumer, failures, interaction)

    then:
    1 * statechange.executeStateChange(*_) >> new StateChangeResult(new Ok([:]), 'interactionMessage')
    1 * statechange.executeStateChangeTeardown(providerVerifier, interaction, providerInfo, consumer, _)
  }

  def 'if the state change is a closure and teardown is set, executes it with the state change as a parameter'() {
    given:
    def closureArgs = []
    consumer = new ConsumerInfo('Bob', { arg1, arg2 ->
      closureArgs << [arg1, arg2]
      true
    })
    def state = new ProviderState('state of the nation')
    def interaction = new RequestResponseInteraction('provider state test', [state],
      new Request(), new Response(200, [:], OptionalBody.body('{}'.bytes)))
    def failures = [:]
    providerInfo.stateChangeTeardown = true

    when:
    DefaultStateChange.INSTANCE.executeStateChange(providerVerifier, providerInfo, consumer, interaction,
      'state of the nation', failures, providerClient)
    DefaultStateChange.INSTANCE.executeStateChangeTeardown(providerVerifier, interaction, providerInfo, consumer,
      providerClient)

    then:
    closureArgs == [[state, 'setup'], [state, 'teardown']]
  }

}
