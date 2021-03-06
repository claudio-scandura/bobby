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
package uk.gov.hmrc.bobby

import org.joda.time.LocalDate
import org.scalatest.{FlatSpec, Matchers}
import uk.gov.hmrc.bobby.domain._

class DependencyCheckerSpec extends FlatSpec with Matchers {

  "The mandatory dependency checker" should "return success result if the version is not in a restricted range" in {
    val d = Dependency("uk.gov.hmrc", "some-service")
    val dc = DependencyChecker(List(DeprecatedDependency(d, VersionRange("(,1.0.0]"), "testing", new LocalDate().minusDays(1))))
    dc.isDependencyValid(d, Version("2.0.0")) shouldBe OK
  }

  it should "return failed result if the version is in a restricted range" in {
    val d = Dependency("uk.gov.hmrc", "some-service")
    val dc = DependencyChecker(List(DeprecatedDependency(d, VersionRange("(,1.0.0]"), "testing", new LocalDate().minusDays(1))))
    dc.isDependencyValid(d, Version("0.1.0")) shouldBe MandatoryFail(DeprecatedDependency(d, VersionRange("(,1.0.0]"), "testing", new LocalDate().minusDays(1)))
  }

  it should "return failed result if the version is in a restricted range of multiple exclude" in {

    val d = Dependency("uk.gov.hmrc", "some-service")
    val dc = DependencyChecker(List(
      DeprecatedDependency(d, VersionRange("(,1.0.0]"), "testing", new LocalDate().minusDays(1)),
      DeprecatedDependency(d, VersionRange("[1.0.0,1.2.0]"), "testing", new LocalDate().minusDays(1)),
      DeprecatedDependency(d, VersionRange("[2.0.0,2.2.0]"), "testing", new LocalDate().minusDays(1))
    ))

    dc.isDependencyValid(d, Version("1.1.0")) shouldBe MandatoryFail(DeprecatedDependency(d, VersionRange("[1.0.0,1.2.0]"), "testing", new LocalDate().minusDays(1)))
  }

  it should "return warning if excludes are not applicable yet" in {
    val d = Dependency("uk.gov.hmrc", "some-service")
    val tomorrow: LocalDate = new LocalDate().plusDays(1)
    val dc = DependencyChecker(List(DeprecatedDependency(d, VersionRange("(,1.0.0]"), "testing", tomorrow)))
    dc.isDependencyValid(d, Version("0.1.0")) shouldBe MandatoryWarn(DeprecatedDependency(d, VersionRange("(,1.0.0]"), "testing", tomorrow))

  }

  it should "return fail if exclude is applicable from today" in {
    val d = Dependency("uk.gov.hmrc", "some-service")
    val today: LocalDate = new LocalDate()
    val dc = DependencyChecker(List(DeprecatedDependency(d, VersionRange("(,1.0.0]"), "testing", today)))
    dc.isDependencyValid(d, Version("0.1.0")) shouldBe MandatoryFail(DeprecatedDependency(d, VersionRange("(,1.0.0]"), "testing", today))

  }


  it should "return failed result if the version is in both restricted warn and fail ranges" in {

    val d = Dependency("uk.gov.hmrc", "some-service")
    val validTomorrow: LocalDate = new LocalDate().plusDays(1)
    val validToday: LocalDate = new LocalDate().minusDays(1)
    val dc = DependencyChecker(List(
      DeprecatedDependency(d, VersionRange("[1.0.0,1.2.0]"), "testing1", validTomorrow),
      DeprecatedDependency(d, VersionRange("[1.0.0,2.2.0]"), "testing2", validToday)
    ))

    dc.isDependencyValid(d, Version("1.1.0")) shouldBe MandatoryFail(DeprecatedDependency(d, VersionRange("[1.0.0,2.2.0]"), "testing2", validToday))
  }

  it should "filter non-relevant deprecated dependencies and return success" in {

    val d = Dependency("uk.gov.hmrc", "some-service")
    val other = Dependency("uk.gov.hmrc", "some-other-service")
    val dc = DependencyChecker(List(
      DeprecatedDependency(other, VersionRange("(,1.0.0]"), "testing", new LocalDate().minusDays(1)),
      DeprecatedDependency(other, VersionRange("[1.0.0,1.2.0]"), "testing", new LocalDate().minusDays(1)),
      DeprecatedDependency(other, VersionRange("[2.0.0,2.2.0]"), "testing", new LocalDate().minusDays(1))
    ))

    dc.isDependencyValid(d, Version("1.1.0")) shouldBe OK

  }
  it should "filter non-relevant deprecated dependencies and return fail when deprecated" in {

    val d = Dependency("uk.gov.hmrc", "some-service")
    val other = Dependency("uk.gov.hmrc", "some-other-service")
    val dc = DependencyChecker(List(
      DeprecatedDependency(other, VersionRange("(,3.0.0]"), "testing", new LocalDate().minusDays(1)),
      DeprecatedDependency(d, VersionRange("[2.0.0,2.2.0]"), "testing2", new LocalDate().minusDays(1))
    ))

    dc.isDependencyValid(d, Version("2.1.0")) shouldBe MandatoryFail(DeprecatedDependency(d, VersionRange("[2.0.0,2.2.0]"), "testing2", new LocalDate().minusDays(1)))
  }

  it should "work when there is no deprecated dependencies" in {
    DependencyChecker(Seq.empty).isDependencyValid(Dependency("org", "me"), Version("1.2.3")) shouldBe OK
  }

}
