package sweet

import org.eclipse.jface.viewers.ComboViewer
import org.eclipse.jface.viewers.ContentViewer
import org.eclipse.jface.viewers.ILabelProvider
import org.eclipse.jface.viewers.ILabelProviderListener
import org.eclipse.jface.viewers.IStructuredContentProvider
import org.eclipse.jface.viewers.Viewer
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.layout.RowData
import org.eclipse.swt.layout.RowLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Combo
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Text
import org.eclipse.swt.widgets.Widget
import org.slf4j.LoggerFactory

class SwtBuilder {

  private static final log = LoggerFactory.getLogger(SwtBuilder)

  List<Display> displayStack = []
  List<Widget> widgetStack = []

  def build(Map attrs = [:], widget, Closure closure = null) {
    log.trace 'build: {} {}', widget.class.name, attrs
    use(SwtCategory) {
      attrs.each { String key, value ->
        if(key != 'style' && key != 'model')
          widget[key] = value
      }
      widgetStack.push(widget)
      try {
        if(closure) {
          closure = closure.rehydrate(this, closure.owner, closure.thisObject)
          closure()
        }
        fixLayoutData()
      } finally {
        widgetStack.pop()
      }
    }
    return widget
  }

  def button(Map attrs = [:]) {
    button attrs, null
  }

  def button(Map attrs = [:], Closure closure) {
    build attrs, new Button(widgetStack.last(), attrs.style ?: SWT.PUSH), closure
  }

  def combo(Map attrs = [:]) {
    combo attrs, null
  }

  def combo(Map attrs = [:], Closure closure) {
    Combo widget = build attrs, new Combo(widgetStack.last(), attrs.style ?: SWT.DROP_DOWN), closure
    if(attrs.model != null) {
      if(widget.data == null)
        widget.data = [:]
      ComboViewer viewer = new ComboViewer(widget)
      widget.data.viewer = viewer
      switch(attrs.model) {
        case Collection:
          setCollectionModel(viewer, attrs.model)
          break
        case Map:
          setMapModel(viewer, attrs.model)
          break
      }
    }
    return widget
  }

  def composite(Map attrs = [:]) {
    composite attrs, null
  }

  def composite(Map attrs = [:], Closure closure) {
    build attrs, new Composite(widgetStack.last(), attrs.style ?: SWT.NONE), closure
  }

  void display(Closure closure) {
    displayStack.push(new Display())
    try {
      closure()
    }
    finally {
      displayStack.pop().dispose()
    }
  }

  private void fixLayoutData() {
    if(widgetStack.size() > 1 && widgetStack[-2].layout && !widgetStack[-1].layoutData) {
      if(widgetStack[-2].layout instanceof GridLayout) {
        log.trace 'fixing layout data: {}', widgetStack[-1]
        widgetStack[-1].layoutData = new GridData()
      }
    }
  }

  Display getDisplay() {
    displayStack.last()
  }

  Shell getShell() {
    widgetStack.iterator().reverse().find { it instanceof Shell }
  }

  void gridLayout(Map attrs = [:]) {
    log.trace 'gridLayout: {}', attrs
    def layout = new GridLayout()
    attrs = [:] << attrs
    if(attrs.spacing != null) {
      attrs.horizontalSpacing = attrs.verticalSpacing = attrs.spacing
      attrs.remove('spacing')
    }
    if(attrs.margin != null) {
      attrs.marginWidth = attrs.marginHeight = attrs.margin
      attrs.remove('margin')
    }
    attrs.each { String key, value ->
      layout[key] = value
    }
    widgetStack.last().layout = layout
  }

  def label(Map attrs = [:]) {
    label attrs, null
  }

  def label(Map attrs = [:], Closure closure) {
    build attrs, new Label(widgetStack.last(), attrs.style ?: SWT.NONE), closure
  }

  void layoutData(Map attrs = [:]) {
    log.trace 'layoutData: {}', attrs
    def data
    if(widgetStack[-2].layout instanceof GridLayout)
      data = new GridData()
    else if(widgetStack[-2].layout instanceof RowLayout)
      data = new RowData()
    else {
      log.warn 'Layout data not supported for layout: {}', widgetStack[-2].layout
      return
    }
    attrs.each { String key, value ->
      data[key] = value
    }
    widgetStack.last().layoutData = data
  }

  def methodMissing(String name, args) {
    log.trace 'method missing: {}, delegating to swt component', name
    widgetStack.last().invokeMethod(name, args)
  }

  void modalLoop() {
    while (!shell.isDisposed()) {
      try {
        if (!display.readAndDispatch())
          display.sleep()
      } catch(Throwable x) {
        x.printStackTrace()
      }
    }
  }

  def propertyMissing(String name, value) {
    log.trace 'property missing: {}, delegating to swt component', name
    widgetStack.last()[name] = value
  }

  def propertyMissing(String name) {
    log.info 'property missing: {}, delegating to swt component', name
    widgetStack.last()[name]
  }

  void rowLayout(Map attrs = [:]) {
    log.trace 'rowLayout: {}', attrs
    def layout = new RowLayout()
    attrs = [:] << attrs
    if(attrs.margin != null) {
      attrs.marginWidth = attrs.marginHeight = attrs.marginLeft = attrs.marginRight = attrs.marginTop = attrs.marginBottom = attrs.margin
      attrs.remove('margin')
    }
    attrs.each { String key, value ->
      layout[key] = value
    }
    widgetStack.last().layout = layout
  }

  void runApplication(Map attrs = [:]) {
    runApplication attrs, null
  }

  void runApplication(Map attrs = [:], Closure closure) {
    display {
      shell attrs, {
        if(closure) {
          closure.delegate = delegate
          closure.resolveStrategy = Closure.DELEGATE_FIRST
          closure()
        }
        open()
        modalLoop()
      }
    }
  }

  private void setCollectionModel(ContentViewer viewer, Collection model) {
    viewer.contentProvider = new IStructuredContentProvider() {
          void dispose() {}

          Object[] getElements(input) {
            model.collect { it?.toString() ?: '' }.toArray()
          }

          void inputChanged(Viewer v, oldInput, newInput) {}
        }

    viewer.input = model
  }

  private void setMapModel(ContentViewer viewer, Map model) {
    viewer.contentProvider = new IStructuredContentProvider() {
          void dispose() {}

          Object[] getElements(input) {
            model.keySet() as Object[]
          }

          void inputChanged(Viewer v, oldInput, newInput) {}
        }

    viewer.labelProvider = new ILabelProvider() {
          void addListener(ILabelProviderListener listener) {}
          void dispose() {}
          Image getImage(elem) { }
          String getText(elem) {
            model[elem]
          }
          boolean isLabelProperty(Object elem, String name) { false }
          void removeListener(ILabelProviderListener listener) {}
        }

    viewer.input = model
  }

  def shell(Map attrs = [:]) {
    shell attrs, null
  }

  def shell(Map attrs = [:], Closure closure) {
    build attrs, new Shell(display), closure
  }

  def text(Map attrs = [:]) {
    text attrs, null
  }

  def text(Map attrs = [:], Closure closure) {
    build attrs, new Text(widgetStack.last(), attrs.style ?: SWT.SINGLE | SWT.BORDER), closure
  }
}
