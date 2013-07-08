package sweetplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class EclipseIdeAppPlugin implements Plugin<Project> {

  private static final launchers = [ "linux" : "shell", "windows" : "windows" ]

  private static String getEclipseIntroId(Project project) {
    String result
    project.sourceSets.main.resources.srcDirs.each { File srcDir ->
      File pluginConfigFile = new File(srcDir, 'plugin.xml')
      if(pluginConfigFile.exists()) {
        def pluginConfig = new XmlParser().parse(pluginConfigFile)
        result = pluginConfig.extension.find({ it.'@point' == 'org.eclipse.ui.intro' })?.intro?.'@id'
      }
    }
    if(result)
      result = "${project.name}.$result"
    return result
  }

  void apply(final Project project) {

    project.apply plugin: 'eclipse-equinox-app'

    project.extensions.create('eclipseIde', EclipseIdeAppPluginExtension)

    EclipseConfig.addRcpDependencies project
    EclipseConfig.addEclipseIdeDependencies project

    EclipseConfig.createRcpConfigurations project
    EclipseConfig.createEclipseIdeConfigurations project

    project.equinox.beforeProductGeneration {

      project.jar {
        manifest {
          if(getEclipseIntroId(project))
            instruction 'Import-Package', 'org.eclipse.ui.intro.config'
        }
      }

      project.eclipseIde.products.each { product ->
        String platform = product.platform ?: PlatformConfig.current_os
        String arch = product.arch ?: PlatformConfig.current_arch
        String language = product.language ?: ''
        if(language)
          project.equinox.product name: "eclipse_ide_${platform}_${arch}_$language", launcher: launchers[platform], suffix: "${platform}-${arch}-${language}", platform: platform, arch: arch, language: language
        else
          project.equinox.product name: "eclipse_ide_${platform}_${arch}", launcher: launchers[platform], suffix: "${platform}-${arch}", platform: platform, arch: arch
      }

      project.equinox.archiveProducts = project.eclipseIde.archiveProducts
      project.equinox.launchParameters = project.eclipseIde.launchParameters
    }
  }
}
