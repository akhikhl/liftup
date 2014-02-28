package org.akhikhl.gradle.knyte

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.*

class TaskUtils {

  static void defineAdditionalTasks(Project project) {

    File manifestFile = new File("${project.buildDir}/tmp/manifests/extendedManifest/MANIFEST.MF")

    project.task('extendManifest') {
      dependsOn project.tasks.classes
      inputs.files { project.configurations.runtime }
      outputs.files manifestFile
      doLast {
        // fix for problem with non-existing classesDir, when the project contains no java/groovy sources
        // (resources-only project)
        project.sourceSets.main.output.classesDir.mkdirs()

        def m = project.osgiManifest {
          setName project.name
          setVersion project.version
          setClassesDir project.sourceSets.main.output.classesDir
          setClasspath (project.configurations.runtime - project.configurations.privateLib)
        }

        m = m.effectiveManifest

        project.sourceSets.main.allSource.srcDirs.each { File srcDir ->
          boolean foundActivator = false
          project.logger.info '{}: Checking for groovy activator', project.name
          project.fileTree(srcDir).include('**/Activator.groovy').files.each { File activatorSourceFile ->
            String activator = activatorSourceFile.absolutePath.substring(srcDir.absolutePath.length())
            if(activator.startsWith('/'))
              activator = activator.substring(1)
            activator = activator.substring(0, activator.length() - 7) // remove '.groovy' file extension
            activator = activator.replaceAll('/', '.')
            m.attributes['Bundle-Activator'] = activator
            m.attributes['Bundle-ActivationPolicy'] = 'lazy'
            project.logger.info '{}: Found groovy activator: {}', project.name, activator
            foundActivator = true
          }
          if(!foundActivator) {
            project.logger.info '{}: Checking for java activator', project.name
            project.fileTree(srcDir).include('**/Activator.java').files.each { File activatorSourceFile ->
              String activator = activatorSourceFile.absolutePath.substring(srcDir.absolutePath.length())
              if(activator.startsWith('/'))
                activator = activator.substring(1)
              activator = activator.substring(0, activator.length() - 5) // remove '.java' file extension
              activator = activator.replaceAll('/', '.')
              m.attributes['Bundle-Activator'] = activator
              m.attributes['Bundle-ActivationPolicy'] = 'lazy'
              project.logger.info '{}: Found java activator: {}', project.name, activator
            }
          }
        }

        def pluginConfig = null
        project.sourceSets.main.resources.srcDirs.each { File srcDir ->
          if(srcDir.exists()) {
            File locatizationDir = new File(srcDir, 'OSGI-INF/l10n')
            // if locatizationDir exists, it will be used by OSGi automatically
            // and there's no need for Bundle-Localization
            if(!locatizationDir.exists()) {
              def localizationFiles = new FileNameFinder().getFileNames(srcDir.absolutePath, 'plugin*.properties')
              if(localizationFiles)
                m.attributes['Bundle-Localization'] = 'plugin'
            }
            if(new File(srcDir, 'plugin.xml').exists())
              pluginConfig = new XmlParser().parse(new File(srcDir, 'plugin.xml'))
          }
        }

        if(pluginConfig) {
          Map importPackages = ManifestUtils.parsePackages(m.attributes['Import-Package'])
          m.attributes['Bundle-SymbolicName'] = "${project.name}; singleton:=true" as String
          project.logger.info 'Analyzing class usage in {}/plugin.xml', project.name
          def classes = pluginConfig.extension.'**'.findAll({ it.'@class' })*.'@class' + pluginConfig.extension.'**'.findAll({ it.'@contributorClass' })*.'@contributorClass'
          def packages = classes.collect { it.substring(0, it.lastIndexOf('.')) }.unique(false)
          packages.each { String packageName ->
            String packagePath = packageName.replaceAll(/\./, '/')
            if(project.sourceSets.main.resources.srcDirs.find { new File(it, packagePath).exists() })
              project.logger.info 'Found package {} within {}, no import needed', packageName, project.name
            else {
              project.logger.info 'Did not find package {} within {}, will be imported', packageName, project.name
              importPackages[packageName] = ''
            }
          }
          m.attributes['Import-Package'] = ManifestUtils.packagesToString(importPackages)
        }
        else
          m.attributes['Bundle-SymbolicName'] = project.name

        if(project.configurations.privateLib.files) {
          Map privatePackages = [:]
          project.configurations.privateLib.files.each { File lib ->
            project.zipTree(lib).visit { f ->
              if(f.isDirectory())
                privatePackages[f.path.replaceAll('/', '.')] = lib.name
            }
          }
          Map importPackages = ManifestUtils.parsePackages(m.attributes['Import-Package'])
          privatePackages.each { privatePackage, lib ->
            if(importPackages.containsKey(privatePackage)) {
              project.logger.info 'Package {} is located in private library {}, will be excluded from Import-Package.', privatePackage, lib
              importPackages['!' + privatePackage] = importPackages.remove(privatePackage)
            }
          }
          m.attributes['Import-Package'] = ManifestUtils.packagesToString(importPackages)
        }

        def platformFragment = { artifact ->
          PlatformConfig.supported_oses.find { os ->
            PlatformConfig.supported_archs.find { arch ->
              artifact.name.endsWith PlatformConfig.map_os_to_suffix[os] + '.' + PlatformConfig.map_arch_to_suffix[arch]
            }
          }
        }

        def languageFragment = { artifact ->
          artifact.name.contains '.nl_'
        }

        def requiredBundles = [ 'org.eclipse.core.runtime', 'org.eclipse.core.resources' ] as LinkedHashSet
        if(pluginConfig && pluginConfig.extension.find { it.'@point'.startsWith 'org.eclipse.core.expressions' })
          requiredBundles.add 'org.eclipse.core.expressions'
        project.configurations.compile.allDependencies.each {
          if(it.name.startsWith('org.eclipse.') && !platformFragment(it) && !languageFragment(it))
            requiredBundles.add it.name
        }
        m.attributes 'Require-Bundle': requiredBundles.sort().join(',')

        // Bundle-Classpath
        def bundleClasspath = m.attributes['Bundle-Classpath']
        if(bundleClasspath)
          bundleClasspath = bundleClasspath.split(',\\s*').collect()
        else
          bundleClasspath = []

        bundleClasspath.add(0, '.')

        project.configurations.privateLib.files.each {
          bundleClasspath.add(it.name)
        }

        bundleClasspath.unique(true)

        m.attributes['Bundle-Classpath'] = bundleClasspath.join(',')

        manifestFile.parentFile.mkdirs()
        manifestFile.withWriter { m.writeTo it }
      } // doLast
    } // extendManifest task

    project.jar {
      dependsOn project.tasks.extendManifest
      inputs.files project.files(manifestFile)
      from { project.configurations.privateLib }
      manifest {
        from(manifestFile.absolutePath) {
          eachEntry { details ->
            def newValue
            if(details.key == 'Require-Bundle') {
              if(details.baseValue && details.mergeValue)
                newValue = ((details.baseValue.split(',') as Set) + (details.mergeValue.split(',') as Set)).join(',')
              else
                newValue = details.mergeValue ?: details.baseValue
            } else if(details.key == 'Import-Package' || details.key == 'Export-Package') {
              Map packages
              if(details.baseValue) {
                packages = ManifestUtils.parsePackages(details.baseValue)
                if(details.mergeValue)
                  ManifestUtils.parsePackages(details.mergeValue).each {
                    if(it.key.startsWith('!'))
                      packages.remove(it.key.substring(1))
                    else
                      packages[it.key] = it.value
                  }
              }
              else if(details.mergeValue)
                packages = ManifestUtils.parsePackages(details.mergeValue).findAll { !it.key.startsWith('!') }
              else
                packages = [:]
              /*
               * here we fix problem with eclipse 4.X bundles and access to bundle
               * 'org.eclipse.core.runtime': if the latter is imported via 'Import-Package',
               * the application throws ClassNotFoundException.
               */
              packages = packages.findAll { !it.key.startsWith('org.eclipse') }
              newValue = ManifestUtils.packagesToString(packages)
            } else
              newValue = details.mergeValue ?: details.baseValue
            if(newValue)
              details.value = newValue
            else
              details.exclude()
          }
        }
      }
    } // jar task

  } // defineAdditionalTasks
}
