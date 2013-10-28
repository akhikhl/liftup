package sweet

import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.slf4j.LoggerFactory

class SwtBuilder {

  private static final log = LoggerFactory.getLogger(SwtBuilder)

  List compStack = []

  void build(Closure closure) {
    closure = closure.rehydrate(this, closure.owner, closure.thisObject)
    closure()
  }

  void build(parent, Closure closure) {
    closure = closure.rehydrate(this, closure.owner, closure.thisObject)
    compStack.push(parent)
    try {
      closure()
    } finally {
      compStack.pop()
    }
  }

  def button(Map attrs = [:], Closure closure = null) {
    log.trace 'button: {}', attrs
    def button = new Button(compStack.last(), attrs.style ?: SWT.PUSH)
    attrs.each { String key, value ->
      button[key] = value
    }
    compStack.push(button)
    try {
      if(closure) {
        closure = closure.rehydrate(this, closure.owner, closure.thisObject)
        closure()
      }
      fixLayoutData()
    } finally {
      compStack.pop()
    }
    return button
  }

  private void fixLayoutData() {
    if(compStack[-2].layout && !compStack[-1].layoutData) {
      log.trace 'setting default layout data: {}', compStack[-1]
      if(compStack[-2].layout instanceof GridLayout)
        compStack[-1].layoutData = new GridData()
    }
  }

  void gridLayout(Map attrs = [:]) {
    log.trace 'gridLayout: {}', attrs
    def layout = new GridLayout()
    attrs.each { String key, value ->
      layout[key] = value
    }
    compStack.last().layout = layout
  }

  void layoutData(Map attrs) {
    log.trace 'layoutData: {}', attrs
    def data
    if(compStack[-2].layout instanceof GridLayout)
      data = new GridData()
    else {
      log.warn 'Layout data not supported for layout: {}', compStack[-2].layout
      return
    }
    attrs.each { String key, value ->
      data[key] = value
    }
    compStack.last().layoutData = data
  }

  def methodMissing(String name, args) {
    log.trace 'method missing: {}, delegating to swt component', name
    compStack.last().invokeMethod(name, args)
  }

  def propertyMissing(String name, value) {
    log.trace 'property missing: {}, delegating to swt component', name
    compStack.last()[name] = value
  }

  def propertyMissing(String name) {
    log.trace 'property missing: {}, delegating to swt component', name
    compStack.last()[name]
  }

  void runWindowApplication(Map attrs = [:], Closure closure) {
    log.trace 'runWindowApplication: {}', attrs
    Display display = new Display()
    try {
      def shell = new Shell(display)
      attrs.each { String key, value ->
        shell[key] = value
      }
      closure = closure.rehydrate(this, closure.owner, closure.thisObject)
      compStack.push(shell)
      try {
        closure()
      } finally {
        compStack.pop()
      }
      shell.open()
      while (!shell.isDisposed())
        if (!display.readAndDispatch())
          display.sleep()
    } finally {
      display.dispose()
    }
  }
}
