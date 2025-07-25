description = "OX adapter for XML validation with Schematron"

// Dependencies of the project
dependencies {

  // module dependencies
  implementation(project(":pso-ox-core"))

  implementation(libs.pso.schematron)
  implementation(libs.pso.xmlwriter)
  implementation(libs.slf4j.api)

  runtimeOnly (libs.saxon.he)
  runtimeOnly (libs.logback.classic)
  runtimeOnly (libs.logback.core)

  testImplementation (libs.junit)
  testImplementation (libs.xmlunit)
}
