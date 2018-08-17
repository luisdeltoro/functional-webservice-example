package ldeltoro.db

import cats.effect.IO
import doobie.hikari.HikariTransactor
import ldeltoro.config.DbConfig
import org.flywaydb.core.Flyway

object Database {
  def transactor(config: DbConfig): IO[HikariTransactor[IO]] = {
    HikariTransactor.newHikariTransactor[IO](config.driver, config.url, config.user, config.password)
  }

  def initialize(transactor: HikariTransactor[IO]): IO[Unit] = {
    transactor.configure { ds =>
      IO {
        val flyWay = new Flyway()
        flyWay.setDataSource(ds)
        flyWay.setBaselineOnMigrate(true)
        flyWay.migrate()
      }
    }
  }
}
