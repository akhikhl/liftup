package org.akhikhl.gradle.liftup

import org.gradle.api.Plugin
import org.gradle.api.Project

class EclipseConfigPlugin implements Plugin<Project> {

  void apply(final Project project) {
    // configuration is created, but not applied to this project
    project.extensions.create('eclipse', EclipseConfig)
  }
}
