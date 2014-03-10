package org.akhikhl.gradle.liftup

import org.gradle.api.GradleException
import org.gradle.api.Project

final class ProjectConfigurer {

  static void configure(Project project, String configName) {

    EclipseConfig defaultConfig = new EclipseConfig()
    defaultConfig.loadFromResourceFile('defaultEclipseConfig.groovy')

    String eclipseVersion
    if(project.hasProperty('eclipseVersion'))
      // project properties are inherently hierarchical, so parent's eclipseVersion will be inherited
      eclipseVersion = project.eclipseVersion
    else {
      Project p = findUpAncestorChain(project, { it.extensions.findByName('eclipse')?.defaultVersion != null })
      eclipseVersion = p != null ? p.eclipse.defaultVersion : defaultConfig.defaultVersion
    }

    def applyEclipseConfig = { EclipseConfig eclipseConfig ->
      EclipseVersionConfig versionConfig = eclipseConfig.versionConfigs[eclipseVersion]
      if(versionConfig != null) {
        if(versionConfig.eclipseGroup != null)
          project.ext.eclipseGroup = versionConfig.eclipseGroup
        def projectConfigs = versionConfig.projectConfigs[configName]
        projectConfigs?.each { Closure projectConfig ->
          projectConfig.resolveStrategy = Closure.DELEGATE_FIRST
          projectConfig.delegate = PlatformConfig
          projectConfig(project)
        }
      }
    }

    applyEclipseConfig(defaultConfig)

    withAllAncestors(project).each { Project p ->
      EclipseConfig config = p.extensions.findByName('eclipse')
      if(config)
        applyEclipseConfig(config)
    }
  }

  private static findUpAncestorChain(Project project, Closure condition) {
    Project p = project
    while(p != null && !condition(p))
      p = p.parent
    return p
  }

  private static List withAllAncestors(Project project) {
    List projects = []
    Project p = project
    while(p != null) {
      projects.add(0, p)
      p = p.parent
    }
    return projects
  }
}
