package io.pact.provider.junit;

import io.pact.provider.PactVerifyProvider;
import io.pact.provider.junit.target.MessageTarget;
import io.pact.provider.junitsupport.Provider;
import io.pact.provider.junitsupport.State;
import io.pact.provider.junitsupport.loader.PactFolder;
import io.pact.provider.junitsupport.target.Target;
import io.pact.provider.junitsupport.target.TestTarget;
import org.junit.runner.RunWith;

@RunWith(PactRunner.class)
@Provider("AmqpProvider")
@PactFolder("src/test/resources/amqp_pacts")
public class MessageTest {
  @TestTarget
  public final Target target = new MessageTarget();

  @State("SomeProviderState")
  public void someProviderState() {}

  @PactVerifyProvider("a test message")
  public String verifyMessageForOrder() {
    return "{\"testParam1\": \"value1\",\"testParam2\": \"value2\"}";
  }
}
