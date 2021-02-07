package io.pact.core.plugins

import io.pact.plugin.Plugin
import mu.KLogging
import java.lang.IllegalArgumentException

object CatalogueManager : KLogging() {
  private val catalogue = mutableMapOf<String, CatalogueEntry>()

  fun registerPluginEntries(name: String, catalogueList: List<Plugin.CatalogueEntry>) {
    catalogueList.forEach {
      val type = CatalogueEntryType.fromString(it.type)
      val key = "plugin/$name/${type}/${it.key}"
      catalogue[key] = CatalogueEntry(type, CatalogueEntryProviderType.PLUGIN, it.key, it.valuesMap)
    }

    logger.debug { "Updated catalogue entries:\n${catalogue.keys.joinToString("\n")}" }
  }

  fun registerCoreEntries(entries: List<CatalogueEntry>) {
    entries.forEach {
      val key = "core/${it.type}/${it.key}"
      catalogue[key] = it
    }

    logger.debug { "Core catalogue entries:\n${catalogue.keys.joinToString("\n")}" }
  }

  fun entries() = catalogue.entries
}

enum class CatalogueEntryType {
  CONTENT_MATCHER, MOCK_SERVER, MATCHER;

  companion object {
    fun fromString(type: String): CatalogueEntryType {
      return when (type) {
        "content-matcher" -> CONTENT_MATCHER
        "matcher" -> MATCHER
        "mock-server" -> MOCK_SERVER
        else -> throw IllegalArgumentException("'$type' is not a valid CatalogueEntryType value")
      }
    }
  }
}

data class CatalogueEntry(
  val type: CatalogueEntryType,
  val providerType: CatalogueEntryProviderType,
  val key: String,
  val values: Map<String, String> = mapOf()
)

enum class CatalogueEntryProviderType {
  CORE, PLUGIN
}
