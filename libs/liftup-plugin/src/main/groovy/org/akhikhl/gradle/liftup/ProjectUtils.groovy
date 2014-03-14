package org.akhikhl.gradle.liftup

import org.gradle.api.Project

final class ProjectUtils {

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

