package io.github.windymelt.rss2discordbis
package server

import wvlet.airframe.http.netty.Netty
import wvlet.airframe.http._
import cats.Id
import wvlet.airframe.http.netty.NettyServer

class Rss2DiscordBis(killswitch: () => ?, repo: Repository[Id])
    extends api.v1.Rss2DiscordBis:
  override def getAllFeeds(): Seq[api.v1.Feed] =
    repo
      .getAllFeeds()
      .map(d =>
        api.v1.Feed(
          d.id,
          d.url,
          d.feedType.toString().asInstanceOf[api.v1.FeedType],
          d.enabled
        )
      )
  override def reload(): Unit = killswitch()

class Server(killswitch: () => ?, repo: Repository[Id]) {
  // Create a Router
  val router = RxRouter.of[Rss2DiscordBis]

  // Starting a new RPC server.
  def run(): Unit = Netty.server
    .withRouter(router)
    .withPort(8080)
    .design
    .bind[Rss2DiscordBis]
    .toInstance {
      Rss2DiscordBis(killswitch, repo)
    }
    .build[NettyServer] { server =>
      server.awaitTermination()
    }
}
