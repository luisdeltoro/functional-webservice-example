package ldeltoro.controller

import cats.effect.IO
import ldeltoro.db.AddressRepository
import ldeltoro.model.Address
import org.http4s.{HttpService, Uri}
import org.http4s.dsl.io._
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.headers.Location
import org.slf4j.LoggerFactory

class AddressController(addressRepository: AddressRepository) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def routes(): HttpService[IO] = HttpService[IO] {
    case req @ GET -> Root / "user" / userId / "addresses" =>
      logger.debug(s"Endpoint /user/$userId/addresses called with GET request: $req")
      val addresses = addressRepository.findAllByUserId(userId)
      addresses.flatMap(a => Ok(a.asJson))
    case req @ POST -> Root / "user" / userId / "addresses" =>
      logger.debug(s"Endpoint /user/$userId/address called with POST request: $req")
      implicit val decoder = jsonOf[IO, Address]

      req.decode[Address] { addr =>
        addressRepository.insert(addr, userId).flatMap { id =>
          Created(Location(Uri.unsafeFromString(s"/user/$userId/addresses/$id")))
        }
      }

  }

}
