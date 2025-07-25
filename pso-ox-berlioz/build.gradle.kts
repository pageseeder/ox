// Dependencies of the project
dependencies {
  compileOnly(libs.jetbrains.annotations)


  // Module dependencies
  implementation(project(":pso-ox-core"))
  implementation(libs.pso.berlioz)
  implementation(libs.pso.xmlwriter)
  implementation(libs.slf4j.api)
  implementation(libs.commons.io)
  implementation(libs.commons.fileupload)
  implementation(libs.javax.activation)
  implementation(libs.javax.servlet)

  runtimeOnly (libs.saxon.he)
  runtimeOnly (libs.logback.classic)
  runtimeOnly (libs.logback.core)

  testImplementation (libs.junit)
}
