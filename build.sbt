val scala3Version = "3.7.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "introprog-discord-bot",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "net.dv8tion" % "JDA" % "5.6.1",
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      "tabby" % "tabby" % "0.2.3" from
        s"https://github.com/bjornregnell/tabby/releases/download/v0.2.3/tabby_3-0.2.3.jar"
    )
  )
