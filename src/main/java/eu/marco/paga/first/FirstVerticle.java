package eu.marco.paga.first;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

public class FirstVerticle extends AbstractVerticle {

    private Map<Integer, Whisky> products = new LinkedHashMap<>();

    private void createSomeData() {
        Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
        products.put(bowmore.getId(), bowmore);
        Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
        products.put(talisker.getId(), talisker);
    }

    public void start(Future<Void> future){
        createSomeData();

        Router router = Router.router(vertx);

        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader("content-type", "text/html")
                    .end("<h1>Hello from my first Vert.x 3 application</h1>");
        });

        router.route("/assets/*").handler(StaticHandler.create("assets"));

        router.get("/api/whiskies").handler(this::getAll);
        router.route("/api/whiskies*").handler(BodyHandler.create());
        router.post("/api/whiskies").handler(this::addOne);
        router.get("/api/whiskies/:id").handler(this::getOne);
        router.delete("/api/whiskies/:id").handler(this::deleteOne);

        final Integer httpPort = config().getInteger("http.port", 8080);

        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(httpPort, event -> {
                    if (event.succeeded()){
                        future.complete();
                    } else {
                        future.fail(event.cause());
                    }
                });
    }

    private void getOne(RoutingContext routingContext) {
        final Integer id = Integer.valueOf(routingContext.request().getParam("id"));
        if(id == null){
            routingContext.response().setStatusCode(NOT_FOUND.code()).end();
        } else {
            if(products.containsKey(id)) {
                final Whisky whisky = products.get(id);
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(whisky));
            } else {
                routingContext.response().setStatusCode(NOT_FOUND.code()).end();
            }

        }
    }

    private void deleteOne(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            Integer idAsInteger = Integer.valueOf(id);
            products.remove(idAsInteger);
        }
        routingContext.response().setStatusCode(204).end();
    }

    private void addOne(RoutingContext routingContext) {
        final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(), Whisky.class);
        products.put(whisky.getId(), whisky);
        routingContext.response()
                .setStatusCode(HttpResponseStatus.CREATED.code())
                .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                .end(Json.encodePrettily(whisky));
    }

    private void getAll(RoutingContext routingContext) {
        routingContext.response()
                .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                .end(Json.encodePrettily(products.values()));
    }
}
