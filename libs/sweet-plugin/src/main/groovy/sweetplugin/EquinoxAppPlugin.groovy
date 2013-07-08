package sweetplugin

import org.apache.commons.io.FilenameUtils
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.*

class EquinoxAppPlugin implements Plugin<Project> {

  private static final String eclipsePluginMask = /([\da-zA-Z_.-]+?)-((\d+\.)+[\da-zA-Z_.-]*)/

  private static final String osgiFrameworkPluginName = 'org.eclipse.osgi'
  private static final String equinoxLauncherPluginName = 'org.eclipse.equinox.launcher'

  private static String getEclipseApplicationId(Project project) {
    String result
    project.sourceSets.main.resources.srcDirs.each { File srcDir ->
      File pluginConfigFile = new File(srcDir, 'plugin.xml')
      if(pluginConfigFile.exists()) {
        def pluginConfig = new XmlParser().parse(pluginConfigFile)
        result = pluginConfig.extension.find({ it.'@point' == 'org.eclipse.core.runtime.applications' })?.'@id'
      }
    }
    if(result)
      result = "${project.name}.$result"
    return result
  }

  private static String getEclipseProductId(Project project) {
    String result
    project.sourceSets.main.resources.srcDirs.each { File srcDir ->
      File pluginConfigFile = new File(srcDir, 'plugin.xml')
      if(pluginConfigFile.exists()) {
        def pluginConfig = new XmlParser().parse(pluginConfigFile)
        result = pluginConfig.extension.find({ it.'@point' == 'org.eclipse.core.runtime.products' })?.'@id'
      }
    }
    if(result)
      result = "${project.name}.$result"
    return result
  }

  private static String getPluginName(String fileName) {
    return fileName.replaceAll(eclipsePluginMask, '$1')
  }

