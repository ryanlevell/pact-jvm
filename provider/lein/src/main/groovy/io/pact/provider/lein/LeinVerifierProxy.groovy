package io.pact.provider.lein

import clojure.java.api.Clojure
import clojure.lang.IFn
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import io.pact.provider.ConsumerInfo
import io.pact.provider.ProviderInfo
import io.pact.provider.ProviderVerifier
import io.pact.provider.VerificationResult

/**
 * Proxy to pass lein project information to the pact verifier
 */
@Canonical
@CompileStatic
class LeinVerifierProxy {

  private static final String LEIN_PACT_VERIFY_NAMESPACE = 'au.com.dius.pact.provider.lein.verify-provider'

  def project
  def args

  @Delegate ProviderVerifier verifier = new ProviderVerifier()

  private final IFn hasProperty = Clojure.var(LEIN_PACT_VERIFY_NAMESPACE, 'has-property?')
  private final IFn getProperty = Clojure.var(LEIN_PACT_VERIFY_NAMESPACE, 'get-property')

  List<VerificationResult.Failed> verifyProvider(ProviderInfo provider) {
    verifier.projectHasProperty = { property ->
      this.hasProperty.invoke(Clojure.read(":$property"), args)
    }
    verifier.projectGetProperty =  { property ->
      this.getProperty.invoke(Clojure.read(":$property"), args)
    }
    verifier.pactLoadFailureMessage = { ConsumerInfo consumer ->
      "You must specify the pactfile to execute for consumer '${consumer.name}' (use :pact-file)"
    }
    verifier.checkBuildSpecificTask = { false }

    verifier.verifyProvider(provider)
      .findAll { it instanceof VerificationResult.Failed } as List<VerificationResult.Failed>
  }

  Closure wrap(IFn fn) {
    return { args -> fn.invoke(args) }
  }
}
