package io.github.windymelt.rss2discordbis.discord

import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

case class WebhookInput(content: String)

object DiscordEndpoint {
  val webhook = endpoint.post.in(jsonBody[WebhookInput]).out(emptyOutput)
}
