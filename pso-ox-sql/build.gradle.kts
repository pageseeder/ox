description = "OX adapter for SQL"

// Dependencies of the project
dependencies {

  // module dependencies
  implementation(project(":pso-ox-core"))
  implementation(libs.pso.xmlwriter)
  implementation(libs.sqlite.jdbc)
  implementation(libs.slf4j.api)

  runtimeOnly (libs.logback.classic)
  runtimeOnly (libs.logback.core)

  testImplementation (libs.junit)
  testImplementation (libs.xmlunit)

}
