package io.github.windymelt.rss2discordbis

import cats.effect.{IO, IOApp, Resource, ExitCode}
import fs2.Stream
import scala.concurrent.duration._
import cats.effect.kernel.Fiber
import cats._
import cats.implicits.{*, given}
import cats.effect.kernel.Ref

object Main extends IOApp {
  type DeliveryTarget = String // TODO: replace with DiscordWebhookUrl or sth.

  def run(args: List[String]): IO[ExitCode] = {
    val feeds = Seq(
      Feed("https://blog.3qe.us/feed", FeedType.Atom) -> "#foo",
      Feed("https://www.scala-js.org/rss", FeedType.RSS) -> "#bar"
    ) // stub. they should be loaded from DB

    def loop: IO[Unit] = {
      val killswitchRef =
        Ref[IO].of(() => IO.unit) // initnal value is a stub

      val delivery = for {
        _ <- IO.println("Loading configuration from DB ...")
        _ <- IO.println("Preparing killswitch ...")
        killswitchRef <- killswitchRef
        perFeedDelivery <- deliverFeeds(feeds)
        killswitch = () =>
          IO.println("killswitch is called") >>
            perFeedDelivery
              .traverse(
                _.cancel
              )
              .void // by calling this, we can restart/reload delivery process
        _ <- killswitchRef.set(killswitch)
        _ <- IO.sleep(30.seconds) // stub: emulating user interaction
        ks <- killswitchRef.get
        _ <- ks()
      } yield ()

      delivery.handleErrorWith { e =>
        IO.println(s"Error: ${e.getMessage()}") >> IO.sleep(1.minute)
      } >> loop
    }

    loop.as(ExitCode.Success)
  }

  // TODO: move to another file or module
  // apiServer can restart/reload delivery process
  def apiServer(
      cancelTokens: Seq[Fiber[IO, Throwable, Unit]]
  ): Resource[IO, Unit] = Resource.make {
    IO.println("Starting API server ...") >> IO.sleep(
      30.seconds // stub: emulating user interaction
    ) >> IO.println("Killing stream...") >> cancelTokens.traverse(
      _.cancel
    ) >> IO.unit
    // TODO: implement API server
  }(_ => IO.println("Stopping API server ..."))

  def deliverFeeds(
      feeds: Seq[(Feed, DeliveryTarget)]
  ): IO[Seq[Fiber[IO, Throwable, Unit]]] = for {
    cancelTokens <- feeds
      .traverse(feed =>
        periodicFetchDeliverProcess(feed._1, 10.second) // TODO: 5 minutes
      )
  } yield cancelTokens

  def periodicFetchDeliverProcess(
      feed: Feed,
      interval: FiniteDuration
  ): IO[Fiber[IO, Throwable, Unit]] = Stream
    .repeatEval(fetchFeed(feed))
    .metered(interval)
    .evalMap(es =>
      IO.println(
        s"delivering entries to Discord [${es.map(_.title).mkString(", ")}]"
      )
    ) // stub
    .compile
    .drain
    .start

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
