package ru.auto.stress.gatling

import io.gatling.commons.stats.Status
import io.gatling.http.request.ExtraInfo

import scala.collection.mutable.ListBuffer

/**
  * Author: ndmelentev (ndmelentev@yandex-team.ru)
  * Created: 13.06.17
  */
trait GatlingUtils {

  var responseTimesOK: ListBuffer[Int] = ListBuffer[Int]()
  var responseTimesKO: ListBuffer[Int] = ListBuffer[Int]()

  def getExtraInfo(extraInfo: ExtraInfo): String = {
    if (extraInfo.status.eq(Status.apply("KO"))) {
      responseTimesKO += extraInfo.response.timings.responseTime
      ",URL:" + extraInfo.request.getUrl +
        " User id: " + extraInfo.request.getHeaders.get("x-uid") +
        " Request: " + extraInfo.request.getStringData +
        " Response: " + extraInfo.response.body.string
    } else {
      responseTimesOK += extraInfo.response.timings.responseTime
      ""
    }
  }

  def printToGraphit(graphiteWriter: GraphiteWriter, numberOfUsers: Int, rampSeconds: Int, scenarioName: String): Unit = {
    responseTimesOK = responseTimesOK.filter(_ != 0)
    responseTimesKO = responseTimesKO.filter(_ != 0)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_OK.p99", percentile(99, responseTimesOK).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_OK.p95", percentile(95, responseTimesOK).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_OK.p75", percentile(75, responseTimesOK).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_OK.p50", percentile(50, responseTimesOK).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_KO.p99", percentile(99, responseTimesKO).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_KO.p95", percentile(95, responseTimesKO).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_KO.p75", percentile(75, responseTimesKO).toString, null)
    graphiteWriter.submit(s".api_stress_test.${numberOfUsers}_users_${rampSeconds}_second.${scenarioName}_time.result_.KO.p50", percentile(50, responseTimesKO).toString, null)
    print(s"\nWaiting 10 sec to send data to Graphit...\n")
    Thread.sleep(10000)
    print(s"Waiting complete.\n")
  }

  def percentile(p: Int, seq: Seq[Int]): Int = {
    val sorted = seq.sorted
    val k = math.ceil((sorted.size - 1) * (p / 100.0)).toInt
    if (k <= sorted.size - 1) {
      sorted(k)
    } else {
      0
    }
  }
}