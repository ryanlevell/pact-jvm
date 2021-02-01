package io.pact.consumer.dsl

import io.pact.core.model.matchingrules.EqualsIgnoreOrderMatcher
import io.pact.core.model.matchingrules.MatchingRuleGroup
import io.pact.core.model.matchingrules.MaxEqualsIgnoreOrderMatcher
import io.pact.core.model.matchingrules.MinEqualsIgnoreOrderMatcher
import io.pact.core.model.matchingrules.MinMaxEqualsIgnoreOrderMatcher
import io.pact.core.model.matchingrules.TypeMatcher
import spock.lang.Specification
import spock.lang.Unroll

class LambdaDslJsonArraySpec extends Specification {

  @Unroll
  def 'generates an array with ignore-order #expectedMatcher.class.simpleName matching'() {
    given:
    def root = LambdaDsl.newJsonArray { }
    root."$method"(*params) {
      it.stringValue('a')
          .stringType('b')
    }

    when:
    def result = root.build().close()

    then:
    result.body.toString() == '[["a","b"]]'
    result.matchers.matchingRules == [
        '$[0]': new MatchingRuleGroup([expectedMatcher]),
        '$[0][1]': new MatchingRuleGroup([TypeMatcher.INSTANCE])
    ]

    where:

    method                 | params | expectedMatcher
    'unorderedArray'       | []     | EqualsIgnoreOrderMatcher.INSTANCE
    'unorderedMinArray'    | [2]    | new MinEqualsIgnoreOrderMatcher(2)
    'unorderedMaxArray'    | [4]    | new MaxEqualsIgnoreOrderMatcher(4)
    'unorderedMinMaxArray' | [2, 4] | new MinMaxEqualsIgnoreOrderMatcher(2, 4)
  }
}
