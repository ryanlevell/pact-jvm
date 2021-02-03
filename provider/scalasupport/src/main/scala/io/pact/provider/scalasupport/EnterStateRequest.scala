package io.pact.provider.scalasupport

import io.pact.core.model.{OptionalBody, Request}

object EnterStateRequest {
  def apply(url: String, state: String): Request = {
    new Request("POST", url, null, null, OptionalBody.body(("{\"state\": \"" + state + "\"}").getBytes), null)
  }
}
