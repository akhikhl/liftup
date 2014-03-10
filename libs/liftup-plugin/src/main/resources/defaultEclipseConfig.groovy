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
  }
}
