package io.pact.core.matchers

import io.pact.core.model.ContentType
import io.pact.core.plugins.CatalogueEntry
import io.pact.core.plugins.CatalogueEntryProviderType
import io.pact.core.plugins.CatalogueEntryType
import io.pact.core.plugins.CatalogueManager
import io.pact.core.plugins.ContentMatcher
import io.pact.core.plugins.PluginManager
import kotlin.reflect.full.createInstance

object MatchingConfig {
  private val coreBodyMatchers = mapOf(
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
  fun lookupContentMatcher(contentType: String?): ContentMatcher? {
    return if (contentType != null) {
      val contentMatcher = CatalogueManager.findContentMatcher(ContentType(contentType))
      if (contentMatcher != null) {
        contentMatcher
      } else {
        val matcher = coreBodyMatchers.entries.find { contentType.matches(Regex(it.key)) }?.value
        if (matcher != null) {
          val clazz = Class.forName(matcher).kotlin
          (clazz.objectInstance ?: clazz.createInstance()) as ContentMatcher?
        } else {
          null
        }
      }
    } else {
      null
    }
  }

  fun contentMatcherCatalogueEntries(): List<CatalogueEntry> {
    return listOf(
      CatalogueEntry(CatalogueEntryType.CONTENT_MATCHER, CatalogueEntryProviderType.CORE, "xml",
        mapOf(
          "content-types" to "application/.*xml,text/xml",
          "implementation" to "io.pact.core.matchers.XmlBodyMatcher"
        )),
      CatalogueEntry(CatalogueEntryType.CONTENT_MATCHER, CatalogueEntryProviderType.CORE, "json",
        mapOf(
          "content-types" to "application/.*json,application/json-rpc,application/jsonrequest",
          "implementation" to "io.pact.core.matchers.JsonBodyMatcher"
        )),
      CatalogueEntry(CatalogueEntryType.CONTENT_MATCHER, CatalogueEntryProviderType.CORE, "text",
        mapOf(
          "content-types" to "text/plain",
          "implementation" to "io.pact.core.matchers.PlainTextBodyMatcher"
        )),
      CatalogueEntry(CatalogueEntryType.CONTENT_MATCHER, CatalogueEntryProviderType.CORE, "multipart-form-data",
        mapOf(
          "content-types" to "multipart/form-data,multipart/mixed",
          "implementation" to "io.pact.core.matchers.MultipartMessageBodyMatcher"
        )),
      CatalogueEntry(CatalogueEntryType.CONTENT_MATCHER, CatalogueEntryProviderType.CORE, "form-urlencoded",
        mapOf(
          "content-types" to "application/x-www-form-urlencoded",
          "implementation" to "io.pact.core.matchers.FormPostBodyMatcher"
        ))
    )
  }
}
