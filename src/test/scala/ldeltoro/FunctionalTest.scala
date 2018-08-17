package ldeltoro

import cats.effect.IO
import fs2.Stream
import ldeltoro.config.{Config, ServerConfig}
import ldeltoro.util.Resources
import org.http4s.Method.GET
import org.http4s.client.blaze.Http1Client
import org.http4s.{EntityDecoder, Request, Response, Uri}
import utest._

object FunctionalTest extends TestSuite with Resources {



  val tests =  Tests {
    "The address service should" - {
      startApplication()

      "return 200 when the /public/address endpoint is called" - {
        val uri = testAppUri / "public" / "address"
        val req = Request[IO](GET, uri)
        val (resp, body) = fetch(req)

        resp.status.code ==> 200
        body.contains("I'm a service") ==> true
      }

    }
  }

  private def fetch(req: Request[IO]): (Response[IO], String) = {
    val d = implicitly[EntityDecoder[IO, String]]
    client.fetch(req)(resp =>
      IO(resp, d.decode(resp, strict = false).fold(throw _, identity).unsafeRunSync)
    ).unsafeRunSync
  }

  def startApplication() = {
    App.bootstrap(Stream.eval(IO(testConfig))).compile.drain.unsafeToFuture
  }

  val client = Http1Client[IO]().unsafeRunSync
  val testAppHost = "localhost"
  val testAppPort = findFreePort
  val testAppUri = Uri.unsafeFromString(s"http://$testAppHost:$testAppPort")
  val testConfig = new Config(
    server = ServerConfig(testAppHost, testAppPort)
  )
}