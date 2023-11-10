package io.github.windymelt.rss2discordbis
package api.v1

import wvlet.airframe.http._

type FeedType = "rss" | "atom"
case class Feed(id: String, url: String, feedType: FeedType, enabled: Boolean)

@RPC trait Rss2DiscordBis:
  def getAllFeeds(): Seq[Feed]

// XXX: We must define this to annotate we can provide RPC service
object Rss2DiscordBis extends RxRouterProvider:
  override def router: RxRouter = RxRouter.of[Rss2DiscordBis]
