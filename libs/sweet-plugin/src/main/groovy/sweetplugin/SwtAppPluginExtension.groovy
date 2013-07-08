package sweetplugin

class SwtAppPluginExtension {

  def products = []

  def product(newValue) {
    products.add newValue
  }
}
