package io.pact.core.matchers

import io.pact.core.model.OptionalBody
import io.pact.core.plugins.ContentMatcher

interface BodyMatcher : ContentMatcher {
  fun matchBody(
    expected: OptionalBody,
    actual: OptionalBody,
    context: Any
  ): BodyMatchResult
}
