package ldeltoro.db

import ldeltoro.model.Address

import cats.effect.IO
import doobie._
import doobie.implicits._

import java.util.UUID

class AddressRepository(transactor: Transactor[IO]) {

  def insertStmt(bId: String, street: String, number: Option[String], userId: String) = sql"INSERT INTO address (bId, street, number, user_id) VALUES ($bId, $street, $number, $userId)".update
  def findAllByUserIdStmt(userId: String) = sql"SELECT street, number FROM address WHERE user_id = $userId".query[Address]

  def insert(address: Address, userId: String): IO[String] = {
    val businessId = UUID.randomUUID().toString
    insertStmt(businessId, address.street, address.number, userId).withUniqueGeneratedKeys[String]("bid").transact(transactor)
  }

  def findAllByUserId(userId: String): IO[List[Address]] = {
    findAllByUserIdStmt(userId).to[List].transact(transactor)
  }
}