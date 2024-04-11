val scalaV = "2.13.13"
val scalaTestV = "3.2.16"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaV,
  //"org.scalatest" %% "scalatest" % scalaTestV % "test"
)

fork in run := true

scalaVersion := scalaV

// docs

enablePlugins(ParadoxMaterialThemePlugin)

Compile / paradoxMaterialTheme := {
  ParadoxMaterialTheme()
    // choose from https://jonas.github.io/paradox-material-theme/getting-started.html#changing-the-color-palette
    .withColor("light-green", "amber")
    // choose from https://jonas.github.io/paradox-material-theme/getting-started.html#adding-a-logo
    .withLogoIcon("cloud")
    .withCopyright("Copyleft © Johannes Rudolph")
    .withRepository(uri("https://github.com/jrudolph/xyz"))
    .withSocial(
      uri("https://github.com/jrudolph"),
      uri("https://twitter.com/virtualvoid")
    )
}

paradoxProperties ++= Map(
  "github.base_url" -> (Compile / paradoxMaterialTheme).value.properties.getOrElse("repo", "")
)
