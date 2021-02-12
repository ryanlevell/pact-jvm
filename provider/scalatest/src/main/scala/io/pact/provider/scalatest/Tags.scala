package io.pact.provider.scalatest

import org.scalatest.Tag

object Tags {

  /**
    * Provider pact tests are annotated with this tag by default. Can be excluded or included in the build process.
    */
  object ProviderTest extends Tag("io.pact.provider.scalatest.Tags.ProviderTest")

}
