package org.akhikhl.gradle.liftup

class EclipseVersionConfig {

  String eclipseGroup

  // project type to list of closures, each closure represents injector
  Map projectConfigs = [:]

  def methodMissing(String configName, args) {
    if(projectConfigs[configName] == null)
      projectConfigs[configName] = []
    args.each {
      projectConfigs[configName].add it
    }
  }
}

