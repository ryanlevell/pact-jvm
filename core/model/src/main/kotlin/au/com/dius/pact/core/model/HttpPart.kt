package au.com.dius.pact.core.model

import au.com.dius.pact.core.model.matchingrules.MatchingRules
import au.com.dius.pact.core.support.ContentTypeUtils
import mu.KLogging
import org.apache.http.entity.ContentType
import java.nio.charset.Charset

/**
 * Base trait for an object that represents part of an http message
 */
abstract class HttpPart {

  abstract var body: OptionalBody
  abstract var headers: MutableMap<String, List<String>>
  abstract var matchingRules: MatchingRules

  fun mimeType(): String = contentTypeHeader()?.split(Regex("\\s*;\\s*"))?.first().orEmpty()

  fun contentTypeHeader(): String? {
    val contentTypeKey = headers.keys.find { CONTENT_TYPE.equals(it, ignoreCase = true) }
    return if (contentTypeKey.isNullOrEmpty()) {
      detectContentType()
    } else {
      headers.get(contentTypeKey)?.first()
    }
  }

  fun jsonBody() = mimeType().matches(Regex("application\\/.*json"))

  fun xmlBody() = mimeType().matches(Regex("application\\/.*xml"))

  fun detectContentType(): String = when {
    body.isPresent() -> {
      val s = body.value!!.take(32).map {
        if (it == '\n'.toByte()) ' ' else it.toChar()
      }.joinToString("")
      ContentTypeUtils.detectContentType(s)
    }
    else -> "text/plain"
  }

  fun setDefaultMimeType(mimetype: String) {
    if (!headers.containsKey(CONTENT_TYPE)) {
      headers[CONTENT_TYPE] = listOf(mimetype)
    }
  }

  companion object : KLogging() {
    private const val CONTENT_TYPE = "Content-Type"
  }

  @Deprecated("use the method on OptionalBody")
  fun charset(): Charset? {
    return try {
      ContentType.parse(contentTypeHeader())?.charset
    } catch (e: Exception) {
      logger.debug { "Failed to parse content type '${contentTypeHeader()}'" }
      null
    }
  }
}
