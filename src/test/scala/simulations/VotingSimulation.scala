package simulations

import scala.util.Random


class VotingSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // Feeder for generating vote requests
  val voteFeeder = Iterator.continually(Map(
    "userId" -> s"user_${Random.nextInt(1000) + 1}",
    "candidateId" -> s"c${Random.nextInt(5) + 1}",
    "electionId" -> "2025-national"
  ))

  // Scenario 1: Cast Vote
  val castVoteScenario = scenario("Cast Vote")
    .feed(voteFeeder)
    .exec(
      http("Cast Vote")
        .post("/api/v1/votes")
        .body(StringBody("""{"userId":"${userId}","candidateId":"${candidateId}","electionId":"${electionId}"}""")).asJson
        .check(status.in(201, 409))
    )

  // Scenario 2: Get Results
  val getResultsScenario = scenario("Get Results")
    .exec(
      http("Get Results")
        .get("/api/v1/elections/2025-national/results")
        .check(status.is(200))
        .check(jsonPath("$.totalVotes").exists)
    )

  // Scenario 3: Check User Vote
  val checkUserVoteScenario = scenario("Check User Vote")
    .feed(voteFeeder)
    .exec(
      http("Check User Vote")
        .get("/api/v1/votes/me?userId=${userId}&electionId=${electionId}")
        .check(status.in(200, 404))
    )

  // Load profile
  setUp(
    castVoteScenario.inject(
      rampUsersPerSec(10).to(1000).during(2.minutes),
      constantUsersPerSec(1000).during(5.minutes)
    ),
    getResultsScenario.inject(
      constantUsersPerSec(100).during(7.minutes)
    ),
    checkUserVoteScenario.inject(
      constantUsersPerSec(200).during(7.minutes)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.percentile(95).lt(500),
      global.successfulRequests.percent.gt(95)
    )
}