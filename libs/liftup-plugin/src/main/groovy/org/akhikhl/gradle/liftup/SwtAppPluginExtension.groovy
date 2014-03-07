package org.akhikhl.gradle.liftup

class SwtAppPluginExtension {

  def products = []

  def product(newValue) {
    products.add newValue
  }
}
