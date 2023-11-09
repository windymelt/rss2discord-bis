package io.github.windymelt.rss2discordbis.discord

import cats.effect.IO
import org.http4s.blaze.client.BlazeClientBuilder
import sttp.tapir.client.http4s.Http4sClientInterpreter

object Discord {
  def post(webhookUrl: String, content: String): IO[Unit] = {
    val (req, res) =
      Http4sClientInterpreter[IO]()
        .toRequest(
          DiscordEndpoint.webhook,
          Some(org.http4s.Uri.unsafeFromString(webhookUrl))
        )
        .apply(WebhookInput(content))

    val clientResource = BlazeClientBuilder[IO].resource
    val parsedResult =
      clientResource.flatMap(_.run(req)).use(res)

    parsedResult.debug("discord").flatTap(_ => IO.println("posted")).void
  }
}
