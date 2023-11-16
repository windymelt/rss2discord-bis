package io.github.windymelt.rss2discordbis
package server

import wvlet.airframe.http.netty.Netty
import wvlet.airframe.http._
import cats.Id
import wvlet.airframe.http.netty.NettyServer
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.effect.IO
import cats.effect.kernel.Fiber

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

class Server(
    killswitch: () => IO[Unit],
    repo: Repository[Id],
    dispatcher: Dispatcher[IO]
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
        Rss2DiscordBis(() => dispatcher.unsafeRunSync(killswitch()), repo)
      }
      .run[NettyServer, NettyServer] { server =>
        {
          server.awaitTermination()
          server
        }
      }
}
