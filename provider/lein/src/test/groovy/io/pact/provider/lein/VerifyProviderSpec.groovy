package io.pact.provider.lein

import io.pact.core.model.UrlSource
import clojure.java.api.Clojure
import clojure.lang.IFn
import io.pact.provider.ConsumerInfo
import io.pact.provider.ProviderInfo
import io.pact.provider.ProviderVerifier
import spock.lang.Specification

@SuppressWarnings('UnnecessaryObjectReferences')
class VerifyProviderSpec extends Specification {

  private ProviderVerifier verifier
  static private IFn toProvider
  static private IFn toConsumer

  def setup() {
    verifier = new ProviderVerifier()
  }

  def setupSpec() {
    IFn require = Clojure.var('clojure.core', 'require')
    require.invoke(Clojure.read('io.pact.provider.lein.verify-provider'))
    toProvider = Clojure.var('io.pact.provider.lein.verify-provider', 'to-provider')
    toConsumer = Clojure.var('io.pact.provider.lein.verify-provider', 'to-consumer')
  }

  def 'to-provider converts the lein project info into a provider'() {
    given:
    def providerInfo = Clojure.read('{ :provider1 { } }').entrySet().first()

    when:
    def provider = toProvider.invoke(verifier, providerInfo)

    then:
    provider == new ProviderInfo(':provider1')
  }

  def 'to-provider sets the appropriate values correctly'() {
    given:
    def providerInfo = Clojure.read('''{
       :provider1 {
          :protocol "HTTPS"
          :host "my-host"
          :port 1234
          :path "/path"
          :insecure true
          ;:trust-store (java.lang.File. "mytrust")
          :trust-store-password "notrust"
          :state-change-url "http://statechange:8080"
          :state-change-uses-body true
          ;:verification-type io.pact.provider.PactVerification/ANNOTATED_METHOD
          :packages-to-scan ["io.pact.provider.lein"]
       }
    }''').entrySet().first()
    def expected = new ProviderInfo(name: ':provider1', protocol: 'HTTPS', host: 'my-host', port: 1234, path: '/path',
      insecure: true, trustStore: null, trustStorePassword: 'notrust',
      stateChangeUrl: new URL('http://statechange:8080'), stateChangeUsesBody: true,
      packagesToScan: ['io.pact.provider.lein'])

    when:
    def provider = toProvider.invoke(verifier, providerInfo)

    then:
    provider.name == expected.name
    provider.protocol == expected.protocol
    provider.host == expected.host
    provider.port == expected.port
    provider.path == expected.path
    provider.insecure == expected.insecure
    provider.trustStore == expected.trustStore
    provider.trustStorePassword == expected.trustStorePassword
    provider.stateChangeUrl == expected.stateChangeUrl
    provider.stateChangeUsesBody == expected.stateChangeUsesBody
    provider.verificationType == expected.verificationType
    new ArrayList(provider.packagesToScan as List) == expected.packagesToScan
  }

  def 'to-consumer converts the lein project info into a consumer'() {
    given:
    def consumerInfo = Clojure.read('{ :consumer1 { } }').entrySet().first()

    when:
    def consumer = toConsumer.invoke(consumerInfo)

    then:
    consumer == new ConsumerInfo(':consumer1')
  }

  def 'to-consumer sets the consumer values correctly'() {
    given:
    def consumerInfo = Clojure.read('''{
      :consumer1 {
        :pact-source "file:///path/to/pact.json"
        :state-change-url "http://statechange:8080"
        :state-change-uses-body true
        ;:verification-type io.pact.provider.PactVerification/ANNOTATED_METHOD
        :packages-to-scan ["io.pact.provider.lein"]
      }
    }''').entrySet().first()
    def expected = new ConsumerInfo(':consumer1', 'http://statechange:8080', true,
      ['io.pact.provider.lein'], null, new UrlSource('file:///path/to/pact.json'))

    when:
    def consumer = toConsumer.invoke(consumerInfo)

    then:
    consumer.name == expected.name
    consumer.pactSource == expected.pactSource
    consumer.stateChange == expected.stateChange
    consumer.stateChangeUsesBody == expected.stateChangeUsesBody
    consumer.verificationType == expected.verificationType
    new ArrayList(consumer.packagesToScan as List) == expected.packagesToScan
    consumer.pactFileAuthentication == expected.pactFileAuthentication
  }

}
