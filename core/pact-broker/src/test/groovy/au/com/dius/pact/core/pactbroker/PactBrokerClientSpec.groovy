package au.com.dius.pact.core.pactbroker

import au.com.dius.pact.com.github.michaelbull.result.Err
import au.com.dius.pact.com.github.michaelbull.result.Ok
import au.com.dius.pact.core.support.Json
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlin.Pair
import kotlin.collections.MapsKt
import org.apache.http.entity.ContentType
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

@SuppressWarnings('UnnecessaryGetter')
class PactBrokerClientSpec extends Specification {

  private PactBrokerClient pactBrokerClient
  private File pactFile
  private String pactContents

  def setup() {
    pactBrokerClient = new PactBrokerClient('http://localhost:8080')
    pactFile = File.createTempFile('pact', '.json')
    pactContents = '''
      {
          "provider" : {
              "name" : "Provider"
          },
          "consumer" : {
              "name" : "Foo Consumer"
          },
          "interactions" : []
      }
    '''
    pactFile.write pactContents
  }

  def 'when fetching consumers, sets the auth if there is any'() {
    given:
    def halClient = Mock(IHalClient)
    halClient.navigate(_, _) >> halClient
    halClient.forAll(_, _) >> { args -> args[1].accept([name: 'bob', href: 'http://bob.com/']) }

    def client = Spy(PactBrokerClient, constructorArgs: [
      'http://pactBrokerUrl', MapsKt.mapOf(new Pair('authentication', ['Basic', '1', '2']))]) {
      newHalClient() >> halClient
    }

    when:
    def consumers = client.fetchConsumers('provider')

    then:
    consumers != []
    consumers.first().name == 'bob'
    consumers.first().source == 'http://bob.com/'
    consumers.first().pactFileAuthentication == ['Basic', '1', '2']
  }

  def 'when fetching consumers for an unknown provider, returns an empty pacts list'() {
    given:
    def halClient = Mock(IHalClient)
    halClient.navigate(_, _) >> halClient
    halClient.forAll(_, _) >> { args -> throw new NotFoundHalResponse() }

    def client = Spy(PactBrokerClient, constructorArgs: ['baseUrl']) {
      newHalClient() >> halClient
    }

    when:
    def consumers = client.fetchConsumers('provider')

    then:
    consumers == []
  }

  def 'when fetching consumers, decodes the URLs to the pacts'() {
    given:
    def halClient = Mock(IHalClient)
    halClient.navigate(_, _) >> halClient
    halClient.forAll(_, _) >> { args -> args[1].accept([name: 'bob', href: 'http://bob.com/a%20b/100+ab']) }

    def client = Spy(PactBrokerClient, constructorArgs: ['http://pactBrokerUrl']) {
      newHalClient() >> halClient
    }

    when:
    def consumers = client.fetchConsumers('provider')

    then:
    consumers != []
    consumers.first().name == 'bob'
    consumers.first().source == 'http://bob.com/a b/100+ab'
  }

  def 'fetches consumers with specified tag successfully'() {
    given:
    def halClient = Mock(IHalClient)
    halClient.navigate(_, _) >> halClient
    halClient.forAll(_, _) >> { args -> args[1].accept([name: 'bob', href: 'http://bob.com/']) }

    def client = Spy(PactBrokerClient, constructorArgs: ['http://pactBrokerUrl']) {
      newHalClient() >> halClient
    }

    when:
    def consumers = client.fetchConsumersWithTag('provider', 'tag')

    then:
    consumers != []
    consumers.first().name == 'bob'
    consumers.first().source == 'http://bob.com/'
    consumers.first().tag == 'tag'
  }

  def 'when fetching consumers with specified tag, sets the auth if there is any'() {
    given:
    def halClient = Mock(IHalClient)
    halClient.navigate(_, _) >> halClient
    halClient.forAll(_, _) >> { args -> args[1].accept([name: 'bob', href: 'http://bob.com/']) }

    def client = Spy(PactBrokerClient, constructorArgs: [
      'http://pactBrokerUrl', MapsKt.mapOf(new Pair('authentication', ['Basic', '1', '2']))]) {
      newHalClient() >> halClient
    }

    when:
    def consumers = client.fetchConsumersWithTag('provider', 'tag')

    then:
    consumers.first().pactFileAuthentication == ['Basic', '1', '2']
  }

