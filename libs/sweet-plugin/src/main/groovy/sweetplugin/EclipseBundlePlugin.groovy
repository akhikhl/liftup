package sweetplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class EclipseBundlePlugin implements Plugin<Project> {

  void apply(final Project project) {

    project.apply plugin: 'osgi'

    EclipseConfig.addEclipseBundleDependencies project

    project.ext { eclipseGroup = EclipseConfig.eclipseGroup }

    project.afterEvaluate { ManifestUtils.extendManifest project }
  }
}
