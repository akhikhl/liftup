package org.akhikhl.gradle.liftup

import org.gradle.api.Plugin
import org.gradle.api.Project

class SwtLibPlugin implements Plugin<Project> {

  void apply(final Project project) {
    project.extensions.create('eclipse', EclipseConfig)
    def configurer = new ProjectConfigurer(project)
    configurer.preConfigure('swtlib')
    project.afterEvaluate {
      configurer.configure('swtlib')
    }
  }
}
