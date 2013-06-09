package sweetplugin

import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project

class EclipseConfig {

  void applyToProject(final Project project) {
    applyResourceFileToProject "platformConfig.gradle", project
    applyResourceFileToProject "eclipseConfig.gradle", project
  }

  void applyResourceFileToProject(String fileName, final Project project) {
    def eclipseConfigFilePath = "${project.buildDir}/$fileName"
    def eclipseConfigFile = new File(eclipseConfigFilePath)
    def oldDigest
    if(eclipseConfigFile.exists())
      eclipseConfigFile.withInputStream { ins ->
        oldDigest = DigestUtils.md5Hex(ins)
      }
    def newDigest = DigestUtils.md5Hex(this.class.getResourceAsStream(fileName))
    if(newDigest != oldDigest) {
      eclipseConfigFile.parentFile.mkdirs()
      eclipseConfigFile.withOutputStream { ostream ->
        ostream << this.class.getResourceAsStream(fileName)
      }
    }
    project.apply from: eclipseConfigFilePath
  }
}
