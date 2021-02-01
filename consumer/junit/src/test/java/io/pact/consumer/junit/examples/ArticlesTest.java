package io.pact.consumer.junit.examples;

import io.pact.core.model.annotations.Pact;
import io.pact.consumer.junit.PactProviderRule;
import io.pact.consumer.junit.PactVerification;
import io.pact.consumer.dsl.PactDslJsonBody;
import io.pact.consumer.dsl.PactDslWithProvider;
import io.pact.consumer.junit.exampleclients.ArticlesRestClient;
import io.pact.core.model.RequestResponsePact;
import org.apache.commons.collections4.MapUtils;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Example taken from https://groups.google.com/forum/#!topic/pact-support/-Kk_OxvcJQY
 */
public class ArticlesTest {
    Map<String, String> headers = MapUtils.putAll(new HashMap<String, String>(),
        new String[]{"Content-Type", "application/json"});

    @Rule
    public PactProviderRule provider = new PactProviderRule("ArticlesProvider", "localhost", 1234, this);

    @Pact(provider = "ArticlesProvider", consumer = "ArticlesConsumer")
    public RequestResponsePact articlesFragment(PactDslWithProvider builder) {
        return builder
            .given("Pact for Issue 313")
            .uponReceiving("retrieving article data")
            .path("/articles.json")
            .method("GET")
            .willRespondWith()
            .headers(headers)
            .status(200)
            .body(
                new PactDslJsonBody()
                    .minArrayLike("articles", 1)
                        .object("variants")
                            .eachKeyLike("0032")
                                .stringType("description", "sample description")
                            .closeObject()
                        .closeObject()
                    .closeObject()
                    .closeArray()
            )
            .toPact();
    }

    @PactVerification("ArticlesProvider")
    @Test
    public void testArticles() throws IOException {
        ArticlesRestClient providerRestClient = new ArticlesRestClient();
        providerRestClient.getArticles("http://localhost:1234");
    }
}
