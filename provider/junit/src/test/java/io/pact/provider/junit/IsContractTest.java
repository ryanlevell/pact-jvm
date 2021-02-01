package io.pact.provider.junit;

import io.pact.provider.junitsupport.Provider;
import io.pact.provider.junitsupport.loader.PactFolder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Provider("myAwesomeService")
@PactFolder("pacts")
public @interface IsContractTest {
}
