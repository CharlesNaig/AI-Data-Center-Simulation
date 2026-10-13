import org.nlogo.app.App;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

class UiApp {
  static String output;
  static void capture(String name, int width, int height) throws Exception {
    EventQueue.invokeAndWait(() -> {
      App.app().getFrame().setSize(width, height);
      App.app().getFrame().validate();
    });
    App.app().command("update-visuals display");
    Thread.sleep(400); // Let native monitors refresh after the completed command.
    EventQueue.invokeAndWait(() -> {
      try {
        var frame = App.app().getFrame();
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        frame.paintAll(graphics);
        graphics.dispose();
        ImageIO.write(image, "png", new File(output, name + "-" + width + "x" + height + ".png"));
      } catch (Exception e) { throw new RuntimeException(e); }
    });
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
      EventQueue.invokeAndWait(() -> App.app().compile());
      EventQueue.invokeAndWait(() -> System.out.println("GUI code characters: " + App.app().getProcedures().length()));
      EventQueue.invokeAndWait(() -> {
        var tab = App.app().tabManager().interfaceTab();
        if (tab.commandCenter().isVisible()) tab.commandCenterToggleAction().actionPerformed(null);
      });
      for (int[] size : new int[][]{{1366,768},{1920,1080}}) {
        for (String scenario : new String[]{"Reference baseline","Rapid growth + drought","Efficiency + reuse"}) {
          String tag = scenario.startsWith("Reference") ? "baseline" : scenario.startsWith("Rapid") ? "shortage" : "efficient";
          App.app().command("set scenario \"" + scenario + "\" setup-scenario");
          capture("ui-app-" + tag + "-ready", size[0], size[1]);
          App.app().command("repeat 120 [go]");
          capture("ui-app-" + tag + "-120", size[0], size[1]);
        }
        App.app().command("set scenario \"Reference baseline\" load-scenario set initial-data-centers 30 set maximum-data-centers 30 setup");
        capture("ui-app-30-facilities", size[0], size[1]);
      }
      System.out.println("PASS: NetLogo application rendered all presets/setup/month120 and 30 facilities at both sizes.");
      EventQueue.invokeAndWait(() -> App.app().getFrame().dispose());
      App.app().workspace().dispose();
      System.exit(0);
    } catch (Throwable e) { e.printStackTrace(); System.exit(1); }
  }
}
