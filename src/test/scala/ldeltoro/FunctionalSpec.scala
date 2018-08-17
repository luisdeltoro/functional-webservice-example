package ldeltoro

import java.util.UUID

import cats.effect.IO
import fs2.Stream
import ldeltoro.config.{Config, DbConfig, ServerConfig}
import ldeltoro.model.Address
import ldeltoro.util.Resources
import org.http4s.Method._
import org.http4s.client.blaze.Http1Client
import org.http4s.{EntityDecoder, Request, Response, Uri}
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.util.CaseInsensitiveString
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll

class FunctionalSpec extends Specification with Resources with BeforeAfterAll {

    "The address service" should {
      "return 200 when the /public/user/{userId}/addresses endpoint is called with GET" in {
        val uri = testAppUri / "public" / "user" / "123" / "addresses"
        val req = Request[IO](GET, uri)
        val (resp, body) = fetch(req)

        resp.status.code must_== 200
      }

      "return 200 when the /public/user/{userId}/addresses/{addrId} endpoint is called with GET and the address exists" in {
        val addrId = createDummyAddr()
        val uri = testAppUri / "public" / "user" / "123" / "addresses" / addrId
        val req = Request[IO](GET, uri)
        val (resp, body) = fetch(req)

        resp.status.code must_== 200
      }

      "return 404 when the /public/user/{userId}/addresses/{addrId} endpoint is called with GET and the address does not exist" in {
        val uri = testAppUri / "public" / "user" / "123" / "addresses" / "non-existent"
        val req = Request[IO](GET, uri)
        val (resp, body) = fetch(req)

        resp.status.code must_== 404
      }

      "return 201 when the /public/user/{userId}/addresses endpoint is called with POST" in {
        val addr = Address("Wall Street", Some("1"))
        val uri = testAppUri / "public" / "user" / "123" / "addresses"
        val req = Request[IO](POST, uri).withBody(addr.asJson)
        val (resp, body) = fetch(req)

        resp.status.code must_== 201
        resp.headers.get(CaseInsensitiveString("Location")).map(_.value) must beSome
      }

      "return 204 when the /public/user/{userId}/addresses/{addrId} endpoint is called with DELETE" in {
        val uri = testAppUri / "public" / "user" / "123" / "addresses" / UUID.randomUUID().toString
        val req = Request[IO](DELETE, uri)
        val (resp, body) = fetch(req)

        resp.status.code must_== 204
      }

      "return 204 when the /public/user/{userId}/addresses/{addrId} endpoint is called with PUT and the address exists" in {
        val addrId = createDummyAddr()
        val addr = Address("Wall Street", Some("2"))
        val uri = testAppUri / "public" / "user" / "123" / "addresses" / addrId
        val req = Request[IO](PUT, uri).withBody(addr.asJson)
        val (resp, body) = fetch(req)

        resp.status.code must_== 204
      }

      "return 404 when the /public/user/{userId}/addresses/{addrId} endpoint is called with PUT and the address does not exist" in {
        val addr = Address("Wall Street", Some("2"))
        val uri = testAppUri / "public" / "user" / "123" / "addresses" / UUID.randomUUID().toString
        val req = Request[IO](PUT, uri).withBody(addr.asJson)
        val (resp, body) = fetch(req)

        resp.status.code must_== 404
      }

    }

  val testAppHost = "localhost"
  val testAppPort = findFreePort
  val testAppUri = Uri.unsafeFromString(s"http://$testAppHost:$testAppPort")
  val client = Http1Client[IO]().unsafeRunSync

  def beforeAll(): Unit = {
    val testConfig = new Config(
      server = ServerConfig(testAppHost, testAppPort),
      database = DbConfig("jdbc:postgresql:address", "addr", "addr", "org.postgresql.Driver")
    )

    App.bootstrap(Stream.eval(IO(testConfig))).compile.drain.unsafeToFuture
  }

  def afterAll(): Unit = {
    client.shutdownNow()
  }

  private def createDummyAddr(): String = {
    val addr = Address("Wall Street", Some("1"))
    val uri = testAppUri / "public" / "user" / "123" / "addresses"
    val req = Request[IO](POST, uri).withBody(addr.asJson)
    val (resp, body) = fetch(req)
    resp.headers.get(CaseInsensitiveString("Location")).map(_.value).get.split("/").last
  }

  private def fetch(req: Request[IO]): (Response[IO], String) = {
    fetch(IO(req))
  }

  private def fetch(req: IO[Request[IO]]): (Response[IO], String) = {
    val d = implicitly[EntityDecoder[IO, String]]
    client.fetch(req)(resp =>
      IO(resp, d.decode(resp, strict = false).fold(throw _, identity).unsafeRunSync)
    ).unsafeRunSync  }
}