package swtapp

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*
import org.gradle.util.GFileUtils

import osgi2maven.bundleImport.*
import setuphelpers.*

class SwtAppPlugin implements Plugin<Project> {

  void apply(final Project project) {

    File swtDir = new File(project.buildDir, "swtDependencies")
    if(!swtDir.exists())
      swtDir = new File(project.rootProject.buildDir, "swtDependencies")

    BundleReader bundleReader = new BundleReader();
    bundleReader.group = "eclipse-juno"

    String lang = project.hasProperty("language") ? project.language : "en";

    project.configurations {
      onejar
    }

    project.dependencies {
      onejar "com.simontuffs:one-jar-ant-task:0.97"
    }

    project.tasks.build << {
      if(project.tasks.build.dependsOnTaskDidWork() || project.tasks.assemble.dependsOnTaskDidWork()) {
        ant.taskdef(name: 'onejar', classname: "com.simontuffs.onejar.ant.OneJarTask", classpath: project.configurations.onejar.asPath)
        def swtFiles = new HashSet()
        File[] files = new File(swtDir, "swt_impl").listFiles({ File file -> file.getName().endsWith(".jar") } as FileFilter)
        if(files)
          files.each { File file -> swtFiles.add file.name }
        for(os in [ "win", "linux" ])
          for(arch in [ "x86_32", "x86_amd_64" ]) {
            String config = "swt_impl_${os}_${arch}"
            files = new File(swtDir, config).listFiles({ File file -> file.getName().endsWith(".jar") } as FileFilter)
            if(files)
              files.each { File file -> swtFiles.add file.name }
          }
        for(os in [ "win", "linux" ])
          for(arch in [ "x86_32", "x86_amd_64" ]) {
            String destFile = "${project.buildDir}/swtapp/${project.name}-${project.version}_${os}_${arch}.jar"
            String config = "swt_impl_${os}_${arch}"
            ant.onejar(destFile: destFile) {
              main(jar: project.tasks.jar.archivePath.toString())
              manifest {
                attribute(name: "Built-By", value: System.getProperty("user.name"))
              }
              lib {
                project.configurations.runtime.each { File file ->
                  if(!swtFiles.contains(file.name))
                    fileset(file: file)
                }
                fileset(dir: "$swtDir/swt_impl")
                fileset(dir: "$swtDir/$config")
              }
            }
            String launchScriptSuffix = os == "win" ? "bat" : "sh"
            File launchScriptFile = new File("${project.buildDir}/swtapp/${project.name}-${project.version}_${os}_${arch}.${launchScriptSuffix}")
            if(os == "win")
              launchScriptFile.text = "@java -jar ${project.name}-${project.version}_${os}_${arch}.jar"
            else {
              launchScriptFile.text = "#!/bin/bash\njava -jar ${project.name}-${project.version}_${os}_${arch}.jar"
              launchScriptFile.setExecutable(true)
            }
            project.logger.info "Created swt application: $destFile"
          }
      }
    }
  }
}
