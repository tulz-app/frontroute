package io.frontroute
package site
package examples
package ex_auth

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString
import io.laminext.AmAny
import io.laminext.AmendedHtmlTag
import org.scalajs.dom

object AuthExample
    extends CodeExample(
      id = "auth",
      title = "Auth",
      description = FileAsString("description.md")
    )((locationProvider: LocationProvider) =>
      (a: AmendedHtmlTag[dom.html.Anchor, AmAny]) =>
        useLocationProvider(locationProvider) { implicit locationProvider =>
          import io.frontroute.renderDSL._
          import com.raquo.laminar.api.L.{a => _, _}

          case class User(id: String)

          sealed trait AuthenticationEvent extends Product with Serializable
          object AuthenticationEvent {
            case object SignedOut               extends AuthenticationEvent
            case class SignedIn(userId: String) extends AuthenticationEvent
          }

          val authenticationEvents = new EventBus[AuthenticationEvent]
          /* <focus> */
          val authenticatedUser: Signal[Option[User]] =
            /* </focus> */
            authenticationEvents.events.foldLeft(Option.empty[User]) {
              case (_, AuthenticationEvent.SignedOut)        => Option.empty
              case (_, AuthenticationEvent.SignedIn(userId)) => Some(User(userId))
            }

          /* <focus> */
          val requireAuthentication: Directive[User] =
            signal(authenticatedUser.signal).collect { case Some(user) => user }
          /* </focus> */

          val route =
            /* <focus> */
            signal(authenticatedUser.signal) { implicit maybeUser =>
              /* </focus> */
              concat(
                pathEnd {
                  complete {
                    div(
                      div(cls := "text-2xl", "Index page."),
                      div(s"Maybe user: $maybeUser")
                    )
                  }
                },
                pathPrefix("private") {
                  /* <focus> */
                  requireAuthentication { user =>
                    /* </focus> */
                    path("profile") {
                      complete {
                        div(
                          div(cls := "text-2xl", "Profile page."),
                          div(s"User: $user")
                        )
                      }
                    }
                  }
                },
                extractUnmatchedPath { unmatched =>
                  complete {
                    div(
                      div(cls := "text-2xl", "Not Found"),
                      div(unmatched.mkString("/", "/", ""))
                    )
                  }
                }
              )
            }

          div(
            div(
              cls := "p-4 min-h-[300px]",
              route
            ),
            div(
              cls := "bg-blue-900 -mx-4 -mb-4 p-2 space-y-2",
              div(
                cls := "font-semibold text-2xl text-blue-200",
                "Sign in a user (empty for log out) ..."
              ),
              div(
                input(
                  tpe         := "text",
                  placeholder := "Input a user ID and hit enter...",
                  onKeyDown.filter(_.key == "Enter").stopPropagation.mapToValue.map { userId =>
                    if (userId.isEmpty) {
                      AuthenticationEvent.SignedOut
                    } else {
                      AuthenticationEvent.SignedIn(userId)
                    }
                  } --> authenticationEvents
                )
              ),
              div(
                cls := "font-semibold text-2xl text-blue-200",
                "Navigation"
              ),
              div(
                cls := "flex flex-col",
                a(
                  cls  := "text-blue-300 hover:text-blue-100",
                  href := "/",
                  "➜ /"
                ),
                a(
                  cls  := "text-blue-300 hover:text-blue-100",
                  href := "/private/profile",
                  "➜ /private/profile"
                )
              )
            )
          )
        }
    )
