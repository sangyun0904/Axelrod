package com.sykim.axelrod.virtualTrader;

import com.sykim.axelrod.model.Player;
import com.sykim.axelrod.model.Stock;
import com.sykim.axelrod.model.TransactionOrder;
import com.sykim.axelrod.repository.PlayerRepository;
import com.sykim.axelrod.repository.StockRepository;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static org.antlr.v4.runtime.tree.xpath.XPath.findAll;

public class TransactionSimulation extends Simulation {

//    @Autowired
//    private static StockRepository stockRepository;
//    @Autowired
//    private static PlayerRepository playerRepository;
//
//    private static HttpProtocolBuilder setupProtocol() {
//        return http.baseUrl("http://localhost:4131")
//                .acceptHeader("application/json")
//                .maxConnectionsPerHost(10)
//                .userAgentHeader("Gatling/Performance Test");
//    }
//
//    private static Iterator<Map<String, Object>> feedDate() {
//        List<Stock> stockList = stockRepository.findAll();
//        List<Player> playerList = playerRepository.findAll();
//
//        Random rand = new Random();
//
//        int playerIdx = rand.nextInt(playerList.size());
//        int stockIdx = rand.nextInt(stockList.size());
//
//        Iterator<Map<String, Object>> iterator;
//        iterator = Stream.generate(() -> {
//            Map<String, Object> stringObjectMap = new HashMap<>();
//            stringObjectMap.put("userId", playerList.get(playerIdx).getUsername());
//            stringObjectMap.put("ticker", stockList.get(stockIdx).getTicker());
//            stringObjectMap.put("quantity", rand.nextLong(1000));
//            stringObjectMap.put("price", stockList.get(stockIdx).getPrice() + rand.nextDouble(-100, 100));
//            return stringObjectMap;
//        })
//                .iterator();
//        return iterator;
//    }
//
//    private static ScenarioBuilder buildPostScenario() {
//        return CoreDsl.scenario("Load Test Creating Transaction")
//                .feed(feedDate())
//                .exec(http("transaction").post("/order/buy")
//                        .header("content-Type", "application/json")
//                        .body(StringBody("{\"userId\":\"${userId}\",\"ticker\":\"${ticker}\",\"quantity\":\"${quantity}\",\"price\":\"${price}\" }"))
//                        .check(status().is(200)));
//    }
//
//    private OpenInjectionStep.RampRate.RampRateOpenInjectionStep postEndpointInjectionProfile() {
//        int totalDesiredUserCount = 200;
//        double userRampUpPerInterval = 50;
//        double rampUpIntervalSeconds = 30;
//        int totalRampUptimeSeconds = 120;
//        int steadyStateDurationSeconds = 300;
//
//        return rampUsersPerSec(userRampUpPerInterval / (rampUpIntervalSeconds / 60)).to(totalDesiredUserCount)
//                .during(Duration.ofSeconds(totalRampUptimeSeconds + steadyStateDurationSeconds));
//    }
//
//    public TransactionSimulation() {
//        setUp(buildPostScenario().injectOpen(postEndpointInjectionProfile())
//                .protocols(setupProtocol()));
//    }
}
