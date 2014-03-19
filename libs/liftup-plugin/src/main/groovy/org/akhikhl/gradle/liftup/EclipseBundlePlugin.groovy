package org.akhikhl.gradle.liftup

import org.gradle.api.Plugin
import org.gradle.api.Project

class EclipseBundlePlugin implements Plugin<Project> {

  void apply(final Project project) {
    project.apply plugin: 'osgi'
    project.extensions.create('eclipse', EclipseConfig)
    def configurer = new ProjectConfigurer(project)
    configurer.preConfigure('eclipseBundle')
    project.afterEvaluate {
      configurer.configure('eclipseBundle')
      TaskUtils.defineEclipseBundleTasks project
    }
  }
}
