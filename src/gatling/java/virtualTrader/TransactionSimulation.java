package virtualTrader;

import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.*;
import net.datafaker.Faker;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class TransactionSimulation extends Simulation {

    private static HttpProtocolBuilder setupProtocol() {
        return http.baseUrl("http://localhost:4131")
                .acceptHeader("application/json")
                .maxConnectionsPerHost(10)
                .userAgentHeader("Gatling/Performance Test");
    }

    private static Iterator<Map<String, Object>> feedData() {

        Faker faker = new Faker();

        Random rand = new Random();

        Iterator<Map<String, Object>> iterator;
        iterator = Stream.generate(() -> {
            Map<String, Object> stringObjectMap = new HashMap<>();
            stringObjectMap.put("playerId", "gatling");
//            stringObjectMap.put("playerId", "admin");
            stringObjectMap.put("ticker", faker.stock().nsdqSymbol());
            stringObjectMap.put("quantity", rand.nextLong(100));
            stringObjectMap.put("price", rand.nextDouble(1000) + 1000d);
            return stringObjectMap;
        })
                .iterator();
        return iterator;
    }

    private static ScenarioBuilder buildPostScenario() {
        return CoreDsl.scenario("Load Test Creating Transaction")
                .feed(feedData())
                .exec(http("transaction").post("/order/buy")
//                .exec(http("transaction").post("/order/sell")
                        .header("Content-Type", "application/json")
                        .body(StringBody("{\"playerId\":\"#{playerId}\",\"ticker\":\"#{ticker}\",\"quantity\":\"#{quantity}\",\"price\":\"#{price}\" }"))
                        .check(status().is(200)));
    }

    private OpenInjectionStep.RampRate.RampRateOpenInjectionStep postEndpointInjectionProfile() {
        int totalDesiredUserCount = 200;
        double userRampUpPerInterval = 50;
        double rampUpIntervalSeconds = 30;
        int totalRampUptimeSeconds = 120;
        int steadyStateDurationSeconds = 300;

        return rampUsersPerSec(userRampUpPerInterval / (rampUpIntervalSeconds / 60)).to(totalDesiredUserCount)
                .during(Duration.ofSeconds(totalRampUptimeSeconds + steadyStateDurationSeconds));
    }

    public TransactionSimulation() {
        setUp(buildPostScenario().injectOpen(postEndpointInjectionProfile())
                .protocols(setupProtocol()));
    }
}
