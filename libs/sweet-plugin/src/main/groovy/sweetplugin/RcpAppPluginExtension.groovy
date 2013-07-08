package sweetplugin


class RcpAppPluginExtension {

  private boolean defaultProducts = true
  def products = [[ name: 'default' ]]

  boolean archiveProducts = false

  def launchParameters = []

  def launchParameter(String newValue) {
    launchParameters.add newValue
  }

  def product(String productName) {
    product( [ name: productName ] )
  }

  def product(Map productSpec) {
    if(defaultProducts) {
      products = []
      defaultProducts = false
    }
    products.add productSpec
  }
}
