/*
 * =========================================================================================
 * Copyright © 2013 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package kamon.metrics

import org.scalatest.{WordSpecLike, Matchers, WordSpec}
import akka.testkit.TestKitBase
import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import kamon.Kamon
import kamon.metrics.ActorMetricsDealer.{ActorMetrics, Subscribe}
import scala.concurrent.duration._

class ActorMetricsSpec extends TestKitBase with WordSpecLike with Matchers {
  implicit lazy val system: ActorSystem = ActorSystem("actor-metrics-spec", ConfigFactory.parseString(
      """
        |kamon.metrics.actors.tracked = ["user/test*"]
      """.stripMargin))

  implicit def self = testActor

  //lazy val metricsExtension = Kamon(Metrics).dealer



    "the Kamon actor metrics" should {
      "track configured actors" in {
        /*system.actorOf(Props.empty, "test-tracked-actor") ! "nothing"
        metricsExtension ! Subscribe("user/test-tracked-actor")*/

        within(2 seconds) {
          expectMsgType[ActorMetricsDealer.ActorMetrics]
        }
      }
    }
}
