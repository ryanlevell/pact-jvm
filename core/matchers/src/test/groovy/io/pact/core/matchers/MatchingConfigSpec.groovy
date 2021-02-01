package io.pact.core.matchers

import spock.lang.Specification
import spock.lang.Unroll

class MatchingConfigSpec extends Specification {

  @Unroll
  def 'maps JSON content types to JSON body matcher'() {
    expect:
    MatchingConfig.lookupBodyMatcher(contentType).class.name == matcherClass

    where:
    contentType               | matcherClass
    'application/json'        | 'io.pact.core.matchers.JsonBodyMatcher'
    'application/xml'         | 'io.pact.core.matchers.XmlBodyMatcher'
    'application/hal+json'    | 'io.pact.core.matchers.JsonBodyMatcher'
    'application/thrift+json' | 'io.pact.core.matchers.JsonBodyMatcher'
    'application/stuff+xml'   | 'io.pact.core.matchers.XmlBodyMatcher'
    'application/json-rpc'    | 'io.pact.core.matchers.JsonBodyMatcher'
    'application/jsonrequest' | 'io.pact.core.matchers.JsonBodyMatcher'
  }

}
