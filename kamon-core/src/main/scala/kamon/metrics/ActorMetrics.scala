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

import org.HdrHistogram.{ AbstractHistogram, AtomicHistogram }
import kamon.util.GlobPathFilter
import scala.collection.concurrent.TrieMap
import scala.collection.JavaConversions.iterableAsScalaIterable
import akka.actor.{Actor, ActorContext, ActorRef}

trait ActorMetrics {
  self: MetricsExtension ⇒

  val config = system.settings.config.getConfig("kamon.metrics.actors")
  val actorMetrics = TrieMap[String, ActorMetricsRecorder]()

  val trackedActors: Vector[GlobPathFilter] = config.getStringList("tracked").map(glob ⇒ new GlobPathFilter(glob)).toVector
  val excludedActors: Vector[GlobPathFilter] = config.getStringList("excluded").map(glob ⇒ new GlobPathFilter(glob)).toVector

  val actorMetricsFactory: () ⇒ ActorMetricsRecorder = {
    val settings = config.getConfig("hdr-settings")
    val processingTimeHdrConfig = HdrConfiguration.fromConfig(settings.getConfig("processing-time"))
    val timeInMailboxHdrConfig = HdrConfiguration.fromConfig(settings.getConfig("time-in-mailbox"))
    val mailboxSizeHdrConfig = HdrConfiguration.fromConfig(settings.getConfig("mailbox-size"))

    () ⇒ new HdrActorMetricsRecorder(processingTimeHdrConfig, timeInMailboxHdrConfig, mailboxSizeHdrConfig)
  }

  import scala.concurrent.duration._
  system.scheduler.schedule(0.seconds, 10.seconds)(
    actorMetrics.collect {
      case (name, recorder: HdrActorMetricsRecorder) ⇒
        println(s"Actor: $name")
        recorder.processingTimeHistogram.copy.getHistogramData.outputPercentileDistribution(System.out, 1000000D)
    })(system.dispatcher)

  def shouldTrackActor(path: String): Boolean =
    trackedActors.exists(glob ⇒ glob.accept(path)) && !excludedActors.exists(glob ⇒ glob.accept(path))

  def registerActor(path: String): ActorMetricsRecorder = actorMetrics.getOrElseUpdate(path, actorMetricsFactory())

  def unregisterActor(path: String): Unit = actorMetrics.remove(path)
}

trait ActorMetricsRecorder {
  def recordTimeInMailbox(waitTime: Long): Unit
  def recordProcessingTime(processingTime: Long): Unit
}

class HdrActorMetricsRecorder(processingTimeHdrConfig: HdrConfiguration, timeInMailboxHdrConfig: HdrConfiguration,
                              mailboxSizeHdrConfig: HdrConfiguration) extends ActorMetricsRecorder {

  val processingTimeHistogram = new AtomicHistogram(processingTimeHdrConfig.highestTrackableValue, processingTimeHdrConfig.significantValueDigits)
  val timeInMailboxHistogram = new AtomicHistogram(timeInMailboxHdrConfig.highestTrackableValue, timeInMailboxHdrConfig.significantValueDigits)
  val mailboxSizeHistogram = new AtomicHistogram(mailboxSizeHdrConfig.highestTrackableValue, mailboxSizeHdrConfig.significantValueDigits)

  def recordTimeInMailbox(waitTime: Long): Unit = timeInMailboxHistogram.recordValue(waitTime)

  def recordProcessingTime(processingTime: Long): Unit = processingTimeHistogram.recordValue(processingTime)

  def snapshot(): HdrActorMetricsSnapshot = HdrActorMetricsSnapshot(processingTimeHistogram.copy(),
    timeInMailboxHistogram.copy(), mailboxSizeHistogram.copy())

  def reset(): Unit = {
    processingTimeHistogram.reset()
    timeInMailboxHistogram.reset()
    mailboxSizeHistogram.reset()
  }
}

case class HdrActorMetricsSnapshot(processingTimeHistogram: AbstractHistogram, timeInMailboxHistogram: AbstractHistogram,
                                   mailboxSizeHistogram: AbstractHistogram)


class MetricsActor extends Actor {
  var oneOffListeners: Map[String, List[ActorRef]]

  def receive = ???
}


object MetricsActor {
  case class Query(path: String, subscribe: Boolean = false)
}
