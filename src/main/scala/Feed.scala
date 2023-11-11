package io.github.windymelt.rss2discordbis

enum FeedType:
  override def toString(): String = this match
    case Atom => "atom"
    case RSS  => "rss"
  case Atom, RSS

final case class Feed(url: String, feedType: FeedType)
