name := "lift-liftscreen-css-binding"
 
scalaVersion := "2.9.1"
 
seq(com.github.siasia.WebPlugin.webSettings :_*)

// If using JRebel uncomment next line
//scanDirectories in Compile := Nil

resolvers ++= Seq(
	  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"
)

libraryDependencies ++= {
  val liftVersion = "2.4" // Put the current/latest lift version here
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-wizard" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-testkit" % liftVersion % "test"
  )
}

// Customize any further dependencies as desired
libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "container,test", // For Jetty 7
  "org.specs2" %% "specs2" % "1.7.1" % "test",
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  "ch.qos.logback" % "logback-classic" % "0.9.30" % "compile->default" // Logging
)

// keep only specifications ending with Spec or Unit
testOptions := Seq(Tests.Filter(s => Seq("Spec", "Unit").exists(s.endsWith(_))))

scalacOptions ++= Seq("-unchecked", "-deprecation")
