package sweetplugin

class SweetAppPluginExtension {

  def products = []

  def product(newValue) {
    products.add newValue
  }
}
