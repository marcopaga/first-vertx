package eu.marco.paga.first;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;

public class Whisky_Api_ShouldIT {

    @BeforeClass
    public static void configureRestAssured() {
        baseURI = "http://localhost";
        port = Integer.getInteger("http.port", 8080);
    }

    @AfterClass
    public static void unconfigureRestAssured() {
        reset();
    }

    @Test
    public void retrieve_all_whsikies() {
        get("/api/whiskies").then()
                .assertThat()
                .statusCode(200)
                .body("name",hasItems("Bowmore 15 Years Laimrig", "Talisker 57° North"));
    }

    @Test
    public void retrieve_an_individual_whsiky() {
        final int id = get("/api/whiskies").then()
                .assertThat()
                .statusCode(200)
                .extract()
                .jsonPath().getInt("find { it.name=='Bowmore 15 Years Laimrig' }.id");

        get("/api/whiskies/" + id).then()
                .assertThat()
                .statusCode(200)
                .body("name", equalTo("Bowmore 15 Years Laimrig"))
                .body("origin", equalTo("Scotland, Islay"))
                .body("id", equalTo(id));
    }

    @Test
    public void return_not_found_for_an_unkown_resource() throws Exception {
        get("/api/whiskies/9999").then()
                .assertThat()
                .statusCode(NOT_FOUND.code());
    }
}
