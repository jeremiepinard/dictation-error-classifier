
lazy val akkaHttpVersion = "10.0.11"
lazy val akkaVersion    = "2.5.11"

enablePlugins(JavaAppPackaging)
enablePlugins(FlywayPlugin)

flywayUrl := "jdbc:postgresql://127.0.0.1:5432/dictation_error_classifier"
flywayUser := "dictation_error_classifier"
flywayPassword := "password"
flywayLocations += "db/migration"

lazy val baseSettings = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.4"
    )),
    name := "dictation-error-classifier",
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "org.scalaz"        %% "scalaz-core"          % "7.3.0-M24",
      "ch.megard" %% "akka-http-cors" % "0.3.0",
      "org.postgresql" % "postgresql" % "42.2.5",
      "org.flywaydb" % "flyway-core" % "5.2.0",
      "be.wegenenverkeer" %% "akka-persistence-pg" % "0.10.0",
      "com.typesafe.akka" %% "akka-persistence" % "2.5.4",

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
    )
  )