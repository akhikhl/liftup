package org.akhikhl.gradle.liftup

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*

class SwtAppPlugin implements Plugin<Project> {

  private static final launchers = [ 'linux' : 'shell', 'windows' : 'windows' ]

  void apply(final Project project) {

    project.apply plugin: 'onejar'

    project.extensions.create('eclipse', EclipseConfig)
    project.extensions.create('swtapp', SwtAppPluginExtension)

    // we use onejar hook, because we need to populate onejar config
    // before onejar starts to generate products.
    project.onejar.beforeProductGeneration {

      def configurer = new ProjectConfigurer(project)
      configurer.configure('swtapp')

      PlatformConfig.supported_oses.each { platform ->
        PlatformConfig.supported_archs.each { arch ->
          String configName = "product_swt_${platform}_${arch}"
          def config = project.configurations.create(configName)
          configurer.configure('swtapp', { model, proj -> model.platformSpecific?.call(proj, configName, platform, arch) })
          //EclipseHelpers.addSwtAppDependencies project, configName, platform, arch
          PlatformConfig.supported_languages.each { language ->
            String localizedConfigName = "product_swt_${platform}_${arch}_${language}"
            def localizedConfig = project.configurations.create(localizedConfigName)
            localizedConfig.extendsFrom config
            configurer.configure('swtapp', { model, proj -> model.platformAndLanguageSpecific?.call(proj, localizedConfigName, platform, arch, language) })
            //EclipseHelpers.addSwtAppDependencies project, localizedConfigName, platform, arch, language
          }
        }
      }

      def products = project.swtapp.products ?: [[]]

      products.each { product ->
        def platform = product.platform ?: PlatformConfig.current_os
        def arch = product.arch ?: PlatformConfig.current_arch
        def language = product.language ?: ''
        if(language)
          project.onejar.product name: "swt_${platform}_${arch}_${language}", launcher: launchers[platform], suffix: "${platform}-${arch}-${language}", platform: platform, arch: arch, language: language
        else
          project.onejar.product name: "swt_${platform}_${arch}", launcher: launchers[platform], suffix: "${platform}-${arch}", platform: platform, arch: arch
      }
    }
  }
}
