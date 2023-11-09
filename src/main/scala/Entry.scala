package io.github.windymelt.rss2discordbis

import java.time.{OffsetDateTime => DateTime}
import java.time.ZoneId
import java.time.ZoneOffset

case class Entry(
    title: String,
    link: String,
    author: String,
    publishedAt: Option[DateTime],
    updatedAt: Option[DateTime]
)
