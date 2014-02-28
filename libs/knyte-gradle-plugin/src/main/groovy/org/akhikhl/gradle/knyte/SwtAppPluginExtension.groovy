package org.akhikhl.gradle.knyte

class SwtAppPluginExtension {

  def products = []

  def product(newValue) {
    products.add newValue
  }
}
