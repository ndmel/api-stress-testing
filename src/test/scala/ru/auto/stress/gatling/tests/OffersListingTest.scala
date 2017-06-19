package ru.auto.stress.gatling.tests

import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import ru.auto.stress.gatling.GatlingSettings

import scala.concurrent.duration._

object OffersListingSearch {

  val feeder: RecordSeqFeederBuilder[String] = csv("offersListingSearch.csv").random

  val search: ChainBuilder = feed(feeder)
    .exec(
      http("Get offers")
        .get("/offers/${category}")
        .header("x-uid", "${uid}")
        .header("x-authorization", "Vertis swagger-025b6a073d84564e709033f07438aa62")
        .check(status.is(200))
        .notSilent
        .asJSON
    )
}

class OffersListingTest extends Simulation with GatlingSettings {

  override val scn: ScenarioBuilder = scenario("Listing").exec(OffersListingSearch.search)
  override val scenarioName: String = "listing"

  setUp(
    scn.inject(rampUsers(numberOfUsers).over(FiniteDuration.apply(rampSeconds, "seconds")))
  ).protocols(httpConf)

  after(
    printToGraphit(graphiteWriter, numberOfUsers, rampSeconds, scenarioName)
  )
}