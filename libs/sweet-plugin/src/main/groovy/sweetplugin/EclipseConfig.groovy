package sweetplugin

import org.gradle.api.Project

class EclipseConfig {

  // eclipseGroup must be the same as in "eclipseDownloadConfig.gradle"
  public static final String eclipseGroup = 'eclipse-juno'

  public static void addEclipseBundleDependencies(Project project) {
    project.dependencies {
      compile "${eclipseGroup}:javax.annotation:1.0.0.v20101115-0725"
      compile "${eclipseGroup}:javax.inject:1.0.0.v20091030"
      compile "${eclipseGroup}:org.eclipse.jface:3.8.102.v20130123-162658"
      compile "${eclipseGroup}:org.eclipse.swt:3.100.1.v4236b"
      compile "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.current_os_suffix}.${PlatformConfig.current_arch_suffix}:3.100.1.v4236b"
      compile "${eclipseGroup}:org.eclipse.ui:3.104.0.v20121024-145224"
    }
  }

  public static void addEquinoxDependencies(Project project) {
    project.dependencies {
      runtime "${eclipseGroup}:com.ibm.icu:4.4.2.v20110823"
      runtime "${eclipseGroup}:javax.xml:1.3.4.v201005080400"
      compile "${eclipseGroup}:org.eclipse.core.runtime:3.8.0.v20120912-155025"
      runtime "${eclipseGroup}:org.eclipse.core.runtime.compatibility.registry:3.5.101.v20130108-163257"
      compile "${eclipseGroup}:org.eclipse.equinox.app:1.3.100.v20120522-1841"
      runtime "${eclipseGroup}:org.eclipse.equinox.ds:1.4.1.v20120926-201320"
      runtime "${eclipseGroup}:org.eclipse.equinox.event:1.2.200.v20120522-2049"
      runtime "${eclipseGroup}:org.eclipse.equinox.launcher:1.3.0.v20120522-1813"
      runtime "${eclipseGroup}:org.eclipse.equinox.launcher.${PlatformConfig.current_os_suffix}.${PlatformConfig.current_arch_suffix}:+"
      runtime "${eclipseGroup}:org.eclipse.equinox.util:1.0.400.v20120917-192807"
      compile "${eclipseGroup}:org.eclipse.osgi:3.8.2.v20130124-134944"
      runtime "${eclipseGroup}:org.eclipse.osgi.services:3.3.100.v20120522-1822"
    }
  }

  public static void addEquinoxDependencies(Project project, String configName, String platform, String arch) {
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.equinox.launcher.${PlatformConfig.map_os_to_suffix[platform]}.${PlatformConfig.map_arch_to_suffix[arch]}:+"
  }

  public static void addRcpDependencies(Project project) {
    project.dependencies {
      compile "${eclipseGroup}:javax.annotation:1.0.0.v20101115-0725"
      compile "${eclipseGroup}:javax.inject:1.0.0.v20091030"
      runtime "${eclipseGroup}:org.eclipse.core.filesystem:1.3.200.v20130115-145044"
      compile "${eclipseGroup}:org.eclipse.jface:3.8.102.v20130123-162658"
      compile "${eclipseGroup}:org.eclipse.swt:3.100.1.v4236b"
      compile "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.current_os_suffix}.${PlatformConfig.current_arch_suffix}:3.100.1.v4236b"
      compile "${eclipseGroup}:org.eclipse.ui:3.104.0.v20121024-145224"
    }
  }

  public static void addRcpDependencies(Project project, String configName, String platform, String arch) {
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.core.filesystem.${PlatformConfig.map_os_to_filesystem_suffix[platform]}.${PlatformConfig.map_arch_to_suffix[arch]}:+"
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.map_os_to_suffix[platform]}.${PlatformConfig.map_arch_to_suffix[arch]}:+"
  }

  public static void addRcpDependencies(Project project, String configName, String platform, String arch, String language) {
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.jface.nl_${language}:+"
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.swt.nl_${language}:+"
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.map_os_to_suffix[platform]}.${PlatformConfig.map_arch_to_suffix[arch]}.nl_${language}:+"
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.ui.nl_${language}:+"
  }

  public static void addSwtAppDependencies(Project project) {
    project.dependencies {
      compile "${eclipseGroup}:org.eclipse.jface:3.8.102.v20130123-162658"
      compile "${eclipseGroup}:org.eclipse.swt:3.100.1.v4236b"
      compile "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.current_os_suffix}.${PlatformConfig.current_arch_suffix}:3.100.1.v4236b"
    }
  }

  public static void addSwtAppDependencies(Project project, String configName, String platform, String arch) {
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.core.filesystem.${PlatformConfig.map_os_to_filesystem_suffix[platform]}.${PlatformConfig.map_arch_to_suffix[arch]}:+"
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.map_os_to_suffix[platform]}.${PlatformConfig.map_arch_to_suffix[arch]}:+"
  }

  public static void addSwtAppDependencies(Project project, String configName, String platform, String arch, String language) {
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.jface.nl_${language}:+"
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.swt.nl_${language}:+"
    project.dependencies.add configName, "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.map_os_to_suffix[platform]}.${PlatformConfig.map_arch_to_suffix[arch]}.nl_${language}:+"
  }

  public static void addSwtLibDependencies(Project project) {
    project.dependencies {
      compile "${eclipseGroup}:org.eclipse.jface:3.8.102.v20130123-162658"
      compile "${eclipseGroup}:org.eclipse.swt:3.100.1.v4236b"
      compile "${eclipseGroup}:org.eclipse.swt.${PlatformConfig.current_os_suffix}.${PlatformConfig.current_arch_suffix}:3.100.1.v4236b"
    }
  }
}
