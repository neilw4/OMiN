package neilw4.omin.model

import java.sql.{Timestamp, Blob}

import android.content.Context
import android.os.AsyncTask

import scala.concurrent.{ExecutionContext, Future}
import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.MTable

// Singleton database.
object Db {
    private var database: Option[Db] = None
    def apply = get _

    def get(appContext: Context) =  {
        if (database.isEmpty) {
            database = Some(new Db(appContext))
        }
        database.get
    }

}

class Db(appContext: Context) {

    private val db = Database.forURL(
        "jdbc:sqlite:" + appContext.getFilesDir + "OMiN",
        driver = "org.sqldroid.SQLDroidDriver"
    )

    // Execution context required by futures should use android's thread pool.
    implicit val exec = ExecutionContext.fromExecutor(
        AsyncTask.THREAD_POOL_EXECUTOR)

    // Runs a function asyncronously within a database transaction.
    def asyncTransaction[T](f: Session => T): Future[T] = Future{db.withTransaction[T](f)}

    // Basic queries.
    val users = TableQuery[Users]
    val userIds = TableQuery[UserIds]
    val messages = TableQuery[Messages]

    // Initialise tables if necessary.
    asyncTransaction {
        implicit session =>
            List(users, userIds, messages).foreach {
                table => MTable.getTables(table.baseTableRow.tableName).firstOption match {
                    case None => table.ddl.create // Table doesn't exist.
                    case _ => {}
                }
            }
    }

    class Users(tag: Tag) extends Table[User](tag, "Users") {
        def index = column[Int]("index", O.PrimaryKey, O.NotNull, O.AutoInc)
        def following = column[Boolean]("following", O.NotNull, O.Default(false))
        override def * = (index.?, following) <> (User.tupled, User.unapply)
    }

    class UserIds(tag: Tag) extends Table[UserId](tag, "UserIds") {
        def index = column[Int]("index", O.NotNull)
        def id = column[String]("id", O.PrimaryKey, O.NotNull)
        def index_of = foreignKey("index_of", index, TableQuery[Users])(_.index)
        override def * = (index, id) <> (UserId.tupled, UserId.unapply)
    }

    class Messages(tag: Tag) extends Table[Message](tag, "Messages") {
        def from = column[Int]("from", O.NotNull)
        def signature = column[Blob]("signature", O.NotNull, O.PrimaryKey)
        def body = column[String]("body", O.NotNull)
        def received = column[Timestamp]("received", O.NotNull)
        def read = column[Boolean]("read", O.NotNull, O.Default(false))
        def distribution_count = column[Int]("distribution_count", O.NotNull, O.Default(0))
        def from_user = foreignKey("from", from, TableQuery[Users])(_.index)
        override def * = (from, signature, body, received, distribution_count, read) <> (Message.tupled, Message.unapply)
    }

    // Creates tables in the database if necessary.
    private def createIfNotExists(tables: TableQuery[_ <: Table[_]]*)(implicit session: Session) {
        tables foreach {
            table => MTable.getTables(table.baseTableRow.tableName).firstOption match {
                case None => table.ddl.create
                case _ => {}
            }
        }
    }

}
