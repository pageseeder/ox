description = "OX adapter for XML diff using Diffx"

// Dependencies of the project
dependencies {

  // module dependencies
  implementation(project(":pso-ox-core"))
  implementation(libs.pso.diffx)
  implementation(libs.pso.xmlwriter)
  implementation(libs.commons.io)
  implementation(libs.jtidy)
  implementation(libs.slf4j.api)

  runtimeOnly (libs.saxon.he)
  runtimeOnly (libs.logback.classic)
  runtimeOnly (libs.logback.core)

  testImplementation (libs.junit)
  testImplementation (libs.xmlunit)
}
