package org.akhikhl.gradle.liftup

import org.gradle.api.Plugin
import org.gradle.api.Project

class SwtLibPlugin implements Plugin<Project> {

  void apply(final Project project) {
    project.extensions.create('eclipse', EclipseConfig)
    project.afterEvaluate {
      new ProjectConfigurer(project).configure('swtlib')
    }
  }
}
