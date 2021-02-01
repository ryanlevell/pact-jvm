package io.pact.consumer.junit

import io.pact.consumer.MessagePactBuilder
import io.pact.consumer.dsl.PactDslJsonBody
import io.pact.core.model.annotations.Pact
import io.pact.core.model.messaging.MessagePact
import org.junit.runner.Description
import org.junit.runners.model.Statement
import spock.lang.Specification

class MessagePactProviderRuleSpec extends Specification {

  private MessagePactProviderRule rule
  private Statement base
  private Description description

  static class TestClass {

    @Pact(provider = 'MessagePactProviderRuleSpec_provider', consumer = 'MessagePactProviderRuleSpec_consumer')
    MessagePact createPact(MessagePactBuilder builder) {
      PactDslJsonBody body = new PactDslJsonBody()
        .integerType('value', 100)
        .stringValue('type', 'COST')

      builder
        .expectsToReceive('a test message')
        .withContent(body)
        .toPact()
    }

    @SuppressWarnings('EmptyMethod')
    @PactVerification()
    def test() { }

  }

  def setup() {
    rule = new MessagePactProviderRule(new TestClass())
    base = Mock(Statement)
    description = Mock(Description)
  }

  def 'it handles tests with no provider states'() {
    given:
    description.getAnnotation(PactVerification) >> TestClass.getDeclaredMethod('test').getAnnotation(PactVerification)

    when:
    rule.apply(base, description).evaluate()

    then:
    noExceptionThrown()
  }

}
