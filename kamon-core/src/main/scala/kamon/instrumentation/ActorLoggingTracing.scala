/*
 * =========================================================================================
 * Copyright © 2013 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package kamon.instrumentation

import org.aspectj.lang.annotation._
import org.aspectj.lang.ProceedingJoinPoint
import kamon.trace.{ TraceContextAware, TraceRecorder }

@Aspect
class ActorLoggingTracing {

  @DeclareMixin("akka.event.Logging.LogEvent+")
  def mixinTraceContextAwareToLogEvent: TraceContextAware = TraceContextAware.default

  @Pointcut("execution(akka.event.Logging.LogEvent+.new(..)) && this(event)")
  def logEventCreation(event: TraceContextAware): Unit = {}

  @After("logEventCreation(event)")
  def captureTraceContext(event: TraceContextAware): Unit = {
    // Force initialization of TraceContextAware
    event.traceContext
  }

  @Pointcut("execution(* akka.event.slf4j.Slf4jLogger.withMdc(..)) && args(logSource, logEvent, logStatement)")
  def withMdcInvocation(logSource: String, logEvent: TraceContextAware, logStatement: () ⇒ _): Unit = {}

  @Around("withMdcInvocation(logSource, logEvent, logStatement)")
  def aroundWithMdcInvocation(pjp: ProceedingJoinPoint, logSource: String, logEvent: TraceContextAware, logStatement: () ⇒ _): Unit = {
    TraceRecorder.withTraceContext(logEvent.traceContext) {
      pjp.proceed()
    }
  }
}
