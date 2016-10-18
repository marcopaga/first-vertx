package eu.marco.paga.first;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;

import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.vertx.core.http.HttpHeaders.*;

@RunWith(VertxUnitRunner.class)
public class FirstVerticleShould {

    private Vertx vertx;
    private int port;

    @Before
    public void setUp(TestContext testContext) throws Exception {
        vertx = Vertx.vertx();
        this.port = findOpenPort();
        DeploymentOptions options = new DeploymentOptions();
        options.setConfig(new JsonObject().put("http.port", port));
        vertx.deployVerticle(FirstVerticle.class.getName(), options, testContext.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext testContext) throws Exception {
        vertx.close(testContext.asyncAssertSuccess());
    }

    @Test
    public void return_hello_on_http_call(TestContext testContext) throws Exception {
        final Async async = testContext.async();

        vertx.createHttpClient().getNow(port, "localhost", "/",
                response -> {
                    response.handler(body -> {
                        testContext.assertTrue(body.toString().contains("first"), "Response body: " + body.toString());
                        async.complete();
                    });
                });
    }

    @Test
    public void create_a_new_entity_on_post(TestContext testContext) throws Exception {
        final Async async = testContext.async();

        final String jsonToPost = Json.encodePrettily(new Whisky("name", "origin"));

        vertx
                .createHttpClient()
                .post(port,"localhost","/api/whiskies")
                .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                .putHeader(CONTENT_LENGTH, String.valueOf(jsonToPost.length()))
                .handler(response -> {
                    testContext.assertEquals(response.statusCode(), HttpResponseStatus.CREATED.code());
                    testContext.assertTrue(response.headers().get(CONTENT_TYPE).contains("application/json"));
                    response.bodyHandler(body -> {
                        final Whisky decodeValue = Json.decodeValue(body.toString(), Whisky.class);
                        testContext.assertEquals("name", decodeValue.getName());
                        testContext.assertEquals("origin", decodeValue.getOrigin());
                        testContext.assertNotNull(decodeValue.getId());
                        async.complete();
                    });
                })
                .write(jsonToPost)
                .end();
    }

    private int findOpenPort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }
}