/* ===================================================
 * Copyright © 2013 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================== */

package kamon.metrics

import akka.actor.{ ExtendedActorSystem, ExtensionIdProvider, ExtensionId }
import akka.actor
import kamon.Kamon
import scala.concurrent.duration._
import scala.collection.concurrent.TrieMap

object Metrics extends ExtensionId[MetricsExtension] with ExtensionIdProvider {
  def lookup(): ExtensionId[_ <: actor.Extension] = Metrics
  def createExtension(system: ExtendedActorSystem): MetricsExtension = new MetricsExtension(system)

}

class MetricsExtension(system: ExtendedActorSystem) extends Kamon.Extension {
  val actorMetrics = TrieMap[String, ActorMetrics]()


  implicit val ec = system.dispatcher
  system.scheduler.schedule(15 seconds, 15 seconds) {
    println("===============================================================")
    actorMetrics.foreach { t =>
      println(s"Actor[${t._1}] - Processed: [${t._2.processingTimeMetric.getHistogramData.getTotalCount}]")
    }
  }

  def registerActor(path: String): ActorMetrics = actorMetrics.getOrElseUpdate(path, ActorMetrics())
  def unregisterActor(path: String): Unit = actorMetrics.remove(path)
}
