plugins {
  id("java-library")
  id("maven-publish")
  alias(libs.plugins.jreleaser)
}

val title: String by project
val gitName: String by project
val website: String by project
val globalVersion = file("version.txt").readText().trim()

subprojects {
  group = "org.pageseeder.ox"
  version = globalVersion
  description = "A Java library designed to simplify document conversion and data analysis, especially for XML, DOCX, and PDF formats.."

  apply(plugin = "java-library")
  apply(plugin = "maven-publish")

  java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain {
      languageVersion.set(JavaLanguageVersion.of(11))
    }
    withJavadocJar()
    withSourcesJar()
  }

  repositories {
    //remove this repository after migrating the modulo pageseeder to another project psn
    maven {
      url = uri("https://gitlab.allette.com.au/api/v4/projects/276/packages/maven")
      credentials(HttpHeaderCredentials::class) {
        name = "Private-Token"
        value = project.findProperty("gitlabPrivateToken") as? String ?: ""
      }
      authentication {
        create<HttpHeaderAuthentication>("header")
      }
    }

    mavenCentral {
      url = uri("https://maven-central.storage.googleapis.com/maven2")
    }
    maven {
      url = uri("https://s01.oss.sonatype.org/content/groups/public/")
    }


    //to fetch ehcache version 2.11.0.2.10
    maven {
      url = uri("https://repo.terracotta.org/maven2/")
    }
  }

  tasks.test {
    //useJUnitPlatform() //Junit 5
    useJUnit() //JUnit 4
  }

  tasks.jar {
    manifest {
      attributes(
        "Implementation-Title" to project.name,
        "Implementation-Version" to project.version,
        "Implementation-Vendor" to "Allette Systems",
        "Specification-Title" to project.description,
        "Specification-Version" to project.version,
        "Specification-Vendor" to "Allette Systems",
        "Created-By" to "Gradle ${gradle.gradleVersion}",
        "Built-By" to System.getProperty("user.name"),
        "Build-Jdk" to System.getProperty("java.version")
      )
    }
  }

  publishing {
    publications {
      create<MavenPublication>("maven") {
        from(components["java"])
        groupId = group.toString()
        pom {
          name.set(title)
          description.set(project.description)
          url.set(website)
          licenses {
            license {
              name.set("The Apache Software License, Version 2.0")
              url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
          }
          organization {
            name.set("Allette Systems")
            url.set("https://www.allette.com.au")
          }
          scm {
            url.set("git@github.com:pageseeder/${gitName}.git")
            connection.set("scm:git:git@github.com:pageseeder/${gitName}.git")
            developerConnection.set("scm:git:git@github.com:pageseeder/${gitName}.git")
          }
          developers {
            developer {
              name.set("Christophe Lauret")
              email.set("clauret@weborganic.com")
            }
            developer {
              name.set("Jean Baptiste")
              name.set("jbreure@weborganic.com")
            }
            developer {
              name.set("Alberto Santos")
              name.set("asantos@allette.com.au")
            }
            developer {
              name.set("Carlos Cabral")
              name.set("ccabral@allette.com.au")
            }
          }
        }
      }
    }
    repositories {
      maven {
        url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
      }
    }
  }
}

jreleaser {
  configFile.set(file("jreleaser.toml"))
}

tasks.wrapper {
  gradleVersion = "8.14"
  distributionType = Wrapper.DistributionType.BIN
}