package neilw4.omin.model

import java.sql.{Blob, Timestamp}

// A user has an index and may be followed by the current user.
// Note that index is optional to allow for cases where it is unknown
// or hasn't been created (SQL will autogenerate an index on creation).
case class User(index: Option[Int], following: Boolean)
// A user also has multiple ids.
case class UserId(index: Int, id: String)
// A message has an originating user, contents, a distribution count and a record of whether it has been read.
case class Message(from: Int, signature: Blob, body: String, received: Timestamp, distribution_count: Int, read: Boolean)
