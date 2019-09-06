package io.pactflow.oasverifier

import java.lang.annotation.Inherited

/**
 * Marks a test as a OAS contract test
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Inherited
annotation class OASContract {
}
