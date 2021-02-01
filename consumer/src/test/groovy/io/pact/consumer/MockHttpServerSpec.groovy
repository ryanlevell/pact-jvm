package io.pact.consumer

import io.pact.core.model.Consumer
import io.pact.core.model.Provider
import io.pact.core.model.RequestResponsePact
import io.pact.consumer.model.MockProviderConfig
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Unroll

import static io.pact.consumer.MockHttpServerKt.mockServer

class MockHttpServerSpec extends Specification {

  @Unroll
  def 'calculated charset test - "#contentTypeHeader"'() {

    expect:
    MockHttpServerKt.calculateCharset(headers).name() == expectedCharset

    where:

    contentTypeHeader               | expectedCharset
    null                            | 'UTF-8'
    'null'                          | 'UTF-8'
    ''                              | 'UTF-8'
    'text/plain'                    | 'UTF-8'
    'text/plain; charset'           | 'UTF-8'
    'text/plain; charset='          | 'UTF-8'
    'text/plain;charset=ISO-8859-1' | 'ISO-8859-1'

    headers = ['Content-Type': [contentTypeHeader]]

  }

  def 'with no content type defaults to UTF-8'() {
    expect:
    MockHttpServerKt.calculateCharset([:]).name() == 'UTF-8'
  }

  def 'ignores case with the header name'() {
    expect:
    MockHttpServerKt.calculateCharset(['content-type': ['text/plain; charset=ISO-8859-1']]).name() == 'ISO-8859-1'
  }

  @Timeout(60)
  @IgnoreIf({ System.env.TRAVIS != 'true' })
  def 'handle more than 200 tests'() {
    given:
    def pact = new RequestResponsePact(new Provider(), new Consumer(), [])
    def config = MockProviderConfig.createDefault()

    when:
    201.times { count ->
      def server = mockServer(pact, config)
      server.runAndWritePact(pact, config.pactVersion) { s, context -> }
    }

    then:
    true
  }

}
