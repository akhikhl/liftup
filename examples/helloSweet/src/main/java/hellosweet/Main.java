package hellosweet;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public final class Main {

  public static void main(String[] args) {

    Display display = new Display();
    try {
      Shell shell = new Shell(display);
      shell.setText("Hello, world!");
      shell.open();
      while (!shell.isDisposed())
        if (!display.readAndDispatch())
          display.sleep();
    } finally {
      display.dispose();
    }
  }
}
