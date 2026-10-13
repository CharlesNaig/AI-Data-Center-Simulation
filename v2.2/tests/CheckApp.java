import org.nlogo.app.App;
import org.nlogo.window.ButtonWidget;
import java.awt.EventQueue;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.color.ColorSpace;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

class CheckApp {
  static String output;
  static List<Component> descendants(Component component) {
    var result = new ArrayList<Component>();
    result.add(component);
    if (component instanceof Container container) for (Component child : container.getComponents()) result.addAll(descendants(child));
    return result;
  }
  static ButtonWidget button(String label) {
    for (Component c : descendants(App.app().getFrame())) if (c instanceof ButtonWidget b && b.name().equals(label)) return b;
    throw new AssertionError("No native button: " + label);
  }
  static void click(String label) throws Exception {
    EventQueue.invokeAndWait(() -> {
      var b = button(label);
      b.mousePressed(new MouseEvent(b, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), InputEvent.BUTTON1_DOWN_MASK, 10, 10, 1, false, MouseEvent.BUTTON1));
      b.mouseReleased(new MouseEvent(b, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 10, 10, 1, false, MouseEvent.BUTTON1));
    });
  }
  static void waitIdle(String label) throws Exception {
    long end = System.currentTimeMillis() + 10000;
    Thread.sleep(100);
    while (button(label).running() && System.currentTimeMillis() < end) Thread.sleep(30);
    if (button(label).running()) throw new AssertionError("Button did not finish: " + label);
  }
  static void check(String expression) throws Exception {
    if (!Boolean.TRUE.equals(App.app().report(expression))) throw new AssertionError(expression);
  }
  static Object state() throws Exception {
    return App.app().report("(list ticks reservoir-ml dc-request-ml household-request-ml shortfall-ml grid-demand-percent green-coverage-percent wellbeing-score recycled-water? solar? reforestation?)");
  }
  static void saveWindow(Window window, String name) {
    try {
      var image = new BufferedImage(window.getWidth(), window.getHeight(), BufferedImage.TYPE_INT_RGB);
      var graphics = image.createGraphics();
      window.paintAll(graphics);
      graphics.dispose();
      ImageIO.write(image, "png", new File(output, name + ".png"));
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  static void finishDialog(String choice, String screenshot) throws Exception {
    Dialog found = null;
    for (int attempt = 0; attempt < 100 && found == null; attempt++) {
      for (Window window : Window.getWindows()) if (window instanceof Dialog d && d.isVisible()) found = d;
      if (found == null) Thread.sleep(50);
    }
    if (found == null) throw new AssertionError("Native dialog did not open");
    final Dialog dialog = found;
    EventQueue.invokeAndWait(() -> {
      if (screenshot != null) saveWindow(dialog, screenshot);
      if (choice != null && !choice.equals("Cancel")) {
        for (Component c : descendants(dialog)) {
          if (c instanceof JComboBox<?> box) box.setSelectedItem(choice);
          if (c instanceof JList<?> list) list.setSelectedValue(choice, true);
          if (c instanceof org.nlogo.swing.ComboBox box) box.setSelectedIndex(
            List.of("Recycled water (50%)", "Solar energy (40%)", "Reforestation (12 months)").indexOf(choice));
        }
      }
      String label = "Cancel".equals(choice) ? "Cancel" : "OK";
      for (Component c : descendants(dialog)) if (c instanceof AbstractButton b && b.getText().equals(label)) { b.doClick(); return; }
      for (Component c : descendants(dialog)) System.out.println(c.getClass().getName() + " " + (c instanceof AbstractButton b ? b.getText() : ""));
      throw new AssertionError("Cannot find dialog " + label);
    });
    Thread.sleep(150);
  }
  static void collapseCommandCenter() {
    var center = App.app().tabManager().interfaceTab().commandCenter();
    center.setVisible(false);
    for (Container p = center.getParent(); p != null; p = p.getParent()) if (p instanceof org.nlogo.swing.SplitPane split) {
      split.setDividerLocation(split.maxClosedDividerLocation());
      split.doLayout();
    }
    for (Container p = center.getParent(); p != null; p = p.getParent()) if (p instanceof JSplitPane split) {
      split.setResizeWeight(1.0);
      split.setDividerSize(0);
      split.setDividerLocation(1.0);
    }
  }
  static void scrollTo(int y) throws Exception {
    EventQueue.invokeAndWait(() -> {
      for (Component c : descendants(App.app().tabManager().interfaceTab())) if (c instanceof JScrollPane pane &&
        descendants(pane.getViewport()).stream().anyMatch(x -> x instanceof ButtonWidget)) pane.getVerticalScrollBar().setValue(y);
    });
  }
  static void capture(String name, int width, int height) throws Exception {
    EventQueue.invokeAndWait(() -> {
      App.app().getFrame().setSize(width, height);
      App.app().getFrame().validate();
      collapseCommandCenter();
    });
    App.app().command("render-scene display");
    Thread.sleep(400);
    EventQueue.invokeAndWait(() -> {
      var frame = App.app().getFrame();
      var tab = App.app().tabManager().interfaceTab();
      var nodes = descendants(frame).stream().filter(c -> c instanceof ButtonWidget || c.getClass().getName().equals("org.nlogo.window.MonitorWidget")).toList();
      for (int i = 0; i < nodes.size(); i++) for (int j = i+1; j < nodes.size(); j++) {
        var a = nodes.get(i); var b = nodes.get(j);
        var aa = SwingUtilities.convertRectangle(a.getParent(), a.getBounds(), tab);
        var bb = SwingUtilities.convertRectangle(b.getParent(), b.getBounds(), tab);
        if (aa.intersects(bb)) throw new AssertionError("Actual native widgets overlap: " + aa + " / " + bb);
      }
    });
    EventQueue.invokeAndWait(() -> saveWindow(App.app().getFrame(), name + "-" + width + "x" + height));
  }
  public static void main(String[] args) {
    try {
      output = args[1];
      App.main(new String[0]);
      Thread.sleep(2000);
      EventQueue.invokeAndWait(() -> {
        try { App.app().open(args[0], false); }
        catch (Exception e) { throw new RuntimeException(e); }
      });
      Thread.sleep(1000);
      EventQueue.invokeAndWait(() -> {
        App.app().compile();
        collapseCommandCenter();
      });
      App.app().command("teaching-defaults");
      click("Setup / Reset"); waitIdle("Setup / Reset");
      check("count households = 10 and count data-centers = 1 and ticks = 0");
      click("Step Month"); waitIdle("Step Month"); check("ticks = 1");
      click("Build Data Center +1"); waitIdle("Build Data Center +1"); check("ticks = 1 and count data-centers = 2 and abs (reservoir-ml - 39.346) < 0.000001");
      Object beforeDialog = state();
      click("Statistics"); finishDialog(null, "statistics-dialog"); waitIdle("Statistics");
      if (!state().equals(beforeDialog)) throw new AssertionError("Statistics mutated model state");
      for (String choice : new String[]{"Recycle Water", "Add Solar", "Reforest"}) {
        click(choice); waitIdle(choice);
        Object once = state();
        click(choice); waitIdle(choice);
        if (!state().equals(once)) throw new AssertionError("Repeated native solution stacked: " + choice);
      }
      check("recycled-water? and solar? and reforestation? and ticks = 1");
      click("Run / Pause");
      long deadline = System.currentTimeMillis() + 15000;
      while (((Number)App.app().report("ticks")).doubleValue() < 3 && System.currentTimeMillis() < deadline) Thread.sleep(50);
      click("Run / Pause"); waitIdle("Run / Pause");
      check("ticks >= 3"); Object paused = state(); Thread.sleep(300);
      if (!state().equals(paused)) throw new AssertionError("Pause did not stop monthly changes");
      App.app().command("teaching-defaults setup repeat 71 [go]");
      click("Run / Pause"); waitIdle("Run / Pause"); check("ticks = 72 and complete?");
      System.out.println("PASS: native Setup, Step, Build, Run/Pause, Statistics, and three repeated direct solution buttons.");
      for (int[] size : new int[][]{{1366,768},{1920,1080}}) {
        scrollTo(0);
        if (size[0] == 1366) {
          App.app().command("set starting-data-centers 2 set starting-households 12 setup enable-solution \"Solar energy (40%)\" enable-solution \"Recycled water (50%)\"");
          capture("leader-reference", size[0], size[1]);
        }
        App.app().command("teaching-defaults setup");
        capture("setup", size[0], size[1]);
        App.app().command("set experiment-case \"combined\" setup-experiment repeat 24 [go-experiment]");
        capture("shortage", size[0], size[1]);
        App.app().command("repeat 2 [go-experiment]");
        capture("saplings", size[0], size[1]);
        App.app().command("repeat 10 [go-experiment]");
        capture("recovery", size[0], size[1]);
        App.app().command("repeat 36 [go-experiment]");
        capture("completion", size[0], size[1]);
        if (size[0] == 1366) { scrollTo(565); capture("charts", size[0], size[1]); scrollTo(0); }
        App.app().command("set starting-data-centers 50 set starting-households 50 setup");
        capture("maximum", size[0], size[1]);
        App.app().command("go");
        if (size[0] == 1366) { scrollTo(565); capture("maximum-charts", size[0], size[1]); scrollTo(0); }
      }
      var shortage = ImageIO.read(new File(output, "shortage-1366x768.png"));
      var grayscale = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(shortage, null);
      ImageIO.write(grayscale, "png", new File(output, "shortage-grayscale-1366x768.png"));
      System.out.println("PASS: native setup, shortage, recovery, completion, and 50/50 layouts at both target sizes.");
      EventQueue.invokeAndWait(() -> App.app().getFrame().dispose());
      App.app().workspace().dispose();
      System.exit(0);
    } catch (Throwable e) { e.printStackTrace(); System.exit(1); }
  }
}
