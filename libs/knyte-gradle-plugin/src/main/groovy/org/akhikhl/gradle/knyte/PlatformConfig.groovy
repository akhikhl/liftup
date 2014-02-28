package org.akhikhl.gradle.knyte

class PlatformConfig {

  public static final supported_oses = ['windows', 'linux']
  public static final supported_archs = ['x86_32', 'x86_64']
  public static final supported_languages = ['de']

  public static final String current_os

  public static final String current_arch

  public static final String current_language

  public static final Map map_os_to_suffix = [ 'windows' : 'win32.win32', 'linux' : 'gtk.linux' ]

  public static final Map map_os_to_filesystem_suffix = [ 'windows' : 'win32', 'linux' : 'linux' ]

  public static final Map map_arch_to_suffix = [ 'x86_32' : 'x86', 'x86_64' : 'x86_64' ]

  public static final String current_os_suffix

  public static final String current_os_filesystem_suffix

  public static final String current_arch_suffix

  static {
    current_os = System.getProperty('os.name')
    if(current_os.substring(0, 5).equalsIgnoreCase('linux'))
      current_os = 'linux'
    else if(current_os.substring(0, 7).equalsIgnoreCase('windows'))
      current_os = 'windows'

    current_arch = System.getProperty('os.arch')
    if(current_arch == 'x86' || current_arch == 'i386')
      current_arch = 'x86_32'
    else if(current_arch == 'amd64')
      current_arch = 'x86_64'

    current_language = System.getProperty('user.language')

    current_os_suffix = map_os_to_suffix[current_os]

    current_os_filesystem_suffix = map_os_to_filesystem_suffix[current_os]

    current_arch_suffix = map_arch_to_suffix[current_arch]
  }
}