  def 'when fetching consumers with specified tag, decodes the URLs to the pacts'() {
    given:
    def halClient = Mock(IHalClient)
    halClient.navigate(_, _) >> halClient
    halClient.forAll(_, _) >> { args -> args[1].accept([name: 'bob', href: 'http://bob.com/a%20b/100+ab']) }

    def client = Spy(PactBrokerClient, constructorArgs: ['http://pactBrokerUrl']) {
      newHalClient() >> halClient
    }

    when:
    def consumers = client.fetchConsumersWithTag('provider', 'tag')

    then:
    consumers != []
    consumers.first().name == 'bob'
    consumers.first().source == 'http://bob.com/a b/100+ab'
  }

  def 'when fetching consumers with specified tag for an unknown provider, returns an empty pacts list'() {
    given:
    def halClient = Mock(IHalClient)
    halClient.navigate(_, _) >> halClient
    halClient.forAll(_, _) >> { args -> throw new NotFoundHalResponse() }

    def client = Spy(PactBrokerClient, constructorArgs: ['baseUrl']) {
      newHalClient() >> halClient
    }

    when:
    def consumers = client.fetchConsumersWithTag('provider', 'tag')

    then:
    consumers == []
  }

  def 'returns an error when uploading a pact fails'() {
    given:
    def halClient = Mock(IHalClient)
    def client = Spy(PactBrokerClient, constructorArgs: ['baseUrl']) {
      newHalClient() >> halClient
    }

    when:
    def result = client.uploadPactFile(pactFile, '10.0.0')

    then:
    1 * halClient.uploadDocument(
      '/pacts/provider/Provider/consumer/Foo%20Consumer/version/10.0.0',
      pactContents, _, false, _) >>
      { args -> args[2].apply('Failed', 'Error') }
    result == 'FAILED! Error'
  }

  def 'encode the provider name, consumer name, tags and version when uploading a pact'() {
    given:
    def halClient = Mock(IHalClient)
    def client = Spy(PactBrokerClient, constructorArgs: ['baseUrl']) {
      newHalClient() >> halClient
    }
    def tag = 'A/B'
    pactContents = '''
      {
          "provider" : {
              "name" : "Provider/A"
          },
          "consumer" : {
              "name" : "Foo Consumer/A"
          },
          "interactions" : []
      }
    '''
    pactFile.write pactContents

    when:
    client.uploadPactFile(pactFile, '10.0.0/B', [tag])

    then:
    1 * halClient.uploadDocument('/pacts/provider/Provider%2FA/consumer/Foo%20Consumer%2FA/version/10.0.0%2FB',
      pactContents, _, false, _) >> { args -> args[2].apply('OK', 'OK') }
    1 * halClient.uploadJson('/pacticipants/Foo%20Consumer%2FA/versions/10.0.0%2FB/tags/A%2FB', '', _, false)
  }

  @Issue('#892')
  def 'when uploading a pact a pact with tags, publish the tags first'() {
    given:
    def halClient = Mock(IHalClient)
    def client = Spy(PactBrokerClient, constructorArgs: ['baseUrl']) {
      newHalClient() >> halClient
    }
    def tag = 'A/B'
    pactContents = '''
      {
          "provider" : {
              "name" : "Provider/A"
          },
          "consumer" : {
              "name" : "Foo Consumer/A"
          },
          "interactions" : []
      }
    '''
    pactFile.write pactContents

    when:
    client.uploadPactFile(pactFile, '10.0.0/B', [tag])

    then:
    1 * halClient.uploadJson('/pacticipants/Foo%20Consumer%2FA/versions/10.0.0%2FB/tags/A%2FB', '', _, false)

    then:
    1 * halClient.uploadDocument('/pacts/provider/Provider%2FA/consumer/Foo%20Consumer%2FA/version/10.0.0%2FB',
      pactContents, _, false, _) >> { args -> args[2].apply('OK', 'OK') }
  }

