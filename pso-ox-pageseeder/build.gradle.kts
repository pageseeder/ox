description = "OX adapter for Pageseeder"

// Dependencies of the project
dependencies {

  // module dependencies
  implementation(project(":pso-ox-core"))
  implementation(libs.psberlioz.simple.core)
  implementation(libs.psberlioz.simple.vault)
  implementation (libs.pso.berlioz.plus)
  implementation(libs.pso.bridge)
  implementation(libs.pso.bridge.berlioz)
  implementation(libs.pso.schematron)
  implementation(libs.pso.xmlwriter)
  implementation(libs.ehcache)
  implementation(libs.slf4j.api)

  runtimeOnly (libs.saxon.he)
  runtimeOnly (libs.logback.classic)
  runtimeOnly (libs.logback.core)

  testImplementation (libs.junit)
  testImplementation (libs.pso.xmlwriter)
}
