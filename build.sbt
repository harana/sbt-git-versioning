import sbt.plugins.SbtPlugin

name := "sbt-git-versioning"
organization := "com.harana"

ThisBuild / versionScheme := Some("early-semver")


ThisBuild / githubOwner := "harana"
ThisBuild / githubRepository := "sbt-git-versioning"

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))

licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

// SbtPlugin requires sbt 1.2.0+
// See: https://developer.lightbend.com/blog/2018-07-02-sbt-1-2-0/#sbtplugin-for-plugin-development
enablePlugins(SbtPlugin)

scalacOptions ++= {
  val linting = CrossVersion.partialVersion(scalaVersion.value) match {
    // some imports are necessary for compat with 2.10.
    // 2.12 needs to chill out with the unused imports warnings.
    case Some((2, 12)) => "-Xlint:-unused,_"
    case _ => "-Xlint"
  }
  Seq("-Xfatal-warnings", linting)
}
crossSbtVersions := List("1.2.8")
publishMavenStyle := true
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "se.sawano.java" % "alphanumeric-comparator" % "1.4.1"
)

// disable scaladoc generation
Compile / doc / sources := Seq.empty
packageDoc / publishArtifact := false