  void apply(final Project project) {

    project.apply plugin: 'osgi'

    project.extensions.create('run', RunExtension)
    project.extensions.create('equinox', EquinoxAppPluginExtension)

    EclipseConfig.addEquinoxDependencies project

    EclipseConfig.createEquinoxConfigurations project

    project.ext { eclipseGroup = EclipseConfig.eclipseGroup }

    project.afterEvaluate {

      ManifestUtils.extendManifest project

      project.equinox.beforeProductGeneration.each { obj ->
        if(obj instanceof Closure)
          obj()
      }

      def wrappedLibsDir = new File("${project.buildDir}/wrappedLibs")

      project.task('wrapLibs') {
        inputs.files project.configurations.runtime
        outputs.dir wrappedLibsDir
        doLast {
          wrappedLibsDir.mkdirs()
          inputs.files.each { lib ->
            def libManifest = ManifestUtils.getManifest(project, lib)
            if(ManifestUtils.isBundle(libManifest))
              return
            String baseLibName = FilenameUtils.getBaseName(lib.name)
            def bundleName
            def bundleVersion = ManifestUtils.getManifestEntry(libManifest, 'Implementation-Version')
            if(bundleVersion) {
              def match = baseLibName =~ '(.+)-' + bundleVersion.replaceAll(/\./, /\\./)
              if(match)
                bundleName = match[0][1]
            }
            if(!bundleName) {
              bundleVersion = ManifestUtils.getManifestEntry(libManifest, 'Specification-Version')
              if(bundleVersion) {
                def match = baseLibName =~ '(.+)-' + bundleVersion.replaceAll(/\./, /\\./)
                if(match)
                  bundleName = match[0][1]
              }
            }
            if(!bundleName) {
              def match = baseLibName =~ /(.+)-((\d+\.)*(\d+)[a-zA-Z_-]*)/
              if(match) {
                bundleName = match[0][1]
                bundleVersion = match[0][2]
              }
            }
            if(bundleVersion) {
              // check for too long version numbers, replace to valid bundle version if needed
              def match = bundleVersion =~ /(\d+\.)(\d+\.)(\d+\.)(([\w-]+\.)+[\w-]+)/
              if(match)
                bundleVersion = match[0][1] + match[0][2] + match[0][3] + match[0][4].replaceAll(/\./, '-')
            }
            bundleName = bundleName ?: baseLibName
            bundleVersion = bundleVersion ?: '1.0'
            String bundlePackageName = "${bundleName}-bundle-${bundleVersion}"
            File manifestFile = new File("${wrappedLibsDir}/${bundlePackageName}-MANIFEST.MF")
            def m = project.osgiManifest {
              setName bundleName
              setSymbolicName bundleName
              setVersion bundleVersion
              setClassesDir lib
              setClasspath project.files(lib)
              instruction 'Bundle-Classpath', lib.name
            }
            m = m.effectiveManifest
            def packages = ManifestUtils.parsePackages(m.attributes['Import-Package'])
            // workarounds for dynamically referenced classes
            if(bundleName.startsWith('ant-optional'))
              packages.remove 'COM.ibm.netrexx.process'
            else if(bundleName.startsWith('commons-logging')) {
              packages.remove 'org.apache.log4j'
              packages.remove 'org.apache.log'
              packages.remove 'org.apache.avalon.framework.logger'
            }
            else if(bundleName.startsWith('jdom')) {
              packages.remove 'oracle.xml.parser'
              packages.remove 'oracle.xml.parser.v2'
              packages.remove 'org.apache.xerces.dom'
              packages.remove 'org.apache.xerces.parsers'
              packages.remove 'org.jaxen.jdom'
              packages.remove 'org.jaxen'
            }
            else if(bundleName.startsWith('saxon'))
              packages.remove 'com.saxonica.validate'
            else if(bundleName.startsWith('ojdbc')) {
              packages.remove 'javax.resource'
              packages.remove 'javax.resource.spi'
              packages.remove 'javax.resource.spi.endpoint'
              packages.remove 'javax.resource.spi.security'
              packages.remove 'oracle.i18n.text.converter'
              packages.remove 'oracle.ons'
              packages.remove 'oracle.security.pki'
            }
            m.attributes.remove 'Import-Package'
            if(packages)
              m.attributes(['Import-Package': ManifestUtils.packagesToString(packages)])
            manifestFile.withWriter { m.writeTo it }

            ant.jar(destFile: "${wrappedLibsDir}/${bundlePackageName}.jar", manifest: manifestFile) { fileset(file: lib) }
            manifestFile.delete()
          }
        }
      } // task wrapLibs

      File equinoxLauncherFile = project.configurations.runtime.find({ getPluginName(it.name) == equinoxLauncherPluginName })
      File frameworkFile = project.configurations.runtime.find({ getPluginName(it.name) == osgiFrameworkPluginName })

      String runDir = "${project.buildDir}/run"
      String runConfigDir = "${runDir}/configuration"
      File runConfigFile = new File("$runConfigDir/config.ini")
      String pluginsDir = "$runDir/plugins"

      project.task('prepareRunConfig') {
        dependsOn project.tasks.jar
        dependsOn project.tasks.wrapLibs
        inputs.files project.configurations.runtime.files
        outputs.files runConfigFile
        doLast {
          // key is plugin name, value is complete launch entry for configuration
          def bundleLaunchList = [:]

          def addBundle = { File file ->
            String pluginName = getPluginName(file.name)
            if(bundleLaunchList.containsKey(pluginName))
              return
            String launchOption = ''
            if(pluginName == 'org.eclipse.core.runtime')
              launchOption = '@start'
            else if(pluginName == 'org.eclipse.equinox.common')
              launchOption = '@2:start'
            else if(pluginName == 'org.eclipse.equinox.ds')
              launchOption = '@1:start'
            if(pluginName != osgiFrameworkPluginName && !pluginName.startsWith(equinoxLauncherPluginName))
              bundleLaunchList[pluginName] = "reference\\:file\\:${file.absolutePath}${launchOption}"
          }

          addBundle project.tasks.jar.archivePath

          wrappedLibsDir.eachFileMatch(~/.*\.jar/) { addBundle it }

          project.configurations.runtime.each {
            if(ManifestUtils.isBundle(project, it))
              addBundle it
          }

          if(project.run.language) {
            project.configurations.findAll({ it.name.endsWith("${PlatformConfig.current_os}_${PlatformConfig.current_arch}_${project.run.language}") }).each { config ->
              config.files.each { file ->
                def m = file.name =~ /([\da-zA-Z_.-]+?)/ + "\\.nl_${project.run.language}" + /-((\d+\.)+[\da-zA-Z_.-]*)/
                if(m) {
                  String pluginName = m[0][1]
                  if(project.configurations.runtime.files.find { getPluginName(it.name) == pluginName })
                    addBundle file
                }
              }
            }
          }

          bundleLaunchList = bundleLaunchList.sort()

          runConfigFile.parentFile.mkdirs()
          runConfigFile.withPrintWriter { PrintWriter configWriter ->
            String applicationId = getEclipseApplicationId(project)
            if(applicationId)
              configWriter.println "eclipse.application=$applicationId"
            String productId = getEclipseProductId(project)
            if(productId)
              configWriter.println "eclipse.product=$productId"
            project.sourceSets.main.resources.srcDirs.each { File srcDir ->
              File splashFile = new File(srcDir, 'splash.bmp')
              if(splashFile.exists())
                configWriter.println "osgi.splashLocation=${splashFile.absolutePath}"
            }
            configWriter.println "osgi.framework=file\\:${frameworkFile.absolutePath}"
            configWriter.println 'osgi.bundles.defaultStartLevel=4'
            configWriter.println 'osgi.bundles=' + bundleLaunchList.values().join(',\\\n  ')
          }

          project.copy {
            from project.configurations.runtime.findAll { it.name.startsWith(equinoxLauncherPluginName) }
            into pluginsDir
            // need to rename them to ensure that platform-specific launcher fragments are automatically found
            rename eclipsePluginMask, '$1_$2'
          }
        }
      }

      project.task('run', type: JavaExec) {
        dependsOn project.tasks.prepareRunConfig
        classpath project.files(new File(pluginsDir, equinoxLauncherFile.name.replaceAll(eclipsePluginMask, '$1_$2')))
        main 'org.eclipse.equinox.launcher.Main'

        def programArgs = [
          '-configuration',
          runConfigDir,
          '-data',
          runDir,
          '-consoleLog'
        ]

        project.sourceSets.main.resources.srcDirs.each { File srcDir ->
          File splashFile = new File(srcDir, 'splash.bmp')
          if(splashFile.exists())
            programArgs.add '-showSplash'
        }

        programArgs.addAll project.run.args

        if(project.run.language) {
          programArgs.add '-nl'
          programArgs.add project.run.language
        }

        args programArgs
      }

      String outputBaseDir = "${project.buildDir}/output"

      def findFileInProducts = { file ->
        project.configurations.find { config ->
          config.name.startsWith('product_') && config.find { it == file }
        }
      }

      project.equinox.products.each { product ->

        def productConfig = project.configurations.findByName("product_${product.name}")

        String platform = product.platform ?: 'any'
        String arch = product.arch ?: 'any'
        String language = product.language ?: 'en'

        String suffix = ''
        if(product.name != 'default')
          suffix = product.suffix ?: product.name

        def launchers
        if(product.launchers)
          launchers = product.launchers
        else if(product.launcher)
          launchers = [product.launcher]
        else if(product.platform == 'windows')
          launchers = ['windows']
        else
          launchers = ['shell']

        String productOutputDir = "${outputBaseDir}/${project.name}-${project.version}"
        if(suffix)
          productOutputDir += '-' + suffix

        String buildTaskName = 'buildProduct'
        if(product.name != 'default')
          buildTaskName += '_' + product.name

        project.task(buildTaskName) { task ->

          dependsOn project.tasks.jar
          dependsOn project.tasks.wrapLibs
          project.tasks.build.dependsOn task

          inputs.files project.configurations.runtime.files

          if(productConfig)
            inputs.files productConfig.files

          outputs.dir productOutputDir

          doLast {
            // key is plugin name, value is complete launch entry for configuration
            def bundleLaunchList = [:]

            def addBundle = { File file ->
              String pluginName = getPluginName(file.name)
              if(bundleLaunchList.containsKey(pluginName))
                return
              String launchOption = ''
              if(pluginName == 'org.eclipse.core.runtime')
                launchOption = '@start'
              else if(pluginName == 'org.eclipse.equinox.common')
                launchOption = '@2:start'
              else if(pluginName == 'org.eclipse.equinox.ds')
                launchOption = '@1:start'
              if(pluginName != osgiFrameworkPluginName && !pluginName.startsWith(equinoxLauncherPluginName))
                bundleLaunchList[pluginName] = "reference\\:file\\:${file.name}${launchOption}"
              project.copy {
                from file
                into "$productOutputDir/plugins"
                // need to rename them to ensure that platform-specific launcher fragments are automatically found
                if(file.name.startsWith(equinoxLauncherPluginName))
                  rename eclipsePluginMask, '$1_$2'
              }
            }

            addBundle project.tasks.jar.archivePath

            wrappedLibsDir.eachFileMatch(~/.*\.jar/) { addBundle it }

            project.configurations.runtime.each {
              if(ManifestUtils.isBundle(project, it) && !findFileInProducts(it))
                addBundle it
            }

            productConfig?.each {
              if(ManifestUtils.isBundle(project, it))
                addBundle it
            }

            bundleLaunchList = bundleLaunchList.sort()

            File configFile = new File("$productOutputDir/configuration/config.ini")
            configFile.parentFile.mkdirs()
            configFile.withPrintWriter { PrintWriter configWriter ->
              String applicationId = getEclipseApplicationId(project)
              if(applicationId)
                configWriter.println "eclipse.application=$applicationId"
              String productId = getEclipseProductId(project)
              if(productId)
                configWriter.println "eclipse.product=$productId"
              configWriter.println "osgi.framework=file\\:plugins/${frameworkFile.name}"
              configWriter.println 'osgi.bundles.defaultStartLevel=4'
              configWriter.println 'osgi.bundles=' + bundleLaunchList.values().join(',\\\n  ')
              project.sourceSets.main.resources.srcDirs.each { File srcDir ->
                if(new File(srcDir, 'splash.bmp').exists())
                  configWriter.println "osgi.splashPath=file\\:plugins/${project.tasks.jar.archivePath.name}"
              }
            }

            String equinoxLauncherName = 'plugins/' + equinoxLauncherFile.name.replaceAll(eclipsePluginMask, '$1_$2')

            def launchParameters = project.equinox.launchParameters

            project.sourceSets.main.resources.srcDirs.each { File srcDir ->
              File splashFile = new File(srcDir, 'splash.bmp')
              if(splashFile.exists())
                launchParameters.add '-showSplash'
            }

            if(language) {
              launchParameters.add '-nl'
              launchParameters.add language
            }

            launchParameters = launchParameters.join(' ')
            if(launchParameters)
              launchParameters = ' ' + launchParameters

            if(launchers.contains('shell')) {
              File launchScriptFile = new File("${productOutputDir}/${project.name}.sh")
              launchScriptFile.text = "#!/bin/bash\njava -jar ${equinoxLauncherName}$launchParameters \"\$@\""
              launchScriptFile.setExecutable(true)
            }

            if(launchers.contains('windows')) {
              File launchScriptFile = new File("${productOutputDir}/${project.name}.bat")
              launchScriptFile.text = "@java -jar ${equinoxLauncherName}$launchParameters %*"
            }

            String versionFileName = "${productOutputDir}/VERSION"
            if(platform == 'windows' || launchers.contains('windows'))
              versionFileName += '.txt'
            new File(versionFileName).text = """\
product: ${project.name}
version: ${project.version}
platform: $platform
architecture: $arch
language: $language
"""
          } // doLast
        } // buildProduct_xxx task

        if(project.equinox.archiveProducts) {
          def archiveTaskName = 'archiveProduct'
          if(product.name != 'default')
            archiveTaskName += '_' + product.name

          def archiveType = launchers.contains('windows') ? Zip : Tar

          project.task(archiveTaskName, type: archiveType) { task ->
            task.dependsOn buildTaskName
            project.tasks.build.dependsOn task
            from new File(productOutputDir)
            into "${project.name}"
            destinationDir = new File(outputBaseDir)
            classifier = suffix
            if(archiveType == Tar) {
              extension = '.tar.gz'
              compression = Compression.GZIP
            }
            task.doLast {
              ant.checksum file: it.archivePath
            }
          }
        }

      } // each product
    } // project.afterEvaluate
  } // apply
}