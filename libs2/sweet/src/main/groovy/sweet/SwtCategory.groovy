package sweet

import org.eclipse.swt.widgets.Listener
import org.eclipse.swt.widgets.Widget

class SwtCategory {

  public static addListener(Widget widget, int eventType, Closure closure) {
    closure = closure.rehydrate(widget, closure.owner, closure.thisObject)
    Listener listener = closure as Listener
    closure.properties.listener = listener
    widget.addListener(eventType, listener)
    return closure
  }

  public static removeListener(Widget widget, int eventType, Closure closure) {
    if(closure.properties.listener)
      widget.removeListener(eventType, closure.properties.listener)
    return closure
  }
}
