package io.pact.consumer.dsl

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.pact.consumer.ConsumerPactBuilder
import io.pact.core.model.ProviderState
import io.pact.core.plugins.DefaultPluginManager
import io.pact.core.plugins.PactPlugin
import java.lang.RuntimeException

open class PactDslWithProvider(
  val consumerPactBuilder: ConsumerPactBuilder,
  private val providerName: String
  ) {
  private var defaultRequestValues: PactDslRequestWithoutPath? = null
  private var defaultResponseValues: PactDslResponse? = null
  private val plugins: MutableList<PactPlugin> = mutableListOf()

  /**
   * Describe the state the provider needs to be in for the pact test to be verified.
   *
   * @param state Provider state
   */
  fun given(state: String): PactDslWithState {
    return PactDslWithState(consumerPactBuilder, consumerPactBuilder.consumerName, providerName,
      ProviderState(state), defaultRequestValues, defaultResponseValues, plugins)
  }

  /**
   * Describe the state the provider needs to be in for the pact test to be verified.
   *
   * @param state Provider state
   * @param params Data parameters for the state
   */
  fun given(state: String, params: Map<String, Any>): PactDslWithState {
    return PactDslWithState(consumerPactBuilder, consumerPactBuilder.consumerName, providerName,
      ProviderState(state, params), defaultRequestValues, defaultResponseValues, plugins)
  }

  /**
   * Describe the state the provider needs to be in for the pact test to be verified.
   *
   * @param firstKey Key of first parameter element
   * @param firstValue Value of first parameter element
   * @param paramsKeyValuePair Additional parameters in key-value pairs
   */
  fun given(state: String, firstKey: String, firstValue: Any, vararg paramsKeyValuePair: Any): PactDslWithState {
    require(paramsKeyValuePair.size % 2 == 0) {
      "Pair key value should be provided, but there is one key without value."
    }
    val params = mutableMapOf<String, Any>()
    params[firstKey] = firstValue
    var i = 0
    while (i < paramsKeyValuePair.size) {
      params[paramsKeyValuePair[i].toString()] = paramsKeyValuePair[i + 1]
      i += 2
    }
    return PactDslWithState(consumerPactBuilder, consumerPactBuilder.consumerName, providerName,
      ProviderState(state, params), defaultRequestValues, defaultResponseValues, plugins)
  }

  /**
   * Description of the request that is expected to be received
   *
   * @param description request description
   */
  fun uponReceiving(description: String): PactDslRequestWithoutPath {
    return PactDslWithState(consumerPactBuilder, consumerPactBuilder.consumerName, providerName,
      defaultRequestValues, defaultResponseValues, plugins)
      .uponReceiving(description)
  }

  /**
   * Adds a plugin to the Pact
   */
  fun usingPlugin(name: String): PactDslWithProvider {
    when (val result = DefaultPluginManager.loadPlugin(name)) {
      is Ok -> plugins.add(result.value)
      is Err -> throw RuntimeException("Could not load plugin $name - ${result.error}")
    }
    return this
  }

  fun setDefaultRequestValues(defaultRequestValues: PactDslRequestWithoutPath?) {
    this.defaultRequestValues = defaultRequestValues
  }

  fun setDefaultResponseValues(defaultResponseValues: PactDslResponse?) {
    this.defaultResponseValues = defaultResponseValues
  }
}
