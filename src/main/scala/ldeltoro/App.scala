package ldeltoro

import cats.effect.IO
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import ldeltoro.config.{Config, ServerConfig}
import ldeltoro.controller.AddressController
import org.http4s.HttpService
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.middleware.{AutoSlash, GZip}

import scala.concurrent.ExecutionContext.Implicits.global

object App extends StreamApp[IO] {

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    bootstrap(Stream.eval(Config.load[IO]))

  def bootstrap(streamConf: Stream[IO, Config]): Stream[IO, ExitCode] =
    for {
      conf                         <- streamConf
      addressController            = new AddressController()
      router                       =  buildRoutes(addressController.routes())
      ServerConfig(host, port)     =  conf.server
      exitCode                     <- startWeb(router, host, port)
    } yield exitCode

  def buildRoutes(addressController: HttpService[IO]): HttpService[IO] = {
    Router(
      "/public"   -> addressController
    )
  }

  def startWeb(service: HttpService[IO], host: String, port: Int): Stream[IO, ExitCode] = {
    BlazeBuilder[IO]
      .bindHttp(port, host)
      .mountService(GZip(AutoSlash(service)), "/")
      .serve
  }

}
