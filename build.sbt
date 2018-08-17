val catsVersion = "1.1.0"
val circeVersion = "0.9.2"
val http4sVersion = "0.18.9"
val specs2Version = "4.1.0"
val pureConfigVersion = "0.9.1"
val doobieVersion = "0.5.3"

val root = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    libraryDependencies ++= List(
      "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-http4s" % pureConfigVersion,
      "org.typelevel" %% "cats-core" % catsVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-literal" % circeVersion,
      "io.circe" %% "circe-java8" % circeVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.log4s" %% "log4s" % "1.3.4",
      "ch.qos.logback" %  "logback-classic" % "1.1.8",
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
      "org.tpolecat" %% "doobie-specs2"  % doobieVersion,
      "org.flywaydb" %  "flyway-core" % "5.1.4",
      "org.specs2" %% "specs2-core" % specs2Version % "test",
      "org.specs2" %% "specs2-mock" % specs2Version % "test",
      "org.specs2" %% "specs2-matcher-extra" % specs2Version % "test"
    )
  )
  .settings(
    name := "address-service",
    scalaVersion := "2.12.6"
  )

testFrameworks += new TestFramework("utest.runner.Framework")