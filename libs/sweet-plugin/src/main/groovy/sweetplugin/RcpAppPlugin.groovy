package sweetplugin

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*

class RcpAppPlugin implements Plugin<Project> {

  private static final launchers = [ "linux" : "shell", "windows" : "windows" ]

  void apply(final Project project) {

    project.apply plugin: 'eclipse-equinox-app'

    project.extensions.create('rcp', RcpAppPluginExtension)

    EclipseConfig.addRcpDependencies project

    EclipseConfig.createRcpConfigurations project

    project.equinox.beforeProductGeneration {

      project.rcp.products.each { product ->
        String platform = product.platform ?: PlatformConfig.current_os
        String arch = product.arch ?: PlatformConfig.current_arch
        String language = product.language ?: ''
        def archiveFiles = []
        if(product.archiveFile)
          archiveFiles.add product.archiveFile
        if(product.archiveFiles)
          archiveFiles.addAll product.archiveFiles
        if(language)
          project.equinox.product name: "rcp_${platform}_${arch}_$language", launcher: launchers[platform], suffix: "${platform}-${arch}-${language}", platform: platform, arch: arch, language: language, jre: product.jre, archiveFiles: archiveFiles
        else
          project.equinox.product name: "rcp_${platform}_${arch}", launcher: launchers[platform], suffix: "${platform}-${arch}", platform: platform, arch: arch, jre: product.jre, archiveFiles: archiveFiles
      }

      project.equinox.archiveProducts = project.rcp.archiveProducts
      project.equinox.additionalFilesToArchive = project.rcp.additionalFilesToArchive
      project.equinox.launchParameters = project.rcp.launchParameters
    }
  }
}
