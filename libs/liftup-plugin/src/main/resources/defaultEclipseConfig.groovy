eclipse {

  defaultVersion = '4.3'

  version('4.3') {

    eclipseGroup = 'eclipse-kepler'

    swtlib { project ->
      project.dependencies {
        compile "${eclipseGroup}:org.eclipse.jface:+"
        compile "${eclipseGroup}:org.eclipse.swt:+"
        compile "${eclipseGroup}:org.eclipse.swt.${current_os_suffix}.${current_arch_suffix}:+"
      }
    }

    swtapp common: { project ->
      project.dependencies {
        compile "${eclipseGroup}:org.eclipse.jface:+"
        compile "${eclipseGroup}:org.eclipse.swt:+"
        compile "${eclipseGroup}:org.eclipse.swt.${current_os_suffix}.${current_arch_suffix}:+"
      }
    },
    platformSpecific: { project, configName, platform, arch ->
      project.dependencies.add configName, "${eclipseGroup}:org.eclipse.swt.${map_os_to_suffix[platform]}.${map_arch_to_suffix[arch]}:+"
    },
    platformAndLanguageSpecific: { project, configName, platform, arch, language ->
      project.dependencies.add configName, "${eclipseGroup}:org.eclipse.jface.nl_${language}:+"
    }
  }
}
