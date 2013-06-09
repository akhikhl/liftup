package sweetplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class SweetLibPlugin implements Plugin<Project> {

  void apply(final Project project) {

    // we import eclipse settings here, so that user may override particular settings in project config
    new EclipseConfig().applyToProject project

    project.dependencies {
      compile "${project.eclipseGroup}:org.eclipse.swt:${project.swt_version}"
      compile "${project.eclipseGroup}:org.eclipse.jface:${project.jface_version}"
      compile "${project.eclipseGroup}:org.eclipse.swt.${project.current_os_suffix}.${project.current_arch_suffix}:${project.swt_version}"
    }
  }
}
