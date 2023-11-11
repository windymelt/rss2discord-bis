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

  def getAllFeedsDispatches(): F[Seq[DBFeedDispatch]]
  def getFeedDispatchById(id: String): F[Option[DBFeedDispatch]]
  def createFeedDispatch(
      id: String,
      feedId: String,
      dispatchToId: String,
      enabled: Boolean = true
  ): F[DBFeedDispatch]
  def saveFeedDispatch(feedDispatch: DBFeedDispatch): F[Unit]
  def deleteFeedDispatchById(id: String): F[Unit]

  def getAllDispatches(): F[Seq[DBDispatch]]
  def getDispatchById(id: String): F[Option[DBDispatch]]
  def createDispatch(
      id: String,
      dispatchType: String,
      webhookUrl: String,
      enabled: Boolean = true
  ): F[DBDispatch]
  def saveDispatch(dispatch: DBDispatch): F[Unit]
  def deleteDispatchById(id: String): F[Unit]
}

case class DBFeed(id: String, url: String, feedType: FeedType, enabled: Boolean)
case class DBFeedDispatch(
    id: String,
    feedId: String,
    dispatchToId: String,
    enabled: Boolean
)
case class DBDispatch(
    id: String,
    dispatchType: String,
    webhookUrl: String,
    enabled: Boolean
)
