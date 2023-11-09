package io.github.windymelt.rss2discordbis

enum FeedType:
  case Atom, RSS

final case class Feed(url: String, feedType: FeedType)
