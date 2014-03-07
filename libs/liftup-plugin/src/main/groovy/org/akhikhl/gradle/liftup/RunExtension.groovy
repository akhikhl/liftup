package org.akhikhl.gradle.liftup

class RunExtension {

  def args = []
  String language

  def arg(String newValue) {
    args.add newValue
  }

  def args(Object[] newValue) {
    args.addAll newValue
  }

  def language(String newValue) {
    language = newValue
  }
}