  @Unroll
  def 'when publishing verification results, return a #result if #reason'() {
    given:
    def halClient = Mock(IHalClient)
    PactBrokerClient client = Spy(PactBrokerClient, constructorArgs: ['baseUrl']) {
      newHalClient() >> halClient
    }
    halClient.postJson('URL', _) >> new Ok(true)

    expect:
    client.publishVerificationResults(attributes, TestResult.Ok.INSTANCE, '0', null).class.simpleName == result

    where:

    reason                              | attributes                                         | result
    'there is no verification link'     | [:]                                                | Err.simpleName
    'the verification link has no href' | ['pb:publish-verification-results': [:]]           | Err.simpleName
    'the broker client returns success' | ['pb:publish-verification-results': [href: 'URL']] | Ok.simpleName
    'the links have different case'     | ['pb:Publish-Verification-Results': [HREF: 'URL']] | Ok.simpleName
  }

  def 'when fetching a pact, return the results as a Map'() {
    given:
    def halClient = Mock(IHalClient)
    PactBrokerClient client = Spy(PactBrokerClient, constructorArgs: ['baseUrl']) {
      newHalClient() >> halClient
    }
    def url = 'https://test.pact.dius.com.au' +
      '/pacts/provider/Activity%20Service/consumer/Foo%20Web%20Client%202/version/1.0.2'
    def json = new JsonObject()
    json.addProperty('a', 'a')
    json.addProperty('b', 100)
    json.add('_links', new JsonObject())
    def array = new JsonArray()
    array.with {
      it.add(true)
      it.add(10.2)
      it.add('test')
    }
    json.add('c', array)

    when:
    def result = client.fetchPact(url)

    then:
    1 * halClient.fetch(url) >> json
    result.pactFile == Json.INSTANCE.toJson([a: 'a', b: 100, _links: [:], c: [true, 10.2, 'test']])
  }

  def 'supports uploading swagger files'() {
    given:
    def halClient = Mock(IHalClient)
    def client = Spy(PactBrokerClient, constructorArgs: ['baseUrl']) {
      newHalClient() >> halClient
    }
    pactContents = '''
      {
        "openapi": "3.0.1",
        "info": {
          "title": "Swagger Petstore",
          "description": "",
          "version": "1.0.0"
        },
        "paths": {
          "/pet": {
            "put": {
              "summary": "Update an existing pet",
              "responses": {
                "400": {
                  "description": "Invalid ID supplied"
                }
              }
            }
          }
        }
      }
    '''
    pactFile.write pactContents

    when:
    client.uploadContract(pactFile, '0.0.0')

    then:
    1 * halClient.uploadDocument('/pacts/provider/Swagger%20Petstore/version/1.0.0',
      pactContents, _, false, 'application/json; charset=UTF-8') >> { args -> args[2].apply('OK', 'OK') }
  }

  def 'supports uploading swagger files in YAML'() {
    given:
    def halClient = Mock(IHalClient)
    def client = Spy(PactBrokerClient, constructorArgs: ['baseUrl']) {
      newHalClient() >> halClient
    }
    pactContents = '''
      openapi: 3.0.1
      info:
        title: Swagger Petstore
        description: 'This is a sample server Petstore server.  You can find out more about     Swagger
          at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).      For
          this sample, you can use the api key `special-key` to test the authorization     filters.\'
        termsOfService: http://swagger.io/terms/
        contact:
          email: apiteam@swagger.io
        license:
          name: Apache 2.0
          url: http://www.apache.org/licenses/LICENSE-2.0.html
        version: '1.0.0'
      paths:
        /pet:
          put:
            summary: Update an existing pet
            responses:
              400:
                description: Invalid ID supplied
    '''
    pactFile.write pactContents

    when:
    client.uploadContract(pactFile, '0.0.0')

    then:
    1 * halClient.uploadDocument('/pacts/provider/Swagger%20Petstore/version/1.0.0',
      pactContents, _, false, 'application/yaml') >> { args -> args[2].apply('OK', 'OK') }
  }
}
