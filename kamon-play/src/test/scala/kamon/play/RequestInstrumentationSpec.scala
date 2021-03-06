/* ===================================================
 * Copyright © 2013-2014 the kamon project <http://kamon.io/>
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

package kamon.play

import play.api.test._
import play.api.mvc.{ Results, Action }
import play.api.mvc.Results.Ok
import scala.Some
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.test.FakeApplication
import kamon.play.action.TraceName

@RunWith(classOf[JUnitRunner])
class RequestInstrumentationSpec extends PlaySpecification {

  System.setProperty("config.file", "./kamon-play/src/test/resources/conf/application.conf")

  def appWithRoutes = FakeApplication(withRoutes = {
    case ("GET", "/async") ⇒
      Action.async {
        Future {
          Ok("Async.async")
        }
      }
    case ("GET", "/notFound") ⇒
      Action {
        Results.NotFound
      }
    case ("GET", "/redirect") ⇒
      Action {
        Results.Redirect("/redirected", MOVED_PERMANENTLY)
      }
    case ("GET", "/default") ⇒
      Action {
        Ok("default")
      }
    case ("GET", "/async-renamed") ⇒
      TraceName("renamed-trace") {
        Action.async {
          Future {
            Ok("Async.async")
          }
        }
      }
  })

  private val traceTokenValue = "kamon-trace-token-test"
  private val traceTokenHeaderName = "X-Trace-Token"
  private val expectedToken = Some(traceTokenValue)
  private val traceTokenHeader = traceTokenHeaderName -> traceTokenValue

  "the Request instrumentation" should {
    "respond to the Async Action with X-Trace-Token" in new WithServer(appWithRoutes) {
      val Some(result) = route(FakeRequest(GET, "/async").withHeaders(traceTokenHeader))
      header(traceTokenHeaderName, result) must equalTo(expectedToken)
    }

    "respond to the NotFound Action with X-Trace-Token" in new WithServer(appWithRoutes) {
      val Some(result) = route(FakeRequest(GET, "/notFound").withHeaders(traceTokenHeader))
      header(traceTokenHeaderName, result) must equalTo(expectedToken)
    }

    "respond to the Default Action with X-Trace-Token" in new WithServer(appWithRoutes) {
      val Some(result) = route(FakeRequest(GET, "/default").withHeaders(traceTokenHeader))
      header(traceTokenHeaderName, result) must equalTo(expectedToken)
    }

    "respond to the Redirect Action with X-Trace-Token" in new WithServer(appWithRoutes) {
      val Some(result) = route(FakeRequest(GET, "/redirect").withHeaders(traceTokenHeader))
      header("Location", result) must equalTo(Some("/redirected"))
      header(traceTokenHeaderName, result) must equalTo(expectedToken)
    }

    "respond to the Async Action with X-Trace-Token and the renamed trace" in new WithServer(appWithRoutes) {
      val Some(result) = route(FakeRequest(GET, "/async-renamed").withHeaders(traceTokenHeader))
      header(traceTokenHeaderName, result) must equalTo(expectedToken)
    }
  }
}