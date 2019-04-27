package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{Future, ExecutionContext}

@Singleton
class AdminRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class AdminTable(tag: Tag) extends Table[Admin](tag, "admin") {

    def id = column[Int]("admin_id", O.PrimaryKey, O.AutoInc)

    def email = column[String]("admin_email")

    def password = column[String]("admin_password")

    def * = (id, email, password) <> ((Admin.apply _).tupled, Admin.unapply)
  }

  val admin = TableQuery[AdminTable]

  def create(email: String, password: String): Future[Admin] = db.run {

    (admin.map(c => (c.email, c.password))

      returning admin.map(_.id)

      into { case ((email, password), id) => Admin(id, email, password) }

      ) += (email, password)
  }

  def list(): Future[Seq[Admin]] = db.run {
    admin.result
  }
}