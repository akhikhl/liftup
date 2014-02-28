package org.akhikhl.gradle.knyte

import org.gradle.api.Plugin
import org.gradle.api.Project

class EclipseBundlePlugin implements Plugin<Project> {

  void apply(final Project project) {

    project.apply plugin: 'osgi'

    EclipseConfig.addEclipseBundleDependencies project

    project.configurations {
      privateLib
      compile.extendsFrom privateLib
    }

    project.ext { eclipseGroup = EclipseConfig.eclipseGroup }

    project.afterEvaluate {
      TaskUtils.defineAdditionalTasks project
    }
  }
}
