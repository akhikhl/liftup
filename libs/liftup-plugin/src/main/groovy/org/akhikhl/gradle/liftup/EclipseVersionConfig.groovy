package org.akhikhl.gradle.liftup

class EclipseVersionConfig {

  String eclipseGroup

  Map modelConfigs = [:]

  def methodMissing(String modelName, args) {
    if(modelConfigs[modelName] == null)
      modelConfigs[modelName] = []
    args.each {
      if(it instanceof Closure)
        modelConfigs[modelName].add new EclipseModelConfig(common: it)
      else if (it instanceof Map)
        modelConfigs[modelName].add new EclipseModelConfig(common: it.common, platformSpecific: it.platformSpecific, platformAndLanguageSpecific: it.platformAndLanguageSpecific)
    }
  }
}

