/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.helpfrontend.controllers

import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.helpfrontend.config.AppConfig
import uk.gov.hmrc.helpfrontend.views.html._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

@Singleton
class HelpController @Inject() (
  appConfig: AppConfig,
  mcc: MessagesControllerComponents,
  termsAndConditionsPage: TermsAndConditionsPage,
  onlineServicesTermsPage: OnlineServicesTermsPage,
  privacyPage: PrivacyPage,
  cookiesPage: CookiesPage
) extends FrontendController(mcc)
    with I18nSupport {

  implicit val config: AppConfig = appConfig

  val termsAndConditions: Action[AnyContent] = Action { implicit request =>
    Ok(termsAndConditionsPage())
  }

  val privacy: Action[AnyContent] = Action { implicit request =>
    Ok(privacyPage())
  }

  val cookies: Action[AnyContent] = Action { implicit request =>
    Redirect(
      appConfig.cookieSettingsUrl,
      MOVED_PERMANENTLY
    )
  }

  val cookieDetails: Action[AnyContent] = Action { implicit request =>
    Ok(cookiesPage())
  }

  private def maybeOverrideLang[A](newLang: Option[String])(action: Action[A]): Action[A] =
    Action.async(action.parser) { initialRequest =>
      Try(initialRequest.withTransientLang(newLang.get)) match {
        case Success(changedRequest) => action(changedRequest).map(_.withLang(changedRequest.lang))
        case Failure(_)              => action(initialRequest)
      }
    }

  def onlineServicesTerms(lang: Option[String] = None): Action[AnyContent] = maybeOverrideLang(lang.map(_.take(2))) {
    Action { implicit request =>
      Ok(onlineServicesTermsPage())
    }
  }
}
