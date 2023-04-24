addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.14.2")
addSbtPlugin ("com.codecommit" % "sbt-github-packages" % "0.5.3")
Compile / unmanagedSourceDirectories += new File("src/main/scala")
libraryDependencies += "se.sawano.java" % "alphanumeric-comparator" % "1.4.1"
