package org.akhikhl.gradle.liftup

import org.gradle.api.GradleException
import org.gradle.api.Project

final class ProjectConfigurer {

  private static final Closure defaultModelAction = { model, project ->
    model.common?.call(project)
  }

  private final Project project
  private final EclipseConfig defaultConfig
  private final String eclipseVersion

  ProjectConfigurer(Project project) {
    this.project = project
    defaultConfig = new EclipseConfig()
    defaultConfig.loadFromResourceFile('defaultEclipseConfig.groovy')
    if(project.hasProperty('eclipseVersion'))
      // project properties are inherently hierarchical, so parent's eclipseVersion will be inherited
      eclipseVersion = project.eclipseVersion
    else {
      Project p = ProjectUtils.findUpAncestorChain(project, { it.extensions.findByName('eclipse')?.defaultVersion != null })
      eclipseVersion = p != null ? p.eclipse.defaultVersion : defaultConfig.defaultVersion
    }
  }

  void configure(String modelName, Closure modelAction = defaultModelAction) {

    def applyModels = { EclipseConfig eclipseConfig ->
      EclipseVersionConfig versionConfig = eclipseConfig.versionConfigs[eclipseVersion]
      if(versionConfig != null) {
        if(versionConfig.eclipseGroup != null)
          project.ext.eclipseGroup = versionConfig.eclipseGroup
        def modelConfigs = versionConfig.modelConfigs[modelName]
        modelConfigs?.each { EclipseModelConfig modelConfig ->
          modelConfig.properties.each { key, value ->
            if(value instanceof Closure) {
              value.delegate = PlatformConfig
              value.resolveStrategy = Closure.DELEGATE_FIRST
            }
          }
          modelAction(modelConfig, project)
        }
      }
    }

    applyModels(defaultConfig)

    ProjectUtils.withAllAncestors(project).each { Project p ->
      EclipseConfig config = p.extensions.findByName('eclipse')
      if(config)
        applyModels(config)
    }
  }
}
