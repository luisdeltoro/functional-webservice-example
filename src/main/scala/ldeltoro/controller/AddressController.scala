package ldeltoro.controller

import cats.effect.IO
import org.http4s.HttpService
import org.http4s.dsl.io._
import org.slf4j.LoggerFactory

class AddressController {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def routes(): HttpService[IO] = HttpService[IO] {
    case req @ GET -> Root / "address" =>
      logger.debug(s"Endpoint /address called with request: $req")
      Ok("I'm a service endpoint")
  }

}
