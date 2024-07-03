package pokemonapi;

import io.gatling.core.controller.inject.closed.ConstantConcurrentUsersInjection;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class PokemonApiTest extends Simulation {
    // Case 1: Add enviroment variables
    String baseUrl = System.getProperty("baseUrl", "https://pokeapi.co/api/v2/pokemon");
    // Define the data
    FeederBuilder.FileBased<Object> feeder = jsonFile("data/pokemon.json").circular();

    // Define de Protocol > base URL and headers
    private HttpProtocolBuilder httpProtocol = http
            .baseUrl(baseUrl);

    // We want to test the Pokemon API

    // Define scenario
    ScenarioBuilder scn = scenario("Prokemon API Test")
            .feed(feeder)
            .exec(http("Get Pikachu")
                    .get("/#{name}")
                    .check(jmesPath("base_experience").isEL("#{baseExperience}"))
                    .check(jmesPath("abilities[0].ability.name").find().saveAs("ability"))
                    .check(bodyString().saveAs("BODY"))
                    .check(status().is(200)))
            .exec(session -> {
                //System.out.println("Body: " + session.getString("BODY"));
                System.out.println("Ability: " + session.getString("ability"));
                return session;
            });

    {
        setUp(
                /*scn.injectOpen(
                        rampUsers(10).during(10)
                )*/
                /*scn.injectClosed(
                        constantConcurrentUsers(10).during(20)
                )*/
                scn.injectOpen(
                        atOnceUsers(10),
                        nothingFor(Duration.ofSeconds(5)),
                        rampUsers(5).during(Duration.ofSeconds(10)),
                        constantUsersPerSec(10).during(Duration.ofSeconds(10))
                )
        ).protocols(httpProtocol);
    }
}
