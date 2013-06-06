package swtdeps

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*
import org.gradle.util.GFileUtils
import org.apache.commons.codec.digest.DigestUtils

import osgi2maven.bundleImport.*
import setuphelpers.*

class SwtDepsPlugin implements Plugin<Project> {

  void apply(final Project project) {

    def eclipseConfigFilePath = "${project.buildDir}/eclipseConfig.gradle"
    def eclipseConfigFile = new File(eclipseConfigFilePath)
    
    def oldDigest;
    if(eclipseConfigFile.exists())
      eclipseConfigFile.withInputStream { ins ->  
        oldDigest = DigestUtils.md5Hex(ins);
      }

    def newDigest = DigestUtils.md5Hex(SwtDepsPlugin.class.getClassLoader().getResourceAsStream("eclipseConfig.gradle"))
    
    if(newDigest != oldDigest) {
      eclipseConfigFile.parentFile.mkdirs()      
      eclipseConfigFile.withOutputStream { os ->  
        os << SwtDepsPlugin.class.getClassLoader().getResourceAsStream("eclipseConfig.gradle")
      }
    }
      
    project.apply from: eclipseConfigFilePath
    
    project.configurations {
      swt_impl
    }

    project.dependencies {
      swt_impl "eclipse-juno:org.eclipse.swt:$project.swt_version"
      swt_impl "eclipse-juno:org.eclipse.jface:$project.jface_version"
    }
    
    project.task("collectSwtDependencies") {
      inputs.file eclipseConfigFile
      outputs.dir "${project.buildDir}/swtDependencies"
      doLast {
        project.copy {
          from project.configurations.swt_impl
          into "${project.buildDir}/swtDependencies/swt_impl"
        }
        for(os in [ "win", "linux" ])
          for(arch in [ "x86_32", "x86_amd_64" ]) {
            def config = "swt_impl_${os}_${arch}"
            project.configurations.create config
            project.dependencies.add config, "eclipse-juno:org.eclipse.swt.${project.map_os_to_suffix[os]}.${project.map_arch_to_suffix[arch]}:$project.swt_version"
            project.copy {
              from project.configurations[config]
              into "${project.buildDir}/swtDependencies/${config}"
            }
          }
      }
    }
    
    if(project.tasks.findByName("build") == null)
      project.task("build")
      
    project.tasks.build {
      doFirst {
        project.tasks.collectSwtDependencies.execute();
      }
    }    
  }
}
