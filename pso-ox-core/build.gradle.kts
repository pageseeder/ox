description = "Core OX API"

// Dependencies of the project
dependencies {

  compileOnly(libs.jetbrains.annotations)

  implementation(libs.pso.xmlwriter)
  implementation(libs.slf4j.api)
  implementation(libs.jtidy)

  runtimeOnly (libs.saxon.he)
  runtimeOnly (libs.logback.classic)
  runtimeOnly (libs.logback.core)

  testImplementation (libs.junit)
  testImplementation (libs.powermock.api.mockito)
  testImplementation (libs.powermock.module.junit4)
  testImplementation (libs.xmlunit)
  testImplementation (libs.xmlunit.core)
  testImplementation (libs.xmlunit.matchers)

  testImplementation(libs.jetbrains.annotations)
}
