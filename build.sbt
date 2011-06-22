name := "curriculum"

organization := "org.technbolts"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.9.0-1"

seq(WebPlugin.webSettings :_*)

retrieveManaged := true // remove this once plugins are working or i understand their layout

libraryDependencies ++= Seq(
  // web
  "org.scalatra" %% "scalatra" % "2.0.0-SNAPSHOT",
  "org.scalatra" %% "scalatra-specs" % "2.0.0-SNAPSHOT" % "test",
  "org.mortbay.jetty" % "jetty" % "6.1.22" % "jetty",
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  
  // logs
  "org.slf4j" % "slf4j-api" % "1.6.0",
  "ch.qos.logback" % "logback-classic" % "0.9.25" % "runtime"

  // persistence BerkeleyDB :p
  "com.sleepycat" % "je" % "4.0.92"

  //test
  "org.scala-tools.testing" %% "specs" % "1.6.8" % "test"
)

resolvers ++= Seq(
  "Oracle Repo" at "http://download.oracle.com/maven",
  "Sonatype OSS" at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Web plugin repo" at "http://siasia.github.com/maven2"
)