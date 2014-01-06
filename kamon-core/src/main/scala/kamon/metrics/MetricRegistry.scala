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

trait Metric {
  def record(value: Long): Unit
}

class DefaultHdrMetric extends Metric {
  val hdr = new AtomicHistogram(3600000000000L, 2)

  def record(value: Long): Unit = hdr.recordValue(value)
}

trait MetricGroup {

  // This should always getOrCreate the metric.
  def apply(metricName: String): Metric
}

class MetricRegistry {
  private val registry = TrieMap[String, MetricGroup]()

  def register[T <: MetricGroup](name: String, metricGroup: T): T = {
    registry.update(name, metricGroup)
    metricGroup
  }

  def find(name: String): MetricGroup = registry(name)
}
