package swt

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*
import org.apache.commons.codec.digest.DigestUtils

class SwtPlugin implements Plugin<Project> {

  void apply(final Project project) {

    def eclipseConfigFilePath = "${project.buildDir}/eclipseConfig.gradle"
    def eclipseConfigFile = new File(eclipseConfigFilePath)
    
    def oldDigest;
    if(eclipseConfigFile.exists())
      eclipseConfigFile.withInputStream { ins ->  
        oldDigest = DigestUtils.md5Hex(ins);
      }

    def newDigest = DigestUtils.md5Hex(SwtPlugin.class.getClassLoader().getResourceAsStream("eclipseConfig.gradle"))
    
    if(newDigest != oldDigest) {
      eclipseConfigFile.parentFile.mkdirs()      
      eclipseConfigFile.withOutputStream { os ->  
        os << SwtPlugin.class.getClassLoader().getResourceAsStream("eclipseConfig.gradle")
      }
    }
      
    project.apply from: eclipseConfigFilePath

    def lang = project.hasProperty("language") ? project.language : "en";
    
    project.logger.info "current_os=${project.current_os}, current_arch=${project.current_arch}"

    def swt = [
      [ group: "eclipse-juno", name: "org.eclipse.swt", version: project.swt_version ],
      [ group: "eclipse-juno", name: "org.eclipse.jface", version: project.jface_version ],
      [ group: "eclipse-juno", name: "org.eclipse.swt.${project.map_os_to_suffix[project.current_os]}.${project.map_arch_to_suffix[project.current_arch]}", version: project.swt_version ]
    ]

    if(lang != "en")
      swt += swt.collect { lib -> [ group: lib.group, name: lib.name + ".nl_" + lang, version: project.swt_lang_version ] }

    for(lib in swt)
      project.dependencies.add "compile", lib
  }
}
