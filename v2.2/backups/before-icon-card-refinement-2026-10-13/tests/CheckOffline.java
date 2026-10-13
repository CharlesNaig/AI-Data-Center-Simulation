import org.nlogo.headless.HeadlessWorkspace;
import java.security.Permission;

class CheckOffline {
  @SuppressWarnings("removal")
  public static void main(String[] args) throws Exception {
    // Test-only sandbox: Java 17 is the bundled NetLogo 7.0.4 runtime.
    System.setSecurityManager(new SecurityManager() {
      public void checkPermission(Permission permission) { }
      public void checkConnect(String host, int port) { throw new SecurityException("Network disabled for offline test"); }
    });
    var w = HeadlessWorkspace.newInstance();
    try {
      w.open(args[0]);
      w.command("setup go enable-solution \"Recycled water (50%)\" enable-solution \"Solar energy (40%)\" enable-solution \"Reforestation (12 months)\" repeat 12 [go]");
      if (!Boolean.TRUE.equals(w.report("count turtles = 11 and ticks = 13"))) throw new AssertionError("Offline run failed");
      System.out.println("PASS: relocated model, seven sprites and numeric glyphs load/run with Java network connections denied.");
    } finally { w.dispose(); }
  }
}
