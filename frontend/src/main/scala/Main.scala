package io.github.windymelt.rss2discordbis

import scalajs.js.annotation.*
import api.v1._
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import org.w3c.dom.html.HTMLHtmlElement
import wvlet.airframe.http.Http

object FrontendMain {
  val client =
    ServiceRPC.newRPCAsyncClient(
      Http.client.withMsgPackEncoding.newAsyncClient("localhost:5173")
    ) // Vite proxy
  @main def main(): Unit = {
    println("Hello world!")
    val app = div(
      h1("Hello world!"),
      button(
        cls := "btn btn-primary",
        "Get all feeds",
        onClick --> { _ =>
          client.Rss2DiscordBis.getAllFeeds().run { feeds =>
            println(feeds)
          }
        }
      )
    )

    val container = dom.document.getElementById("app")
    renderOnDomContentLoaded(container, app)
  }
}
