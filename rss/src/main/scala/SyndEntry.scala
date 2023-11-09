package io.github.windymelt.rss2discordbis.rss

import com.rometools.rome.feed.synd.SyndEntry
import java.time.{OffsetDateTime => DateTime}
import java.time.ZoneId
import java.time.ZoneOffset
import scala.concurrent.duration.FiniteDuration
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.net.URL

case class RssEntry(
    title: String,
    link: String,
    author: String,
    publishedAt: Option[DateTime],
    updatedAt: Option[DateTime]
)

// TODO: provide TZ_OFFSET from main app
val tzOffset: Int = sys.env
  .get("TZ_OFFSET")
  .map(_.toInt)
  .getOrElse(
    FiniteDuration(
      ZoneOffset
        .of(DateTime.now().toZonedDateTime().getZone().getId())
        .getTotalSeconds(),
      "seconds"
    ).toHours.toInt
  )

extension (e: SyndEntry)
  def asRssEntry: RssEntry = RssEntry(
    e.getTitle(),
    e.getLink(),
    e.getAuthor(),
    e.publishedDateTime,
    e.updatedDateTime
  )
  def publishedDateTime: Option[DateTime] = Option(e.getPublishedDate).map {
    _.toInstant()
      .atZone(ZoneId.of(ZoneOffset.ofHours(tzOffset).getId()))
      .toOffsetDateTime()
  }
  def updatedDateTime: Option[DateTime] = Option(e.getUpdatedDate).map {
    _.toInstant()
      .atZone(ZoneId.of(ZoneOffset.ofHours(tzOffset).getId()))
      .toOffsetDateTime()
  }

object Rss {
  def fetchEntries(
      feedUrl: String
  ): Seq[RssEntry] = {
    import scala.collection.JavaConverters._
    val feed = new SyndFeedInput().build(new XmlReader(new URL(feedUrl)))
    feed
      .getEntries()
      .asScala
      .toSeq
      .map(_.asRssEntry)
  }
}
