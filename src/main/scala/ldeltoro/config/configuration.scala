package ldeltoro.config

import cats.Show
import cats.effect.Sync
import cats.syntax.flatMap._
import org.http4s.Uri
import org.slf4j.LoggerFactory
import pureconfig.error.{ConfigReaderException, ConfigReaderFailure}
import pureconfig.loadConfig
import pureconfig.modules.http4s._

case class Config(
  server: ServerConfig,
  database: DbConfig
)

case class ServerConfig(host : String, port : Int)
case class DbConfig(url: String, user:String, password:String, driver: String)

object Config {
  private val logger = LoggerFactory.getLogger(Config.getClass)

  implicit val configFailureShow =
    Show.show[ConfigReaderFailure](
      f => s"${f.location.map(_.description + "> ").getOrElse("")}${f.description}"
    )

  def loadFatal(): Config = loadConfig[Config] match {
    case Right(c) => c
    case Left(errors) =>
      errors.toList.foreach(e => logger.error("Error while loading configuration", e))
      throw new UninitializedError()
  }

  def load[F[_]](implicit E: Sync[F]): F[Config] =
    E.delay(loadConfig[Config]).flatMap {
      case Right(ok) => E.pure(ok)
      case Left(e) => E.raiseError(new ConfigReaderException[Config](e))
    }
}