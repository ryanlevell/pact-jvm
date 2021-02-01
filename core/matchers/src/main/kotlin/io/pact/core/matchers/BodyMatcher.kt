package io.pact.core.matchers

import io.pact.core.model.OptionalBody

interface BodyMatcher {
  fun matchBody(
    expected: OptionalBody,
    actual: OptionalBody,
    context: MatchingContext
  ): BodyMatchResult
}
