package org.akhikhl.gradle.liftup

class EclipseConfig {

  String defaultVersion

  Map versionConfigs = [:]

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
    if(versionConfigs[versionString] == null)
      versionConfigs[versionString] = new EclipseVersionConfig()
    versionDef.resolveStrategy = Closure.DELEGATE_FIRST
    versionDef.delegate = versionConfigs[versionString]
    versionDef()
  }
}

