package io.pact.consumer

import io.pact.core.support.BuiltToolConfig

data class PactTestExecutionContext(var pactFolder: String = BuiltToolConfig.pactDirectory)
