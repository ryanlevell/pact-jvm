package io.pact.core.pactbroker

import io.pact.core.support.Json
import io.pact.core.support.json.JsonValue

data class PactBrokerResult(
  val name: String,
  val source: String,
  val pactBrokerUrl: String,
  val pactFileAuthentication: List<String> = listOf(),
  val notices: List<VerificationNotice> = listOf(),
  val pending: Boolean = false,
  val tag: String? = null,
  val wip: Boolean = false,
  val usedNewEndpoint: Boolean = false
)

data class VerificationNotice(
  val `when`: String,
  val text: String
) {
  companion object {
    fun fromJson(json: JsonValue): VerificationNotice? {
      return if (json is JsonValue.Object) {
        VerificationNotice(Json.toString(json["when"]), Json.toString(json["text"]))
      } else {
        null
      }
    }
  }
}
