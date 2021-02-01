package io.pact.consumer.groovy

import io.pact.core.model.matchingrules.MatchingRule

/**
 * Matcher for validating the values in a map
 */
class ValuesMatcher extends Matcher {

  MatchingRule getMatcher() {
    io.pact.core.model.matchingrules.ValuesMatcher.INSTANCE
  }

}
