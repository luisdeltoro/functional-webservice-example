package ldeltoro.controller

import cats.effect.IO
import ldeltoro.db.AddressRepository
import ldeltoro.model.Address
import org.http4s.{HttpService, Request, Response, Uri}
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
      getAllHandler(req, userId)
    case req @ GET -> Root / "user" / userId / "addresses" / addrId =>
      getSingleHandler(req, userId, addrId)
    case req @ POST -> Root / "user" / userId / "addresses" =>
      createHandler(req, userId)
    case req @ DELETE -> Root / "user" / userId / "addresses" / addrId =>
      deleteHandler(req, userId, addrId)
    case req @ PUT -> Root / "user" / userId / "addresses" / addrId =>
      updateHandler(req, userId, addrId)
  }

  private def getAllHandler(req: Request[IO], userId: String): IO[Response[IO]] = {
    logger.debug(s"Endpoint /user/$userId/addresses called with GET request: $req")
    val addresses = addressRepository.findAllByUserId(userId)
    addresses.flatMap(a => Ok(a.asJson))
  }

  private def getSingleHandler(req: Request[IO], userId: String, addrId: String): IO[Response[IO]] = {
    logger.debug(s"Endpoint /user/$userId/addresses/$addrId called with GET request: $req")
    addressRepository.find(addrId).flatMap(_.fold(NotFound())(a => Ok(a.asJson)))
  }

  private def createHandler(req: Request[IO], userId: String): IO[Response[IO]] = {
    logger.debug(s"Endpoint /user/$userId/address called with POST request: $req")
    implicit val decoder = jsonOf[IO, Address]

    req.decode[Address] { addr =>
      addressRepository.insert(addr, userId).flatMap { id =>
        Created(Location(Uri.unsafeFromString(s"/user/$userId/addresses/$id")))
      }
    }
  }

  private def deleteHandler(req: Request[IO], userId: String, addrId: String): IO[Response[IO]] = {
    logger.debug(s"Endpoint /user/$userId/addresses/$addrId called with GET request: $req")
    addressRepository.delete(addrId).flatMap(_ => NoContent())
  }

  def updateHandler(req: Request[IO], userId: String, addrId: String): IO[Response[IO]] = {
    logger.debug(s"Endpoint /user/$userId/address called with PUT request: $req")
    implicit val decoder = jsonOf[IO, Address]

    req.decode[Address] { addr =>
      addressRepository.update(addr, addrId).flatMap( id => id match {
        case 0 => NotFound()
        case _ => NoContent()
      })
    }
  }
  
}
