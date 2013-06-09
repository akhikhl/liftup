package sweetplugin

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*

class SweetAppPlugin implements Plugin<Project> {

  private def launchers = [ "linux" : "shell", "windows" : "windows" ]

  void apply(final Project project) {

    project.apply plugin: "onejar"

    project.extensions.create("sweetapp", SweetAppPluginExtension)

    // we import eclipse settings here, so that user may override particular settings in project config
    new EclipseConfig().applyToProject project

    // we use onejar hook, because we need to populate onejar config
    // before onejar starts to generate products.
    project.onejar.beforeProductGeneration {

      project.dependencies {
        compile "${project.eclipseGroup}:org.eclipse.swt:${project.swt_version}"
        compile "${project.eclipseGroup}:org.eclipse.jface:${project.jface_version}"
        compile "${project.eclipseGroup}:org.eclipse.swt.${project.current_os_suffix}.${project.current_arch_suffix}:${project.swt_version}"
      }

      def products = project.sweetapp.products ?: [[]]

      products.each { product ->
        def platform = product.platform ?: project.current_os
        def arch = product.arch ?: project.current_arch
        def language = product.language ?: ""
        def configName = "swt_${platform}_${arch}"
        if(language)
          configName += "_$language"
        project.configurations.create configName
        project.dependencies.add configName, "${project.eclipseGroup}:org.eclipse.swt.${project.map_os_to_suffix[platform]}.${project.map_arch_to_suffix[arch]}:${project.swt_version}"
        if(language) {
          project.dependencies.add configName, "${project.eclipseGroup}:org.eclipse.swt.nl_${language}:+"
          project.dependencies.add configName, "${project.eclipseGroup}:org.eclipse.jface.nl_${language}:+"
          project.dependencies.add configName, "${project.eclipseGroup}:org.eclipse.swt.${project.current_os_suffix}.${project.current_arch_suffix}.nl_${language}:+"
          project.dependencies.add configName, "${project.eclipseGroup}:org.eclipse.swt.${project.map_os_to_suffix[platform]}.${project.map_arch_to_suffix[arch]}.nl_${language}:+"
          project.onejar.product name: configName, launcher: launchers[platform], suffix: "${platform}-${arch}-${language}", platform: platform, arch: arch, language: language
        } else
          project.onejar.product name: configName, launcher: launchers[platform], suffix: "${platform}-${arch}", platform: platform, arch: arch
      }
    }
  }
}
