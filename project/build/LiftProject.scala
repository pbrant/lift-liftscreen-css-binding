import sbt._

class LiftProject(info: ProjectInfo) extends DefaultWebProject(info) with IdeaProject {
  val liftVersion = "2.5-SNAPSHOT"

  // uncomment the following if you want to use the snapshot repo
  //  val scalatoolsSnapshot = ScalaToolsSnapshots

  // If you're using JRebel for Lift development, uncomment
  // this line
  // override def scanDirectories = Nil

  lazy val JavaNet = "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

  override def libraryDependencies = Set(
    "net.liftweb" %% "lift-common" % liftVersion % "compile" withSources(),
    "net.liftweb" %% "lift-util" % liftVersion % "compile" withSources(),
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile" withSources(),
    "net.liftweb" %% "lift-wizard" % liftVersion % "compile" withSources(),
    "org.mortbay.jetty" % "jetty" % "6.1.22" % "test",
    "junit" % "junit" % "4.5" % "test",
    "ch.qos.logback" % "logback-classic" % "0.9.26",
    "org.scala-tools.testing" %% "specs" % "1.6.9" % "test"
  ) ++ super.libraryDependencies
}
