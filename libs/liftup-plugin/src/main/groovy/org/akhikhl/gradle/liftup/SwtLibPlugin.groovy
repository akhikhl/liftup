package org.akhikhl.gradle.liftup

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class SwtLibPlugin implements Plugin<Project> {

  void apply(final Project project) {

    project.extensions.create('eclipse', EclipseConfig)
    project.eclipse.loadFromResourceFile('config.groovy')

    project.afterEvaluate {
      def eclipseVersion = project.hasProperty('eclipseVersion') ? project.eclipseVersion : project.eclipse.defaultVersion
      def versionClosure = project.eclipse.versions[eclipseVersion]
      if(versionClosure == null)
        throw new GradleException("Eclipse version '$eclipseVersion' is not defined.")
    }

    EclipseHelpers.addSwtLibDependencies project

    project.ext { eclipseGroup = EclipseHelpers.eclipseGroup }
  }
}
