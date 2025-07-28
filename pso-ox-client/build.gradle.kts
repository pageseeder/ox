description = "OX Client"

// Dependencies of the project
dependencies {
  compileOnly(libs.jetbrains.annotations)

  // module dependencies
  implementation(project(":pso-ox-core"))
  implementation(libs.pso.bastille)
  implementation(libs.pso.berlioz)
  implementation(libs.pso.cobble)
  implementation(libs.pso.xmlwriter)
  implementation(libs.slf4j.api)
  implementation(libs.commons.io)
  implementation(libs.jakarta.xml.bind.api)
  implementation(libs.javax.servlet)
  implementation(libs.jaxws.api)
  implementation(libs.jaxb.runtime)

  runtimeOnly (libs.saxon.he)
  runtimeOnly (libs.logback.classic)
  runtimeOnly (libs.logback.core)

  testImplementation (libs.junit)
  testImplementation (libs.xmlunit)
}
