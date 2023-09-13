package pl.piomin.services.vertx.account;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(VertxExtension.class)
public class AccountServerTests {

    final static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:5.0"));

    @BeforeAll
    static void init(Vertx vertx) {
        mongoDBContainer.start();

        vertx.deployVerticle(new MongoVerticle(mongoDBContainer.getReplicaSetUrl()));
        vertx.deployVerticle(new AccountServer());
    }

    @AfterAll
    static void destroy() {
        mongoDBContainer.stop();
    }

    @Test
    void shouldFindAll(Vertx vertx, VertxTestContext testContext) {
        HttpClient client = vertx.createHttpClient();
        client.request(HttpMethod.GET, 8080, "localhost", "/account")
                .compose(req -> req.send().compose(HttpClientResponse::body))
                .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
                    assertNotNull(buffer.toString());
                    testContext.completeNow();
                })));
    }
}
