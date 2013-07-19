package sweetplugin

import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.*
import org.gradle.api.file.FileTree
import org.gradle.api.plugins.*

class ManifestUtils {

  public static void extendManifest(Project project) {

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
          setClasspath project.configurations.runtime
        }
        
        m = m.effectiveManifest
        
        project.sourceSets.main.java.srcDirs.each { File srcDir ->
          project.fileTree(srcDir).include('**/Activator.java').files.each { File activatorSourceFile ->
            String activator = activatorSourceFile.absolutePath.substring(srcDir.absolutePath.length())
            if(activator.startsWith('/'))
              activator = activator.substring(1)
            activator = activator.substring(0, activator.length() - 5) // remove '.java' file extension
            activator = activator.replaceAll('/', '.')
            m.attributes 'Bundle-Activator': activator
            m.attributes 'Bundle-ActivationPolicy': 'lazy'
          }
        }

        File pluginConfigFile = null
        project.sourceSets.main.resources.srcDirs.each { File srcDir ->
          if(srcDir.exists()) {
            File locatizationDir = new File(srcDir, 'OSGI-INF/l10n')
            // if locatizationDir exists, it will be used by OSGi automatically
            // and there's no need for Bundle-Localization
            if(!locatizationDir.exists()) {
              def localizationFiles = new FileNameFinder().getFileNames(srcDir.absolutePath, 'plugin*.properties')
              if(localizationFiles)
                m.attributes 'Bundle-Localization': 'plugin'
            }
            if(new File(srcDir, 'plugin.xml').exists())
              pluginConfigFile = new File(srcDir, 'plugin.xml')
          }
        }

        if(pluginConfigFile) {
          def importPackages = parsePackages(m.attributes['Import-Package'])
          m.attributes 'Bundle-SymbolicName': "${project.name}; singleton:=true"
          project.logger.info 'Analyzing class usage in {}/plugin.xml', project.name
          def pluginConfig = new XmlParser().parse(pluginConfigFile)
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
          m.attributes 'Import-Package': packagesToString(importPackages)
        }
        else
          m.attributes 'Bundle-SymbolicName': project.name
          
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
          
        def requiredBundles = [ 'org.eclipse.core.runtime' ] as LinkedHashSet
        project.configurations.compile.allDependencies.each {
          if(it.name.startsWith('org.eclipse.') && !platformFragment(it) && !languageFragment(it))
            requiredBundles.add it.name
        }
        m.attributes 'Require-Bundle': requiredBundles.sort().join(',')
        
        // m.attributes 'Require-Bundle': 'org.eclipse.core.runtime'

        manifestFile.parentFile.mkdirs()
        manifestFile.withWriter { m.writeTo it }
      }
    }

    project.jar {
      dependsOn project.tasks.extendManifest
      inputs.files project.files(manifestFile)
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
              if(details.baseValue && details.mergeValue)
                newValue = parsePackages(details.baseValue) + parsePackages(details.mergeValue)
              else
                newValue = parsePackages(details.mergeValue ?: details.baseValue)
              /*
               * here we fix problem with eclipse 4.X bundles and access to bundle
               * 'org.eclipse.core.runtime': if the latter is imported via 'Import-Package',
               * the application throws ClassNotFoundException.
               */
              def iterator = newValue.entrySet().iterator()
              while (iterator.hasNext())
                if (iterator.next().key.startsWith('org.eclipse'))
                  iterator.remove()
              newValue = packagesToString(newValue)
            } else
              newValue = details.mergeValue ?: details.baseValue
            if(newValue)
              details.value = newValue
            else
              details.exclude()
          }
        }
      }
    }
  }

  public static java.util.jar.Manifest getManifest(Project project, File file) {
    String checksum
    file.withInputStream {
      checksum = DigestUtils.md5Hex(it)
    }
    String tmpFolder = "${project.buildDir}/tmp/manifests/${DigestUtils.md5Hex(file.absolutePath)}"
    String manifestFileName = 'META-INF/MANIFEST.MF'
    File manifestFile = new File("$tmpFolder/$manifestFileName")
    File savedChecksumFile = new File(tmpFolder, 'sourceChecksum')
    String savedChecksum = savedChecksumFile.exists() ? savedChecksumFile.text : ''
    if(savedChecksum != checksum && !manifestFile.exists()) {
      FileTree tree
      if(file.isFile() && (file.name.endsWith('.zip') || file.name.endsWith('.jar')))
        tree = project.zipTree(file)
      else if(file.isDirectory())
        tree = project.fileTree(file)
      else
        return null
      manifestFile.parentFile.mkdirs()
      manifestFile.text = ''
      project.copy {
        from tree
        include manifestFileName
        into tmpFolder
      }
      savedChecksumFile.parentFile.mkdirs()
      savedChecksumFile.text = checksum
    }
    def libManifest
    manifestFile.withInputStream {
      libManifest = new java.util.jar.Manifest(it)
    }
    return libManifest
  }

  public static String getManifestEntry(java.util.jar.Manifest manifest, String entryName) {
    if(manifest != null)
      for (def key in manifest.getMainAttributes().keySet()) {
        String attrName = key.toString()
        if(attrName == entryName)
          return manifest.getMainAttributes().getValue(attrName)
      }
    return null
  }

  public static boolean isBundle(java.util.jar.Manifest m) {
    return getManifestEntry(m, 'Bundle-SymbolicName') != null || getManifestEntry(m, 'Bundle-Name') != null
  }

  public static boolean isBundle(Project project, File file) {
    return isBundle(getManifest(project, file))
  }

  public static boolean isFragmentBundle(Project project, File file) {
    return isFragmentBundle(getManifest(project, file))
  }

  public static boolean isFragmentBundle(java.util.jar.Manifest m) {
    return getManifestEntry(m, 'Fragment-Host') != null
  }

  public static boolean isWrapperBundle(java.util.jar.Manifest m) {
    return getManifestEntry(m, 'Wrapped-Library') != null
  }

  public static String packagesToString(Map packages) {
    return packages.collect({ it.key + it.value }).join(',')
  }

  public static Map parsePackages(packagesString) {
    def packages = [:]
    if(packagesString)
      packagesString.eachMatch '([\\w\\-\\.]+)(((;[\\w\\-\\.]+((:?)=((("[^"]*")|([\\w\\-\\.]+))))?)*),?)', {
        packages[it[1]] = it[3]
      }
    return packages
  }
}
