package io.pact.provider.junitsupport.target

import java.lang.annotation.Inherited

/**
 * Mark [io.pact.provider.junit.target.Target] for contract tests
 *
 * @see io.pact.provider.junit.target.Target
 *
 * @see HttpTarget
 */
@Retention(AnnotationRetention.RUNTIME)
@kotlin.annotation.Target(AnnotationTarget.FIELD)
@Inherited
annotation class TestTarget
