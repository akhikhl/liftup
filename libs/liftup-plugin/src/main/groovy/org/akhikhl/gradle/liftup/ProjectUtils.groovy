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

