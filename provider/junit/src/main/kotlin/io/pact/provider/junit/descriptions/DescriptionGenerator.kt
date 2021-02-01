package io.pact.provider.junit.descriptions

import io.pact.core.model.Interaction
import io.pact.core.model.Pact
import io.pact.core.model.PactSource
import io.pact.provider.junitsupport.TestDescription
import org.junit.runner.Description
import org.junit.runners.model.TestClass

/**
 * Class responsible for building junit tests Description.
 */
class DescriptionGenerator(
  private val testClass: TestClass,
  @Deprecated("Pass the pact source and consumer name in")
  private val pact: Pact?,
  private val pactSource: PactSource? = null,
  private val consumerName: String? = null
) {

  /**
   * Builds an instance of junit Description adhering with this logic for building the name:
   * If the PactSource is of type <code>BrokerUrlSource</code> and its tag is not empty then
   * the test name will be "#consumername [tag:#tagname] - Upon #interaction".
   * For all the other cases "#consumername - Upon #interaction"
   * @param interaction the Interaction under test
   */
  fun generate(interaction: Interaction): Description {
    val generator = TestDescription(interaction, pactSource ?: pact?.source, consumerName, pact?.consumer)
    return Description.createTestDescription(testClass.javaClass, generator.generateDescription())
  }
}
