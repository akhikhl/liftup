package sweet

import org.eclipse.jface.resource.ImageRegistry
import org.eclipse.jface.viewers.ComboViewer
import org.eclipse.jface.viewers.ContentViewer
import org.eclipse.jface.viewers.ILabelProvider
import org.eclipse.jface.viewers.ILabelProviderListener
import org.eclipse.jface.viewers.IStructuredContentProvider
import org.eclipse.jface.viewers.Viewer
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.FillData
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.layout.RowData
import org.eclipse.swt.layout.RowLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Combo
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Decorations
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Menu
import org.eclipse.swt.widgets.MenuItem
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Text
import org.eclipse.swt.widgets.ToolBar
import org.eclipse.swt.widgets.ToolItem
import org.eclipse.swt.widgets.Widget
import org.slf4j.LoggerFactory

class SwtBuilder {

  private static final log = LoggerFactory.getLogger(SwtBuilder)

  List<Display> displayStack = []
  List<Widget> widgetStack = []

  def build(widget, Closure closure = null) {
    build [:], widget, closure
  }

  def build(Map attrs, widget, Closure closure = null) {
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
    build attrs, new Button(currentWidget, attrs.style ?: SWT.PUSH), closure
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
    build attrs, new Composite(currentWidget, attrs.style ?: SWT.NONE), closure
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

  void endModal(result = true) {
    if(shell.data == null)
      shell.data = [:]
    shell.data.endModal = result
  }

  void fillLayout(Map attrs = [:]) {
    log.trace 'fillLayout: {}', attrs
    def layout = new FillLayout(attrs.type ?: SWT.HORIZONTAL)
    attrs = [:] << attrs
    if(attrs.margin != null) {
      attrs.marginWidth = attrs.marginHeight = attrs.margin
      attrs.remove('margin')
    }
    attrs.each { String key, value ->
      layout[key] = value
    }
    currentWidget.layout = layout
  }

  private void fixLayoutData() {
    if(widgetStack.size() > 1
    && (widgetStack[-2] instanceof Composite)
    && widgetStack[-2].layout
    && (widgetStack[-1] instanceof Control)
    && !widgetStack[-1].layoutData) {
      if(widgetStack[-2].layout instanceof GridLayout) {
        log.trace 'fixing layout data: {}', widgetStack[-1]
        widgetStack[-1].layoutData = new GridData()
      }
      else if(widgetStack[-2].layout instanceof RowLayout) {
        log.trace 'fixing layout data: {}', widgetStack[-1]
        widgetStack[-1].layoutData = new RowData()
      }
      else if(widgetStack[-2].layout instanceof FillLayout) {
        log.trace 'fixing layout data: {}', widgetStack[-1]
        widgetStack[-1].layoutData = new FillData()
      }
    }
  }

  Widget getCurrentWidget() {
    widgetStack.last()
  }

  Decorations getDecorations() {
    widgetStack.iterator().reverse().find { it instanceof Decorations }
  }

  Display getDisplay() {
    displayStack.last()
  }

  Image getResourceImage(String resourceName) {
    if(display.data == null)
      display.data = [:]
    if(display.data.imageRegistry == null)
      display.data.imageRegistry = new ImageRegistry(display)
    Image result = display.data.imageRegistry.get(resourceName)
    if(!result) {
      this.class.classLoader.getResourceAsStream(resourceName)?.withStream {
        result = new Image(display, it)
      }
      if(!result)
        throw new RuntimeException("Could not find resource: '$resourceName'")
      display.data.imageRegistry.put(resourceName, result)
    }
    return result
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
    currentWidget.layout = layout
  }

  def label(Map attrs = [:]) {
    label attrs, null
  }

  def label(Map attrs = [:], Closure closure) {
    build attrs, new Label(currentWidget, attrs.style ?: SWT.NONE), closure
  }

  void layoutData(Map attrs = [:]) {
    log.trace 'layoutData: {}', attrs
    def data
    if(widgetStack[-2].layout instanceof GridLayout)
      data = new GridData()
    else if(widgetStack[-2].layout instanceof RowLayout)
      data = new RowData()
    else if(widgetStack[-2].layout instanceof FillLayout)
      data = new FillData()
    else {
      log.warn 'Layout data not supported for layout: {}', widgetStack[-2].layout
      return
    }
    attrs.each { String key, value ->
      data[key] = value
    }
    currentWidget.layoutData = data
  }

  def menu(Map attrs = [:], String name) {
    menu attrs, name, null
  }

  def menu(Map attrs = [:], String name, Closure closure) {
    attrs = [:] << attrs
    def subMenu = attrs.subMenu
    if(attrs.image instanceof String)
      attrs.image = getResourceImage(attrs.image)
    if(subMenu) {
      attrs.remove('subMenu')
      MenuItem item = build attrs, new MenuItem(currentWidget, SWT.CASCADE), closure
      item.text = name
      item.menu = build new Menu(shell, SWT.DROP_DOWN), subMenu
      return item
    }
    MenuItem item = build attrs, new MenuItem(currentWidget, SWT.PUSH), closure
    item.text = name
    return item
  }

  def menuBar(Map attrs = [:]) {
    menuBar attrs, null
  }

  def menuBar(Map attrs = [:], Closure closure) {
    shell.setMenuBar build(attrs, new Menu(currentWidget, attrs.style ?: SWT.BAR), closure)
  }

  def methodMissing(String name, args) {
    log.trace 'methodMissing {} {}', name, args
    if(currentWidget instanceof Menu) {
      Map attrs = args.find({ it instanceof Map }) ?: [:]
      Closure closure = args.find { it instanceof Closure }
      return menu(attrs, name, closure)
    }
    if(currentWidget instanceof ToolBar) {
      Map attrs = args.find({ it instanceof Map }) ?: [:]
      Closure closure = args.find { it instanceof Closure }
      return toolbarItem(attrs, name, closure)
    }
    currentWidget.invokeMethod(name, args)
  }

  def modalLoop() {
    while (!shell.isDisposed()) {
      try {
        if(shell.data?.endModal)
          return shell.data.endModal
        if (!display.readAndDispatch())
          display.sleep()
      } catch(Throwable x) {
        x.printStackTrace()
      }
    }
  }

  def propertyMissing(String name, value) {
    log.trace 'propertyMissing {}={}', name, value
    currentWidget[name] = value
  }

  def propertyMissing(String name) {
    log.trace 'propertyMissing {}', name
    currentWidget[name]
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
    currentWidget.layout = layout
  }

  def runApplication(Map shellAttrs = [:]) {
    runApplication shellAttrs, null
  }

  def runApplication(Map shellAttrs = [:], Closure closure) {
    def result
    display {
      shell shellAttrs, {
        if(closure) {
          closure.delegate = delegate
          closure.resolveStrategy = Closure.DELEGATE_FIRST
          closure()
        }
        open()
        result = modalLoop()
      }
    }
    return result
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
    build attrs, new Text(currentWidget, attrs.style ?: SWT.SINGLE | SWT.BORDER), closure
  }

  def toolBar(Map attrs = [:]) {
    toolBar attrs, null
  }

  def toolBar(Map attrs = [:], Closure closure) {
    def toolbar = build(attrs, new ToolBar(decorations, SWT.NONE), closure)
    def clientArea = decorations.clientArea
    toolbar.setLocation(clientArea.x, clientArea.y)
    return toolbar
  }

  def toolbarItem(Map attrs = [:], String name) {
    toolbarItem attrs, name, null
  }

  def toolbarItem(Map attrs = [:], String name, Closure closure) {
    log.info 'toolbarItem {}', name
    attrs = [:] << attrs
    def subMenu = attrs.subMenu
    if(attrs.image instanceof String)
      attrs.image = getResourceImage(attrs.image)
    if(subMenu) {
      attrs.remove('subMenu')
      ToolItem item = build attrs, new ToolItem(currentWidget, SWT.DROP_DOWN), closure
      item.text = name
      item.menu = build new Menu(shell, SWT.DROP_DOWN), subMenu
      return item
    }
    ToolItem item = build attrs, new ToolItem(currentWidget, SWT.PUSH), closure
    item.text = name
    return item
  }
}
