package io.pact.consumer.junit.events;

import io.pact.consumer.junit.MatcherTestUtils;
import io.pact.core.model.annotations.Pact;
import io.pact.consumer.junit.PactProviderRule;
import io.pact.consumer.junit.PactVerification;
import io.pact.consumer.dsl.DslPart;
import io.pact.consumer.dsl.PactDslJsonBody;
import io.pact.consumer.dsl.PactDslWithProvider;
import io.pact.core.model.FeatureToggles;
import io.pact.core.model.RequestResponsePact;
import io.pact.core.model.matchingrules.MatchingRuleGroup;
import io.pact.core.model.matchingrules.TypeMatcher;
import io.pact.core.model.matchingrules.ValuesMatcher;
import org.apache.http.entity.ContentType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * THIS IS A TEST FOR https://github.com/DiUS/pact-jvm/issues/401
 */
public class EventsRepositoryDictionaryNestedArrayConsumerTest {

  private static final Integer PORT = 8092;

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("EventsProvider", "localhost", PORT, this);

  @BeforeClass
  public static void setup() {
    FeatureToggles.toggleFeature("pact.feature.matchers.useMatchValuesMatcher", true);
  }

  @AfterClass
  public static void cleanup() {
    FeatureToggles.reset();
  }

  @Pact(provider = "EventsProvider", consumer = "EventsConsumerDictionaryNestedArray")
  public RequestResponsePact createPact(PactDslWithProvider builder) {

    DslPart body = new PactDslJsonBody()
      .object("events")
      //key is dynamic (i.e. think dictionary or java map
      //see https://github.com/DiUS/pact-jvm/issues/313
      //see https://github.com/DiUS/pact-jvm/tree/master/pact-jvm-consumer-junit
      .eachKeyMappedToAnArrayLike("ant") //broken, see pact-jvm issue 401
        .stringType("title", "ant")
      //we dont care about other attributes here. neither does pact :-);
      ;

    RequestResponsePact pact = builder
      .given("initialStateForEventsTest")
      .uponReceiving("a request to get events keyed by title")
      .path("/dictionaryNestedArray")
      .headers("Accept", ContentType.APPLICATION_JSON.toString())
      .method("GET")
      .willRespondWith()
      .status(200)
      .body(body)
      .toPact();

    MatcherTestUtils.assertResponseMatcherKeysEqualTo(pact, "body",
      "$.events",
      "$.events.*[*].title"
    );

    HashMap<String, MatchingRuleGroup> matchingRules = new HashMap<>();
    matchingRules.put("$.events", new MatchingRuleGroup(Collections.singletonList(ValuesMatcher.INSTANCE)));
    matchingRules.put("$.events.*[*].title", new MatchingRuleGroup(Collections.singletonList(TypeMatcher.INSTANCE)));
    assertThat(pact.getInteractions().get(0).getResponse().getMatchingRules().rulesForCategory("body").getMatchingRules(),
      is(equalTo(matchingRules)));

    return pact;
  }

  @Test
  @PactVerification(value = "EventsProvider")
  public void runTest() {
    Map<String, Map<String, List<Event>>> events = new EventsRepository("http://localhost:" + PORT).getEventsMapNestedArray();
    assertThat(events.entrySet(), hasSize(1));
    assertThat(events.get("events").get("ant").get(0).getTitle(), is("ant"));
  }

}
