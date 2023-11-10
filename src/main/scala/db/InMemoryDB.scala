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
}
