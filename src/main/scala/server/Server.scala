package io.github.windymelt.rss2discordbis
package server

import wvlet.airframe.http.netty.Netty
import wvlet.airframe.http._

class Rss2DiscordBis extends api.v1.Rss2DiscordBis:
  override def getAllFeeds(): Seq[api.v1.Feed] =
    Seq(
      api.v1.Feed("1", "https://example.com/rss", api.v1.FeedType.RSS, true),
      api.v1.Feed("2", "https://example.com/atom", api.v1.FeedType.Atom, true)
    )

class Server(killswitch: () => ?) {
  // Create a Router
  val router = RxRouter.of[Rss2DiscordBis]

// Starting a new RPC server.
  def run() = Netty.server
    .withRouter(router)
    .withPort(8080)
    .start { server =>
      server.awaitTermination()
    }
}
