package org.akhikhl.gradle.liftup

import org.gradle.api.Plugin
import org.gradle.api.Project

class EclipseBundlePlugin implements Plugin<Project> {

  void apply(final Project project) {

    project.apply plugin: 'osgi'

    EclipseHelpers.addEclipseBundleDependencies project

    project.configurations {
      privateLib
      compile.extendsFrom privateLib
    }

    project.ext { eclipseGroup = EclipseHelpers.eclipseGroup }

    project.afterEvaluate {
      TaskUtils.defineEclipseBundleTasks project
    }
  }
}
