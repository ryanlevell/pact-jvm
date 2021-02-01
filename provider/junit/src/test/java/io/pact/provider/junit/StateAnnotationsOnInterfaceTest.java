package io.pact.provider.junit;

import io.pact.provider.junit.target.HttpTarget;
import io.pact.provider.junitsupport.Provider;
import io.pact.provider.junitsupport.loader.PactFolder;
import io.pact.provider.junitsupport.target.Target;
import io.pact.provider.junitsupport.target.TestTarget;
import com.github.restdriver.clientdriver.ClientDriverRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(PactRunner.class)
@Provider("providerWithMultipleInteractions")
@PactFolder("pacts")
public class StateAnnotationsOnInterfaceTest implements StateInterface1, StateInterface2 {

  @ClassRule
  public static final ClientDriverRule embeddedProvider = new ClientDriverRule(8333);

  public ClientDriverRule embeddedProvider() {
    return embeddedProvider;
  }

  @TestTarget
  public final Target target = new HttpTarget(8333);

}
