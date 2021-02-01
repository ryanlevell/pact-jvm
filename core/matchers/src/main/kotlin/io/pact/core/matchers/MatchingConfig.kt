package io.pact.core.matchers

import kotlin.reflect.full.createInstance

object MatchingConfig {
  val bodyMatchers = mapOf(
    "application/.*xml" to "io.pact.core.matchers.XmlBodyMatcher",
    "text/xml" to "io.pact.core.matchers.XmlBodyMatcher",
    "application/.*json" to "io.pact.core.matchers.JsonBodyMatcher",
    "application/json-rpc" to "io.pact.core.matchers.JsonBodyMatcher",
    "application/jsonrequest" to "io.pact.core.matchers.JsonBodyMatcher",
    "text/plain" to "io.pact.core.matchers.PlainTextBodyMatcher",
    "multipart/form-data" to "io.pact.core.matchers.MultipartMessageBodyMatcher",
    "multipart/mixed" to "io.pact.core.matchers.MultipartMessageBodyMatcher",
    "application/x-www-form-urlencoded" to "io.pact.core.matchers.FormPostBodyMatcher"
  )

  @JvmStatic
  fun lookupBodyMatcher(contentType: String?): BodyMatcher? {
    return if (contentType != null) {
      val matcher = bodyMatchers.entries.find { contentType.matches(Regex(it.key)) }?.value
      if (matcher != null) {
        val clazz = Class.forName(matcher).kotlin
        (clazz.objectInstance ?: clazz.createInstance()) as BodyMatcher?
      } else {
        null
      }
    } else {
      null
    }
  }
}
