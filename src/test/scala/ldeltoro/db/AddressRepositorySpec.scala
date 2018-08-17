package ldeltoro.db

import java.util.UUID

import cats.effect.IO
import org.specs2.mutable.Specification
import doobie.specs2._
import doobie.util.transactor.Transactor

class AddressRepositorySpec extends Specification with IOChecker {

  val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:address", "addr", "addr"
  )

  val testee = new AddressRepository(transactor)

  check(testee.insertStmt(UUID.randomUUID().toString, "Wall Street", Some("1"), "1234"))
  check(testee.updateStmt(UUID.randomUUID().toString, "Wall Street", Some("1")))
  check(testee.deleteStmt(UUID.randomUUID().toString))
  check(testee.findStmt(UUID.randomUUID().toString))
  check(testee.findAllByUserIdStmt("123"))
}
