package io.github.windymelt.rss2discordbis

import scalajs.js.annotation.*
import api.v1._
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import org.w3c.dom.html.HTMLHtmlElement
import wvlet.airframe.http.Http
import com.raquo.airstream.eventbus.EventBus
import io.github.windymelt.rss2discordbis.components.ToastMessage

object FrontendMain {
  @main def main(): Unit = {
    val app = div(
      h1("Hello world!"),
      button(
        cls := "btn btn-primary",
        "Get all feeds",
        onClick --> { _ =>
          onClickGetFeed()
        }
      ),
      button(
        cls := "btn btn-danger",
        "Reload",
        onClick --> { _ =>
          onClickReload()
        }
      ),
      components.Toasts(messageQueue)
    )

    val container = dom.document.getElementById("app")
    renderOnDomContentLoaded(container, app)
  }

  val client =
    ServiceRPC.newRPCAsyncClient(
      Http.client.withMsgPackEncoding.newAsyncClient("localhost:5173")
    ) // Vite proxy

  val messageQueue: EventBus[ToastMessage] = EventBus[ToastMessage]()

  def tprint[A](message: A): Unit =
    messageQueue.emit(ToastMessage(message.toString()))

  val onClickGetFeed = () => {
    client.Rss2DiscordBis
      .getAllFeeds()
      .tapOnFailure { _ =>
        tprint("Failed to get feeds")
      }
      .run { feeds =>
        println(feeds)
        tprint(feeds)
      }
  }

  val onClickReload = () => {
    client.Rss2DiscordBis
      .reload()
      .tapOnFailure { _ =>
        tprint("Failed to reload")
      }
      .run { _ =>
        tprint("Reloaded")
      }
  }
}
