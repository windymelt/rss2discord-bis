package io.github.windymelt.rss2discordbis

import cats._
import cats.effect.Deferred
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.effect.kernel.Fiber
import cats.effect.kernel.Ref
import cats.implicits.{*, given}
import fs2.Stream
import wvlet.airframe.http.netty.NettyServer

import scala.concurrent.duration._

object Main extends IOApp {
  type DeliveryTarget = DBDispatch

  val repo: Repository[Id] = db.InMemoryDB() // TOOD: parametarize
  repo.createFeed("1", "https://blog.3qe.us/feed", FeedType.Atom)
  repo.createFeed("2", "https://www.scala-js.org/rss", FeedType.RSS)
  repo.createDispatch(
    "1",
    "discord",
    sys.env("WEBHOOK_URL_1")
  )
  repo.createFeedDispatch("1", "1", "1")
  repo.createFeedDispatch("2", "2", "1")

  def run(args: List[String]): IO[ExitCode] = {
    loop.as(ExitCode.Success)
  }
  val killswitchRef =
    Ref[IO].of(() => IO.unit) // initnal value is a stub

  val killswitchDeferred = Deferred[IO, Unit]

  val loop: IO[Unit] = {
    val delivery = for {
      _ <- IO.println("Loading configuration from DB ...")
      _ <- IO.println("Preparing killswitch ...")
      killswitchRef <- killswitchRef
      killswitchDeferred <- killswitchDeferred
      perFeedDelivery <- deliverFeeds(repo.getAllFeeds().flatMap { feed =>
        val dispatches = repo
          .getAllFeedsDispatches()
          .filter(_.feedId == feed.id)
          .map(_.dispatchToId)
          .flatMap(repo.getDispatchById)
        dispatches.map(dispatchTo =>
          (Feed(feed.url, feed.feedType), dispatchTo)
        )
      })
      killswitch = () =>
        IO.println("killswitch is called") >>
          perFeedDelivery
            .traverse(
              _.cancel
            ) >> killswitchDeferred
            .complete(
              ()
            )
            .void // by calling this, we can restart/reload delivery process
      _ <- killswitchRef.set(killswitch)
      _ <- apiServer(killswitch).use(_ =>
        IO.println(
          "Waiting for reload command."
        ) >> killswitchDeferred.get >> IO
          .sleep(1.second) /* Give time to respond */ >> IO
          .println("Reloading...")
      )
    } yield ()

    delivery.handleErrorWith { e =>
      IO.println(s"Error: ${e.getMessage()}") >> IO.sleep(1.minute)
    }
  }.foreverM

  // TODO: move to another file or module
  // apiServer can restart/reload delivery process
  def apiServer(
      killswitch: () => IO[Unit]
  ): Resource[IO, Any] =
    Resource.make[IO, Fiber[IO, Throwable, NettyServer]](
      IO.blocking(server.Server(killswitch, repo).run()).start
    )(server =>
      IO.println("closing server ...") >> IO(server.cancel) >> IO.println(
        "server stopped."
      )
    )

  def deliverFeeds(
      feeds: Seq[(Feed, DeliveryTarget)]
  ): IO[Seq[Fiber[IO, Throwable, Unit]]] = for {
    cancelTokens <- feeds
      .traverse(feed =>
        periodicFetchDeliverProcess(feed._1, feed._2, 5.minutes)
      )
  } yield cancelTokens

  def periodicFetchDeliverProcess(
      feed: Feed,
      deliveryTarget: DeliveryTarget,
      interval: FiniteDuration
  ): IO[Fiber[IO, Throwable, Unit]] = {
    Stream
      .repeatEval(fetchFeed(feed))
      .metered(interval)
      .evalMap(es =>
        es
          .filter {
            case e if (e.publishedAt orElse e.updatedAt).isDefined =>
              val dt = (e.publishedAt orElse e.updatedAt).get
              val now = java.time.OffsetDateTime.now()
              val timeAfter = now.minusMinutes(5)
              dt.isAfter(timeAfter)
            case _ => false
          }
          .traverse { e =>
            val content = s"""${e.title}
${e.link}
"""
            discord.Discord.post(deliveryTarget.webhookUrl, content)
          }
      )
      .compile
      .drain
      .start
  }

  def fetchFeed(feed: Feed): IO[Seq[Entry]] = // stub
    IO.println(s"fetching a feed ${feed.url} ...") >> IO.blocking {
      // Assuming feed type is RSS or Atom
      rss.Rss
        .fetchEntries(feed.url)
        .map(re =>
          Entry(re.title, re.link, re.author, re.publishedAt, re.updatedAt)
        )
    }
}
