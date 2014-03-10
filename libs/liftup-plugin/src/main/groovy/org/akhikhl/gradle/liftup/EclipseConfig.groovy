package org.akhikhl.gradle.liftup

final class EclipseConfig {

  String defaultVersion

  Map versions = [:]

  void loadFromResourceFile(String configFileName) {
    Binding binding = new Binding()
    def self = this
    binding.eclipse = { Closure closure ->
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure.delegate = self
      closure()
    }
    GroovyShell shell = new GroovyShell(binding)
    this.getClass().getClassLoader().getResourceAsStream(configFileName).withReader('UTF-8') {
      shell.evaluate(it)
    }
  }

  void version(String versionString, Closure versionDef) {
    versions[versionString] = versionDef
  }
}

