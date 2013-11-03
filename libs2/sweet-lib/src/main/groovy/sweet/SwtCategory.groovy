package sweet

import org.eclipse.swt.events.ArmListener
import org.eclipse.swt.events.ControlListener
import org.eclipse.swt.events.DisposeListener
import org.eclipse.swt.events.FocusListener
import org.eclipse.swt.events.HelpListener
import org.eclipse.swt.events.KeyListener
import org.eclipse.swt.events.MenuListener
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.events.MouseListener
import org.eclipse.swt.events.MouseMoveListener
import org.eclipse.swt.events.MouseTrackListener
import org.eclipse.swt.events.PaintListener
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.events.ShellListener
import org.eclipse.swt.events.TraverseListener
import org.eclipse.swt.events.TreeListener
import org.eclipse.swt.events.VerifyListener
import org.eclipse.swt.widgets.Listener
import org.eclipse.swt.widgets.Widget
import org.slf4j.LoggerFactory

class SwtCategory {

  private static final log = LoggerFactory.getLogger(SwtBuilder)

  public static addArmListener(Widget widget, Closure closure) {
    addListener_ widget, closure, ArmListener, { widget.addArmListener(it) }
  }

  public static removeArmListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeArmListener(it) }
  }

  public static addControlListener(Widget widget, Closure closure) {
    addListener_ widget, closure, ControlListener, { widget.addControlListener(it) }
  }

  public static removeControlListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeControlListener(it) }
  }

  public static addDisposeListener(Widget widget, Closure closure) {
    addListener_ widget, closure, DisposeListener, { widget.addDisposeListener(it) }
  }

  public static removeDisposeListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeDisposeListener(it) }
  }

  public static addFocusListener(Widget widget, Closure closure) {
    addListener_ widget, closure, FocusListener, { widget.addFocusListener(it) }
  }

  public static removeFocusListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeFocusListener(it) }
  }

  public static addHelpListener(Widget widget, Closure closure) {
    addListener_ widget, closure, HelpListener, { widget.addHelpListener(it) }
  }

  public static removeHelpListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeHelpListener(it) }
  }

  public static addKeyListener(Widget widget, Closure closure) {
    addListener_ widget, closure, KeyListener, { widget.addKeyListener(it) }
  }

  public static removeKeyListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeKeyListener(it) }
  }

  public static addMenuListener(Widget widget, Closure closure) {
    addListener_ widget, closure, MenuListener, { widget.addMenuListener(it) }
  }

  public static removeMenuListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeMenuListener(it) }
  }

  public static addModifyListener(Widget widget, Closure closure) {
    addListener_ widget, closure, ModifyListener, { widget.addModifyListener(it) }
  }

  public static removeModifyListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeModifyListener(it) }
  }

  public static addMouseListener(Widget widget, Closure closure) {
    addListener_ widget, closure, MouseListener, { widget.addMouseListener(it) }
  }

  public static removeMouseListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeMouseListener(it) }
  }

  public static addMouseMoveListener(Widget widget, Closure closure) {
    addListener_ widget, closure, MouseMoveListener, { widget.addMouseMoveListener(it) }
  }

  public static removeMouseMoveListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeMouseMoveListener(it) }
  }

  public static addMouseTrackListener(Widget widget, Closure closure) {
    addListener_ widget, closure, MouseTrackListener, { widget.addMouseTrackListener(it) }
  }

  public static removeMouseTrackListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeMouseTrackListener(it) }
  }

  public static addPaintListener(Widget widget, Closure closure) {
    addListener_ widget, closure, PaintListener, { widget.addPaintListener(it) }
  }

  public static removePaintListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removePaintListener(it) }
  }

  public static addSelectionListener(Widget widget, Closure closure) {
    addListener_ widget, closure, SelectionListener, { widget.addSelectionListener(it) }
  }

  public static removeSelectionListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeSelectionListener(it) }
  }

  public static addShellListener(Widget widget, Closure closure) {
    addListener_ widget, closure, ShellListener, { widget.addShellListener(it) }
  }

  public static removeShellListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeShellListener(it) }
  }

  public static addTraverseListener(Widget widget, Closure closure) {
    addListener_ widget, closure, TraverseListener, { widget.addTraverseListener(it) }
  }

  public static removeTraverseListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeTraverseListener(it) }
  }

  public static addTreeListener(Widget widget, Closure closure) {
    addListener_ widget, closure, TreeListener, { widget.addTreeListener(it) }
  }

  public static removeTreeListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeTreeListener(it) }
  }

  public static addVerifyListener(Widget widget, Closure closure) {
    addListener_ widget, closure, VerifyListener, { widget.addVerifyListener(it) }
  }

  public static removeVerifyListener(Widget widget, Closure closure) {
    removeListener_ widget, closure, { widget.removeVerifyListener(it) }
  }

  public static addListener(Widget widget, int eventType, Closure closure) {
    addListener_ widget, closure, Listener, { widget.addListener(eventType, it) }
  }

  public static removeListener(Widget widget, int eventType, Closure closure) {
    removeListener_ widget, closure, { widget.removeListener(eventType, it) }
  }

  private static addListener_(Widget widget, Closure listenerClosure, Class listenerClass, Closure addClosure) {
    def listener = { event ->
      Closure c = listenerClosure.rehydrate(event.widget, listenerClosure.owner, listenerClosure.thisObject)
      c.resolveStrategy = Closure.DELEGATE_FIRST
      use(SwtCategory) {
        if(c.maximumNumberOfParameters == 0)
          c()
        else
          c(event)
      }
    }.asType(listenerClass)
    if(widget.data == null)
      widget.data = [:]
    if(widget.data.listeners == null)
      widget.data.listeners = [:]
    widget.data.listeners[listenerClosure] = listener
    addClosure(listener)
    log.trace 'addListener, widget={}, listeners={}', widget, widget.data.listeners
    return listenerClosure
  }

  private static removeListener_(Widget widget, Closure listenerClosure, Closure removeClosure) {
    def listener = widget.data?.listeners?.get(listenerClosure)
    if(listener) {
      removeClosure(listener)
      widget.data.listeners.remove(listenerClosure)
      log.trace 'removeListener, widget={}, listeners={}', widget, widget.data.listeners
    }
    return listenerClosure
  }
}
