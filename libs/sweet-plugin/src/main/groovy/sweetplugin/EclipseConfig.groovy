package sweetplugin

import org.gradle.api.Project

class EclipseConfig {

  // eclipseGroup must be the same as in "eclipseDownloadConfig.gradle"
  public static final String eclipseGroup = 'eclipse-kepler'

  public static void addEclipseBundleDependencies(Project project) {
    project.dependencies {
      compile "${eclipseGroup}:javax.annotation:+"
      compile "${eclipseGroup}:javax.inject:+"
      compile "${eclipseGroup}:org.eclipse.jface:+"
      compile "${eclipseGroup}:org.eclipse.swt:+"
      compile "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.current_os_suffix}.${PlatformConfig.current_arch_suffix}:+"
      compile "${eclipseGroup}:org.eclipse.ui:+"
    }
    project.tasks.jar.manifest {
      instruction 'Require-Bundle', 'org.eclipse.jface'
      instruction 'Require-Bundle', 'org.eclipse.swt'
      instruction 'Require-Bundle', 'org.eclipse.ui'
    }
  }

  public static void addEclipseIdeDependencies(Project project) {
    project.dependencies {
      compile "$eclipseGroup:org.eclipse.ui.ide:+"
      compile "$eclipseGroup:org.eclipse.ui.ide.application:+"
      compile "$eclipseGroup:org.eclipse.equinox.p2.core:+"
      compile "$eclipseGroup:org.eclipse.equinox.p2.engine:+"
      compile "$eclipseGroup:org.eclipse.equinox.p2.metadata:+"
      compile "$eclipseGroup:org.eclipse.equinox.p2.metadata.repository:+"
      compile "$eclipseGroup:org.eclipse.equinox.p2.repository:+"
      compile "$eclipseGroup:org.eclipse.equinox.security:+"
      compile "$eclipseGroup:org.eclipse.ui.intro:+"
    }
    project.tasks.jar.manifest {
      instruction 'Require-Bundle', 'org.eclipse.ui.ide'
      instruction 'Require-Bundle', 'org.eclipse.ui.ide.application'
      instruction 'Require-Bundle', 'org.eclipse.ui.intro'
    }
  }

  public static void addEclipseIdeDependencies(Project project, String configName, String platform, String arch) {
    // no dependencies here
  }

  public static void addEclipseIdeDependencies(Project project, String configName, String platform, String arch, String language) {
    project.dependencies {
      compile "$eclipseGroup:org.eclipse.ui.ide.nl_${language}:+"
      compile "$eclipseGroup:org.eclipse.ui.ide.application.nl_${language}:+"
      compile "$eclipseGroup:org.eclipse.ui.intro.nl_${language}:+"
    }
  }

  public static void addEquinoxDependencies(Project project) {
    project.dependencies {
      runtime "${eclipseGroup}:com.ibm.icu:+"
      runtime "${eclipseGroup}:javax.xml:+"
      compile "${eclipseGroup}:org.eclipse.core.runtime:+"
      runtime "${eclipseGroup}:org.eclipse.core.runtime.compatibility.registry:+"
      compile "${eclipseGroup}:org.eclipse.equinox.app:+"
      runtime "${eclipseGroup}:org.eclipse.equinox.ds:+"
      runtime "${eclipseGroup}:org.eclipse.equinox.event:+"
      runtime "${eclipseGroup}:org.eclipse.equinox.launcher:+"
      runtime "${eclipseGroup}:org.eclipse.equinox.launcher.${PlatformConfig.current_os_suffix}.${PlatformConfig.current_arch_suffix}:+"
      runtime "${eclipseGroup}:org.eclipse.equinox.util:+"
      compile "${eclipseGroup}:org.eclipse.osgi:+"
      runtime "${eclipseGroup}:org.eclipse.osgi.services:+"
    }
    project.tasks.jar.manifest {
      instruction 'Require-Bundle', 'org.eclipse.core.runtime'
    }
  }

  public static void addEquinoxDependencies(Project project, String configName, String platform, String arch) {
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.equinox.launcher.${PlatformConfig.map_os_to_suffix[platform]}.${PlatformConfig.map_arch_to_suffix[arch]}:+"
  }

