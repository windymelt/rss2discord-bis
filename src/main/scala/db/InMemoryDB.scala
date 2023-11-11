package io.github.windymelt.rss2discordbis
package db

import cats.Id

class InMemoryDB extends Repository[Id] {
  private var feeds: scala.collection.mutable.Map[String, DBFeed] =
    scala.collection.mutable.Map.empty

  def getAllFeeds(): Id[Seq[DBFeed]] = feeds.values.toSeq
  def getFeedById(id: String): Id[Option[DBFeed]] = feeds.get(id)
  def createFeed(
      id: String,
      url: String,
      feedType: FeedType,
      enabled: Boolean = true
  ): Id[DBFeed] = {
    val feed = DBFeed(id, url, feedType, enabled)
    feeds += (id -> feed)
    feed
  }
  def saveFeed(feed: DBFeed): Id[Unit] = {
    feeds += (feed.id -> feed)
  }
  def deleteFeedById(id: String): Id[Unit] = {
    feeds -= id
  }

  private var feedDispatches
      : scala.collection.mutable.Map[String, DBFeedDispatch] =
    scala.collection.mutable.Map.empty

  def getAllFeedsDispatches(): Id[Seq[DBFeedDispatch]] =
    feedDispatches.values.toSeq
  def getFeedDispatchById(id: String): Id[Option[DBFeedDispatch]] =
    feedDispatches.get(id)
  def createFeedDispatch(
      id: String,
      feedId: String,
      dispatchToId: String,
      enabled: Boolean = true
  ): Id[DBFeedDispatch] = {
    val feedDispatch = DBFeedDispatch(id, feedId, dispatchToId, enabled)
    feedDispatches += (id -> feedDispatch)
    feedDispatch
  }
  def saveFeedDispatch(feedDispatch: DBFeedDispatch): Id[Unit] = {
    feedDispatches += (feedDispatch.id -> feedDispatch)
  }
  def deleteFeedDispatchById(id: String): Id[Unit] = {
    feedDispatches -= id
  }

  private var dispatches: scala.collection.mutable.Map[String, DBDispatch] =
    scala.collection.mutable.Map.empty

  def getAllDispatches(): Id[Seq[DBDispatch]] = dispatches.values.toSeq
  def getDispatchById(id: String): Id[Option[DBDispatch]] = dispatches.get(id)
  def createDispatch(
      id: String,
      dispatchType: String,
      webhookUrl: String,
      enabled: Boolean = true
  ): Id[DBDispatch] = {
    val dispatch = DBDispatch(id, dispatchType, webhookUrl, enabled)
    dispatches += (id -> dispatch)
    dispatch
  }
  def saveDispatch(dispatch: DBDispatch): Id[Unit] = {
    dispatches += (dispatch.id -> dispatch)
  }
  def deleteDispatchById(id: String): Id[Unit] = {
    dispatches -= id
  }
}
