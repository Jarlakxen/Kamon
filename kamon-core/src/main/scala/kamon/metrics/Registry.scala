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

import scala.collection.concurrent.TrieMap
import org.HdrHistogram.AtomicHistogram

trait MetricGroup

trait MetricKey {
  type M <: MetricGroup
}


case class ActorMetricLookup(path: String) extends MetricKey {
  type M = ActorMetric
}

class ActorMetric extends MetricGroup {
  val processingTime = new AtomicHistogram(3600 * 1000 * 1000 * 1000, 2)
  val timeInMailbox = new AtomicHistogram(3600 * 1000 * 1000 * 1000, 2)
}

class Registry {
  val metrics = TrieMap[MetricKey, MetricGroup]()


  def create(key: MetricKey): key.M = metrics(key)
}
