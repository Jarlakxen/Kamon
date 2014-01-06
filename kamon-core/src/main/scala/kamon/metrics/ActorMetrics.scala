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

import kamon.metrics.ActorMetrics.{ MailboxSize, TimeInMailbox, ProcessingTime }

class ActorMetrics extends MetricGroup {
  val processingTimeMetric = new DefaultHdrMetric
  val timeInMailboxMetric = new DefaultHdrMetric
  val mailboxSizeMetric = new DefaultHdrMetric

  def apply(metricName: String): Metric = metricName match {
    case "ProcessingTime" ⇒ processingTimeMetric
    case "TimeInMailbox"  ⇒ timeInMailboxMetric
    case "MailboxSize"    ⇒ mailboxSizeMetric
  }

}

object ActorMetrics {
  sealed trait ActorMetricsID
  case object ProcessingTime extends ActorMetricsID
  case object TimeInMailbox extends ActorMetricsID
  case object MailboxSize extends ActorMetricsID
}
