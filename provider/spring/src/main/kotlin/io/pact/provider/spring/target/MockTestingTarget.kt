package io.pact.provider.spring.target

import io.pact.core.model.Interaction
import io.pact.core.model.PactSource
import io.pact.provider.IConsumerInfo
import io.pact.provider.IProviderInfo
import io.pact.provider.IProviderVerifier
import io.pact.provider.PactVerification
import io.pact.provider.ProviderInfo
import io.pact.provider.ProviderUtils
import io.pact.provider.ProviderVerifier
import io.pact.provider.VerificationResult
import io.pact.provider.junit.target.BaseTarget
import io.pact.provider.junitsupport.Provider
import io.pact.provider.junitsupport.TargetRequestFilter
import java.net.URLClassLoader
import java.util.function.Consumer
import java.util.function.Supplier

abstract class MockTestingTarget(
  var runTimes: Int
) : BaseTarget() {

  override fun getProviderInfo(source: PactSource): ProviderInfo {
    val provider = ProviderUtils.findAnnotation(testClass.javaClass, Provider::class.java)!!
    val providerInfo = ProviderInfo(provider.value)

    val methods = testClass.getAnnotatedMethods(TargetRequestFilter::class.java)
    if (methods.isNotEmpty()) {
      validateTargetRequestFilters(methods)
      providerInfo.requestFilter = Consumer<Any> { httpRequest ->
        methods.forEach { method ->
          try {
            method.invokeExplosively(testTarget, httpRequest)
          } catch (t: Throwable) {
            throw AssertionError("Request filter method ${method.name} failed with an exception", t)
          }
        }
      }
    }

    return providerInfo
  }

  override fun setupVerifier(
    interaction: Interaction,
    provider: IProviderInfo,
    consumer: IConsumerInfo,
    pactSource: PactSource?
  ): IProviderVerifier {
    var verifier = createProviderVerifier()

    setupReporters(verifier)

    verifier.projectClasspath = Supplier { (ClassLoader.getSystemClassLoader() as URLClassLoader).urLs.toList() }

    verifier.initialiseReporters(provider)
    verifier.reportVerificationForConsumer(consumer, provider, pactSource)

    if (interaction.providerStates.isNotEmpty()) {
      for ((name) in interaction.providerStates) {
        verifier.reportStateForInteraction(name.toString(), provider, consumer, true)
      }
    }

    verifier.reportInteractionDescription(interaction)

    return verifier
  }

  protected fun doTestInteraction(
    consumerName: String,
    interaction: Interaction,
    source: PactSource,
    callVerifierFn: (
      provider: ProviderInfo,
      consumer: IConsumerInfo,
      verifier: IProviderVerifier,
      failures: HashMap<String, Any>
    ) -> VerificationResult
  ) {
    val provider = getProviderInfo(source)
    val consumer = consumerInfo(consumerName, source)
    provider.verificationType = PactVerification.ANNOTATED_METHOD

    val verifier = setupVerifier(interaction, provider, consumer, source)

    val failures = HashMap<String, Any>()

    val results = 1.rangeTo(runTimes).map {
      callVerifierFn(provider, consumer, verifier, failures)
    }

    val initial = VerificationResult.Ok(interaction.interactionId)
    val result = results.fold(initial) { acc: VerificationResult, r -> acc.merge(r) }

    reportTestResult(result, verifier)

    try {
      if (result is VerificationResult.Failed) {
        val errors = results.filterIsInstance<VerificationResult.Failed>()
        verifier.displayFailures(errors)
        throw AssertionError(verifier.generateErrorStringFromVerificationResult(errors))
      }
    } finally {
      verifier.finaliseReports()
    }
  }

  abstract fun createProviderVerifier(): ProviderVerifier
}
