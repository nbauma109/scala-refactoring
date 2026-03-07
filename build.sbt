import xerial.sbt.Sonatype.sonatypeCentralHost

name := "org.scala-refactoring.library"
version := "1.0.3"
scalaVersion := "2.12.21"
moduleName := name.value
organization := "io.github.nbauma109"
crossScalaVersions := Seq("2.12.21", "2.13.18")
crossVersion := CrossVersion.full
fork := true
parallelExecution in Test := false
autoAPIMappings := true

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "com.novocode" % "junit-interface" % "0.11" % Test
)

libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, 12)) => Seq(
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.7"
  )
  case Some((2, 13)) => Seq(
    "org.scala-lang.modules" %% "scala-parser-combinators" % "2.4.0"
  )
  case _ => Nil
})

scalacOptions ++= Seq(
  "-deprecation:false",
  "-encoding", "UTF-8",
  "-feature",
  "-language:_",
  "-unchecked"
)

Seq(Compile, Test).flatMap { config =>
  inConfig(config) {
    unmanagedSourceDirectories += {
      val dir = scalaSource.value
      val Some((major, minor)) = CrossVersion.partialVersion(scalaVersion.value)
      file(s"${dir.getPath}-$major.$minor")
    }
  }
}

publishMavenStyle := true
ThisBuild / sonatypeCredentialHost := sonatypeCentralHost
useGpg := true
publishTo := sonatypePublishToBundle.value
publishArtifact in Test := false
pomExtra := (
  <url>https://github.com/nbauma109/scala-refactoring</url>
  <licenses>
    <license>
      <name>Scala License</name>
      <url>http://www.scala-lang.org/node/146</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:https://github.com/nbauma109/scala-refactoring.git</connection>
    <developerConnection>scm:git:git@github.com:nbauma109/scala-refactoring.git</developerConnection>
    <tag>master</tag>
    <url>https://github.com/nbauma109/scala-refactoring</url>
  </scm>
  <developers>
    <developer>
      <id>misto</id>
      <name>Mirko Stocker</name>
      <email>me@misto.ch</email>
    </developer>
  </developers>
)

credentials ++= {
  val credentialHost = (ThisBuild / sonatypeCredentialHost).value
  val config = Path.userHome / ".m2" / "credentials"
  if (config.exists) Seq(Credentials(config))
  else {
    for {
      username <- sys.env.get("SONATYPE_USERNAME")
      password <- sys.env.get("SONATYPE_PASSWORD")
    } yield Credentials("Sonatype Nexus Repository Manager", credentialHost, username, password)
  }.toSeq
}

import scala.sys.process._

// sbt doesn't automatically load the content of the MANIFST.MF file, therefore
// we have to do it here by ourselves. Furthermore, the version format in the
// MANIFEST.MF is `version.qualifier`, which means that we have to replace
// `version` by the actual version and `qualifier` with a unique identifier
// otherwise OSGi can't find out which nightly build is newest and therefore
// not all caches are updated with the correct version of a nightly.
packageOptions in Compile in packageBin += {
  val in = new java.io.FileInputStream(new java.io.File("MANIFEST.MF.prototype"))
  val m = try {
    val manifest = new java.util.jar.Manifest(in)
    val attr = manifest.getMainAttributes
    val key = "Bundle-Version"
    val versionSuffix = scalaBinaryVersion.value.replace('.', '_')
    // get the version but get rid of "-SNAPSHOT" suffix if it exists
    val v = {
      val v = version.value
      val i = v.lastIndexOf('-')
      if (i > 0)
        v.substring(0, i)
      else
        v
    }
    val date = new java.text.SimpleDateFormat("yyyyMMddHHmm").format(new java.util.Date)
    val sha = "git rev-parse --short HEAD".!!.trim
    attr.putValue(key, attr.getValue(key).replace("version.qualifier", s"$v.$versionSuffix-$date-$sha"))
    manifest
  } finally in.close()
  Package.JarManifest(m)
}
