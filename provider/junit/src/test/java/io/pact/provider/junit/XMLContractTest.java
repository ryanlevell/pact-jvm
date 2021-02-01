package io.pact.provider.junit;

import io.pact.provider.junit.target.HttpTarget;
import io.pact.provider.junitsupport.Provider;
import io.pact.provider.junitsupport.VerificationReports;
import io.pact.provider.junitsupport.loader.PactFolder;
import io.pact.provider.junitsupport.target.Target;
import io.pact.provider.junitsupport.target.TestTarget;
import com.github.restdriver.clientdriver.ClientDriverRequest;
import com.github.restdriver.clientdriver.ClientDriverRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

import static com.github.restdriver.clientdriver.RestClientDriver.giveEmptyResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;

@RunWith(PactRunner.class)
@Provider("xml_provider")
@PactFolder("pacts")
@VerificationReports({"console", "json", "markdown"})
public class XMLContractTest {
  @ClassRule
  public static final ClientDriverRule embeddedService = new ClientDriverRule(8332);

  @TestTarget
  public final Target target = new HttpTarget(8332);

  @Before
  public void before() {
    embeddedService.addExpectation(
      onRequestTo("/attr").withMethod(ClientDriverRequest.Method.POST), giveEmptyResponse()
    );
  }
}
