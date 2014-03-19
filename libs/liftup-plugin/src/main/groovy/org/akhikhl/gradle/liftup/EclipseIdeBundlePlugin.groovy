package org.akhikhl.gradle.liftup

import org.gradle.api.Plugin
import org.gradle.api.Project

class EclipseIdeBundlePlugin implements Plugin<Project> {

  void apply(final Project project) {
    project.apply plugin: 'osgi'
    project.extensions.create('eclipse', EclipseConfig)
    project.afterEvaluate {
      new ProjectConfigurer(project).configure('eclipseBundle')
      EclipseHelpers.addEclipseIdeDependencies project
      TaskUtils.defineEclipseBundleTasks project
    }
  }
}
