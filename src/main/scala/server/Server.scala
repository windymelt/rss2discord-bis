package io.github.windymelt.rss2discordbis
package server

import cats.Id
import cats.effect.IO
import wvlet.airframe.http._
import wvlet.airframe.http.netty.Netty
import wvlet.airframe.http.netty.NettyServer

class Rss2DiscordBis(
    killswitch: () => IO[Unit],
    repo: Repository[Id]
) extends api.v1.Rss2DiscordBis {
  import cats.effect.unsafe.implicits._

  override def getAllFeeds(): Seq[api.v1.Feed] =
    IO {
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
    }.unsafeRunSync()

  override def reload(): Unit = (
    IO.println("Reloading") >> killswitch()
  ).unsafeRunSync()
}

class Server(
    killswitch: () => IO[Unit],
    repo: Repository[Id]
) {
  // Create a Router
  val router = RxRouter.of[Rss2DiscordBis]

  // Starting a new RPC server.
  def run(): NettyServer =
    Netty.server
      .withRouter(router)
      .withPort(8080)
      .design
      .bind[Rss2DiscordBis]
      .toInstance {
        Rss2DiscordBis(killswitch, repo)
      }
      .run[NettyServer, NettyServer] { server =>
        {
          server.awaitTermination()
          server
        }
      }
}
