package io.github.windymelt.rss2discordbis

import cats.Monad

trait Repository[F[_]: Monad] {
  def getAllFeeds(): F[Seq[DBFeed]]
  def getFeedById(id: String): F[Option[DBFeed]]
  def createFeed(
      id: String,
      url: String,
      feedType: FeedType,
      enabled: Boolean = true
  ): F[DBFeed]
  def saveFeed(feed: DBFeed): F[Unit]
  def deleteFeedById(id: String): F[Unit]
}

case class DBFeed(id: String, url: String, feedType: FeedType, enabled: Boolean)
