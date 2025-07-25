description = "Contains some helpful classes for test ox."

// Dependencies of the project
dependencies {


  implementation(project(":pso-ox-core"))
  implementation(project(":pso-ox-berlioz"))

  implementation(libs.pso.berlioz)
  implementation(libs.pso.xmlwriter)

  implementation(libs.slf4j.api)
  implementation(libs.junit)
  implementation (libs.xmlunit.core)
  implementation (libs.xmlunit.matchers)
  implementation (libs.hamcrest.java)
  implementation (libs.hancrest.junit)
  implementation (libs.mockito.all)
  implementation (libs.powermock.mockito.release.full)

  compileOnly(libs.jetbrains.annotations)
  compileOnly(libs.javax.servlet)

  runtimeOnly (libs.saxon.he)
  runtimeOnly (libs.logback.classic)
  runtimeOnly (libs.logback.core)

  testImplementation(libs.jetbrains.annotations)
  testImplementation(libs.javax.servlet)
  testImplementation(project(":pso-ox-psml"))
  testImplementation(project(":pso-ox-schematron"))
}
