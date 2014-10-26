package neilw4.omin

import android.content.Context
import neilw4.omin.model._

import scala.concurrent.Future
import scala.slick.driver.SQLiteDriver.simple._

class OminManager(appContext: Context) {
    private lazy val db = Db.get(appContext)
    def sendMessage(contents: String) = {/*TODO*/}

    // All messages from users that the user is following.
    def getFollowedMessages: Future[List[Message]] = db.asyncTransaction {
        implicit session =>
            db.messages.innerJoin(db.users).on(_.from === _.index).filter(_._2.following).map(_._1).list
    }

    // All messages that haven't been read yet.
    def getUnreadMessages: Future[List[Message]] = db.asyncTransaction {
        implicit session => db.messages.filterNot(_.read).sortBy(_.received).list
    }

    // Mark a message as read.
    def readMessage(message: Message) = db.asyncTransaction {
        implicit session =>
            db.messages.filter(message == _).map(_.read).update(true)
    }

    // Start following a user by id.
    def followUserId(id: String) = db.asyncTransaction {
        implicit session => db.userIds.innerJoin(db.users).filter(_._1.id == id).firstOption match {
            case Some((id, user)) => db.users.filter(user.equals).map(_.following).update(true)
            case None => {
                val index = (db.users returning db.users.map(_.index)) += User(None, true)
                db.userIds += UserId(index, id)
            }
        }
    }

    // All users that are being followed by the current user.
    def getFollowedUsers: Future[List[User]] = db.asyncTransaction {
        implicit session => db.users.filter(_.following).list
    }

    // Stop following a user.
    def unfollowUser(user: User) = db.asyncTransaction {
        implicit session =>
            db.users.filter(user.equals).map(_.following).update(false)
    }

}
