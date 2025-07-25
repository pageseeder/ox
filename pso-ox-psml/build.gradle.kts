description = "OX adapter for PSML files manipulation"

// Dependencies of the project
dependencies {

  // module dependencies
  implementation(project(":pso-ox-core"))
  implementation(libs.pso.psml)
  implementation(libs.pso.schematron)
  implementation(libs.pso.xmlwriter)
  implementation(libs.slf4j.api)

  runtimeOnly (libs.saxon.he)
  runtimeOnly (libs.logback.classic)
  runtimeOnly (libs.logback.core)
  runtimeOnly (libs.topologi.xerces)

  testImplementation(project(":pso-ox-test"))
  testImplementation (libs.junit)
}
