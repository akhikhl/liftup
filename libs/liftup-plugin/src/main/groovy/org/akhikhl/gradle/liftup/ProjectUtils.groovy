package org.akhikhl.gradle.liftup

import java.nio.file.Paths

import org.gradle.api.Project

final class ProjectUtils {

  static String findBundleActivator(Project project) {
    String activator
    project.sourceSets.main.allSource.srcDirs.findResult { File srcDir ->
      project.fileTree(srcDir).include('**/Activator.groovy').files.findResult { File activatorSourceFile ->
        activator = Paths.get(srcDir.absolutePath).relativize(Paths.get(activatorSourceFile.absolutePath)).toString()
        activator = activator.substring(0, activator.length() - 7) // remove '.groovy' file extension
        activator = activator.replaceAll('/', '.') // convert to package.class
      }
      if(!activator)
        project.fileTree(srcDir).include('**/Activator.java').files.findResult { File activatorSourceFile ->
          activator = Paths.get(srcDir.absolutePath).relativize(Paths.get(activatorSourceFile.absolutePath)).toString()
          activator = activator.substring(0, activator.length() - 5) // remove '.java' file extension
          activator = activator.replaceAll('/', '.') // convert to package.class
        }
      activator
    }
  }

  static findPluginConfig(Project project) {
    def pluginFile = findPluginConfigFile(project)
    pluginFile ? new XmlParser().parse(pluginFile) : null
  }

  static findPluginConfigFile(Project project) {
    project.sourceSets.main.resources.srcDirs.findResult { File srcDir ->
      def pluginFile = new File(srcDir, 'plugin.xml')
      pluginFile.exists() ? pluginFile : null
    }
  }

  static findPluginLocalizationFiles(Project project) {
    project.sourceSets.main.resources.srcDirs.findResult { File srcDir ->
      if(srcDir.exists()) {
        File locatizationDir = new File(srcDir, 'OSGI-INF/l10n')
        // if locatizationDir exists, it will be used by OSGi automatically
        // and there's no need for Bundle-Localization
        if(!locatizationDir.exists()) {
          def localizationFiles = new FileNameFinder().getFileNames(srcDir.absolutePath, 'plugin*.properties')
          if(localizationFiles)
            return localizationFiles
        }
      }
      null
    }
  }

  static findUpAncestorChain(Project project, Closure condition) {
    Project p = project
    while(p != null && !condition(p))
      p = p.parent
    return p
  }

  static List withAllAncestors(Project project) {
    List projects = []
    Project p = project
    while(p != null) {
      projects.add(0, p)
      p = p.parent
    }
    return projects
  }
}

