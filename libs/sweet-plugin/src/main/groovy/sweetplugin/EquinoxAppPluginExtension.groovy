package sweetplugin

class EquinoxAppPluginExtension {

  boolean archiveProducts = false

  def beforeProductGeneration = []

  def launchParameters = []

  private boolean defaultProducts = true
  def products = [[ name: 'default' ]]

  def beforeProductGeneration(newValue) {
    beforeProductGeneration.add newValue
  }

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
