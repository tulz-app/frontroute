package frontroute.site
package examples
package ex_auth

import frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object AuthExample
    extends CodeExample(
      id = "auth",
      title = "Auth",
      description = FileAsString("description.md"),
      links = Seq(
        "/",
        "/private/profile"
      )
    )(() => {
      import frontroute._
      import com.raquo.laminar.api.L._

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
        authenticationEvents.events.scanLeft(Option.empty[User]) {
          case (_, AuthenticationEvent.SignedOut)        => Option.empty
          case (_, AuthenticationEvent.SignedIn(userId)) => Some(User(userId))
        }

      val route = {
        div(
          child <-- authenticatedUser.signal.map { maybeUser =>
            div(
              firstMatch(
                pathEnd {
                  div(
                    div(cls := "text-2xl", "Index page."),
                    div(s"Maybe user: $maybeUser")
                  )
                },
                /* <focus> */
                provideOption(maybeUser) { user =>
                  /* </focus> */
                  pathPrefix("private") {
                    path("profile") {
                      div(
                        div(cls := "text-2xl", "Profile page."),
                        div(s"User: $user")
                      )
                    }
                  }
                  /* <focus> */
                },
                /* </focus> */
                extractUnmatchedPath { unmatched =>
                  div(
                    div(cls := "text-2xl", "Not Found"),
                    div(unmatched.mkString("/", "/", ""))
                  )
                }
              )
            )
          }
        )
      }

      routes(
        div(
          cls := "p-4 min-h-[300px]",
          route
        ),
        div(
          cls := "bg-blue-900 -mx-6 p-2 space-y-2",
          div(
            cls := "font-semibold text-xl text-blue-200",
            "Sign in a user (empty for log out):"
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
          )
        )
      )
    })
