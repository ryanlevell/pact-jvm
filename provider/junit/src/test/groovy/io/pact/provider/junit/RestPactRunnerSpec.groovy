package io.pact.provider.junit

import io.pact.core.model.Consumer
import io.pact.core.model.FilteredPact
import io.pact.core.model.OptionalBody
import io.pact.core.model.Pact
import io.pact.core.model.ProviderState
import io.pact.core.model.Request
import io.pact.core.model.RequestResponseInteraction
import io.pact.core.model.RequestResponsePact
import io.pact.core.model.Response
import io.pact.core.model.messaging.Message
import io.pact.core.model.messaging.MessagePact
import io.pact.provider.junitsupport.IgnoreNoPactsToVerify
import io.pact.provider.junitsupport.Provider
import io.pact.provider.junitsupport.loader.PactFilter
import io.pact.provider.junitsupport.loader.PactFolder
import io.pact.provider.junitsupport.target.Target
import io.pact.provider.junitsupport.target.TestTarget
import spock.lang.Specification

class RestPactRunnerSpec extends Specification {

  private List<Pact> pacts
  private Consumer consumer, consumer2
  private io.pact.core.model.Provider provider
  private List<RequestResponseInteraction> interactions
  private List<Message> interactions2
  private RequestResponsePact reqResPact

  @Provider('myAwesomeService')
  @PactFolder('pacts')
  @PactFilter('State 1')
  @IgnoreNoPactsToVerify
  class TestClass {
    @TestTarget
    Target target
  }

  @Provider('myAwesomeService')
  @PactFolder('pacts')
  class TestClass2 {
    @TestTarget
    Target target
  }

  def setup() {
    consumer = new Consumer('Consumer 1')
    consumer2 = new Consumer('Consumer 2')
    provider = new io.pact.core.model.Provider('myAwesomeService')
    interactions = [
      new RequestResponseInteraction('Req 1', [
        new ProviderState('State 1')
      ], new Request(), new Response()),
      new RequestResponseInteraction('Req 2', [
        new ProviderState('State 1'),
        new ProviderState('State 2')
      ], new Request(), new Response())
    ]
    interactions2 = [
      new Message('Req 3', [
        new ProviderState('State 3')
      ], OptionalBody.body('{}'.bytes)),
      new Message('Req 4', [
        new ProviderState('State X')
      ], OptionalBody.empty())
    ]
    reqResPact = new RequestResponsePact(provider, consumer, interactions)
    pacts = [
      reqResPact,
      new MessagePact(provider, consumer2, interactions2)
    ]
  }

  def 'only verifies request response pacts'() {
    given:
    RestPactRunner pactRunner = new RestPactRunner(TestClass)

    when:
    def result = pactRunner.filterPacts(pacts)

    then:
    result.size() == 1
    result*.pact == [ reqResPact ]
  }

  def 'handles filtered pacts'() {
    given:
    RestPactRunner pactRunner = new RestPactRunner(TestClass2)
    pacts = [ new FilteredPact(reqResPact, { true }) ]

    when:
    def result = pactRunner.filterPacts(pacts)

    then:
    result.size() == 1
  }

}
