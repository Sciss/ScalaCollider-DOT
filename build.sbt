lazy val baseName  = "ScalaCollider-DOT"
lazy val baseNameL = baseName.toLowerCase

lazy val projectVersion = "1.6.4"
lazy val mimaVersion    = "1.6.0"

lazy val deps = new {
 val main = new {
   val scalaCollider = "2.6.4"
   val ugens         = "1.21.1"
  }
}

// sonatype plugin requires that these are in global
ThisBuild / version       := projectVersion
ThisBuild / organization  := "de.sciss"
ThisBuild / versionScheme := Some("pvp")

lazy val root = project.withId(baseNameL).in(file("."))
  .settings(
    name               := baseName,
//    version            := projectVersion,
//    organization       := "de.sciss",
    description        := "Utility for exporting a ScalaCollider UGen Graph as GraphViz .dot file",
    homepage           := Some(url(s"https://github.com/Sciss/${name.value}")),
    licenses           := Seq("AGPL v3+" -> url("http://www.gnu.org/licenses/agpl-3.0.txt")),
    scalaVersion       := "2.13.5",
    crossScalaVersions := Seq("3.0.0", "2.13.5", "2.12.13"),
    mimaPreviousArtifacts := Set(organization.value %% baseNameL % mimaVersion),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8"),
    scalacOptions ++= {
      // if (isDotty.value) Nil else 
      Seq("-Xlint", "-Xsource:2.13")
    },
    scalacOptions ++= {
      val sv = scalaVersion.value
      if (sv.startsWith("2.13.")) "-Wvalue-discard" :: Nil else Nil
    },
    // sources in (Compile, doc) := {
    //   if (isDotty.value) Nil else (sources in (Compile, doc)).value // dottydoc is complaining about something
    // },
    libraryDependencies ++= Seq(
      "de.sciss" %% "scalacollider"           % deps.main.scalaCollider,
      "de.sciss" %  "scalacolliderugens-spec" % deps.main.ugens
    )
  )
  .settings(publishSettings)

// ---- publishing ----
lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  developers := List(
    Developer(
      id    = "sciss",
      name  = "Hanns Holger Rutz",
      email = "contact@sciss.de",
      url   = url("https://www.sciss.de")
    )
  ),
  scmInfo := {
    val h = "github.com"
    val a = s"Sciss/${name.value}"
    Some(ScmInfo(url(s"https://$h/$a"), s"scm:git@$h:$a.git"))
  },
)

