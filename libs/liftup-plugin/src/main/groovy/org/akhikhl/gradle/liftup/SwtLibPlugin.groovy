package org.akhikhl.gradle.liftup

import org.gradle.api.Plugin
import org.gradle.api.Project

class SwtLibPlugin implements Plugin<Project> {

  void apply(final Project project) {

    EclipseHelpers.addSwtLibDependencies project

    project.ext { eclipseGroup = EclipseHelpers.eclipseGroup }
  }
}
