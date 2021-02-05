package io.pact.consumer.dsl

import io.pact.consumer.ConsumerPactBuilder
import io.pact.core.model.ProviderState
import io.pact.core.plugins.PactPlugin

class PactDslWithState @JvmOverloads constructor(
  private val consumerPactBuilder: ConsumerPactBuilder,
  var consumerName: String,
  var providerName: String,
  private val defaultRequestValues: PactDslRequestWithoutPath?,
  private val defaultResponseValues: PactDslResponse?,
  private val plugins: MutableList<PactPlugin> = mutableListOf()
  ) {
  @JvmField
  val state: MutableList<ProviderState> = mutableListOf()

  @JvmOverloads
  constructor(
    consumerPactBuilder: ConsumerPactBuilder,
    consumerName: String,
    providerName: String,
    state: ProviderState,
    defaultRequestValues: PactDslRequestWithoutPath?,
    defaultResponseValues: PactDslResponse?,
    plugins: MutableList<PactPlugin> = mutableListOf()
  ) : this(consumerPactBuilder, consumerName, providerName, defaultRequestValues, defaultResponseValues, plugins) {
    this.state.add(state)
  }

  /**
   * Description of the request that is expected to be received
   *
   * @param description request description
   */
  fun uponReceiving(description: String): PactDslRequestWithoutPath {
    return PactDslRequestWithoutPath(consumerPactBuilder, this, description, defaultRequestValues,
      defaultResponseValues)
  }

  /**
   * Adds another provider state to this interaction
   * @param stateDesc Description of the state
   */
  fun given(stateDesc: String): PactDslWithState {
    state.add(ProviderState(stateDesc))
    return this
  }

  /**
   * Adds another provider state to this interaction
   * @param stateDesc Description of the state
   * @param params State data parameters
   */
  fun given(stateDesc: String, params: Map<String, Any>): PactDslWithState {
    state.add(ProviderState(stateDesc, params))
    return this
  }
}
