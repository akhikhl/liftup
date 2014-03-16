package org.akhikhl.gradle.liftup

import java.nio.file.Paths

import org.gradle.api.Project

final class ProjectUtils {

  /**
   * Finds bundle activator.
   *
   * @param project project being analyzed, not modified.
   * @return qualified name (package.class) of the bundle activator, if present.
   */
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

  /**
   * Finds eclipse plugin configuration file, 'plugin.xml'.
   *
   * @param project project being analyzed, not modified.
   * @return java.io.File, pointing to 'plugin.xml', or null, if configuration file does not exist.
   */
  static File findPluginConfigFile(Project project) {
    ([project.projectDir] + project.sourceSets.main.resources.srcDirs).findResult { File dir ->
      File pluginFile = new File(dir, 'plugin.xml')
      pluginFile.exists() ? pluginFile : null
    }
  }

  /**
   * Finds eclipse plugin configuration localization files, 'plugin*.properties'.
   *
   * @param project project being analyzed, not modified.
   * @return list of strings (absolute paths) to plugin configuration localization files
   * or null, if no localization files are found.
   */
  static List<String> findPluginLocalizationFiles(Project project) {
    ([project.projectDir] + project.sourceSets.main.resources.srcDirs).findResult { File dir ->
      if(dir.exists()) {
        File locatizationDir = new File(dir, 'OSGI-INF/l10n')
        // if locatizationDir exists, it will be used by OSGi automatically
        // and there's no need for Bundle-Localization
        if(!locatizationDir.exists()) {
          List<String> localizationFiles = new FileNameFinder().getFileNames(dir.absolutePath, 'plugin*.properties')
          if(localizationFiles)
            return localizationFiles
        }
      }
      null
    }
  }

  /**
   * Finds a first project satisfying the given condition in the given ancestor chain.
   *
   * @param project project being analyzed, not modified.
   * @param condition closure which is repeatedly called against every project in the ancestor chain.
   *   If closure returns "truthy" value, the condition is satisfied and iteration breaks.
   * @return first project satisfying the given condition.
   */
  static Project findUpAncestorChain(Project project, Closure condition) {
    Project p = project
    while(p != null && !condition(p))
      p = p.parent
    return p
  }

  /**
   * Returns the list of all ancestors + the given project.
   *
   * @param project project being analyzed, not modified.
   * @return list of projects, first element is root, last element is the given project.
   */
  static List<Project> withAllAncestors(Project project) {
    List<Project> projects = []
    Project p = project
    while(p != null) {
      projects.add(0, p)
      p = p.parent
    }
    return projects
  }
}