  public static void addRcpDependencies(Project project) {
    project.dependencies {
      compile "${eclipseGroup}:javax.annotation:+"
      compile "${eclipseGroup}:javax.inject:+"
      runtime "${eclipseGroup}:org.eclipse.core.filesystem:+"
      runtime "${eclipseGroup}:org.eclipse.core.net:+"
      compile "${eclipseGroup}:org.eclipse.jface:+"
      compile "${eclipseGroup}:org.eclipse.swt:+"
      compile "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.current_os_suffix}.${PlatformConfig.current_arch_suffix}:+"
      compile "${eclipseGroup}:org.eclipse.ui:+"
    }
    project.tasks.jar.manifest {
      instruction 'Require-Bundle', 'org.eclipse.core.filesystem'
      instruction 'Require-Bundle', 'org.eclipse.core.net'
      instruction 'Require-Bundle', 'org.eclipse.jface'
      instruction 'Require-Bundle', 'org.eclipse.swt'
      instruction 'Require-Bundle', 'org.eclipse.ui'
    }
  }

  public static void addRcpDependencies(Project project, String configName, String platform, String arch) {
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.core.filesystem.${PlatformConfig.map_os_to_filesystem_suffix[platform]}.${PlatformConfig.map_arch_to_suffix[arch]}:+"
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.core.net.${PlatformConfig.map_os_to_filesystem_suffix[platform]}.${PlatformConfig.map_arch_to_suffix[arch]}:+"
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.map_os_to_suffix[platform]}.${PlatformConfig.map_arch_to_suffix[arch]}:+"
  }

  public static void addRcpDependencies(Project project, String configName, String platform, String arch, String language) {
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.core.net.${PlatformConfig.map_os_to_filesystem_suffix[platform]}.${PlatformConfig.map_arch_to_suffix[arch]}.nl_${language}:+"
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.jface.nl_${language}:+"
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.ui.nl_${language}:+"
  }

  public static void addSwtAppDependencies(Project project) {
    project.dependencies {
      compile "${eclipseGroup}:org.eclipse.jface:+"
      compile "${eclipseGroup}:org.eclipse.swt:+"
      compile "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.current_os_suffix}.${PlatformConfig.current_arch_suffix}:+"
    }
  }

  public static void addSwtAppDependencies(Project project, String configName, String platform, String arch) {
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.map_os_to_suffix[platform]}.${PlatformConfig.map_arch_to_suffix[arch]}:+"
  }

  public static void addSwtAppDependencies(Project project, String configName, String platform, String arch, String language) {
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.jface.nl_${language}:+"
  }

  public static void addSwtLibDependencies(Project project) {
    project.dependencies {
      compile "${eclipseGroup}:org.eclipse.jface:+"
      compile "${eclipseGroup}:org.eclipse.swt:+"
      compile "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.current_os_suffix}.${PlatformConfig.current_arch_suffix}:+"
    }
  }

  public static void createEclipseIdeConfigurations(Project project) {
    PlatformConfig.supported_oses.each { platform ->
      PlatformConfig.supported_archs.each { arch ->
        String configName = "product_eclipse_ide_${platform}_${arch}"
        def config = project.configurations.create(configName)
        config.extendsFrom project.configurations.findByName("product_rcp_${platform}_${arch}")
        addEclipseIdeDependencies project, configName, platform, arch
        PlatformConfig.supported_languages.each { language ->
          String localizedConfigName = "product_eclipse_ide_${platform}_${arch}_${language}"
          def localizedConfig = project.configurations.create(localizedConfigName)
          localizedConfig.extendsFrom config
          localizedConfig.extendsFrom project.configurations.findByName("product_rcp_${platform}_${arch}_${language}")
          addEclipseIdeDependencies project, localizedConfigName, platform, arch, language
        }
      }
    }
  }

  public static void createEquinoxConfigurations(Project project) {
    PlatformConfig.supported_oses.each { platform ->
      PlatformConfig.supported_archs.each { arch ->
        String configName = "product_equinox_${platform}_${arch}"
        project.configurations.create configName
        addEquinoxDependencies project, configName, platform, arch
      }
    }
  }

  public static void createRcpConfigurations(Project project) {
    PlatformConfig.supported_oses.each { platform ->
      PlatformConfig.supported_archs.each { arch ->
        String configName = "product_rcp_${platform}_${arch}"
        def config = project.configurations.create(configName)
        config.extendsFrom project.configurations.findByName("product_equinox_${platform}_${arch}")
        addRcpDependencies project, configName, platform, arch
        PlatformConfig.supported_languages.each { language ->
          String localizedConfigName = "product_rcp_${platform}_${arch}_${language}"
          def localizedConfig = project.configurations.create(localizedConfigName)
          localizedConfig.extendsFrom config
          addRcpDependencies project, localizedConfigName, platform, arch, language
        }
      }
    }
  }
}
