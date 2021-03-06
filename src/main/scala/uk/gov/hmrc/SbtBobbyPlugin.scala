/*
 * Copyright 2015 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.hmrc

import sbt.Keys._
import sbt._
import uk.gov.hmrc.bobby.Bobby

object SbtBobbyPlugin extends AutoPlugin {

  override def trigger = allRequirements

  override lazy val projectSettings = Seq(
    parallelExecution in GlobalScope := true,

    onLoad in Global := {
      Bobby.findDependenciesWithNewerVersions(libraryDependencies.value, scalaVersion.value) _ compose (onLoad in Global).value
    },
    onLoad in Global := {
      Bobby.findDeprecatedDependencies(libraryDependencies.value) _ compose (onLoad in Global).value
    }
  )
}


