package org.akhikhl.gradle.knyte

import org.gradle.api.Plugin
import org.gradle.api.Project

class SwtLibPlugin implements Plugin<Project> {

  void apply(final Project project) {

    EclipseConfig.addSwtLibDependencies project

    project.ext { eclipseGroup = EclipseConfig.eclipseGroup }
  }
}
